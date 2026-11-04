package in.aerosaga.config;

import in.aerosaga.workflow.MissionWorkflowImpl;
import in.aerosaga.activity.MissionActivityImpl;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalWorkerConfig {

    @Bean
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            MissionActivityImpl missionActivityImpl,
            @Value("${aerosaga.temporal.task-queue}") String taskQueue) {

        WorkerFactory factory =
                WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(taskQueue);

        // Register workflow implementation
        worker.registerWorkflowImplementationTypes(
                MissionWorkflowImpl.class
        );

        // Register database update activity
        worker.registerActivitiesImplementations(
                missionActivityImpl
        );

        // Start Temporal worker
        factory.start();

        return factory;
    }
}