package in.aerosaga.service;

import in.aerosaga.dto.DroneSummaryDto;
import in.aerosaga.telemetry.DroneTelemetry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelemetryService {

    private static final long STALE_THRESHOLD_MS = 10_000L;
    private static final long OFFLINE_THRESHOLD_MS = 30_000L;

    public static final List<String> KNOWN_DRONES = List.of("DRONE-101", "DRONE-102", "DRONE-103");

    private final Map<String, DroneTelemetry> latestTelemetryMap = new ConcurrentHashMap<>();

    public TelemetryService() {
        // Pre-populate known drones with default telemetry so dashboard starts clean
        long now = System.currentTimeMillis();
        latestTelemetryMap.put("DRONE-101", new DroneTelemetry("DRONE-101", 15.1394, 76.9214, 0.0, 100.0, 0));
        latestTelemetryMap.put("DRONE-102", new DroneTelemetry("DRONE-102", 15.1400, 76.9220, 0.0, 100.0, 0));
        latestTelemetryMap.put("DRONE-103", new DroneTelemetry("DRONE-103", 15.1385, 76.9205, 0.0, 100.0, 0));
    }

    public void updateTelemetry(DroneTelemetry telemetry) {
        if (telemetry != null && telemetry.getDroneId() != null) {
            latestTelemetryMap.put(telemetry.getDroneId(), telemetry);
        }
    }

    public Optional<DroneTelemetry> getLatestTelemetry(String droneId) {
        return Optional.ofNullable(latestTelemetryMap.get(droneId));
    }

    public Map<String, DroneTelemetry> getAllLatestTelemetry() {
        return Collections.unmodifiableMap(latestTelemetryMap);
    }

    public List<DroneSummaryDto> getDroneSummaries(Map<String, String> droneMissionAssignments) {
        List<DroneSummaryDto> summaries = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (String droneId : KNOWN_DRONES) {
            DroneTelemetry telemetry = latestTelemetryMap.get(droneId);
            String status;
            double battery = 0.0;
            double altitude = 0.0;
            double lat = 0.0;
            double lon = 0.0;
            long timestamp = 0;

            if (telemetry != null) {
                battery = telemetry.getBatteryLevel();
                altitude = telemetry.getAltitude();
                lat = telemetry.getLatitude();
                lon = telemetry.getLongitude();
                timestamp = telemetry.getTimestamp();

                long age = (timestamp > 0) ? (now - timestamp) : Long.MAX_VALUE;
                if (timestamp == 0) {
                    status = "READY";
                } else if (age < STALE_THRESHOLD_MS) {
                    status = "ACTIVE";
                } else if (age < OFFLINE_THRESHOLD_MS) {
                    status = "STALE";
                } else {
                    status = "OFFLINE";
                }
            } else {
                status = "OFFLINE";
            }

            String currentMission = (droneMissionAssignments != null)
                    ? droneMissionAssignments.get(droneId)
                    : null;

            summaries.add(new DroneSummaryDto(
                    droneId,
                    status,
                    battery,
                    altitude,
                    lat,
                    lon,
                    timestamp,
                    currentMission,
                    currentMission != null ? "ON_MISSION" : "IDLE"
            ));
        }

        return summaries;
    }

    public boolean isValidDroneId(String droneId) {
        return droneId != null && KNOWN_DRONES.contains(droneId.trim().toUpperCase());
    }
}
