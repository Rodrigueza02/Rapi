package processor.service;

import dtos.*;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processor.exception.OrderNotFoundException;
import processor.model.DriverAssignment;
import processor.model.Order;
import processor.ports.OrderEventPublisherPort;
import processor.ports.OrderRepositoryPort;
import processor.strategy.DriverAssignmentStrategy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Orquesta el ciclo de vida inicial de un pedido nuevo:
 *
 * <pre>
 *   t=0            → ORDER_CREATED  (publicado y persistido inmediatamente)
 *   t=prepareDelay → ORDER_PREPARED (publicado tras el delay configurado)
 *   t=assignDelay  → DRIVER_ASSIGNED (publicado tras el segundo delay)
 * </pre>
 *
 * <p>Los delays permiten que las tarjetas CREATED y PREPARED sean visibles
 * en el dashboard antes de avanzar al estado siguiente. Los delays son
 * configurables desde {@code application.yml} y no bloquean el hilo de
 * procesamiento de RabbitMQ.
 *
 * <p>DRIVER_ASSIGNED dispara el driver-simulator, que gestiona los estados
 * ON_THE_WAY → ARRIVING → DELIVERED.
 */
public class ProcessOrderService {

    private static final Logger log = LoggerFactory.getLogger(ProcessOrderService.class);

    private final OrderRepositoryPort      orderRepository;
    private final OrderEventPublisherPort  eventPublisher;
    private final DriverAssignmentStrategy driverStrategy;
    private final Clock                    clock;
    private final Duration                 prepareDelay;
    private final Duration                 assignDelay;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(4);

    public ProcessOrderService(OrderRepositoryPort orderRepository,
                               OrderEventPublisherPort eventPublisher,
                               DriverAssignmentStrategy driverStrategy,
                               Clock clock,
                               Duration prepareDelay,
                               Duration assignDelay) {
        this.orderRepository = Objects.requireNonNull(orderRepository,  "orderRepository");
        this.eventPublisher  = Objects.requireNonNull(eventPublisher,   "eventPublisher");
        this.driverStrategy  = Objects.requireNonNull(driverStrategy,   "driverStrategy");
        this.clock           = Objects.requireNonNull(clock,            "clock");
        this.prepareDelay    = Objects.requireNonNull(prepareDelay,     "prepareDelay");
        this.assignDelay     = Objects.requireNonNull(assignDelay,      "assignDelay");
    }

    /**
     * Procesa un pedido nuevo (desde RabbitMQ).
     * Si ya existe (re-entrega de RabbitMQ) solo republica DRIVER_ASSIGNED.
     */
    public void process(OrderCreatedCommand command) {
        Objects.requireNonNull(command, "command");

        var existing = orderRepository.findById(command.orderId());
        if (existing.isPresent()) {
            // Idempotencia: re-entrega de RabbitMQ
            Order order = existing.get();
            DriverAssignment a = order.assignment()
                    .orElseThrow(() -> new IllegalStateException(
                            "Order " + order.orderId() + " already exists but has no driver"));
            eventPublisher.publish(new DriverAssignedEvent(
                    order.orderId(), a.driverId(), order.customerId(), a.assignedAt()));
            return;
        }

        startOrderLifecycle(command);
    }

    /** Cancela un pedido cancelable. Publica ORDER_CANCELLED. */
    public void cancelOrder(String orderId) {
        Objects.requireNonNull(orderId, "orderId");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        orderRepository.save(order);
        eventPublisher.publish(new OrderCancelledEvent(
                order.orderId(),
                order.assignment().map(DriverAssignment::driverId).orElse("none"),
                order.customerId(),
                clock.instant()));
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // ── privado ───────────────────────────────────────────────────────────────

    /**
     * Inicia el ciclo CREATED → PREPARED → ASSIGNED con delays configurables.
     * No bloquea el hilo del consumidor RabbitMQ.
     */
    private void startOrderLifecycle(OrderCreatedCommand command) {
        Instant now = clock.instant();
        String itemsJson = toItemsJson(command);

        Order order = Order.create(
                command.orderId(), command.customerId(),
                command.deliveryAddress(), command.totalAmount(), now, itemsJson);

        // 1. Persistir en CREATED y publicar evento inmediatamente
        orderRepository.save(order);
        eventPublisher.publish(new OrderCreatedEvent(
                order.orderId(), null, order.customerId(),
                order.deliveryAddress(),
                command.items(),
                order.totalAmount(),
                now));
        log.info("ORDER_CREATED: orderId={}", order.orderId());

        // 2. Después de prepareDelay → PREPARED
        scheduler.schedule(() -> runSafely("PREPARE", () -> {
            Instant preparedAt = clock.instant();
            order.prepare(preparedAt);
            orderRepository.save(order);
            eventPublisher.publish(new OrderPreparedEvent(
                    order.orderId(), null, order.customerId(), preparedAt));
            log.info("ORDER_PREPARED: orderId={}", order.orderId());

            // 3. Después de assignDelay → ASSIGNED
            scheduler.schedule(() -> runSafely("ASSIGN", () -> {
                Instant assignedAt = clock.instant();
                String driverId = driverStrategy.selectDriver(order);
                order.assignDriver(new DriverAssignment(driverId, assignedAt));
                orderRepository.save(order);
                eventPublisher.publish(new DriverAssignedEvent(
                        order.orderId(), driverId, order.customerId(), assignedAt));
                log.info("DRIVER_ASSIGNED: orderId={} driverId={}", order.orderId(), driverId);
            }), assignDelay.toMillis(), TimeUnit.MILLISECONDS);

        }), prepareDelay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void runSafely(String step, Runnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            log.error("ProcessOrderService step '{}' failed", step, e);
        }
    }

    private String toItemsJson(OrderCreatedCommand command) {
        if (command.items() == null || command.items().isEmpty()) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(command.items());
        } catch (Exception e) {
            return String.join(",", command.items());
        }
    }
}
