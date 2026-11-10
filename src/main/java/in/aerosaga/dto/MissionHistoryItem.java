package in.aerosaga.dto;

import java.util.List;

public class MissionHistoryItem {

    private String workflowId;
    private List<String> droneIds;
    private long startTime;
    private Long endTime;
    private Long durationMs;
    private String status;
    private String result;
    private String error;

    public MissionHistoryItem() {
    }

    public MissionHistoryItem(String workflowId, List<String> droneIds, long startTime,
                              Long endTime, Long durationMs, String status,
                              String result, String error) {
        this.workflowId = workflowId;
        this.droneIds = droneIds;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.status = status;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
