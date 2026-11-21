package processor.strategy;

import processor.model.Order;

import java.util.List;
import java.util.Random;

public class RandomDriverStrategy implements DriverAssignmentStrategy {

    private final List<String> driverIds;
    private final Random random;

    public RandomDriverStrategy(List<String> driverIds, Random random) {
        if (driverIds == null || driverIds.isEmpty()) {
            throw new IllegalArgumentException("driverIds must not be null or empty");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.driverIds = List.copyOf(driverIds);
        this.random = random;
    }

    @Override
    public String selectDriver(Order order) {
        return driverIds.get(random.nextInt(driverIds.size()));
    }
}