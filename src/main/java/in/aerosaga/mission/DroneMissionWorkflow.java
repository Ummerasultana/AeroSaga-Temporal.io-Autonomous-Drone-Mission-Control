
package in.aerosaga.mission;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.SignalMethod;

@WorkflowInterface
public interface DroneMissionWorkflow {

    @WorkflowMethod
    String executeMission(String droneId);

    @SignalMethod
    void requestAbort();

    @QueryMethod
    String getMissionStatus();
}
