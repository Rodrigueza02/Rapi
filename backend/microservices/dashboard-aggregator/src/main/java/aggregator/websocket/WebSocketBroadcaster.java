package aggregator.websocket;

import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebSocketBroadcaster
        implements DashboardBroadcastPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI backendUri;

    private WebSocket connection;

    public WebSocketBroadcaster(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${dashboard.backend.websocket-url}")
            String websocketUrl) {

        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.backendUri = URI.create(websocketUrl);
    }

    @Override
    public synchronized void broadcast(OrderView orderView) {

        String message = toJson(orderView);

        try {
            WebSocket socket = getConnection();

            socket.sendText(message, true).join();

        } catch (RuntimeException exception) {

            // La siguiente llamada intentará reconectarse.
            connection = null;

            throw new IllegalStateException(
                    "No se pudo enviar la actualización al Dashboard",
                    exception
            );
        }
    }

    private WebSocket getConnection() {

        if (connection == null
                || connection.isInputClosed()
                || connection.isOutputClosed()) {

            connection = httpClient
                    .newWebSocketBuilder()
                    .buildAsync(
                            backendUri,
                            new DashboardSocketListener()
                    )
                    .join();
        }

        return connection;
    }

    private String toJson(OrderView view) {

        Map<String, Object> message = new LinkedHashMap<>();

        message.put("order_id",   view.getOrderId());
        message.put("driver_id",  view.getDriverId());
        message.put("customer_id", view.getCustomerId());
        message.put("state",      view.getState().name());
        message.put("last_updated", view.getLastUpdated().toString());

        // Incluir campos de dirección, productos y total cuando estén disponibles,
        // para que el frontend pueda actualizar la tabla sin un fetch REST adicional.
        if (view.getDeliveryAddress() != null)
            message.put("delivery_address", view.getDeliveryAddress());
        if (view.getTotalAmount() != null)
            message.put("total_amount", view.getTotalAmount());
        if (view.getItemsJson() != null)
            message.put("items_json", view.getItemsJson());

        try {
            return objectMapper.writeValueAsString(message);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudo convertir OrderView a JSON",
                    exception
            );
        }
    }

    private static class DashboardSocketListener
            implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(
                WebSocket webSocket,
                CharSequence data,
                boolean last) {

            webSocket.request(1);
            return null;
        }
    }
}
