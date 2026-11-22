
package smsworker.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.Names;
import dtos.NotificationCommand;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import smsworker.service.SmsSenderService;

import java.io.IOException;

@Component
public class SmsListener {

    private final ObjectMapper objectMapper;
    private final SmsSenderService smsSenderService;

    public SmsListener(
            ObjectMapper objectMapper,
            SmsSenderService smsSenderService) {

        this.objectMapper = objectMapper;
        this.smsSenderService = smsSenderService;
    }

    @RabbitListener(queues = Names.SMS_QUEUE)
    public void receive(byte[] body) throws IOException {

        NotificationCommand command =
                objectMapper.readValue(
                        body,
                        NotificationCommand.class
                );

        smsSenderService.send(command);
    }
}
