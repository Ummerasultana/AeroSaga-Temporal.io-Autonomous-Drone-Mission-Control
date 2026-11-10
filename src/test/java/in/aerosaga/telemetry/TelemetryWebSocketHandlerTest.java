package in.aerosaga.telemetry;

import in.aerosaga.service.TelemetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TelemetryWebSocketHandlerTest {

    private TelemetryService telemetryService;
    private TelemetryWebSocketHandler handler;
    private List<WebSocketMessage<?>> sentMessages;
    private WebSocketSession dashboardSession;
    private WebSocketSession droneSession;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService();
        handler = new TelemetryWebSocketHandler(telemetryService);
        sentMessages = new ArrayList<>();

        dashboardSession = createSession("dash-123", "ws://localhost:8081/telemetry/dashboard", sentMessages);
        droneSession = createSession("drone-101-session", "ws://localhost:8081/telemetry/drone", new ArrayList<>());
    }

    private WebSocketSession createSession(String id, String uriStr, List<WebSocketMessage<?>> sink) {
        return (WebSocketSession) Proxy.newProxyInstance(
                WebSocketSession.class.getClassLoader(),
                new Class<?>[]{WebSocketSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getId".equals(name)) return id;
                    if ("getUri".equals(name)) return URI.create(uriStr);
                    if ("isOpen".equals(name)) return true;
                    if ("sendMessage".equals(name)) {
                        sink.add((WebSocketMessage<?>) args[0]);
                        return null;
                    }
                    if ("hashCode".equals(name)) return id.hashCode();
                    if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
                    if ("toString".equals(name)) return "Session[" + id + "]";
                    return null;
                }
        );
    }

    @Test
    void testDashboardConnectionLifecycle() {
        handler.afterConnectionEstablished(dashboardSession);
        assertEquals(1, handler.getDashboardSessionCount());

        handler.afterConnectionClosed(dashboardSession, CloseStatus.NORMAL);
        assertEquals(0, handler.getDashboardSessionCount());
    }

    @Test
    void testTelemetryMessageBroadcast() {
        handler.afterConnectionEstablished(dashboardSession);

        String validTelemetryJson = """
                {
                    "droneId": "DRONE-101",
                    "latitude": 15.1394,
                    "longitude": 76.9214,
                    "altitude": 120.0,
                    "batteryLevel": 95.0,
                    "timestamp": 1700000000000
                }
                """;

        handler.handleTextMessage(droneSession, new TextMessage(validTelemetryJson));

        // Verify telemetry saved in service
        assertTrue(telemetryService.getLatestTelemetry("DRONE-101").isPresent());
        assertEquals(95.0, telemetryService.getLatestTelemetry("DRONE-101").get().getBatteryLevel());

        // Verify message was broadcast to dashboard
        assertFalse(sentMessages.isEmpty());
        boolean hasBroadcast = sentMessages.stream()
                .anyMatch(m -> m.getPayload().toString().contains("DRONE-101"));
        assertTrue(hasBroadcast);
    }

    @Test
    void testMalformedTelemetryDoesNotCrashHandler() {
        assertDoesNotThrow(() ->
                handler.handleTextMessage(droneSession, new TextMessage("NOT_A_VALID_JSON{:::}}"))
        );
    }
}
