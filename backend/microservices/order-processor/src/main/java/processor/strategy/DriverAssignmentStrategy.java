package processor.strategy;

import processor.model.Order;


public interface DriverAssignmentStrategy {

    String selectDriver(Order order);
}