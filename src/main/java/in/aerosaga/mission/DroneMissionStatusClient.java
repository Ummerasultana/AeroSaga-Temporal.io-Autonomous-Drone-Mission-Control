
package in.aerosaga.mission;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class DroneMissionStatusClient {

    public static void main(String[] args) {

        String workflowId = args.length > 0
                ? args[0]
                : "drone-mission-emergency-002";

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setTarget("127.0.0.1:7233")
                                .build()
                );

        try {
            WorkflowClient client = WorkflowClient.newInstance(service);

            WorkflowStub workflow =
                    client.newUntypedWorkflowStub(workflowId);

            String status = workflow.query(
                    "getMissionStatus",
                    String.class
            );

            System.out.println("Workflow ID: " + workflowId);
            System.out.println("Mission status: " + status);

        } catch (Exception e) {
            System.err.println(
                    "Unable to query mission status: " + e.getMessage()
            );

        } finally {
            service.shutdown();
        }
    }
}
