package in.aerosaga.service;

import in.aerosaga.workflow.MissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TemporalMissionService {

    private final WorkflowClient workflowClient;

    @Value("${aerosaga.temporal.task-queue}")
    private String taskQueue;

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

        MissionWorkflow workflow = workflowClient.newWorkflowStub(
                MissionWorkflow.class, options);

        WorkflowClient.start(
                workflow::executeMission,
                missionId,
                droneId,
                startLatitude,
                startLongitude,
                targetLatitude,
                targetLongitude
        );
    }
}