package processor.state;

import processor.exception.InvalidStateTransitionException;
import java.util.Map;

public sealed interface OrderState
        permits CreatedState, AssignedState, OnTheWayState, DeliveredState, CancelledState {

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

    /**
     * Solo se puede cancelar desde CREATED o ASSIGNED.
     * ON_THE_WAY y DELIVERED no permiten cancelación.
     */
    default OrderState cancel() {
        throw notAllowed("cancel");
    }

    private InvalidStateTransitionException notAllowed(String action) {
        return new InvalidStateTransitionException(
                "Cannot " + action + " when the order is " + name());
    }

    static OrderState fromName(String name) {
        OrderState state = Map.<String, OrderState>of(
                "CREATED",   new CreatedState(),
                "ASSIGNED",  new AssignedState(),
                "ON_THE_WAY", new OnTheWayState(),
                "DELIVERED",  new DeliveredState(),
                "CANCELLED",  new CancelledState()
        ).get(name);
        if (state == null) {
            throw new IllegalArgumentException("Unknown order state: " + name);
        }
        return state;
    }
}
