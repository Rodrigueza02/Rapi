package processor.state;

public final class CreatedState implements OrderState {

    @Override public String name() { return "CREATED"; }

    @Override public OrderState prepare()      { return new PreparedState(); }

    @Override public OrderState assignDriver() { return new AssignedState(); }

    @Override public OrderState cancel()       { return new CancelledState(); }
}
