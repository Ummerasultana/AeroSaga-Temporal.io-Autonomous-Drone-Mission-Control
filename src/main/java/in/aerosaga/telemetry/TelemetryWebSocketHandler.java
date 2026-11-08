package in.aerosaga.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<WebSocketSession> dashboardSessions =
            ConcurrentHashMap.newKeySet();

    private final Set<WebSocketSession> droneSessions =
            ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        System.out.println(
                "WebSocket URI: " + session.getUri()
        );

        String query = session.getUri().getQuery();

        if ("drone".equalsIgnoreCase(query)) {

            droneSessions.add(session);

            System.out.println(
                    "DRONE SESSION ADDED: " + session.getId()
            );

        } else {

            dashboardSessions.add(session);

            System.out.println(
                    "DASHBOARD SESSION ADDED: " + session.getId()
            );
        }

        System.out.println(
                "Current drone sessions: " +
                        droneSessions.size()
        );

        System.out.println(
                "Current dashboard sessions: " +
                        dashboardSessions.size()
        );
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        DroneTelemetry telemetry =
                objectMapper.readValue(
                        message.getPayload(),
                        DroneTelemetry.class
                );

        System.out.println(
                "Telemetry received from " +
                        telemetry.getDroneId()
        );

        broadcastToDashboards(telemetry);
    }

    private void broadcastToDashboards(
            DroneTelemetry telemetry) throws Exception {

        String json =
                objectMapper.writeValueAsString(telemetry);

        TextMessage message =
                new TextMessage(json);

        System.out.println(
                "Broadcasting telemetry to " +
                        dashboardSessions.size() +
                        " dashboard(s)"
        );

        for (WebSocketSession session :
                dashboardSessions) {

            if (session.isOpen()) {

                synchronized (session) {

                    session.sendMessage(message);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            org.springframework.web.socket.CloseStatus status) {

        dashboardSessions.remove(session);
        droneSessions.remove(session);

        System.out.println(
                "Connection closed: " +
                        session.getId()
        );

        System.out.println(
                "Current drone sessions: " +
                        droneSessions.size()
        );

        System.out.println(
                "Current dashboard sessions: " +
                        dashboardSessions.size()
        );
    }
}