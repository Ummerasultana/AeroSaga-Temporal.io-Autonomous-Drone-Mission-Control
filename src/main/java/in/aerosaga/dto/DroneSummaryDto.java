package in.aerosaga.dto;

public class DroneSummaryDto {

    private String droneId;
    private String status;
    private double batteryLevel;
    private double altitude;
    private double latitude;
    private double longitude;
    private long lastUpdated;
    private String currentMissionId;
    private String missionStatus;

    public DroneSummaryDto() {
    }

    public DroneSummaryDto(String droneId, String status, double batteryLevel, double altitude,
                           double latitude, double longitude, long lastUpdated,
                           String currentMissionId, String missionStatus) {
        this.droneId = droneId;
        this.status = status;
        this.batteryLevel = batteryLevel;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = lastUpdated;
        this.currentMissionId = currentMissionId;
        this.missionStatus = missionStatus;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getCurrentMissionId() {
        return currentMissionId;
    }

    public void setCurrentMissionId(String currentMissionId) {
        this.currentMissionId = currentMissionId;
    }

    public String getMissionStatus() {
        return missionStatus;
    }

    public void setMissionStatus(String missionStatus) {
        this.missionStatus = missionStatus;
    }
}
