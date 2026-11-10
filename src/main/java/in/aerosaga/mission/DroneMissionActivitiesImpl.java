package in.aerosaga.mission;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneMissionActivitiesImpl implements DroneMissionActivities {

    private static final Logger logger = LoggerFactory.getLogger(DroneMissionActivitiesImpl.class);

    private final long stepDurationMs;

    public DroneMissionActivitiesImpl() {
        this(Long.getLong("aerosaga.activity.step-duration-ms", 2500L));
    }

    public DroneMissionActivitiesImpl(long stepDurationMs) {
        this.stepDurationMs = stepDurationMs;
    }

    @Override
    public void takeoff(String droneId) {
        log(droneId, "Taking off");
        simulateAction(Math.max(100, stepDurationMs / 2));
        log(droneId, "Takeoff completed");
    }

    @Override
    public void navigate(String droneId) {
        log(droneId, "Navigating to delivery location");
        simulateAction(stepDurationMs);
        log(droneId, "Navigation completed");
    }

    @Override
    public void dropPackage(String droneId) {
        log(droneId, "Attempting package delivery");
        simulateAction(Math.max(100, stepDurationMs / 2));

        if (Boolean.getBoolean("aerosaga.test.drop-failure") || droneId.toUpperCase().contains("FAIL")) {
            log(droneId, "Package delivery failed (simulated failure mode)");
            throw new RuntimeException("Simulated package delivery failure for " + droneId);
        }

        log(droneId, "Package delivered successfully");
    }

    @Override
    public void returnToBase(String droneId) {
        log(droneId, "Returning to base");
        simulateAction(stepDurationMs);
        log(droneId, "Safely returned to base and landed");
    }

    private void simulateAction(long durationMs) {
        if (Boolean.getBoolean("aerosaga.test.fast") || durationMs <= 0) {
            return;
        }
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Activity interrupted", e);
        }
    }

    private void log(String droneId, String action) {
        logger.info("Drone {}: {}", droneId, action);
    }
}
