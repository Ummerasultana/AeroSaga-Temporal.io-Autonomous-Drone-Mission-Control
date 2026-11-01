package in.aerosaga.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MissionRequest {

    @NotBlank(message = "Drone ID is required")
    private String droneId;

    @NotNull(message = "Start latitude is required")
    @DecimalMin(value = "-90.0", message = "Start latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Start latitude must not exceed 90")
    private Double startLatitude;

    @NotNull(message = "Start longitude is required")
    @DecimalMin(value = "-180.0", message = "Start longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Start longitude must not exceed 180")
    private Double startLongitude;

    @NotNull(message = "Target latitude is required")
    @DecimalMin(value = "-90.0", message = "Target latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Target latitude must not exceed 90")
    private Double targetLatitude;

    @NotNull(message = "Target longitude is required")
    @DecimalMin(value = "-180.0", message = "Target longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Target longitude must not exceed 180")
    private Double targetLongitude;

    public MissionRequest() {
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getTargetLatitude() {
        return targetLatitude;
    }

    public void setTargetLatitude(Double targetLatitude) {
        this.targetLatitude = targetLatitude;
    }

    public Double getTargetLongitude() {
        return targetLongitude;
    }

    public void setTargetLongitude(Double targetLongitude) {
        this.targetLongitude = targetLongitude;
    }
}