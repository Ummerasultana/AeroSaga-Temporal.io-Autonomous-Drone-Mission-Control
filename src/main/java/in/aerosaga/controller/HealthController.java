package in.aerosaga.controller;

import in.aerosaga.dto.HealthResponse;
import in.aerosaga.service.MissionService;
import in.aerosaga.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final MissionService missionService;
    private final TelemetryService telemetryService;

    public HealthController(MissionService missionService, TelemetryService telemetryService) {
        this.missionService = missionService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        boolean temporalOk = missionService.isTemporalConnected();
        String temporalStatus = temporalOk ? "CONNECTED" : "DISCONNECTED";

        HealthResponse health = new HealthResponse(
                "UP",
                temporalStatus,
                missionService.getActiveMissions().size(),
                TelemetryService.KNOWN_DRONES.size(),
                System.currentTimeMillis()
        );

        return ResponseEntity.ok(health);
    }
}
