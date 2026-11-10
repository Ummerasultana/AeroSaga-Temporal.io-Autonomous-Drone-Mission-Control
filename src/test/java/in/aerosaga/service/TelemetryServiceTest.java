package in.aerosaga.service;

import in.aerosaga.dto.DroneSummaryDto;
import in.aerosaga.telemetry.DroneTelemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TelemetryServiceTest {

    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService();
    }

    @Test
    void testInitialKnownDrones() {
        assertTrue(telemetryService.isValidDroneId("DRONE-101"));
        assertTrue(telemetryService.isValidDroneId("DRONE-102"));
        assertTrue(telemetryService.isValidDroneId("DRONE-103"));
        assertFalse(telemetryService.isValidDroneId("DRONE-999"));
        assertFalse(telemetryService.isValidDroneId(null));
        assertFalse(telemetryService.isValidDroneId(""));
    }

    @Test
    void testUpdateTelemetry() {
        long now = System.currentTimeMillis();
        DroneTelemetry telemetry = new DroneTelemetry("DRONE-101", 15.145, 76.930, 150.0, 92.5, now);
        telemetryService.updateTelemetry(telemetry);

        Optional<DroneTelemetry> retrieved = telemetryService.getLatestTelemetry("DRONE-101");
        assertTrue(retrieved.isPresent());
        assertEquals("DRONE-101", retrieved.get().getDroneId());
        assertEquals(15.145, retrieved.get().getLatitude());
        assertEquals(76.930, retrieved.get().getLongitude());
        assertEquals(150.0, retrieved.get().getAltitude());
        assertEquals(92.5, retrieved.get().getBatteryLevel());
    }

    @Test
    void testDroneSummariesActiveAndStale() {
        long now = System.currentTimeMillis();
        // DRONE-101 active (recent timestamp)
        telemetryService.updateTelemetry(new DroneTelemetry("DRONE-101", 15.14, 76.92, 100.0, 85.0, now - 1000));
        // DRONE-102 stale (15 seconds ago)
        telemetryService.updateTelemetry(new DroneTelemetry("DRONE-102", 15.14, 76.92, 110.0, 80.0, now - 15000));

        Map<String, String> missionAssignments = Map.of("DRONE-101", "multi-drone-001");
        List<DroneSummaryDto> summaries = telemetryService.getDroneSummaries(missionAssignments);

        assertEquals(3, summaries.size());

        DroneSummaryDto d101 = summaries.stream().filter(d -> d.getDroneId().equals("DRONE-101")).findFirst().orElseThrow();
        assertEquals("ACTIVE", d101.getStatus());
        assertEquals("multi-drone-001", d101.getCurrentMissionId());
        assertEquals("ON_MISSION", d101.getMissionStatus());

        DroneSummaryDto d102 = summaries.stream().filter(d -> d.getDroneId().equals("DRONE-102")).findFirst().orElseThrow();
        assertEquals("STALE", d102.getStatus());
        assertNull(d102.getCurrentMissionId());
        assertEquals("IDLE", d102.getMissionStatus());
    }
}
