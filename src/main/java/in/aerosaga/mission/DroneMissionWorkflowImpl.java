
package in.aerosaga.mission;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.QueryMethod;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

    private boolean abortRequested = false;
    private String missionStatus = "PENDING";

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
        missionStatus = "ABORTING";

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .warn("Emergency abort requested!");
    }

    @Override
    public String getMissionStatus() {
        return missionStatus;
    }

    @Override
    public String executeMission(String droneId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Mission started for drone: " + droneId);

        missionStatus = "TAKING_OFF";
        activities.takeoff(droneId);

        if (abortRequested) {
            return abortMission(droneId);
        }

        missionStatus = "NAVIGATING";
        activities.navigate(droneId);

        if (abortRequested) {
            return abortMission(droneId);
        }

        missionStatus = "DELIVERING";

        try {
            activities.dropPackage(droneId);

        } catch (Exception deliveryFailure) {

            missionStatus = "RETURNING";

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

            missionStatus = "FAILED";

            // Fail the mission after attempting compensation.
            throw ApplicationFailure.newFailure(
                    "Package delivery failed for drone: " + droneId,
                    "PackageDeliveryFailed"
            );
        }

        if (abortRequested) {
            return abortMission(droneId);
        }

        missionStatus = "RETURNING";
        activities.returnToBase(droneId);

        missionStatus = "COMPLETED";

        return "Mission completed successfully for drone: " + droneId;
    }

    private String abortMission(String droneId) {

        missionStatus = "ABORTING";

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .warn("Aborting mission and returning to base: " + droneId);

        activities.returnToBase(droneId);

        missionStatus = "ABORTED";

        return "Mission aborted; drone returned to base: " + droneId;
    }
}
