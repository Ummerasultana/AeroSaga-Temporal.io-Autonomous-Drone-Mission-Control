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

    // Stores dashboard connections
    private final Set<WebSocketSession> dashboardSessions =
            ConcurrentHashMap.newKeySet();

    // Stores drone connections
    private final Set<WebSocketSession> droneSessions =
            ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        String clientType =
                session.getUri()
                        .getQuery();

        if ("drone".equalsIgnoreCase(clientType)) {

            droneSessions.add(session);

            System.out.println(
                    "Drone connected: " +
                            session.getId()
            );

        } else {

            dashboardSessions.add(session);

            System.out.println(
                    "Dashboard connected: " +
                            session.getId()
            );
        }
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

        // Send telemetry ONLY to dashboards
        broadcastToDashboards(telemetry);
    }

    private void broadcastToDashboards(
            DroneTelemetry telemetry) throws Exception {

        String json =
                objectMapper.writeValueAsString(
                        telemetry
                );

        TextMessage message =
                new TextMessage(json);

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
    }
}