package in.aerosaga.dto;

public class AbortMissionResponse {

    private String workflowId;
    private String status;
    private String message;

    public AbortMissionResponse() {
    }

    public AbortMissionResponse(String workflowId, String status, String message) {
        this.workflowId = workflowId;
        this.status = status;
        this.message = message;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
