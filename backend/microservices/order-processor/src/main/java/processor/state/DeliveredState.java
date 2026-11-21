package processor.state;

public final class DeliveredState implements OrderState {

    @Override
    public String name() {
        return "DELIVERED";
    }

    // No sobrescribe nada: es un estado final. Toda transición está prohibida.
}