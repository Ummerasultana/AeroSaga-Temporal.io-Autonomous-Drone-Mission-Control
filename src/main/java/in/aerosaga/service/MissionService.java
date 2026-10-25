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

        return response;
    }

    public MissionResponse getMission(String missionId) {
        return missions.get(missionId);
    }

    public List<MissionResponse> getAllMissions() {
        return new ArrayList<>(missions.values());
    }

    public MissionResponse updateMissionStatus(
            String missionId, String status) {

        MissionResponse mission = missions.get(missionId);

        if (mission == null) {
            return null;
        }

        MissionStatus validStatus;

        try {
            validStatus = MissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid mission status");
        }

        mission.setStatus(validStatus.name());

        return mission;
    }
}