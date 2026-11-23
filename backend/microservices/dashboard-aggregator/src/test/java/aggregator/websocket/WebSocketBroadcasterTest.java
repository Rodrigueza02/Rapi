
package aggregator.websocket;

import aggregator.model.OrderView;

import com.fasterxml.jackson.databind.ObjectMapper;

import dtos.DriverAssignedEvent;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebSocketBroadcasterTest {

    @Test
    void shouldSendOrderViewAsJson() {

        HttpClient httpClient = mock(HttpClient.class);
        WebSocket.Builder builder =
                mock(WebSocket.Builder.class);
        WebSocket socket = mock(WebSocket.class);

        when(httpClient.newWebSocketBuilder())
                .thenReturn(builder);

        when(builder.buildAsync(
                any(),
                any(WebSocket.Listener.class)
        )).thenReturn(
                CompletableFuture.completedFuture(socket)
        );

        when(socket.sendText(anyString(), eq(true)))
                .thenReturn(
                        CompletableFuture.completedFuture(socket)
                );

        OrderView view = OrderView.from(
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        Instant.parse("2026-01-01T10:00:00Z")
                )
        );

        WebSocketBroadcaster broadcaster =
                new WebSocketBroadcaster(
                        httpClient,
                        new ObjectMapper(),
                        "ws://localhost:8086/ws/ingest"
                );

        broadcaster.broadcast(view);

        
        verify(socket).sendText(
                argThat(message ->
                        message.toString().contains("\"order_id\":\"order-1\"")
                        && message.toString().contains("\"state\":\"ASSIGNED\"")
                ),
                eq(true)
        );

    }
}
