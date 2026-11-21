package processor.state;

public final class AssignedState implements OrderState {

    @Override
    public String name() {
        return "ASSIGNED";
    }

    @Override
    public OrderState startDelivery() {
        return new OnTheWayState();
    }
}