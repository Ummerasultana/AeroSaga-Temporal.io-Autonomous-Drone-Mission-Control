
package in.aerosaga.mission;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl
        implements DroneMissionWorkflow {

    private final DroneMissionActivities activities =
            Workflow.newActivityStub(
                    DroneMissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofMinutes(2))
                            .build()
            );

    @Override
    public String executeMission(String droneId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Mission started for drone: " + droneId);

        activities.takeoff(droneId);
        activities.navigate(droneId);
        activities.dropPackage(droneId);
        activities.returnToBase(droneId);

        return "Mission completed successfully for drone: "
                + droneId;
    }
}
