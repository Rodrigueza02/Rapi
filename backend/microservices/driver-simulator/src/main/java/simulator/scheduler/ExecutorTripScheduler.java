package simulator.scheduler;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import simulator.ports.TripSchedulerPort;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Adaptador: implementa los temporizadores con un ScheduledExecutorService de Java. */
@Component
public class ExecutorTripScheduler implements TripSchedulerPort {

    private static final Logger log = LoggerFactory.getLogger(ExecutorTripScheduler.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    @Override
    public void schedule(Duration delay, Runnable task) {
        executor.schedule(() -> runSafely(task), delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Un error dentro de una tarea programada se perdería en silencio: aquí se registra. */
    private void runSafely(Runnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            log.error("A scheduled trip step failed", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}