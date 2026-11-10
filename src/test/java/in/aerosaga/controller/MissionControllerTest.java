package in.aerosaga.controller;

import tools.jackson.databind.ObjectMapper;
import in.aerosaga.config.AeroSagaProperties;
import in.aerosaga.dto.AbortMissionResponse;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.dto.MissionStatusResponse;
import in.aerosaga.service.MissionService;
import in.aerosaga.service.TelemetryService;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MissionControllerTest {

    private MissionController controller;
    private MissionService missionService;
    private TelemetryService telemetryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService();
        AeroSagaProperties properties = new AeroSagaProperties();

        // Create pure Java dynamic proxies for Temporal
        WorkflowClient dummyClient = (WorkflowClient) Proxy.newProxyInstance(
                WorkflowClient.class.getClassLoader(),
                new Class<?>[]{WorkflowClient.class},
                (proxy, method, args) -> {
                    if ("newWorkflowStub".equals(method.getName())) {
                        Class<?> returnType = (Class<?>) args[0];
                        return Proxy.newProxyInstance(
                                returnType.getClassLoader(),
                                new Class<?>[]{returnType},
                                (p, m, a) -> null
                        );
                    }
                    if ("newUntypedWorkflowStub".equals(method.getName())) {
                        return Proxy.newProxyInstance(
                                WorkflowStub.class.getClassLoader(),
                                new Class<?>[]{WorkflowStub.class},
                                (p, m, a) -> {
                                    if ("query".equals(m.getName())) return "NAVIGATING";
                                    return null;
                                }
                        );
                    }
                    return null;
                }
        );

        WorkflowServiceStubs dummyService = (WorkflowServiceStubs) Proxy.newProxyInstance(
                WorkflowServiceStubs.class.getClassLoader(),
                new Class<?>[]{WorkflowServiceStubs.class},
                (proxy, method, args) -> {
                    if ("isShutdown".equals(method.getName())) return false;
                    return null;
                }
        );

        missionService = new MissionService(dummyClient, dummyService, properties, telemetryService);
        controller = new MissionController(missionService, telemetryService);
    }

    @Test
    void testStartMissionWithArrayPayload() throws Exception {
        var json = objectMapper.readTree("[\"DRONE-101\"]");
        ResponseEntity<MissionResponse> response = controller.startMission(json);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getWorkflowId().startsWith("drone-mission-"));
        assertEquals(List.of("DRONE-101"), response.getBody().getDroneIds());
    }

    @Test
    void testStartMissionWithObjectPayload() throws Exception {
        var json = objectMapper.readTree("{\"droneIds\": [\"DRONE-102\", \"DRONE-103\"]}");
        ResponseEntity<MissionResponse> response = controller.startMission(json);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getWorkflowId().startsWith("multi-drone-"));
        assertEquals(List.of("DRONE-102", "DRONE-103"), response.getBody().getDroneIds());
    }

    @Test
    void testGetMissionStatus() throws Exception {
        var json = objectMapper.readTree("[\"DRONE-101\"]");
        ResponseEntity<MissionResponse> startResp = controller.startMission(json);
        String workflowId = startResp.getBody().getWorkflowId();

        ResponseEntity<MissionStatusResponse> statusResp = controller.getMissionStatus(workflowId);
        assertEquals(HttpStatus.OK, statusResp.getStatusCode());
        assertNotNull(statusResp.getBody());
        assertEquals("NAVIGATING", statusResp.getBody().getStatus());
        assertEquals(50, statusResp.getBody().getProgressPercent());
    }

    @Test
    void testAbortMission() throws Exception {
        var json = objectMapper.readTree("[\"DRONE-101\"]");
        ResponseEntity<MissionResponse> startResp = controller.startMission(json);
        String workflowId = startResp.getBody().getWorkflowId();

        ResponseEntity<AbortMissionResponse> abortResp = controller.abortMission(workflowId);
        assertEquals(HttpStatus.OK, abortResp.getStatusCode());
        assertEquals("ABORTING", abortResp.getBody().getStatus());
    }

    @Test
    void testGetActiveMissionsAndHistory() throws Exception {
        var json = objectMapper.readTree("[\"DRONE-101\"]");
        controller.startMission(json);

        assertEquals(1, controller.getActiveMissions().getBody().size());
        assertEquals(1, controller.getMissionHistory().getBody().size());
        assertEquals(3, controller.getDrones().getBody().size());
    }
}
