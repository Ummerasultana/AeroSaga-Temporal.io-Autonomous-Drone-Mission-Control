package in.aerosaga.service;

import in.aerosaga.dto.MissionResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MissionService {

    private final Map<String, MissionResponse> missions = new ConcurrentHashMap<>();

    public MissionResponse createMission(String droneId) {

        String missionId = UUID.randomUUID().toString();

        MissionResponse response = new MissionResponse(
                missionId,
                droneId,
                "STARTED"
        );

        missions.put(missionId, response);

        return response;
    }

    public MissionResponse getMission(String missionId) {
        return missions.get(missionId);
    }

    public MissionResponse updateMissionStatus(
            String missionId, String status) {

        MissionResponse mission = missions.get(missionId);

        if (mission == null) {
            return null;
        }

        mission.setStatus(status);

        return mission;
    }
}