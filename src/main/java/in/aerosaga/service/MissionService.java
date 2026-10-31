package in.aerosaga.service;

import in.aerosaga.dto.MissionRequest;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.enums.MissionStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MissionService {

    private final Map<String, MissionResponse> missions =
            new ConcurrentHashMap<>();

    private final TemporalMissionService temporalMissionService;

    public MissionService(
            TemporalMissionService temporalMissionService) {
        this.temporalMissionService = temporalMissionService;
    }

    public MissionResponse createMission(MissionRequest request) {
        String missionId = UUID.randomUUID().toString();

        MissionResponse response = new MissionResponse(
                missionId,
                request.getDroneId(),
                MissionStatus.STARTED.name(),
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getTargetLatitude(),
                request.getTargetLongitude()
        );

        missions.put(missionId, response);

        try {
            temporalMissionService.startMissionWorkflow(
                    missionId,
                    request.getDroneId(),
                    request.getStartLatitude(),
                    request.getStartLongitude(),
                    request.getTargetLatitude(),
                    request.getTargetLongitude()
            );
        } catch (RuntimeException e) {
            missions.remove(missionId);
            throw new IllegalStateException(
                    "Unable to start mission workflow", e
            );
        }

        return response;
    }

    public MissionResponse getMission(String missionId) {
        return missions.get(missionId);
    }

    public List<MissionResponse> getAllMissions() {
        return new ArrayList<>(missions.values());
    }

    public synchronized MissionResponse updateMissionStatus(
            String missionId, String status) {

        MissionResponse mission = missions.get(missionId);

        if (mission == null) {
            return null;
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Mission status is required"
            );
        }

        MissionStatus newStatus;

        try {
            newStatus = MissionStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid mission status: " + status
            );
        }

        MissionStatus currentStatus =
                MissionStatus.valueOf(mission.getStatus());

        if (currentStatus == newStatus) {
            return mission;
        }

        if (currentStatus == MissionStatus.COMPLETED
                || currentStatus == MissionStatus.ABORTED) {
            throw new IllegalStateException(
                    "A completed or aborted mission cannot be changed"
            );
        }

        if (newStatus == MissionStatus.STARTED) {
            throw new IllegalArgumentException(
                    "A mission cannot be reset to STARTED"
            );
        }

        mission.setStatus(newStatus.name());

        return mission;
    }

    public synchronized MissionResponse cancelMission(
            String missionId) {

        MissionResponse mission = missions.get(missionId);

        if (mission == null) {
            return null;
        }

        MissionStatus currentStatus =
                MissionStatus.valueOf(mission.getStatus());

        if (currentStatus == MissionStatus.COMPLETED
                || currentStatus == MissionStatus.ABORTED) {
            throw new IllegalStateException(
                    "Completed or aborted missions cannot be cancelled"
            );
        }

        temporalMissionService.cancelMissionWorkflow(missionId);

        mission.setStatus(MissionStatus.ABORTED.name());

        return mission;
    }
}