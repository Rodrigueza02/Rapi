package auth.service;

import auth.config.JwtConfig;
import auth.dto.LoginResponse;
import auth.dto.ProfileResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Lógica de negocio de autenticación.
 *
 * Principio SRP: valida credenciales y delega la generación/verificación
 * de tokens a JwtService.
 *
 * Usuario único configurado en application.yml (sin base de datos).
 */
@Service
public class AuthService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final String     adminEmail;
    private final String     adminPassword;
    private final String     adminName;
    private final JwtService jwtService;
    private final long       expiresIn;

    public AuthService(
            @Value("${app.admin.email}")    String adminEmail,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.admin.name}")     String adminName,
            JwtService jwtService,
            JwtConfig  jwtConfig) {

        this.adminEmail    = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName     = adminName;
        this.jwtService    = jwtService;
        this.expiresIn     = jwtConfig.getExpiresIn();
    }

    /**
     * Verifica las credenciales y devuelve la respuesta de login con JWT.
     *
     * @throws IllegalArgumentException si el email o la contraseña son incorrectos
     */
    public LoginResponse login(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Credenciales requeridas.");
        }

        boolean emailMatch    = adminEmail.equalsIgnoreCase(email.trim());
        boolean passwordMatch = adminPassword.equals(password);

        if (!emailMatch || !passwordMatch) {
            // Mismo mensaje para email y password: evita enumerar usuarios.
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        String token = jwtService.generate(
                adminEmail,
                Map.of("rol", ROLE_ADMIN, "nombre", adminName)
        );

        return new LoginResponse(token, ROLE_ADMIN, adminName, adminEmail, expiresIn);
    }

    /**
     * Extrae el perfil del usuario desde el JWT.
     *
     * @param bearerHeader valor completo del header Authorization ("Bearer <token>")
     * @throws JwtException si el token es inválido o expirado
     */
    public ProfileResponse me(String bearerHeader) {
        String token  = extractToken(bearerHeader);
        Claims claims = jwtService.verify(token);

        String nombre = claims.get("nombre", String.class);
        String rol    = claims.get("rol",    String.class);
        String email  = claims.getSubject();

        return new ProfileResponse(nombre, rol, email);
    }

    // ── privado ───────────────────────────────────────────────────────────────

    private String extractToken(String bearerHeader) {
        if (bearerHeader == null || !bearerHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization inválido.");
        }
        return bearerHeader.substring(7).trim();
    }
}
