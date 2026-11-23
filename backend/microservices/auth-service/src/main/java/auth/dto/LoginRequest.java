package auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cuerpo del POST /api/auth/login.
 * El frontend envía { "email": "...", "password": "..." }.
 */
public record LoginRequest(
        @JsonProperty("email")    String email,
        @JsonProperty("password") String password
) {}
