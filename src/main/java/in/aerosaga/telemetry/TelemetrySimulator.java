
package in.aerosaga.telemetry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class TelemetrySimulator {

    private static volatile boolean running = true;

    public static void main(String[] args) {

        HttpClient client = HttpClient.newHttpClient();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:8081/telemetry"),
                        new WebSocket.Listener() {

                            @Override
                            public void onOpen(WebSocket webSocket) {
                                System.out.println(
                                        "Drone connected to Telemetry Gateway!"
                                );

                                webSocket.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                System.out.println("Received: " + data);
                                webSocket.request(1);
                                return null;
                            }

                            @Override
                            public void onError(
                                    WebSocket webSocket,
                                    Throwable error) {

                                System.err.println(
                                        "WebSocket error: " + error.getMessage()
                                );

                                running = false;
                            }
                        })
                .join();

        double latitude = 15.1394;
        double longitude = 76.9214;
        double altitude = 120.5;
        double battery = 100.0;

        System.out.println("Starting continuous drone simulation...");

        try {
            while (running && battery > 0) {

                // Simulate small GPS movements
                latitude += ThreadLocalRandom.current()
                        .nextDouble(-0.0001, 0.0001);

                longitude += ThreadLocalRandom.current()
                        .nextDouble(-0.0001, 0.0001);

                // Simulate altitude changes
                altitude += ThreadLocalRandom.current()
                        .nextDouble(-2.0, 2.0);

                altitude = Math.max(0, altitude);

                // Simulate battery consumption
                battery = Math.max(0, battery - 0.5);

                String telemetryJson = String.format(
                        java.util.Locale.US,
                        """
                        {
                            "droneId": "DRONE-101",
                            "latitude": %.6f,
                            "longitude": %.6f,
                            "altitude": %.2f,
                            "batteryLevel": %.2f,
                            "timestamp": %d
                        }
                        """,
                        latitude,
                        longitude,
                        altitude,
                        battery,
                        System.currentTimeMillis()
                );

                webSocket.sendText(telemetryJson, true).join();

                System.out.println(
                        "Telemetry sent: " + telemetryJson
                );

                Thread.sleep(2000);
            }

            System.out.println("Drone simulation completed.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Drone simulation interrupted.");

        } finally {
            webSocket.sendClose(
                    WebSocket.NORMAL_CLOSURE,
                    "Simulation stopped"
            ).join();
        }
    }
}
