package aggregator.state;

/**
 * Estados visibles de un pedido en el Dashboard.
 * Orden cronológico: CREATED → PREPARED → ASSIGNED → ON_THE_WAY → ARRIVING → DELIVERED
 * Estado terminal alternativo: CANCELLED
 */
public enum OrderViewState {

    CREATED,
    PREPARED,
    ASSIGNED,
    ON_THE_WAY,
    ARRIVING,
    DELIVERED,
    CANCELLED;

    public boolean isActive() {
        return this == CREATED || this == PREPARED || this == ASSIGNED
                || this == ON_THE_WAY || this == ARRIVING;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean canTransitionTo(OrderViewState next) {
        if (next == null) return false;
        return switch (this) {
            case CREATED    -> next == PREPARED   || next == CANCELLED;
            case PREPARED   -> next == ASSIGNED   || next == CANCELLED;
            case ASSIGNED   -> next == ON_THE_WAY || next == ARRIVING || next == CANCELLED;
            case ON_THE_WAY -> next == ARRIVING   || next == DELIVERED;
            case ARRIVING   -> next == DELIVERED;
            case DELIVERED  -> false;
            case CANCELLED  -> false;
        };
    }
}
