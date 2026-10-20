
package in.aerosaga.telemetry;

public class DroneTelemetry {

    private String droneId;
    private double latitude;
    private double longitude;
    private double altitude;
    private double batteryLevel;
    private long timestamp;

    public DroneTelemetry() {
    }

    public DroneTelemetry(
            String droneId,
            double latitude,
            double longitude,
            double altitude,
            double batteryLevel,
            long timestamp) {
        this.droneId = droneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.batteryLevel = batteryLevel;
        this.timestamp = timestamp;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
