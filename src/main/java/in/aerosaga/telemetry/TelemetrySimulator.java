package in.aerosaga.telemetry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class TelemetrySimulator {

    public static void main(String[] args) {

        simulateDrone(
                "DRONE-101",
                15.1394,
                76.9214,
                120.0,
                100.0
        );

        simulateDrone(
                "DRONE-102",
                15.1400,
                76.9220,
                110.0,
                100.0
        );

        simulateDrone(
                "DRONE-103",
                15.1385,
                76.9205,
                130.0,
                100.0
        );

        System.out.println("All drone simulations started.");
    }

    private static void simulateDrone(
            String droneId,
            double initialLatitude,
            double initialLongitude,
            double initialAltitude,
            double initialBattery) {

        Thread droneThread = new Thread(() -> {

            HttpClient client = HttpClient.newHttpClient();

            WebSocket webSocket = client.newWebSocketBuilder()
                    .buildAsync(
                            URI.create("ws://localhost:8081/telemetry/drone"),
                            new WebSocket.Listener() {

                                @Override
                                public void onOpen(WebSocket webSocket) {

                                    System.out.println(
                                            droneId +
                                                    " connected to Telemetry Gateway!"
                                    );

                                    webSocket.request(1);
                                }

                                @Override
                                public CompletionStage<?> onText(
                                        WebSocket webSocket,
                                        CharSequence data,
                                        boolean last) {

                                    System.out.println(
                                            droneId +
                                                    " received: " +
                                                    data
                                    );

                                    webSocket.request(1);

                                    return null;
                                }

                                @Override
                                public void onError(
                                        WebSocket webSocket,
                                        Throwable error) {

                                    System.err.println(
                                            droneId +
                                                    " WebSocket error: " +
                                                    error.getMessage()
                                    );
                                }
                            }
                    )
                    .join();

            double latitude = initialLatitude;
            double longitude = initialLongitude;
            double altitude = initialAltitude;
            double battery = initialBattery;

            System.out.println(
                    "Starting simulation for " + droneId
            );

            try {

                while (battery > 0) {

                    latitude += ThreadLocalRandom.current()
                            .nextDouble(-0.0001, 0.0001);

                    longitude += ThreadLocalRandom.current()
                            .nextDouble(-0.0001, 0.0001);

                    altitude += ThreadLocalRandom.current()
                            .nextDouble(-2.0, 2.0);

                    altitude = Math.max(0, altitude);

                    battery = Math.max(
                            0,
                            battery - 0.5
                    );

                    String telemetryJson = String.format(
                            java.util.Locale.US,
                            """
                            {
                                "droneId": "%s",
                                "latitude": %.6f,
                                "longitude": %.6f,
                                "altitude": %.2f,
                                "batteryLevel": %.2f,
                                "timestamp": %d
                            }
                            """,
                            droneId,
                            latitude,
                            longitude,
                            altitude,
                            battery,
                            System.currentTimeMillis()
                    );

                    webSocket
                            .sendText(telemetryJson, true)
                            .join();

                    System.out.println(
                            droneId +
                                    " telemetry sent: " +
                                    telemetryJson
                    );

                    Thread.sleep(2000);
                }

                System.out.println(
                        droneId +
                                " simulation completed."
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.out.println(
                        droneId +
                                " simulation interrupted."
                );

            } finally {

                webSocket
                        .sendClose(
                                WebSocket.NORMAL_CLOSURE,
                                "Simulation stopped"
                        )
                        .join();
            }

        });

        droneThread.start();
    }
}