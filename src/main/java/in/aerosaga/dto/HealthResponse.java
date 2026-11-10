package in.aerosaga.dto;

public class HealthResponse {

    private String status;
    private String temporalStatus;
    private int activeMissionsCount;
    private int totalDronesCount;
    private long timestamp;

    public HealthResponse() {
    }

    public HealthResponse(String status, String temporalStatus, int activeMissionsCount, int totalDronesCount, long timestamp) {
        this.status = status;
        this.temporalStatus = temporalStatus;
        this.activeMissionsCount = activeMissionsCount;
        this.totalDronesCount = totalDronesCount;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTemporalStatus() {
        return temporalStatus;
    }

    public void setTemporalStatus(String temporalStatus) {
        this.temporalStatus = temporalStatus;
    }

    public int getActiveMissionsCount() {
        return activeMissionsCount;
    }

    public void setActiveMissionsCount(int activeMissionsCount) {
        this.activeMissionsCount = activeMissionsCount;
    }

    public int getTotalDronesCount() {
        return totalDronesCount;
    }

    public void setTotalDronesCount(int totalDronesCount) {
        this.totalDronesCount = totalDronesCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
