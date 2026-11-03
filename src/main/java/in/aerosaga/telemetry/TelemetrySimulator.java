package in.aerosaga.telemetry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class TelemetrySimulator {

    public static void main(String[] args) {

        HttpClient client = HttpClient.newHttpClient();

        client.newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:8081/telemetry"),
                        new WebSocket.Listener() {

                            @Override
                            public void onOpen(WebSocket webSocket) {

                                System.out.println(
                                        "Drone connected to Telemetry Gateway!"
                                );

                                String telemetryJson = """
                                        {
                                            "droneId": "DRONE-101",
                                            "latitude": 15.1394,
                                            "longitude": 76.9214,
                                            "altitude": 120.5,
                                            "batteryLevel": 85
                                        }
                                        """;

                                webSocket.sendText(
                                        telemetryJson,
                                        true
                                );

                                System.out.println(
                                        "Telemetry data sent successfully!"
                                );

                                webSocket.sendClose(
                                        WebSocket.NORMAL_CLOSURE,
                                        "Simulation completed"
                                );

                                webSocket.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                System.out.println(
                                        "Received: " + data
                                );

                                webSocket.request(1);
                                return null;
                            }

                            @Override
                            public void onError(
                                    WebSocket webSocket,
                                    Throwable error) {

                                System.err.println(
                                        "Error: " + error.getMessage()
                                );
                            }
                        }
                )
                .join();
    }
}