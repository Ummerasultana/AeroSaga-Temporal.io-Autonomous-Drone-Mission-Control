package in.aerosaga.mission;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:5173")
public class MissionController {

    private final WorkflowClient workflowClient;

    public MissionController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/start")
    public String startMission(
            @RequestBody List<String> droneIds) {

        MultiDroneMissionWorkflow workflow =
                workflowClient.newWorkflowStub(
                        MultiDroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(
                                        "multi-drone-" +
                                                UUID.randomUUID()
                                )
                                .setTaskQueue(
                                        DroneWorker.TASK_QUEUE
                                )
                                .build()
                );

        WorkflowClient.start(
                workflow::executeMissions,
                droneIds
        );

        return "Mission started for drones: " + droneIds;
    }
}