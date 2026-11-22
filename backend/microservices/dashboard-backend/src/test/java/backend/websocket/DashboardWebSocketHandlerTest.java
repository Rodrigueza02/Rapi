
package backend.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DashboardWebSocketHandlerTest {

    @Test
    void shouldBroadcastAggregatorMessageToClient()
            throws Exception {

        DashboardWebSocketHandler handler =
                new DashboardWebSocketHandler();

        WebSocketSession client =
                mock(WebSocketSession.class);

        WebSocketSession aggregator =
                mock(WebSocketSession.class);

        when(client.getUri())
                .thenReturn(URI.create("ws://localhost:8086/ws"));

        when(aggregator.getUri())
                .thenReturn(
                        URI.create("ws://localhost:8086/ws/ingest")
                );

        when(client.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(client);
        handler.afterConnectionEstablished(aggregator);

        handler.handleTextMessage(
                aggregator,
                new TextMessage("{\"order_id\":\"order-1\"}")
        );

        verify(client).sendMessage(
                argThat(message ->
                        message instanceof TextMessage
                        && ((TextMessage) message)
                                .getPayload()
                                .contains("order-1")
                )
        );

        verify(aggregator, never()).sendMessage(any());
    }
}
