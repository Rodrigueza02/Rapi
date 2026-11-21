package processor.state;

public final class OnTheWayState implements OrderState {

    @Override
    public String name() {
        return "ON_THE_WAY";
    }

    @Override
    public OrderState registerArrival() {
        return this; 
    }

    @Override
    public OrderState completeDelivery() {
        return new DeliveredState();
    }
}