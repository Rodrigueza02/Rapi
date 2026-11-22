
package aggregator.state;

/**
 * Estados visibles de un pedido en el Dashboard.
 */
public enum OrderViewState {

    ASSIGNED,
    ARRIVING,
    DELIVERED;

    /**
     * Verifica las transiciones permitidas.
     */
    public boolean canTransitionTo(OrderViewState next) {
        if (next == null) {
            return false;
        }

        return switch (this) {
            case ASSIGNED -> next == ARRIVING;
            case ARRIVING -> next == DELIVERED;
            case DELIVERED -> false;
        };
    }
}
