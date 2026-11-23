package processor.service;

import dtos.DriverAssignedEvent;
import dtos.OrderCreatedCommand;
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

    private final OrderRepositoryPort     orderRepository;
    private final OrderEventPublisherPort eventPublisher;
    private final DriverAssignmentStrategy driverStrategy;
    private final Clock                   clock;

    public ProcessOrderService(OrderRepositoryPort orderRepository,
                               OrderEventPublisherPort eventPublisher,
                               DriverAssignmentStrategy driverStrategy,
                               Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository,  "orderRepository");
        this.eventPublisher  = Objects.requireNonNull(eventPublisher,   "eventPublisher");
        this.driverStrategy  = Objects.requireNonNull(driverStrategy,   "driverStrategy");
        this.clock           = Objects.requireNonNull(clock,            "clock");
    }

    /** Procesa un pedido nuevo: asigna repartidor y publica evento Kafka. */
    public void process(OrderCreatedCommand command) {
        Objects.requireNonNull(command, "command");
        Order order = orderRepository.findById(command.orderId())
                .orElseGet(() -> registerNewOrder(command));
        DriverAssignment assignment = order.assignment()
                .orElseThrow(() -> new IllegalStateException(
                        "Order " + order.orderId() + " has no driver assigned"));
        eventPublisher.publish(new DriverAssignedEvent(
                order.orderId(), assignment.driverId(), order.customerId(), assignment.assignedAt()));
    }

    /**
     * Cancela un pedido si está en estado CREATED o ASSIGNED.
     * El estado CancelledState lanza InvalidStateTransitionException
     * si el pedido ya está en camino o entregado.
     */
    public void cancelOrder(String orderId) {
        Objects.requireNonNull(orderId, "orderId");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        orderRepository.save(order);
    }

    private Order registerNewOrder(OrderCreatedCommand command) {
        Instant now   = clock.instant();
        String itemsJson = command.items() == null || command.items().isEmpty() ? null
                : convertItemsToJson(command.items());
        Order   order = Order.create(command.orderId(), command.customerId(),
                command.deliveryAddress(), command.totalAmount(), now, itemsJson);
        String driverId = driverStrategy.selectDriver(order);
        order.assignDriver(new DriverAssignment(driverId, now));
        orderRepository.save(order);
        return order;
    }

    private String convertItemsToJson(java.util.List<String> items) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(items);
        } catch (Exception e) {
            return String.join(",", items);
        }
    }
}
