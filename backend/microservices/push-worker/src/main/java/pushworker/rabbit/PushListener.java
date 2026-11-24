
package pushworker.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.Names;
import dtos.NotificationCommand;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pushworker.service.PushSenderService;

import java.io.IOException;

@Component
public class PushListener {

    private final PushSenderService pushSenderService;
    private final ObjectMapper objectMapper;

    public PushListener(
            PushSenderService pushSenderService,
            ObjectMapper objectMapper) {

        this.pushSenderService = pushSenderService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = Names.PUSH_QUEUE)
    public void receive(byte[] messageBody) throws IOException {

        NotificationCommand command =
                objectMapper.readValue(
                        messageBody,
                        NotificationCommand.class
                );

        pushSenderService.send(command);
    }
}
