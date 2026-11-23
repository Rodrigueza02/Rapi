package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequest(
        @JsonProperty("nombre") String nombre,
        @JsonProperty("email") String email,
        @JsonProperty("password") String password
) {}
