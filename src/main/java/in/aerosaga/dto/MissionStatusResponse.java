package in.aerosaga.dto;

import java.util.List;
import java.util.Map;

public class MissionStatusResponse {

    private String workflowId;
    private List<String> droneIds;
    private String status;
    private Map<String, String> droneStatuses;
    private String currentStage;
    private int progressPercent;
    private Long startTime;
    private Long endTime;
    private String result;
    private String error;

    public MissionStatusResponse() {
    }

    public MissionStatusResponse(String workflowId, List<String> droneIds, String status,
                                 Map<String, String> droneStatuses, String currentStage,
                                 int progressPercent, Long startTime, Long endTime,
                                 String result, String error) {
        this.workflowId = workflowId;
        this.droneIds = droneIds;
        this.status = status;
        this.droneStatuses = droneStatuses;
        this.currentStage = currentStage;
        this.progressPercent = progressPercent;
        this.startTime = startTime;
        this.endTime = endTime;
        this.result = result;
        this.error = error;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public List<String> getDroneIds() {
        return droneIds;
    }

    public void setDroneIds(List<String> droneIds) {
        this.droneIds = droneIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getDroneStatuses() {
        return droneStatuses;
    }

    public void setDroneStatuses(Map<String, String> droneStatuses) {
        this.droneStatuses = droneStatuses;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
