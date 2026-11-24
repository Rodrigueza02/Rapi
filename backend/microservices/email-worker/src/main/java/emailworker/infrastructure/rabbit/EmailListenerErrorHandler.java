package emailworker.infrastructure.rabbit;

import com.rabbitmq.client.Channel;
import emailworker.exception.DuplicateCommandException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmailListenerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(EmailListenerErrorHandler.class);

    public void ackDuplicate(Channel channel, long tag, DuplicateCommandException e)
            throws IOException {
        log.info("Comando duplicado ({}), descartando con ACK: commandId={}",
                e.getCurrentStatus(), e.getCommandId());
        channel.basicAck(tag, false);
    }

    public void nackAndDiscard(Channel channel, long tag, String commandId, Throwable e)
            throws IOException {
        log.error("Error irrecuperable procesando commandId={}: {}", commandId,
                e.getMessage(), e);
        channel.basicNack(tag, false, false);
    }

    public void nackWithRequeue(Channel channel, long tag, String commandId, Throwable e)
            throws IOException {
        log.warn("Error transitorio procesando commandId={}, sin requeue: {}",
                commandId, e.getMessage());
        channel.basicNack(tag, false, false);
    }
}
