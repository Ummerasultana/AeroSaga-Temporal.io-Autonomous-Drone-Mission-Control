package in.aerosaga.controller;

import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.aerosaga.dto.*;
import in.aerosaga.service.MissionService;
import in.aerosaga.service.TelemetryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;
    private final TelemetryService telemetryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MissionController(MissionService missionService, TelemetryService telemetryService) {
        this.missionService = missionService;
        this.telemetryService = telemetryService;
    }

    @PostMapping("/start")
    public ResponseEntity<MissionResponse> startMission(@RequestBody JsonNode payload) {
        List<String> droneIds = new ArrayList<>();

        if (payload.isArray()) {
            for (JsonNode node : payload) {
                droneIds.add(node.asText());
            }
        } else if (payload.has("droneIds") && payload.get("droneIds").isArray()) {
            for (JsonNode node : payload.get("droneIds")) {
                droneIds.add(node.asText());
            }
        } else {
            throw new IllegalArgumentException("Request payload must be an array of drone IDs or an object with 'droneIds'");
        }

        MissionResponse response = missionService.startMission(droneIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{workflowId}/status")
    public ResponseEntity<MissionStatusResponse> getMissionStatus(@PathVariable String workflowId) {
        MissionStatusResponse response = missionService.getMissionStatus(workflowId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workflowId}/abort")
    public ResponseEntity<AbortMissionResponse> abortMission(@PathVariable String workflowId) {
        AbortMissionResponse response = missionService.abortMission(workflowId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<MissionStatusResponse>> getActiveMissions() {
        return ResponseEntity.ok(missionService.getActiveMissions());
    }

    @GetMapping("/history")
    public ResponseEntity<List<MissionHistoryItem>> getMissionHistory() {
        return ResponseEntity.ok(missionService.getMissionHistory());
    }

    @GetMapping("/drones")
    public ResponseEntity<List<DroneSummaryDto>> getDrones() {
        return ResponseEntity.ok(telemetryService.getDroneSummaries(missionService.getActiveDroneAssignments()));
    }
}
