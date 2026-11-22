package simulator.ports;

import java.time.Duration;


public interface TripSchedulerPort {

    void schedule(Duration delay, Runnable task);
}