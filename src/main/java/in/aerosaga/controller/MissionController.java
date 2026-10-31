package in.aerosaga.controller;

import in.aerosaga.dto.MissionRequest;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.dto.MissionStatusRequest;
import in.aerosaga.service.MissionService;
import in.aerosaga.service.TemporalMissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;
    private final TemporalMissionService temporalMissionService;

    public MissionController(
            MissionService missionService,
            TemporalMissionService temporalMissionService) {
        this.missionService = missionService;
        this.temporalMissionService = temporalMissionService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(
                "Mission Management API is working!"
        );
    }

    @PostMapping
    public ResponseEntity<MissionResponse> createMission(
            @Valid @RequestBody MissionRequest request) {

        MissionResponse response =
                missionService.createMission(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MissionResponse>> getAllMissions() {
        return ResponseEntity.ok(
                missionService.getAllMissions()
        );
    }

    @GetMapping("/{missionId}")
    public ResponseEntity<?> getMission(
            @PathVariable String missionId) {

        MissionResponse mission =
                missionService.getMission(missionId);

        if (mission == null) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "error", "Mission not found",
                            "missionId", missionId
                    )
            );
        }

        return ResponseEntity.ok(mission);
    }

    @PutMapping("/{missionId}/status")
    public ResponseEntity<?> updateMissionStatus(
            @PathVariable String missionId,
            @RequestBody MissionStatusRequest request) {

        MissionResponse existing =
                missionService.getMission(missionId);

        if (existing == null) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "error", "Mission not found",
                            "missionId", missionId
                    )
            );
        }

        MissionResponse updated =
                missionService.updateMissionStatus(
                        missionId,
                        request.getStatus()
                );

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{missionId}/progress")
    public ResponseEntity<Map<String, String>> getMissionProgress(
            @PathVariable String missionId) {

        MissionResponse mission =
                missionService.getMission(missionId);

        if (mission == null) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "error", "Mission not found",
                            "missionId", missionId
                    )
            );
        }

        String progress =
                temporalMissionService.getMissionProgress(missionId);

        return ResponseEntity.ok(
                Map.of(
                        "missionId", missionId,
                        "progress", progress
                )
        );
    }

    @PutMapping("/{missionId}/cancel")
    public ResponseEntity<?> cancelMission(
            @PathVariable String missionId) {

        MissionResponse cancelled =
                missionService.cancelMission(missionId);

        if (cancelled == null) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "error", "Mission not found",
                            "missionId", missionId
                    )
            );
        }

        return ResponseEntity.ok(cancelled);
    }
}