package in.aerosaga.dto;

import java.util.List;

public class MissionResponse {

    private String workflowId;
    private List<String> droneIds;
    private String status;
    private long startTime;
    private String message;

    public MissionResponse() {
    }

    public MissionResponse(String workflowId, List<String> droneIds, String status, long startTime, String message) {
        this.workflowId = workflowId;
        this.droneIds = droneIds;
        this.status = status;
        this.startTime = startTime;
        this.message = message;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
