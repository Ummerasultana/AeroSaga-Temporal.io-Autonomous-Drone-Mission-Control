package in.aerosaga.dto;

public class MissionResponse {

    private String missionId;
    private String droneId;
    private String status;

    private Double startLatitude;
    private Double startLongitude;
    private Double targetLatitude;
    private Double targetLongitude;

    public MissionResponse(
            String missionId,
            String droneId,
            String status,
            Double startLatitude,
            Double startLongitude,
            Double targetLatitude,
            Double targetLongitude) {

        this.missionId = missionId;
        this.droneId = droneId;
        this.status = status;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
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

    public Double getStartLatitude() {
        return startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public Double getTargetLatitude() {
        return targetLatitude;
    }

    public Double getTargetLongitude() {
        return targetLongitude;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}