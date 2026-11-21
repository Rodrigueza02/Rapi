package processor.model;

import java.time.Instant;

/**
 * Value Object: a quién se le asignó el pedido y cuándo. Inmutable y Fail-Fast.
 */
public record DriverAssignment(String driverId, Instant assignedAt) {

    public DriverAssignment {
        if (driverId == null || driverId.isBlank()) {
            throw new IllegalArgumentException("driverId must not be null or blank");
        }
        if (assignedAt == null) {
            throw new IllegalArgumentException("assignedAt must not be null");
        }
    }
}