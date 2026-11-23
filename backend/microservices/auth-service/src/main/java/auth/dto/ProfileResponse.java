package auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del GET /api/auth/me.
 *
 * El authService.js del frontend espera:
 *   profile.nombre
 *   profile.rol
 */
public record ProfileResponse(
        @JsonProperty("nombre") String nombre,
        @JsonProperty("rol")    String rol,
        @JsonProperty("email")  String email
) {}
