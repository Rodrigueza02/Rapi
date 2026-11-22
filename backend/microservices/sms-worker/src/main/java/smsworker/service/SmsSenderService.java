package smsworker.service;

import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsSenderService {

    private static final Logger log =
            LoggerFactory.getLogger(SmsSenderService.class);

    public void send(NotificationCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Notification command is required"
            );
        }

        if (!Names.ROUTING_CLIENT_SMS.equals(command.routingKey())) {
            throw new IllegalArgumentException(
                    "Unsupported SMS routing key: "
                            + command.routingKey()
            );
        }

        NotificationPayloadRecord payload = command.payload();

        log.info(
                "SIMULATED SMS | commandId={} | orderId={} | recipientId={}",
                command.commandId(),
                payload.orderId(),
                payload.recipientId()
        );

        log.debug(
                "SMS simulation completed for commandId={}",
                command.commandId()
        );
    }
}
