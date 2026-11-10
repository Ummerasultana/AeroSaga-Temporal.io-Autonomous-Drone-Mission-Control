package in.aerosaga.mission;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Map;

@WorkflowInterface
public interface MultiDroneMissionWorkflow {

    @WorkflowMethod
    List<String> executeMissions(List<String> droneIds);

    @SignalMethod
    void requestAbort();

    @QueryMethod
    String getMissionStatus();

    @QueryMethod
    Map<String, String> getDroneStatuses();
}
