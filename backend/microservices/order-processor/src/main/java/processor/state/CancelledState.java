package processor.state;

/**
 * Estado final: pedido cancelado.
 * No permite ninguna transición posterior.
 */
public final class CancelledState implements OrderState {
    @Override
    public String name() {
        return "CANCELLED";
    }
}
