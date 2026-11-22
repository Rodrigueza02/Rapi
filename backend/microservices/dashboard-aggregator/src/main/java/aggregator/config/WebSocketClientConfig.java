
package aggregator.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketClientConfig {

    @Bean
    public HttpClient dashboardHttpClient() {
        return HttpClient.newHttpClient();
    }
}
