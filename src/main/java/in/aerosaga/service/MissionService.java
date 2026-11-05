package in.aerosaga.service;

import in.aerosaga.dto.MissionRequest;
import in.aerosaga.dto.MissionResponse;
import in.aerosaga.dto.MissionProgressResponse;
import in.aerosaga.entity.Mission;
import in.aerosaga.enums.MissionStatus;
import in.aerosaga.repository.MissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final TemporalMissionService temporalMissionService;

    public MissionService(
            MissionRepository missionRepository,
            TemporalMissionService temporalMissionService) {

        this.missionRepository = missionRepository;
        this.temporalMissionService = temporalMissionService;
    }

    // Create a new mission
    @Transactional
    public MissionResponse createMission(MissionRequest request) {

        // Validate start and target locations
        if (request.getStartLatitude()
                    .equals(request.getTargetLatitude())
                && request.getStartLongitude()
                    .equals(request.getTargetLongitude())) {

            throw new IllegalArgumentException(
                    "Start location and target location cannot be the same"
            );
        }

        String missionId = UUID.randomUUID().toString();

        // Create database entity
        Mission mission = new Mission(
                missionId,
                request.getDroneId(),
                MissionStatus.STARTED.name(),
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getTargetLatitude(),
                request.getTargetLongitude()
        );

        // Save mission in PostgreSQL
        missionRepository.save(mission);

        try {
            // Start Temporal workflow
            temporalMissionService.startMissionWorkflow(
                    missionId,
                    request.getDroneId(),
                    request.getStartLatitude(),
                    request.getStartLongitude(),
                    request.getTargetLatitude(),
                    request.getTargetLongitude()
            );

        } catch (RuntimeException e) {

            // Remove mission if workflow fails to start
            missionRepository.delete(mission);

            throw new IllegalStateException(
                    "Unable to start mission workflow", e
            );
        }

        return convertToResponse(mission);
    }

    // Get a mission by ID
    public MissionResponse getMission(String missionId) {

        return missionRepository.findByMissionId(missionId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    // Get structured mission progress
    public MissionProgressResponse getMissionProgress(String missionId) {

        Mission mission = missionRepository
                .findByMissionId(missionId)
                .orElse(null);

        if (mission == null) {
            return null;
        }

        return new MissionProgressResponse(
                mission.getMissionId(),
                mission.getDroneId(),
                mission.getCurrentStage(),
                mission.getStatus(),
                "COMPLETED".equals(mission.getCurrentStage())
        );
    }

    // Get all missions
    public List<MissionResponse> getAllMissions() {

        return missionRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Update mission status
    @Transactional
    public synchronized MissionResponse updateMissionStatus(
            String missionId, String status) {

        Mission mission = missionRepository
                .findByMissionId(missionId)
                .orElse(null);

        if (mission == null) {
            return null;
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Mission status is required"
            );
        }

        MissionStatus newStatus;

        try {
            newStatus = MissionStatus.valueOf(
                    status.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid mission status: " + status
            );
        }

        MissionStatus currentStatus =
                MissionStatus.valueOf(mission.getStatus());

        if (currentStatus == newStatus) {
            return convertToResponse(mission);
        }

        if (currentStatus == MissionStatus.COMPLETED
                || currentStatus == MissionStatus.ABORTED) {

            throw new IllegalStateException(
                    "A completed or aborted mission cannot be changed"
            );
        }

        if (newStatus == MissionStatus.STARTED) {
            throw new IllegalArgumentException(
                    "A mission cannot be reset to STARTED"
            );
        }

        mission.setStatus(newStatus.name());

        // Save updated status in PostgreSQL
        missionRepository.save(mission);

        return convertToResponse(mission);
    }

    // Cancel a mission
    @Transactional
    public synchronized MissionResponse cancelMission(
            String missionId) {

        Mission mission = missionRepository
                .findByMissionId(missionId)
                .orElse(null);

        if (mission == null) {
            return null;
        }

        MissionStatus currentStatus =
                MissionStatus.valueOf(mission.getStatus());

        if (currentStatus == MissionStatus.COMPLETED
                || currentStatus == MissionStatus.ABORTED) {

            throw new IllegalStateException(
                    "Completed or aborted missions cannot be cancelled"
            );
        }

        // Cancel Temporal workflow
        temporalMissionService.cancelMissionWorkflow(missionId);

        mission.setStatus(MissionStatus.ABORTED.name());

        // Save cancellation in PostgreSQL
        missionRepository.save(mission);

        return convertToResponse(mission);
    }

    // Filter missions using PostgreSQL queries
    public List<MissionResponse> filterMissions(
            String droneId, String status) {

        List<Mission> missions;

        boolean hasDroneId =
                droneId != null && !droneId.isBlank();

        boolean hasStatus =
                status != null && !status.isBlank();

        String cleanDroneId =
                hasDroneId ? droneId.trim() : null;

        String cleanStatus =
                hasStatus ? status.trim().toUpperCase() : null;

        if (hasDroneId && hasStatus) {

            missions = missionRepository.findByDroneIdAndStatus(
                    cleanDroneId, cleanStatus
            );

        } else if (hasDroneId) {

            missions = missionRepository.findByDroneId(
                    cleanDroneId
            );

        } else if (hasStatus) {

            missions = missionRepository.findByStatus(
                    cleanStatus
            );

        } else {

            missions = missionRepository.findAll();
        }

        return missions.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Convert database entity into API response
    private MissionResponse convertToResponse(Mission mission) {

        return new MissionResponse(
                mission.getMissionId(),
                mission.getDroneId(),
                mission.getStatus(),
                mission.getStartLatitude(),
                mission.getStartLongitude(),
                mission.getTargetLatitude(),
                mission.getTargetLongitude()
        );
    }
}