package in.aerosaga.mission;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiDroneMissionWorkflowImpl implements MultiDroneMissionWorkflow {

    private boolean abortRequested = false;
    private String missionStatus = "PENDING";
    private final Map<String, String> droneStatuses = new HashMap<>();

    private final DroneMissionActivities activities =
            Workflow.newActivityStub(
                    DroneMissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofMinutes(3))
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
        Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                .warn("Emergency abort requested for multi-drone mission");

        for (Map.Entry<String, String> entry : droneStatuses.entrySet()) {
            String current = entry.getValue();
            if (!"COMPLETED".equals(current) && !"FAILED".equals(current) && !"ABORTED".equals(current)) {
                droneStatuses.put(entry.getKey(), "ABORTING");
            }
        }
    }

    @Override
    public String getMissionStatus() {
        return missionStatus;
    }

    @Override
    public Map<String, String> getDroneStatuses() {
        return new HashMap<>(droneStatuses);
    }

    @Override
    public List<String> executeMissions(List<String> droneIds) {
        Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                .info("Starting concurrent missions for drones: " + droneIds);

        for (String droneId : droneIds) {
            droneStatuses.put(droneId, "PENDING");
        }

        missionStatus = "TAKING_OFF";

        List<Promise<String>> promises = new ArrayList<>();
        for (String droneId : droneIds) {
            Promise<String> promise = Async.function(() -> executeSingleDroneFlow(droneId));
            promises.add(promise);
        }

        Promise.allOf(promises.toArray(new Promise[0])).get();

        List<String> results = new ArrayList<>();
        boolean anyFailed = false;
        boolean anyAborted = false;

        for (Promise<String> promise : promises) {
            String res = promise.get();
            results.add(res);
            if (res.startsWith("Failed")) {
                anyFailed = true;
            } else if (res.startsWith("Aborted")) {
                anyAborted = true;
            }
        }

        if (abortRequested || anyAborted) {
            missionStatus = "ABORTED";
        } else if (anyFailed) {
            missionStatus = "FAILED";
        } else {
            missionStatus = "COMPLETED";
        }

        Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                .info("Multi-drone mission concluded with overall status: " + missionStatus);

        return results;
    }

    private String executeSingleDroneFlow(String droneId) {
        if (abortRequested) {
            return abortSingleDrone(droneId);
        }

        droneStatuses.put(droneId, "TAKING_OFF");
        activities.takeoff(droneId);

        if (abortRequested) {
            return abortSingleDrone(droneId);
        }

        droneStatuses.put(droneId, "NAVIGATING");
        updateOverallStatus("NAVIGATING");
        activities.navigate(droneId);

        if (abortRequested) {
            return abortSingleDrone(droneId);
        }

        droneStatuses.put(droneId, "DELIVERING");
        updateOverallStatus("DELIVERING");

        try {
            activities.dropPackage(droneId);
        } catch (Exception deliveryFailure) {
            Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                    .error("Package delivery failed for drone: " + droneId, deliveryFailure);

            droneStatuses.put(droneId, "RETURNING");
            try {
                activities.returnToBase(droneId);
            } catch (Exception returnFailure) {
                Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                        .error("Return to base failed for drone: " + droneId, returnFailure);
            }

            droneStatuses.put(droneId, "FAILED");
            return "Failed: Package delivery failed for drone: " + droneId;
        }

        if (abortRequested) {
            return abortSingleDrone(droneId);
        }

        droneStatuses.put(droneId, "RETURNING");
        updateOverallStatus("RETURNING");
        activities.returnToBase(droneId);

        droneStatuses.put(droneId, "COMPLETED");
        return "Mission completed successfully for drone: " + droneId;
    }

    private String abortSingleDrone(String droneId) {
        droneStatuses.put(droneId, "ABORTING");
        Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                .warn("Aborting mission for drone: " + droneId + "; returning to base");

        try {
            activities.returnToBase(droneId);
        } catch (Exception e) {
            Workflow.getLogger(MultiDroneMissionWorkflowImpl.class)
                    .error("Failed to return to base during abort: " + droneId, e);
        }

        droneStatuses.put(droneId, "ABORTED");
        return "Aborted: Drone " + droneId + " returned to base";
    }

    private synchronized void updateOverallStatus(String stage) {
        if (!"ABORTING".equals(missionStatus) && !"ABORTED".equals(missionStatus) && !"FAILED".equals(missionStatus)) {
            missionStatus = stage;
        }
    }
}
