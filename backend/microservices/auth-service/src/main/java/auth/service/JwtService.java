package auth.service;

import auth.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Firma y verifica JWT usando HS256.
 *
 * Principio SRP: esta clase solo conoce la lógica JWT,
 * no sabe nada de usuarios ni de HTTP.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long      expiresInMillis;

    public JwtService(JwtConfig config) {
        byte[] keyBytes = Base64.getDecoder().decode(config.getSecret());
        this.signingKey     = Keys.hmacShaKeyFor(keyBytes);
        this.expiresInMillis = config.getExpiresIn() * 1_000L;
    }

    /**
     * Genera un token firmado con los claims indicados.
     *
     * @param subject  valor del claim "sub" (email del usuario)
     * @param extra    claims adicionales que se incrustan en el payload
     * @return token JWT como String
     */
    public String generate(String subject, Map<String, Object> extra) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claims(extra)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiresInMillis))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifica la firma y la expiración del token.
     *
     * @param token JWT a verificar
     * @return claims del payload
     * @throws JwtException si el token es inválido o ha expirado
     */
    public Claims verify(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
