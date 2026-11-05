package in.aerosaga.controller;

import in.aerosaga.dto.MissionRequest;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.dto.MissionProgressResponse;
import in.aerosaga.dto.MissionStatusRequest;
import in.aerosaga.service.MissionService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
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

        return ResponseEntity.ok(
                missionService.createMission(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<MissionResponse>> getAllMissions() {
        return ResponseEntity.ok(
                missionService.getAllMissions()
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<List<MissionResponse>> filterMissions(
            @RequestParam(required = false) String droneId,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                missionService.filterMissions(droneId, status)
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

    // Get structured mission progress
    @GetMapping("/{missionId}/progress")
    public ResponseEntity<?> getMissionProgress(
            @PathVariable String missionId) {

        MissionProgressResponse progress =
                missionService.getMissionProgress(missionId);

        if (progress == null) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "error", "Mission not found",
                            "missionId", missionId
                    )
            );
        }

        return ResponseEntity.ok(progress);
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