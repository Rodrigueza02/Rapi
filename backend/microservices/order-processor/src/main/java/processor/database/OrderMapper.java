package processor.database;

import processor.model.DriverAssignment;
import processor.model.Order;
import processor.state.OrderState;

import java.util.Optional;

final class OrderMapper {

    private OrderMapper() {
    }

    static OrderEntity toEntity(Order order) {
        Optional<DriverAssignment> assignment = order.assignment();
        return new OrderEntity(
                order.orderId(),
                order.customerId(),
                order.deliveryAddress(),
                order.totalAmount(),
                order.statusName(),
                assignment.map(DriverAssignment::driverId).orElse(null),
                order.createdAt(),
                assignment.map(DriverAssignment::assignedAt).orElse(null),
                order.arrivedAt().orElse(null),
                order.deliveredAt().orElse(null),
                order.itemsJson().orElse(null));
    }

    static Order toDomain(OrderEntity entity) {
        DriverAssignment assignment = entity.getDriverId() == null
                ? null
                : new DriverAssignment(entity.getDriverId(), entity.getAssignedAt());

        Order order = Order.restore(
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getDeliveryAddress(),
                entity.getTotalAmount(),
                entity.getCreatedAt(),
                OrderState.fromName(entity.getStatus()),
                assignment,
                entity.getArrivedAt(),
                entity.getDeliveredAt());
        return order;
    }
}