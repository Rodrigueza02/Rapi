package processor.rabbit;

import dtos.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import processor.service.ProcessOrderService;

import java.util.Map;

/**
 * Escucha solicitudes de cancelación desde RabbitMQ.
 * SRP: única responsabilidad — recibir el mensaje y delegar al service.
 */
@Component
public class CancelOrderListener {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderListener.class);

    private final ProcessOrderService processOrderService;

    public CancelOrderListener(ProcessOrderService processOrderService) {
        this.processOrderService = processOrderService;
    }

    @RabbitListener(queues = Names.CANCEL_ORDERS_QUEUE)
    public void onCancelOrder(Map<String, String> message) {
        String orderId = message.get("order_id");
        log.info("Cancel request received for order {}", orderId);
        processOrderService.cancelOrder(orderId);
    }
}
