package in.aerosaga.service;

import in.aerosaga.config.AeroSagaProperties;
import in.aerosaga.dto.*;
import in.aerosaga.mission.DroneMissionWorkflow;
import in.aerosaga.mission.MultiDroneMissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MissionService {

    private static final Logger logger = LoggerFactory.getLogger(MissionService.class);

    private final WorkflowClient workflowClient;
    private final WorkflowServiceStubs workflowServiceStubs;
    private final AeroSagaProperties properties;
    private final TelemetryService telemetryService;

    // In-memory mission store
    private final Map<String, MissionRecord> missionRegistry = new ConcurrentHashMap<>();
    // Tracks active missions assigned to drones to prevent duplicate runs
    private final Map<String, String> activeDroneAssignments = new ConcurrentHashMap<>();

    public MissionService(WorkflowClient workflowClient,
                          WorkflowServiceStubs workflowServiceStubs,
                          AeroSagaProperties properties,
                          TelemetryService telemetryService) {
        this.workflowClient = workflowClient;
        this.workflowServiceStubs = workflowServiceStubs;
        this.properties = properties;
        this.telemetryService = telemetryService;
    }

    public synchronized MissionResponse startMission(List<String> rawDroneIds) {
        if (rawDroneIds == null || rawDroneIds.isEmpty()) {
            throw new IllegalArgumentException("At least one drone must be selected for the mission");
        }

        List<String> droneIds = rawDroneIds.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());

        for (String droneId : droneIds) {
            if (!telemetryService.isValidDroneId(droneId)) {
                throw new IllegalArgumentException("Invalid drone ID: '" + droneId + "'. Supported drones: " + TelemetryService.KNOWN_DRONES);
            }
            if (activeDroneAssignments.containsKey(droneId)) {
                String existingWorkflowId = activeDroneAssignments.get(droneId);
                throw new IllegalStateException("Drone " + droneId + " is currently engaged in active mission: " + existingWorkflowId);
            }
        }

        long now = System.currentTimeMillis();
        String workflowId;

        try {
            if (droneIds.size() == 1) {
                String singleDroneId = droneIds.get(0);
                workflowId = "drone-mission-" + UUID.randomUUID().toString().substring(0, 8);

                DroneMissionWorkflow workflow = workflowClient.newWorkflowStub(
                        DroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(properties.getTaskQueue())
                                .build()
                );

                WorkflowClient.start(workflow::executeMission, singleDroneId);
            } else {
                workflowId = "multi-drone-" + UUID.randomUUID().toString().substring(0, 8);

                MultiDroneMissionWorkflow workflow = workflowClient.newWorkflowStub(
                        MultiDroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(properties.getTaskQueue())
                                .build()
                );

                WorkflowClient.start(workflow::executeMissions, droneIds);
            }
        } catch (Exception e) {
            logger.error("Failed to start workflow with Temporal", e);
            throw new RuntimeException("Failed to initiate mission on Temporal orchestrator: " + e.getMessage(), e);
        }

        // Register active drone assignments
        for (String droneId : droneIds) {
            activeDroneAssignments.put(droneId, workflowId);
        }

        MissionRecord record = new MissionRecord(workflowId, droneIds, now, "PENDING");
        missionRegistry.put(workflowId, record);

        logger.info("Successfully started mission {} with drones {}", workflowId, droneIds);
        return new MissionResponse(workflowId, droneIds, "PENDING", now, "Mission successfully initialized on Temporal");
    }

    public MissionStatusResponse getMissionStatus(String workflowId) {
        MissionRecord record = missionRegistry.get(workflowId);
        if (record == null) {
            throw new NoSuchElementException("Mission not found with ID: " + workflowId);
        }

        if (!record.isTerminal()) {
            try {
                WorkflowStub stub = workflowClient.newUntypedWorkflowStub(workflowId);
                String temporalStatus = stub.query("getMissionStatus", String.class);

                if (temporalStatus != null && !temporalStatus.isBlank()) {
                    record.setStatus(temporalStatus);
                }

                if (record.getDroneIds().size() > 1) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, String> perDrone = stub.query("getDroneStatuses", Map.class);
                        if (perDrone != null) {
                            record.setDroneStatuses(perDrone);
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    record.getDroneStatuses().put(record.getDroneIds().get(0), record.getStatus());
                }

                if (record.isTerminal()) {
                    record.setEndTime(System.currentTimeMillis());
                    // Free active drone assignments
                    for (String droneId : record.getDroneIds()) {
                        activeDroneAssignments.remove(droneId);
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not query Temporal workflow {}: {}", workflowId, e.getMessage());
            }
        }

        return toStatusResponse(record);
    }

    public AbortMissionResponse abortMission(String workflowId) {
        MissionRecord record = missionRegistry.get(workflowId);
        if (record == null) {
            throw new NoSuchElementException("Cannot abort: Mission not found with ID: " + workflowId);
        }

        if (record.isTerminal()) {
            return new AbortMissionResponse(workflowId, record.getStatus(), "Mission already reached terminal state: " + record.getStatus());
        }

        try {
            WorkflowStub stub = workflowClient.newUntypedWorkflowStub(workflowId);
            stub.signal("requestAbort");
            record.setStatus("ABORTING");
            logger.warn("Emergency abort signal dispatched for mission {}", workflowId);
            return new AbortMissionResponse(workflowId, "ABORTING", "Emergency abort signal sent successfully");
        } catch (Exception e) {
            logger.error("Failed to signal abort to workflow {}", workflowId, e);
            throw new RuntimeException("Failed to send abort signal to Temporal: " + e.getMessage(), e);
        }
    }

    public List<MissionStatusResponse> getActiveMissions() {
        List<MissionStatusResponse> active = new ArrayList<>();
        for (MissionRecord record : missionRegistry.values()) {
            if (!record.isTerminal()) {
                active.add(getMissionStatus(record.getWorkflowId()));
            }
        }
        return active;
    }

    public List<MissionHistoryItem> getMissionHistory() {
        return missionRegistry.values().stream()
                .sorted(Comparator.comparingLong(MissionRecord::getStartTime).reversed())
                .map(r -> new MissionHistoryItem(
                        r.getWorkflowId(),
                        r.getDroneIds(),
                        r.getStartTime(),
                        r.getEndTime(),
                        r.getEndTime() != null ? (r.getEndTime() - r.getStartTime()) : null,
                        r.getStatus(),
                        r.getResult(),
                        r.getError()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, String> getActiveDroneAssignments() {
        return Collections.unmodifiableMap(activeDroneAssignments);
    }

    public boolean isTemporalConnected() {
        try {
            // Check whether the gRPC channel is connected/alive
            return workflowServiceStubs != null && !workflowServiceStubs.isShutdown();
        } catch (Exception e) {
            return false;
        }
    }

    public int getTotalMissionsCount() {
        return missionRegistry.size();
    }

    private MissionStatusResponse toStatusResponse(MissionRecord record) {
        int progress = calculateProgress(record.getStatus());
        return new MissionStatusResponse(
                record.getWorkflowId(),
                record.getDroneIds(),
                record.getStatus(),
                record.getDroneStatuses(),
                record.getStatus(),
                progress,
                record.getStartTime(),
                record.getEndTime(),
                record.getResult(),
                record.getError()
        );
    }

    private int calculateProgress(String status) {
        if (status == null) return 0;
        return switch (status.toUpperCase()) {
            case "PENDING" -> 5;
            case "TAKING_OFF" -> 25;
            case "NAVIGATING" -> 50;
            case "DELIVERING" -> 75;
            case "RETURNING" -> 90;
            case "COMPLETED" -> 100;
            case "ABORTING" -> 65;
            case "ABORTED", "FAILED" -> 100;
            default -> 10;
        };
    }

    // Inner class representing stored mission record
    public static class MissionRecord {
        private final String workflowId;
        private final List<String> droneIds;
        private final long startTime;
        private Long endTime;
        private String status;
        private Map<String, String> droneStatuses = new HashMap<>();
        private String result;
        private String error;

        public MissionRecord(String workflowId, List<String> droneIds, long startTime, String status) {
            this.workflowId = workflowId;
            this.droneIds = droneIds;
            this.startTime = startTime;
            this.status = status;
            for (String d : droneIds) {
                droneStatuses.put(d, status);
            }
        }

        public boolean isTerminal() {
            return "COMPLETED".equalsIgnoreCase(status)
                    || "FAILED".equalsIgnoreCase(status)
                    || "ABORTED".equalsIgnoreCase(status);
        }

        public String getWorkflowId() { return workflowId; }
        public List<String> getDroneIds() { return droneIds; }
        public long getStartTime() { return startTime; }
        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Map<String, String> getDroneStatuses() { return droneStatuses; }
        public void setDroneStatuses(Map<String, String> droneStatuses) { this.droneStatuses = droneStatuses; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
