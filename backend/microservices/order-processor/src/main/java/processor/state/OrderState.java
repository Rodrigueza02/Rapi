package processor.state;

import processor.exception.InvalidStateTransitionException;

import java.util.Map;

public sealed interface OrderState
        permits CreatedState, AssignedState, OnTheWayState, DeliveredState {

    String name();

    default OrderState assignDriver() {
        throw notAllowed("assign a driver");
    }

    default OrderState startDelivery() {
        throw notAllowed("start the delivery");
    }

    default OrderState registerArrival() {
        throw notAllowed("register the arrival");
    }

    default OrderState completeDelivery() {
        throw notAllowed("complete the delivery");
    }

    private InvalidStateTransitionException notAllowed(String action) {
        return new InvalidStateTransitionException(
                "Cannot " + action + " when the order is " + name());
    }

    /** Reconstruye un estado a partir de su nombre (lo usará la base de datos). */
    static OrderState fromName(String name) {
        OrderState state = Map.<String, OrderState>of(
                "CREATED", new CreatedState(),
                "ASSIGNED", new AssignedState(),
                "ON_THE_WAY", new OnTheWayState(),
                "DELIVERED", new DeliveredState()).get(name);
        if (state == null) {
            throw new IllegalArgumentException("Unknown order state: " + name);
        }
        return state;
    }
}