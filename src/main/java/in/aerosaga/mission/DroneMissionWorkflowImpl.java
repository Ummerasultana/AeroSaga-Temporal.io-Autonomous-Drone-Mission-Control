
package in.aerosaga.mission;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

    private boolean abortRequested = false;

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
    public void requestAbort() {
        abortRequested = true;

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .warn("Emergency abort requested!");
    }

    @Override
    public String executeMission(String droneId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Mission started for drone: " + droneId);

        activities.takeoff(droneId);

        if (abortRequested) {
            return abortMission(droneId);
        }

        activities.navigate(droneId);

        if (abortRequested) {
            return abortMission(droneId);
        }

        try {
            activities.dropPackage(droneId);

        } catch (Exception deliveryFailure) {

            Workflow.getLogger(DroneMissionWorkflowImpl.class)
                    .error("Package delivery failed for drone: " + droneId,
                            deliveryFailure);

            // Compensation: attempt to return the drone to base.
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

        if (abortRequested) {
            return abortMission(droneId);
        }

        // Normal path: return to base after successful delivery.
        activities.returnToBase(droneId);

        return "Mission completed successfully for drone: " + droneId;
    }

    private String abortMission(String droneId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .warn("Aborting mission and returning to base: " + droneId);

        activities.returnToBase(droneId);

        return "Mission aborted; drone returned to base: " + droneId;
    }
}
