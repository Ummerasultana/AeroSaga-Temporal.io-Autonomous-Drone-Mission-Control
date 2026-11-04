package in.aerosaga.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MissionActivity {

    @ActivityMethod
    void updateMissionProgress(
            String missionId,
            String currentStage,
            String status
    );
}