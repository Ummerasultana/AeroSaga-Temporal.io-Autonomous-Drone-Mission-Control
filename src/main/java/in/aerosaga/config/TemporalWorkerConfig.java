package in.aerosaga.config;

import in.aerosaga.workflow.MissionWorkflowImpl;
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
            @Value("${aerosaga.temporal.task-queue}") String taskQueue) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(
                MissionWorkflowImpl.class
        );

        factory.start();

        return factory;
    }
}