package auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Lee los valores jwt.secret y jwt.expires-in del application.yml
 * y los expone como bean inyectable.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    /** Clave Base64 de al menos 256 bits para firmar los tokens HS256. */
    private String secret;

    /** Tiempo de vida del token en segundos (por defecto 3600). */
    private long expiresIn = 3600;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
