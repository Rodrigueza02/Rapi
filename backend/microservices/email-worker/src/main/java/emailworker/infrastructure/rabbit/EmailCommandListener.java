package emailworker.infrastructure.rabbit;

import com.rabbitmq.client.Channel;
import emailworker.application.EmailProcessingService;
import emailworker.application.IdempotencyGuard;
import emailworker.domain.EmailMessage;
import emailworker.dto.EmailCommand;
import emailworker.exception.DuplicateCommandException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmailCommandListener {

    private static final Logger log = LoggerFactory.getLogger(EmailCommandListener.class);

    private final IdempotencyGuard guard;
    private final EmailCommandMapper mapper;
    private final EmailProcessingService processor;
    private final EmailListenerErrorHandler errorHandler;

    public EmailCommandListener(IdempotencyGuard guard,
                                EmailCommandMapper mapper,
                                EmailProcessingService processor,
                                EmailListenerErrorHandler errorHandler) {
        this.guard        = guard;
        this.mapper       = mapper;
        this.processor    = processor;
        this.errorHandler = errorHandler;
    }

    @RabbitListener(queues = "${app.email.queue}")
    public void onEmailCommand(EmailCommand cmd,
                               Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        if (!isValid(cmd)) {
            log.warn("Comando inválido recibido, descartando: commandId={}", cmd.commandId());
            channel.basicNack(tag, false, false);
            return;
        }

        log.debug("Recibido EmailCommand: commandId={}", cmd.commandId());

        try {
            mapper.persist(cmd);
            guard.checkCanProcess(cmd.commandId());
            EmailMessage message = mapper.toMessage(cmd);
            processor.process(message);
            channel.basicAck(tag, false);
            log.info("EmailCommand procesado con éxito: commandId={}", cmd.commandId());

        } catch (DuplicateCommandException e) {
            errorHandler.ackDuplicate(channel, tag, e);

        } catch (RuntimeException e) {
            errorHandler.nackWithRequeue(channel, tag, cmd.commandId(), e);
        }
    }

    private boolean isValid(EmailCommand cmd) {
        return cmd != null
                && isNotBlank(cmd.commandId())
                && isNotBlank(cmd.recipientEmail())
                && isNotBlank(cmd.subject())
                && isNotBlank(cmd.bodyText())
                && isNotBlank(cmd.senderEmail());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
