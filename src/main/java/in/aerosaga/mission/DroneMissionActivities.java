
package in.aerosaga.mission;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DroneMissionActivities {

    @ActivityMethod
    void takeoff(String droneId);

    @ActivityMethod
    void navigate(String droneId);

    @ActivityMethod
    void dropPackage(String droneId);

    @ActivityMethod
    void returnToBase(String droneId);
}
