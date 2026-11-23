package processor.service;

import dtos.DriverAssignedEvent;
import dtos.OrderCancelledEvent;
import dtos.OrderCreatedCommand;
import dtos.OrderPreparedEvent;
import processor.exception.OrderNotFoundException;
import processor.model.DriverAssignment;
import processor.model.Order;
import processor.ports.OrderEventPublisherPort;
import processor.ports.OrderRepositoryPort;
import processor.strategy.DriverAssignmentStrategy;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class ProcessOrderService {

    private final OrderRepositoryPort      orderRepository;
    private final OrderEventPublisherPort  eventPublisher;
    private final DriverAssignmentStrategy driverStrategy;
    private final Clock                    clock;

    public ProcessOrderService(OrderRepositoryPort orderRepository,
                               OrderEventPublisherPort eventPublisher,
                               DriverAssignmentStrategy driverStrategy,
                               Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository,  "orderRepository");
        this.eventPublisher  = Objects.requireNonNull(eventPublisher,   "eventPublisher");
        this.driverStrategy  = Objects.requireNonNull(driverStrategy,   "driverStrategy");
        this.clock           = Objects.requireNonNull(clock,            "clock");
    }

    /**
     * Procesa un pedido nuevo (desde RabbitMQ):
     *   Si es nuevo  → CREATED → PREPARED → ASSIGNED; publica ORDER_PREPARED + DRIVER_ASSIGNED.
     *   Si ya existe → solo republica DRIVER_ASSIGNED (entrega duplicada de RabbitMQ).
     */
    public void process(OrderCreatedCommand command) {
        Objects.requireNonNull(command, "command");

        var existing = orderRepository.findById(command.orderId());
        if (existing.isPresent()) {
            // Pedido ya procesado — solo republicar evento (idempotencia)
            Order order = existing.get();
            DriverAssignment a = order.assignment()
                    .orElseThrow(() -> new IllegalStateException("Order " + order.orderId() + " has no driver"));
            eventPublisher.publish(new DriverAssignedEvent(
                    order.orderId(), a.driverId(), order.customerId(), a.assignedAt()));
            return;
        }

        createNewOrder(command);
    }

    /** Cancela un pedido desde CREATED, PREPARED o ASSIGNED. Publica ORDER_CANCELLED. */
    public void cancelOrder(String orderId) {
        Objects.requireNonNull(orderId, "orderId");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel(); // lanza InvalidStateTransitionException si no es cancelable
        orderRepository.save(order);
        Instant now = clock.instant();
        eventPublisher.publish(new OrderCancelledEvent(
                order.orderId(),
                order.assignment().map(DriverAssignment::driverId).orElse("none"),
                order.customerId(),
                now));
    }

    // ── privado ───────────────────────────────────────────────────────────────

    private void createNewOrder(OrderCreatedCommand command) {
        Instant now = clock.instant();
        String itemsJson = command.items() == null || command.items().isEmpty()
                ? null : convertItemsToJson(command.items());

        Order order = Order.create(command.orderId(), command.customerId(),
                command.deliveryAddress(), command.totalAmount(), now, itemsJson);

        // CREATED → PREPARED
        order.prepare(now);
        orderRepository.save(order);
        eventPublisher.publish(new OrderPreparedEvent(
                order.orderId(), null, order.customerId(), now));

        // PREPARED → ASSIGNED
        String driverId = driverStrategy.selectDriver(order);
        order.assignDriver(new DriverAssignment(driverId, now));
        orderRepository.save(order);
        eventPublisher.publish(new DriverAssignedEvent(
                order.orderId(), driverId, order.customerId(), now));
    }

    private String convertItemsToJson(java.util.List<String> items) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(items);
        } catch (Exception e) {
            return String.join(",", items);
        }
    }
}
