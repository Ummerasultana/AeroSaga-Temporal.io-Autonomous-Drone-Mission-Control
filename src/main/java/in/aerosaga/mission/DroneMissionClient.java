
package in.aerosaga.mission;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class DroneMissionClient {

    private static final String WORKFLOW_ID =
            "drone-mission-emergency-002";

    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setTarget("127.0.0.1:7233")
                                .build()
                );

        try {
            WorkflowClient client = WorkflowClient.newInstance(service);

            if (args.length > 0
                    && args[0].equalsIgnoreCase("abort")) {

                // Connect to the existing workflow execution.
                WorkflowStub existingWorkflow =
                        client.newUntypedWorkflowStub(WORKFLOW_ID);

                existingWorkflow.signal("requestAbort");

                System.out.println(
                        "Emergency abort signal sent to workflow: "
                                + WORKFLOW_ID
                );

            } else {

                DroneMissionWorkflow workflow =
                        client.newWorkflowStub(
                                DroneMissionWorkflow.class,
                                WorkflowOptions.newBuilder()
                                        .setWorkflowId(WORKFLOW_ID)
                                        .setTaskQueue(DroneWorker.TASK_QUEUE)
                                        .build()
                        );

                String result = WorkflowClient.execute(
                        workflow::executeMission,
                        "DRONE-001"
                ).get();

                System.out.println(result);
            }

        } catch (Exception e) {
            System.err.println(
                    "Workflow operation failed: " + e.getMessage()
            );
            e.printStackTrace();

        } finally {
            service.shutdown();
        }
    }
}
