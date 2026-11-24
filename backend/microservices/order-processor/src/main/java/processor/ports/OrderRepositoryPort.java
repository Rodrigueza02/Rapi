package processor.ports;

import processor.model.Order;

import java.util.Optional;

public interface OrderRepositoryPort {

    void save(Order order);

    Optional<Order> findById(String orderId);
}