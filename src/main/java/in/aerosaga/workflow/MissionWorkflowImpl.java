package in.aerosaga.workflow;

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

        currentStage = "VALIDATING_ROUTE";
        logger.info("Validating route for mission {}", missionId);
        Workflow.sleep(Duration.ofSeconds(2));

        currentStage = "PREPARING_DRONE";
        logger.info("Preparing drone {}", droneId);
        Workflow.sleep(Duration.ofSeconds(2));

        currentStage = "EXECUTING_MISSION";
        logger.info(
                "Mission {}: starting coordinates {}, {}; target coordinates {}, {}",
                missionId,
                startLatitude,
                startLongitude,
                targetLatitude,
                targetLongitude
        );
        Workflow.sleep(Duration.ofSeconds(2));

        currentStage = "COMPLETED";
        completed = true;

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