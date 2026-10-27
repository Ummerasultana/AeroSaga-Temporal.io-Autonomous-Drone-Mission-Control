package in.aerosaga;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. Test mission creation
    @Test
    void createMissionTest() throws Exception {

        String request = """
                {
                    "droneId": "DRONE-TEST",
                    "startLatitude": 28.6139,
                    "startLongitude": 77.2090,
                    "targetLatitude": 28.5355,
                    "targetLongitude": 77.3910
                }
                """;

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.droneId").value("DRONE-TEST"))
                .andExpect(jsonPath("$.status").value("STARTED"));
    }

    // 2. Test get all missions
    @Test
    void getAllMissionsTest() throws Exception {

        mockMvc.perform(get("/api/missions"))
                .andExpect(status().isOk());
    }

    // 3. Test invalid mission status
    @Test
    void invalidStatusTest() throws Exception {

        String request = """
                {
                    "droneId": "DRONE-TEST",
                    "startLatitude": 28.6139,
                    "startLongitude": 77.2090,
                    "targetLatitude": 28.5355,
                    "targetLongitude": 77.3910
                }
                """;

        String response = mockMvc.perform(
                post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String missionId = response
                .split("\"missionId\":\"")[1]
                .split("\"")[0];

        String statusRequest = """
                {
                    "status": "HELLO"
                }
                """;

        mockMvc.perform(
                put("/api/missions/" + missionId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusRequest))
                .andExpect(status().isBadRequest());
    }

    // 4. Test missing drone ID
    @Test
    void missingDroneIdTest() throws Exception {

        String request = """
                {
                    "startLatitude": 28.6139,
                    "startLongitude": 77.2090,
                    "targetLatitude": 28.5355,
                    "targetLongitude": 77.3910
                }
                """;

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    // 5. Test missing coordinates
    @Test
    void missingCoordinatesTest() throws Exception {

        String request = """
                {
                    "droneId": "DRONE-TEST"
                }
                """;

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    // 6. Test mission not found
    @Test
    void missionNotFoundTest() throws Exception {

        mockMvc.perform(
                get("/api/missions/non-existing-id"))
                .andExpect(status().isNotFound());
    }

    // 7. Test valid status update
    @Test
    void validStatusUpdateTest() throws Exception {

        String request = """
                {
                    "droneId": "DRONE-TEST",
                    "startLatitude": 28.6139,
                    "startLongitude": 77.2090,
                    "targetLatitude": 28.5355,
                    "targetLongitude": 77.3910
                }
                """;

        String response = mockMvc.perform(
                post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String missionId = response
                .split("\"missionId\":\"")[1]
                .split("\"")[0];

        String statusRequest = """
                {
                    "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(
                put("/api/missions/" + missionId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("IN_PROGRESS"));
    }
}