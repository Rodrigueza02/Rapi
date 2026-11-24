
package backend.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class DashboardWebSocketHandler
        extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DashboardWebSocketHandler.class);

    private final Set<WebSocketSession> clients =
            ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        if (isClient(session)) {
            clients.add(session);
            log.debug("Cliente WS conectado: {} (total: {})", session.getId(), clients.size());
        }
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws IOException {

        // Solo las conexiones de ingestión pueden publicar actualizaciones.
        if (!isIngest(session)) {
            return;
        }

        broadcast(message.getPayload());
    }

    /**
     * Envía {@code payload} a todos los clientes frontend conectados.
     *
     * <p>Cambios respecto al original:
     * <ul>
     *   <li>Captura {@link IOException} e {@link IllegalStateException} por cliente.</li>
     *   <li>Retira el cliente problemático y <b>continúa</b> con los demás en lugar
     *       de interrumpir el broadcast y relanzar la excepción.</li>
     *   <li>Nunca propaga la excepción al llamador (el ingestor de Kafka), de modo que
     *       un cliente navegador desconectado no detiene la ingestión de eventos.</li>
     * </ul>
     */
    public void broadcast(String payload) {

        for (WebSocketSession client : clients) {

            if (!client.isOpen()) {
                clients.remove(client);
                log.debug("Cliente WS ya cerrado, retirado: {}", client.getId());
                continue;
            }

            synchronized (client) {
                try {
                    client.sendMessage(new TextMessage(payload));
                } catch (IllegalStateException e) {
                    // La sesión fue cerrada justo entre la comprobación isOpen() y el envío.
                    clients.remove(client);
                    log.warn("Cliente WS cerrado durante broadcast (IllegalStateException), retirado: {}", client.getId());
                } catch (IOException e) {
                    // Error de red o I/O en el cliente — se retira y se continúa.
                    clients.remove(client);
                    log.warn("IOException al enviar a cliente WS {}, retirado: {}", client.getId(), e.getMessage());
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        clients.remove(session);
        log.debug("Cliente WS desconectado: {} status: {} (restantes: {})",
                session.getId(), status.getCode(), clients.size());
    }

    private boolean isClient(WebSocketSession session) {
        return session.getUri() != null && "/ws".equals(session.getUri().getPath());
    }

    private boolean isIngest(WebSocketSession session) {
        return session.getUri() != null && "/ws/ingest".equals(session.getUri().getPath());
    }
}
