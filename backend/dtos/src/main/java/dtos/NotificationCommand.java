package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.UUID;

public record NotificationCommand(
        @JsonProperty("command_id") String commandId,
        @JsonProperty("routing_key") String routingKey,
        @JsonProperty("payload") NotificationPayloadRecord payload) {

    private static final Set<String> VALID_ROUTING_KEYS =
            Set.of(Names.ROUTING_CLIENT_SMS, Names.ROUTING_DRIVER_PUSH);

    public NotificationCommand {
        Validation.requireText(commandId, "command_id");
        Validation.requireText(routingKey, "routing_key");
        if (!VALID_ROUTING_KEYS.contains(routingKey)) {
            throw new IllegalArgumentException("routing_key not supported: " + routingKey);
        }
        Validation.requireNonNull(payload, "payload");
    }

    public static NotificationCommand of(String routingKey, NotificationPayloadRecord payload) {
        return new NotificationCommand(UUID.randomUUID().toString(), routingKey, payload);
    }
}