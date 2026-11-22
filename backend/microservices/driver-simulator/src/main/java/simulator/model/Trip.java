package simulator.model;

public final class Trip {

    public enum Phase { EN_ROUTE, ARRIVING, DELIVERED }

    private final String orderId;
    private final String driverId;
    private final String customerId;
    private Phase phase;

    private Trip(String orderId, String driverId, String customerId) {
        this.orderId = requireText(orderId, "orderId");
        this.driverId = requireText(driverId, "driverId");
        this.customerId = requireText(customerId, "customerId");
        this.phase = Phase.EN_ROUTE;
    }

    /** Todo viaje nace EN_ROUTE (en camino). */
    public static Trip start(String orderId, String driverId, String customerId) {
        return new Trip(orderId, driverId, customerId);
    }

    public void markArriving() {
        requirePhase(Phase.EN_ROUTE, "mark the driver as arriving");
        this.phase = Phase.ARRIVING;
    }

    public void markDelivered() {
        requirePhase(Phase.ARRIVING, "mark the order as delivered");
        this.phase = Phase.DELIVERED;
    }

    public String orderId() {
        return orderId;
    }

    public String driverId() {
        return driverId;
    }

    public String customerId() {
        return customerId;
    }

    public Phase phase() {
        return phase;
    }

    private void requirePhase(Phase expected, String action) {
        if (phase != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + " when the trip is " + phase + " (expected " + expected + ")");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return value;
    }
}