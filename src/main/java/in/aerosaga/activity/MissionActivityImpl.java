package in.aerosaga.activity;

import in.aerosaga.entity.Mission;
import in.aerosaga.repository.MissionRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MissionActivityImpl implements MissionActivity {

    private final MissionRepository missionRepository;

    public MissionActivityImpl(
            MissionRepository missionRepository) {

        this.missionRepository = missionRepository;
    }

    @Override
    @Transactional
    public void updateMissionProgress(
            String missionId,
            String currentStage,
            String status) {

        Mission mission = missionRepository
                .findByMissionId(missionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mission not found: " + missionId
                ));

        mission.setCurrentStage(currentStage);
        mission.setStatus(status);

        missionRepository.save(mission);
    }
}