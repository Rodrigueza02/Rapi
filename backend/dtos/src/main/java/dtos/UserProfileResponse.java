
package dtos;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileResponse(

    @JsonProperty("user_id")
    UUID userId,

    @JsonProperty("nombre")
    String nombre,

    @JsonProperty("email")
    String email,

    @JsonProperty("rol")
    String rol

) {}
