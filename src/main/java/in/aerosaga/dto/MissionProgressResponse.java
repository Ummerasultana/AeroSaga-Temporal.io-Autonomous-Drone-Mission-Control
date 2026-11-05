package in.aerosaga.dto;

public class MissionProgressResponse {

    private String missionId;
    private String droneId;
    private String currentStage;
    private String status;
    private boolean completed;

    public MissionProgressResponse(
            String missionId,
            String droneId,
            String currentStage,
            String status,
            boolean completed) {

        this.missionId = missionId;
        this.droneId = droneId;
        this.currentStage = currentStage;
        this.status = status;
        this.completed = completed;
    }

    public String getMissionId() {
        return missionId;
    }

    public String getDroneId() {
        return droneId;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public String getStatus() {
        return status;
    }

    public boolean isCompleted() {
        return completed;
    }
}