
package in.aerosaga.mission;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import io.temporal.failure.ApplicationFailure;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

    private final DroneMissionActivities activities =
            Workflow.newActivityStub(
                    DroneMissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofMinutes(2))
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setMaximumAttempts(1)
                                            .build()
                            )
                            .build()
            );

    @Override
    public String executeMission(String droneId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Mission started for drone: " + droneId);

        activities.takeoff(droneId);
        activities.navigate(droneId);

        try {
            activities.dropPackage(droneId);

        } catch (Exception deliveryFailure) {

            Workflow.getLogger(DroneMissionWorkflowImpl.class)
                    .error("Package delivery failed for drone: " + droneId,
                            deliveryFailure);

            // Compensation: return the drone to base.
            try {
                activities.returnToBase(droneId);

            } catch (Exception returnFailure) {
                Workflow.getLogger(DroneMissionWorkflowImpl.class)
                        .error("Return to base also failed for drone: " + droneId,
                                returnFailure);
            }

            // Fail the mission after attempting compensation.
            throw ApplicationFailure.newFailure(
                    "Package delivery failed for drone: " + droneId,
                    "PackageDeliveryFailed"
            );
        }

        // Normal path: return to base after successful delivery.
        activities.returnToBase(droneId);

        return "Mission completed successfully for drone: " + droneId;
    }
}
