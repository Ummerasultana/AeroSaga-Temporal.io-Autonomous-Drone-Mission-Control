package in.aerosaga.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TemporalMissionService {

    private final WorkflowClient workflowClient;

    @Value("${aerosaga.temporal.task-queue}")
    private String taskQueue;

    @Value("${aerosaga.temporal.workflow-type}")
    private String workflowType;

    public TemporalMissionService(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    public void startMissionWorkflow(
            String missionId,
            String droneId,
            Double startLatitude,
            Double startLongitude,
            Double targetLatitude,
            Double targetLongitude) {

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId("mission-" + missionId)
                .build();

        System.out.println(
                "Temporal workflow prepared: " + workflowType
                        + " for mission: " + missionId
        );

        // The actual workflow start call will be added
        // after defining our own workflow contract.
    }
}