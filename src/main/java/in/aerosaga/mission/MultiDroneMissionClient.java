
package in.aerosaga.mission;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.List;
import java.util.UUID;

public class MultiDroneMissionClient {

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

            MultiDroneMissionWorkflow workflow =
                    client.newWorkflowStub(
                            MultiDroneMissionWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setWorkflowId(
                                            "multi-drone-" + UUID.randomUUID()
                                    )
                                    .setTaskQueue(DroneWorker.TASK_QUEUE)
                                    .build()
                    );

            List<String> droneIds =
                    List.of("DRONE-101", "DRONE-102", "DRONE-103");

            System.out.println("Starting missions for: " + droneIds);

            List<String> results =
                    WorkflowClient.execute(
                            workflow::executeMissions,
                            droneIds
                    ).get();

            results.forEach(System.out::println);

        } catch (Exception e) {
            System.err.println(
                    "Multi-drone mission failed: " + e.getMessage()
            );
            e.printStackTrace();
        } finally {
            service.shutdown();
        }
    }
}
