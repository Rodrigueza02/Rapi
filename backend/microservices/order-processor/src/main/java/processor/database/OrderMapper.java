package processor.database;

import processor.model.DriverAssignment;
import processor.model.Order;
import processor.state.OrderState;

import java.util.Optional;

final class OrderMapper {
    private OrderMapper() {}

    static OrderEntity toEntity(Order order) {
        Optional<DriverAssignment> a = order.assignment();
        return new OrderEntity(
                order.orderId(), order.customerId(), order.deliveryAddress(),
                order.totalAmount(), order.statusName(),
                a.map(DriverAssignment::driverId).orElse(null),
                order.createdAt(),
                order.preparedAt().orElse(null),
                a.map(DriverAssignment::assignedAt).orElse(null),
                order.arrivedAt().orElse(null),
                order.deliveredAt().orElse(null),
                order.itemsJson().orElse(null));
    }

    static Order toDomain(OrderEntity e) {
        DriverAssignment a = e.getDriverId() == null ? null
                : new DriverAssignment(e.getDriverId(), e.getAssignedAt());
        return Order.restore(e.getOrderId(), e.getCustomerId(), e.getDeliveryAddress(),
                e.getTotalAmount(), e.getCreatedAt(), OrderState.fromName(e.getStatus()),
                a, e.getPreparedAt(), e.getArrivedAt(), e.getDeliveredAt(),
                e.getItemsJson());
    }
}
