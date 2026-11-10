package in.aerosaga.mission;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DroneMissionActivitiesTest {

    private DroneMissionActivitiesImpl activities;

    @BeforeEach
    void setUp() {
        System.setProperty("aerosaga.test.fast", "true");
        activities = new DroneMissionActivitiesImpl(10);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("aerosaga.test.fast");
        System.clearProperty("aerosaga.test.drop-failure");
    }

    @Test
    void testNormalMissionActivities() {
        assertDoesNotThrow(() -> activities.takeoff("DRONE-101"));
        assertDoesNotThrow(() -> activities.navigate("DRONE-101"));
        assertDoesNotThrow(() -> activities.dropPackage("DRONE-101"));
        assertDoesNotThrow(() -> activities.returnToBase("DRONE-101"));
    }

    @Test
    void testDropPackageSimulatedFailure() {
        System.setProperty("aerosaga.test.drop-failure", "true");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> activities.dropPackage("DRONE-101"));
        assertTrue(ex.getMessage().contains("Simulated package delivery failure"));
    }

    @Test
    void testDropPackageFailureByDroneIdPattern() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> activities.dropPackage("DRONE-FAIL-01"));
        assertTrue(ex.getMessage().contains("Simulated package delivery failure"));
    }
}
