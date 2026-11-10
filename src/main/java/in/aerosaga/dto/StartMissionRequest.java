package in.aerosaga.dto;

import java.util.ArrayList;
import java.util.List;

public class StartMissionRequest {

    private List<String> droneIds = new ArrayList<>();

    public StartMissionRequest() {
    }

    public StartMissionRequest(List<String> droneIds) {
        this.droneIds = droneIds;
    }

    public List<String> getDroneIds() {
        return droneIds;
    }

    public void setDroneIds(List<String> droneIds) {
        this.droneIds = droneIds;
    }
}
