
package in.aerosaga.mission;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface MultiDroneMissionWorkflow {

    @WorkflowMethod
    List<String> executeMissions(List<String> droneIds);
}
