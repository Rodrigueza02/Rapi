package emailworker.infrastructure.rabbit;

import com.rabbitmq.client.Channel;
import emailworker.application.EmailProcessingService;
import emailworker.application.IdempotencyGuard;
import emailworker.domain.EmailMessage;
import emailworker.domain.EmailNotificationType;
import emailworker.domain.EmailStatus;
import emailworker.dto.EmailCommand;
import emailworker.exception.DuplicateCommandException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailCommandListenerTest {

    @Mock IdempotencyGuard guard;
    @Mock EmailCommandMapper mapper;
    @Mock EmailProcessingService processor;
    @Mock EmailListenerErrorHandler errorHandler;
    @Mock Channel channel;

    @InjectMocks EmailCommandListener listener;

    EmailCommand validCmd;
    EmailMessage validMessage;

    @BeforeEach
    void setUp() {
        validCmd = new EmailCommand("cmd-1", "to@x.com", "from@x.com",
                "reply@x.com", "Asunto", "Cuerpo", null, "ADMIN_MANUAL", null);
        validMessage = new EmailMessage("cmd-1", "to@x.com", "from@x.com",
                "reply@x.com", "Asunto", "Cuerpo", null, EmailNotificationType.ADMIN_MANUAL, null);
    }

    @Test
    void processesValidCommand_thenAcks() throws IOException {
        when(mapper.toMessage(validCmd)).thenReturn(validMessage);
        listener.onEmailCommand(validCmd, channel, 1L);
        verify(processor).process(validMessage);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void duplicateCommand_acksWithoutProcessing() throws IOException {
        doThrow(new DuplicateCommandException("cmd-1", EmailStatus.SMTP_ACCEPTED))
                .when(guard).checkCanProcess("cmd-1");
        listener.onEmailCommand(validCmd, channel, 2L);
        verify(processor, never()).process(any());
        verify(errorHandler).ackDuplicate(eq(channel), eq(2L), any());
    }

    @Test
    void invalidCommand_nacksWithoutProcessing() throws IOException {
        EmailCommand bad = new EmailCommand(null, "to@x.com", "from@x.com",
                "reply@x.com", "Asunto", "Cuerpo", null, "ADMIN_MANUAL", null);
        listener.onEmailCommand(bad, channel, 3L);
        verify(processor, never()).process(any());
        verify(channel).basicNack(3L, false, false);
    }

    @Test
    void runtimeException_nacksViaErrorHandler() throws IOException {
        when(mapper.toMessage(validCmd)).thenReturn(validMessage);
        doThrow(new RuntimeException("smtp error")).when(processor).process(any());
        listener.onEmailCommand(validCmd, channel, 4L);
        verify(errorHandler).nackWithRequeue(eq(channel), eq(4L), eq("cmd-1"), any());
    }
}
