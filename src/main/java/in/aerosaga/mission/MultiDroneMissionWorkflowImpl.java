
package in.aerosaga.mission;

import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import io.temporal.activity.ActivityOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MultiDroneMissionWorkflowImpl
        implements MultiDroneMissionWorkflow {

    private final DroneMissionActivities activities =
            Workflow.newActivityStub(
                    DroneMissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofMinutes(2))
                            .build()
            );

    @Override
    public List<String> executeMissions(List<String> droneIds) {

        List<Promise<String>> promises = new ArrayList<>();

        for (String droneId : droneIds) {
            Promise<String> promise = Async.function(() -> {
                activities.takeoff(droneId);
                activities.navigate(droneId);
                activities.dropPackage(droneId);
                activities.returnToBase(droneId);

                return "Mission completed for " + droneId;
            });

            promises.add(promise);
        }

        Promise.allOf(promises.toArray(new Promise[0])).get();

        List<String> results = new ArrayList<>();

        for (Promise<String> promise : promises) {
            results.add(promise.get());
        }

        return results;
    }
}
