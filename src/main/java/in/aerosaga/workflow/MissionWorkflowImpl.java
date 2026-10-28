package in.aerosaga.workflow;

import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

public class MissionWorkflowImpl implements MissionWorkflow {

    private static final Logger logger =
            Workflow.getLogger(MissionWorkflowImpl.class);

    @Override
    public void executeMission(
            String missionId,
            String droneId,
            Double startLatitude,
            Double startLongitude,
            Double targetLatitude,
            Double targetLongitude) {

        logger.info("Mission workflow started: {}", missionId);
        logger.info("Drone ID: {}", droneId);
        logger.info("Starting coordinates: {}, {}",
                startLatitude, startLongitude);
        logger.info("Target coordinates: {}, {}",
                targetLatitude, targetLongitude);

        // Initial workflow implementation.
        // Actual drone execution activities can be added later.
    }
}