
package in.aerosaga.mission;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneMissionActivitiesImpl
        implements DroneMissionActivities {

    private static final Logger logger =
            LoggerFactory.getLogger(DroneMissionActivitiesImpl.class);

    @Override
    public void takeoff(String droneId) {
        log(droneId, "Taking off");
    }

    @Override
    public void navigate(String droneId) {
        log(droneId, "Navigating to delivery location");

        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Navigation activity interrupted", e);
        }

        log(droneId, "Navigation completed");
    }

    @Override
    public void dropPackage(String droneId) {
        log(droneId, "Package delivered");
    }

    @Override
    public void returnToBase(String droneId) {
        log(droneId, "Returning to base");
    }

    private void log(String droneId, String action) {
        logger.info("Drone {}: {}", droneId, action);
    }
}
