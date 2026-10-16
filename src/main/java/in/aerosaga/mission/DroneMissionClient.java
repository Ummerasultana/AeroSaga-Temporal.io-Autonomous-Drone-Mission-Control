package in.aerosaga.mission;

import in.aerosaga.mission.DroneMissionWorkflow;
import in.aerosaga.mission.DroneWorker;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class DroneMissionClient {

    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setTarget("127.0.0.1:7233")
                                .build()
                );

        try {
            WorkflowClient client =
                    WorkflowClient.newInstance(service);

            DroneMissionWorkflow workflow =
                    client.newWorkflowStub(
                            DroneMissionWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setWorkflowId("drone-mission-recovery-2")
                                    .setTaskQueue(DroneWorker.TASK_QUEUE)
                                    .build()
                    );

            String result = workflow.executeMission("DRONE-002");

            System.out.println(result);

        } finally {
            service.shutdown();
        }
    }
}
