
package in.aerosaga.mission;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class DroneWorker {

    public static final String TASK_QUEUE = "DRONE_TASK_QUEUE";

    public static void main(String[] args) {

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setTarget("127.0.0.1:7233")
                                .build()
                );

        WorkflowClient client =
                WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class);

        worker.registerActivitiesImplementations(
                new DroneMissionActivitiesImpl());

        factory.start();

        System.out.println(
                "Drone worker started. Waiting for missions...");
    }
}
