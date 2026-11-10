package in.aerosaga.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.aerosaga.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TelemetryService telemetryService;

    private final Set<WebSocketSession> dashboardSessions = ConcurrentHashMap.newKeySet();
    private final Set<WebSocketSession> droneSessions = ConcurrentHashMap.newKeySet();

    public TelemetryWebSocketHandler(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        logger.info("WebSocket connected: {} (Session ID: {})", path, session.getId());

        if (path.endsWith("/dashboard")) {
            dashboardSessions.add(session);
            logger.info("DASHBOARD CONNECTED: {}. Total active dashboard sessions: {}", session.getId(), dashboardSessions.size());

            // Send current known telemetry immediately upon connection
            sendCurrentStateToDashboard(session);
        } else if (path.endsWith("/drone")) {
            droneSessions.add(session);
            logger.info("DRONE CONNECTED: {}. Total drone sessions: {}", session.getId(), droneSessions.size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            DroneTelemetry telemetry = objectMapper.readValue(message.getPayload(), DroneTelemetry.class);
            if (telemetry.getDroneId() == null || telemetry.getDroneId().isBlank()) {
                logger.warn("Received invalid telemetry without droneId: {}", message.getPayload());
                return;
            }

            // Update cached telemetry in service
            telemetryService.updateTelemetry(telemetry);

            // Broadcast to connected dashboards
            broadcastToDashboards(telemetry);
        } catch (Exception e) {
            logger.warn("Failed to parse telemetry message: {}. Reason: {}", message.getPayload(), e.getMessage());
        }
    }

    private void sendCurrentStateToDashboard(WebSocketSession session) {
        try {
            Map<String, DroneTelemetry> all = telemetryService.getAllLatestTelemetry();
            for (DroneTelemetry telemetry : all.values()) {
                if (session.isOpen()) {
                    String json = objectMapper.writeValueAsString(telemetry);
                    synchronized (session) {
                        session.sendMessage(new TextMessage(json));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error sending initial telemetry state to session {}: {}", session.getId(), e.getMessage());
        }
    }

    private void broadcastToDashboards(DroneTelemetry telemetry) {
        try {
            String json = objectMapper.writeValueAsString(telemetry);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession dashboard : dashboardSessions) {
                if (dashboard.isOpen()) {
                    try {
                        synchronized (dashboard) {
                            dashboard.sendMessage(textMessage);
                        }
                    } catch (IOException e) {
                        logger.warn("Failed to send telemetry to dashboard session {}: {}", dashboard.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error formatting telemetry for broadcast", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.warn("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception ignored) {
        }
        dashboardSessions.remove(session);
        droneSessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        dashboardSessions.remove(session);
        droneSessions.remove(session);
        logger.info("Connection closed: {} (Status: {}). Remaining dashboards: {}",
                session.getId(), status, dashboardSessions.size());
    }

    public int getDashboardSessionCount() {
        return dashboardSessions.size();
    }

    public int getDroneSessionCount() {
        return droneSessions.size();
    }
}
