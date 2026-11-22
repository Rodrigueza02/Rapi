package processor.service;

import dtos.DriverAssignedEvent;
import dtos.OrderCreatedCommand;
import processor.model.DriverAssignment;
import processor.model.Order;
import processor.ports.OrderEventPublisherPort;
import processor.ports.OrderRepositoryPort;
import processor.strategy.DriverAssignmentStrategy;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class ProcessOrderService {

    private final OrderRepositoryPort orderRepository;
    private final OrderEventPublisherPort eventPublisher;
    private final DriverAssignmentStrategy driverStrategy;
    private final Clock clock;

    public ProcessOrderService(OrderRepositoryPort orderRepository,
                               OrderEventPublisherPort eventPublisher,
                               DriverAssignmentStrategy driverStrategy,
                               Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.driverStrategy = Objects.requireNonNull(driverStrategy, "driverStrategy");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void process(OrderCreatedCommand command) {
        Objects.requireNonNull(command, "command");

        Order order = orderRepository.findById(command.orderId())
                .orElseGet(() -> registerNewOrder(command));

        DriverAssignment assignment = order.assignment()
                .orElseThrow(() -> new IllegalStateException(
                        "Order " + order.orderId() + " has no driver assigned"));

        eventPublisher.publish(new DriverAssignedEvent(
                order.orderId(), assignment.driverId(), assignment.assignedAt()));
    }

    private Order registerNewOrder(OrderCreatedCommand command) {
        Instant now = clock.instant();
        Order order = Order.create(command.orderId(), command.customerId(),
                command.deliveryAddress(), command.totalAmount(), now);

        String driverId = driverStrategy.selectDriver(order);   // Strategy
        order.assignDriver(new DriverAssignment(driverId, now)); // State dentro de Order

        orderRepository.save(order);
        return order;
    }
}