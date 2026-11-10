package in.aerosaga.service;

import in.aerosaga.config.AeroSagaProperties;
import in.aerosaga.dto.AbortMissionResponse;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.dto.MissionStatusResponse;
import in.aerosaga.mission.DroneMissionWorkflow;
import in.aerosaga.mission.MultiDroneMissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class MissionServiceTest {

    private AeroSagaProperties properties;
    private TelemetryService telemetryService;
    private MissionService missionService;
    private AtomicBoolean signalCalled;

    @BeforeEach
    void setUp() {
        properties = new AeroSagaProperties();
        properties.setTaskQueue("DRONE_TASK_QUEUE");
        telemetryService = new TelemetryService();
        signalCalled = new AtomicBoolean(false);

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
                                    if ("query".equals(m.getName())) {
                                        String queryType = (String) a[0];
                                        if ("getMissionStatus".equals(queryType)) return "NAVIGATING";
                                        if ("getDroneStatuses".equals(queryType)) return Map.of("DRONE-101", "NAVIGATING");
                                    }
                                    if ("signal".equals(m.getName())) {
                                        signalCalled.set(true);
                                        return null;
                                    }
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
    }

    @Test
    void testStartSingleDroneMissionSuccess() {
        MissionResponse response = missionService.startMission(List.of("DRONE-101"));

        assertNotNull(response);
        assertNotNull(response.getWorkflowId());
        assertTrue(response.getWorkflowId().startsWith("drone-mission-"));
        assertEquals(List.of("DRONE-101"), response.getDroneIds());
        assertEquals("PENDING", response.getStatus());

        assertTrue(missionService.getActiveDroneAssignments().containsKey("DRONE-101"));
        assertEquals(response.getWorkflowId(), missionService.getActiveDroneAssignments().get("DRONE-101"));
    }

    @Test
    void testStartMultiDroneMissionSuccess() {
        List<String> drones = List.of("DRONE-101", "DRONE-102");
        MissionResponse response = missionService.startMission(drones);

        assertNotNull(response);
        assertTrue(response.getWorkflowId().startsWith("multi-drone-"));
        assertEquals(drones, response.getDroneIds());

        assertTrue(missionService.getActiveDroneAssignments().containsKey("DRONE-101"));
        assertTrue(missionService.getActiveDroneAssignments().containsKey("DRONE-102"));
    }

    @Test
    void testStartMissionRejectsEmptyDrones() {
        assertThrows(IllegalArgumentException.class, () -> missionService.startMission(List.of()));
        assertThrows(IllegalArgumentException.class, () -> missionService.startMission(null));
    }

    @Test
    void testStartMissionRejectsInvalidDrone() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> missionService.startMission(List.of("DRONE-INVALID")));
        assertTrue(ex.getMessage().contains("Invalid drone ID"));
    }

    @Test
    void testDuplicateMissionPrevention() {
        missionService.startMission(List.of("DRONE-101"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> missionService.startMission(List.of("DRONE-101")));
        assertTrue(ex.getMessage().contains("currently engaged in active mission"));
    }

    @Test
    void testGetMissionStatus() {
        MissionResponse startResp = missionService.startMission(List.of("DRONE-101"));
        MissionStatusResponse statusResp = missionService.getMissionStatus(startResp.getWorkflowId());

        assertNotNull(statusResp);
        assertEquals("NAVIGATING", statusResp.getStatus());
        assertEquals(50, statusResp.getProgressPercent());
    }

    @Test
    void testGetMissionStatusNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> missionService.getMissionStatus("non-existent-id"));
    }

    @Test
    void testAbortMission() {
        MissionResponse startResp = missionService.startMission(List.of("DRONE-101"));
        AbortMissionResponse abortResp = missionService.abortMission(startResp.getWorkflowId());

        assertNotNull(abortResp);
        assertEquals("ABORTING", abortResp.getStatus());
        assertTrue(signalCalled.get());
    }

    @Test
    void testActiveMissionsAndHistory() {
        missionService.startMission(List.of("DRONE-101"));

        assertEquals(1, missionService.getActiveMissions().size());
        assertEquals(1, missionService.getMissionHistory().size());
    }
}
