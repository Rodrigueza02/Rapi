package processor.state;

/** CREADO → EN_PREPARACION → ASIGNADO */
public final class PreparedState implements OrderState {

    @Override public String name() { return "PREPARED"; }

    @Override public OrderState assignDriver() { return new AssignedState(); }

    @Override public OrderState cancel() { return new CancelledState(); }
}
