package processor.state;

import org.junit.jupiter.api.Test;
import processor.exception.InvalidStateTransitionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateTest {

    @Test
    void createdOnlyAllowsAssigningADriver() {
        OrderState created = new CreatedState();

        assertThat(created.assignDriver()).isInstanceOf(AssignedState.class);
        assertThatThrownBy(created::startDelivery).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(created::registerArrival).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(created::completeDelivery).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void assignedOnlyAllowsStartingTheDelivery() {
        OrderState assigned = new AssignedState();

        assertThat(assigned.startDelivery()).isInstanceOf(OnTheWayState.class);
        assertThatThrownBy(assigned::assignDriver).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(assigned::registerArrival).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(assigned::completeDelivery).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void onTheWayAllowsArrivalAndDelivery() {
        OrderState onTheWay = new OnTheWayState();

        assertThat(onTheWay.registerArrival()).isInstanceOf(OnTheWayState.class);
        assertThat(onTheWay.completeDelivery()).isInstanceOf(DeliveredState.class);
        assertThatThrownBy(onTheWay::assignDriver).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(onTheWay::startDelivery).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void deliveredIsAFinalState() {
        OrderState delivered = new DeliveredState();

        assertThatThrownBy(delivered::assignDriver).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(delivered::startDelivery).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(delivered::registerArrival).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(delivered::completeDelivery).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void fromNameRebuildsEachState() {
        assertThat(OrderState.fromName("CREATED")).isInstanceOf(CreatedState.class);
        assertThat(OrderState.fromName("ASSIGNED")).isInstanceOf(AssignedState.class);
        assertThat(OrderState.fromName("ON_THE_WAY")).isInstanceOf(OnTheWayState.class);
        assertThat(OrderState.fromName("DELIVERED")).isInstanceOf(DeliveredState.class);
    }

    @Test
    void fromNameRejectsUnknownNames() {
        assertThatThrownBy(() -> OrderState.fromName("FLYING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FLYING");
    }
}