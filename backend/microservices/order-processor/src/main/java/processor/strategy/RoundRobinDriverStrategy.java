package processor.strategy;

import processor.model.Order;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinDriverStrategy implements DriverAssignmentStrategy {

    private final List<String> driverIds;
    private final AtomicInteger next = new AtomicInteger();

    public RoundRobinDriverStrategy(List<String> driverIds) {
        if (driverIds == null || driverIds.isEmpty()) {
            throw new IllegalArgumentException("driverIds must not be null or empty");
        }
        this.driverIds = List.copyOf(driverIds);
    }

    @Override
    public String selectDriver(Order order) {
        int index = Math.floorMod(next.getAndIncrement(), driverIds.size());
        return driverIds.get(index);
    }

    /** Índice del próximo turno (0-based). Permite que el aggregator se sincronice. */
    public int currentIndex() {
        return Math.floorMod(next.get(), driverIds.size());
    }

    public List<String> getDriverIds() {
        return driverIds;
    }
}