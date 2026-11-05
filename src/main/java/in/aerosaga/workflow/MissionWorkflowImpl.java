package in.aerosaga.workflow;

import in.aerosaga.activity.MissionActivity;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import org.slf4j.Logger;

import java.time.Duration;

public class MissionWorkflowImpl implements MissionWorkflow {

    private static final Logger logger =
            Workflow.getLogger(MissionWorkflowImpl.class);

    private String missionId;
    private String droneId;
    private String currentStage = "NOT_STARTED";
    private boolean completed = false;

    private final MissionActivity missionActivity =
            Workflow.newActivityStub(
                    MissionActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(10)
                            )
                            .build()
            );

    @Override
    public void executeMission(
            String missionId,
            String droneId,
            Double startLatitude,
            Double startLongitude,
            Double targetLatitude,
            Double targetLongitude) {

        this.missionId = missionId;
        this.droneId = droneId;

        logger.info("Mission workflow started: {}", missionId);
        logger.info("Drone ID: {}", droneId);

        // Stage 1: Validate route
        currentStage = "VALIDATING_ROUTE";

        missionActivity.updateMissionProgress(
                missionId,
                currentStage,
                "STARTED"
        );

        logger.info("Validating route for mission {}", missionId);

        Workflow.sleep(Duration.ofSeconds(2));

        // Stage 2: Prepare drone
        currentStage = "PREPARING_DRONE";

        missionActivity.updateMissionProgress(
                missionId,
                currentStage,
                "IN_PROGRESS"
        );

        logger.info("Preparing drone {}", droneId);

        Workflow.sleep(Duration.ofSeconds(2));

        // Stage 3: Execute mission
        currentStage = "EXECUTING_MISSION";

        missionActivity.updateMissionProgress(
                missionId,
                currentStage,
                "IN_PROGRESS"
        );

        logger.info(
                "Mission {}: starting coordinates {}, {}; target coordinates {}, {}",
                missionId,
                startLatitude,
                startLongitude,
                targetLatitude,
                targetLongitude
        );

        Workflow.sleep(Duration.ofSeconds(15));

        // Stage 4: Complete mission
        currentStage = "COMPLETED";
        completed = true;

        missionActivity.updateMissionProgress(
                missionId,
                currentStage,
                "COMPLETED"
        );

        logger.info("Mission workflow completed: {}", missionId);
    }

    @Override
    public String getProgress() {

        return "Mission ID: " + missionId
                + ", Drone ID: " + droneId
                + ", Stage: " + currentStage
                + ", Completed: " + completed;
    }
}