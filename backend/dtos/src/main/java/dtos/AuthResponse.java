
package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("email") String email,
        @JsonProperty("rol") String rol
) {}
