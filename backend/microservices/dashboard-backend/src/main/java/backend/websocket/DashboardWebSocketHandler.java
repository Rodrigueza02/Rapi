
package backend.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class DashboardWebSocketHandler
        extends TextWebSocketHandler {

    private final Set<WebSocketSession> clients =
            ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        if (isClient(session)) {
            clients.add(session);
        }
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws IOException {

        // Solo las conexiones de ingestión
        // pueden publicar actualizaciones.
        if (!isIngest(session)) {
            return;
        }

        broadcast(message.getPayload());
    }

    public void broadcast(String payload) throws IOException {

        for (WebSocketSession client : clients) {

            if (!client.isOpen()) {
                clients.remove(client);
                continue;
            }

            synchronized (client) {
                try {
                    client.sendMessage(new TextMessage(payload));
                } catch (IOException exception) {
                    clients.remove(client);
                    throw exception;
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        clients.remove(session);
    }

    private boolean isClient(WebSocketSession session) {
        return "/ws".equals(session.getUri().getPath());
    }

    private boolean isIngest(WebSocketSession session) {
        return "/ws/ingest".equals(session.getUri().getPath());
    }
}
