package in.aerosaga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "aerosaga")
public class AeroSagaProperties {

    private String temporalTarget = "127.0.0.1:7233";
    private String taskQueue = "DRONE_TASK_QUEUE";
    private String namespace = "default";
    private long stepDurationMs = 2500;
    private List<String> allowedOrigins = List.of("http://localhost:5173");

    public String getTemporalTarget() {
        return temporalTarget;
    }

    public void setTemporalTarget(String temporalTarget) {
        this.temporalTarget = temporalTarget;
    }

    public String getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(String taskQueue) {
        this.taskQueue = taskQueue;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getStepDurationMs() {
        return stepDurationMs;
    }

    public void setStepDurationMs(long stepDurationMs) {
        this.stepDurationMs = stepDurationMs;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
