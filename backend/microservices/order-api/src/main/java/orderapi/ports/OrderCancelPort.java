package orderapi.ports;

/**
 * Puerto de cancelación de pedidos.
 * La implementación concreta envía la solicitud al order-processor vía RabbitMQ.
 */
public interface OrderCancelPort {
    void cancel(String orderId);
}
