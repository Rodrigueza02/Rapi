
package pushworker.service;

import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushSenderService {

    private static final Logger log =
            LoggerFactory.getLogger(PushSenderService.class);

    public void send(NotificationCommand command) {

        if (!Names.ROUTING_DRIVER_PUSH.equals(command.routingKey())) {
            throw new IllegalArgumentException(
                    "Unsupported push routing key: " + command.routingKey()
            );
        }

        NotificationPayloadRecord payload = command.payload();

        log.info(
                "SIMULATED PUSH | commandId={} | orderId={} | driverId={} | message={}",
                command.commandId(),
                payload.orderId(),
                payload.recipientId(),
                payload.message()
        );
    }
}
