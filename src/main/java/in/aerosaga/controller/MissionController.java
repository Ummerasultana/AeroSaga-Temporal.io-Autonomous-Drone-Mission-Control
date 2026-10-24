package in.aerosaga.controller;

import in.aerosaga.dto.MissionRequest;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.dto.MissionStatusRequest;
import in.aerosaga.service.MissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping("/test")
    public String test() {
        return "Mission Management API is working!";
    }

    @PostMapping
    public ResponseEntity<MissionResponse> startMission(
            @Valid @RequestBody MissionRequest request) {

        MissionResponse response =
                missionService.createMission(request.getDroneId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{missionId}")
    public ResponseEntity<MissionResponse> getMission(
            @PathVariable String missionId) {

        MissionResponse response =
                missionService.getMission(missionId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{missionId}/status")
    public ResponseEntity<MissionResponse> updateMissionStatus(
            @PathVariable String missionId,
            @Valid @RequestBody MissionStatusRequest request) {

        MissionResponse response =
                missionService.updateMissionStatus(
                        missionId, request.getStatus());

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}