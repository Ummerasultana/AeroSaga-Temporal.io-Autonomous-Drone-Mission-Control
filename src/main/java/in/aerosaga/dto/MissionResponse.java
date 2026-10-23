package in.aerosaga.dto;

public class MissionResponse {

    private String missionId;
    private String droneId;
    private String status;

    public MissionResponse(String missionId, String droneId, String status) {
        this.missionId = missionId;
        this.droneId = droneId;
        this.status = status;
    }

    public String getMissionId() {
        return missionId;
    }

    public String getDroneId() {
        return droneId;
    }

    public String getStatus() {
        return status;
    }
}