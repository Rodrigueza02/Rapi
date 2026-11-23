package orderapi.service;

import orderapi.ports.OrderCancelPort;
import org.springframework.stereotype.Service;

/**
 * SRP: única responsabilidad — orquestar la cancelación de un pedido.
 * Delega la comunicación al puerto OrderCancelPort.
 */
@Service
public class CancelOrderService {

    private final OrderCancelPort cancelPort;

    public CancelOrderService(OrderCancelPort cancelPort) {
        this.cancelPort = cancelPort;
    }

    public void cancel(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId es requerido.");
        }
        cancelPort.cancel(orderId);
    }
}
