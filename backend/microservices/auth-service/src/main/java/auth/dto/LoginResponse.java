package auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del POST /api/auth/login.
 *
 * El authService.js del frontend espera exactamente estos campos:
 *   data.access_token
 *   data.rol
 *   data.nombre
 *   data.email
 *   data.expires_in
 */
public record LoginResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("rol")          String rol,
        @JsonProperty("nombre")       String nombre,
        @JsonProperty("email")        String email,
        @JsonProperty("expires_in")   long   expiresIn
) {}
