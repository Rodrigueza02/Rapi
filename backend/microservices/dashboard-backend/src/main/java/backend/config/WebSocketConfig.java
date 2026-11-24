
package backend.config;

import backend.websocket.DashboardWebSocketHandler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DashboardWebSocketHandler handler;

    public WebSocketConfig(
            DashboardWebSocketHandler handler) {

        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {

        registry.addHandler(handler, "/ws/ingest");

        registry.addHandler(handler, "/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "https://rappi-stream.duckdns.org",
                        "http://rappi-stream.duckdns.org"
                );
    }
}
