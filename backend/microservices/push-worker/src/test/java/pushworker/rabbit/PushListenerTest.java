
package pushworker.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.NotificationCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pushworker.service.PushSenderService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushListenerTest {

    @Mock
    private PushSenderService pushSenderService;

    @Mock
    private ObjectMapper objectMapper;

    private PushListener pushListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pushListener = new PushListener(
                pushSenderService,
                objectMapper
        );
    }

    @Test
    void shouldDeserializeMessageAndSendPush() throws IOException {
        // Arrange
        byte[] messageBody = "test-message"
                .getBytes(StandardCharsets.UTF_8);

        NotificationCommand command = mock(NotificationCommand.class);

        when(objectMapper.readValue(
                messageBody,
                NotificationCommand.class
        )).thenReturn(command);

        // Act
        pushListener.receive(messageBody);

        // Assert
        verify(objectMapper, times(1))
                .readValue(messageBody, NotificationCommand.class);

        verify(pushSenderService, times(1))
                .send(command);
    }

    @Test
    void shouldPropagateExceptionWhenDeserializationFails()
            throws IOException {

        // Arrange
        byte[] messageBody = "invalid-json"
                .getBytes(StandardCharsets.UTF_8);

        when(objectMapper.readValue(
                messageBody,
                NotificationCommand.class
        )).thenThrow(new IOException("Invalid JSON"));

        // Act & Assert
        assertThrows(
                IOException.class,
                () -> pushListener.receive(messageBody)
        );

        verify(pushSenderService, never())
                .send(any(NotificationCommand.class));
    }
}
