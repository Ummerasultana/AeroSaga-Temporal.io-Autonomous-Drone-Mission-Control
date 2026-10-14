
package in.aerosaga.mission;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class DroneMissionClient {

    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client =
                WorkflowClient.newInstance(service);

        DroneMissionWorkflow workflow =
                client.newWorkflowStub(
                        DroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId("drone-mission-001")
                                .setTaskQueue(DroneWorker.TASK_QUEUE)
                                .build()
                );

        String result = workflow.executeMission("DRONE-001");

        System.out.println(result);

        service.shutdown();
    }
}
