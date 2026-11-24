package processor.rabbit;

import dtos.Names;
import dtos.OrderCreatedCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import processor.service.ProcessOrderService;

@Component
public class NewOrderListener {

    private static final Logger log = LoggerFactory.getLogger(NewOrderListener.class);

    private final ProcessOrderService processOrderService;

    public NewOrderListener(ProcessOrderService processOrderService) {
        this.processOrderService = processOrderService;
    }

    @RabbitListener(queues = Names.NEW_ORDERS_QUEUE)
    public void onNewOrder(OrderCreatedCommand command) {
        log.info("Command received from RabbitMQ: order {}", command.orderId());
        processOrderService.process(command);
    }
}