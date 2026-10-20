package in.aerosaga.controller;

import in.aerosaga.dto.MissionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    @GetMapping("/test")
    public String test() {
        return "Mission Management API is working!";
    }

    @PostMapping
    public ResponseEntity<String> startMission(
            @Valid @RequestBody MissionRequest request) {

        String missionId = UUID.randomUUID().toString();

        return ResponseEntity.ok(
                "Mission started successfully. Mission ID: " + missionId
        );
    }
}