package in.aerosaga.telemetry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class TelemetryTestClient {

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        client.newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:8081/telemetry"),
                        new WebSocket.Listener() {

                            @Override
                            public void onOpen(WebSocket webSocket) {
                                System.out.println(
                                        "Connected to AeroSaga Telemetry Gateway!"
                                );

                                webSocket.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                System.out.println(
                                        "Received telemetry: " + data
                                );

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
                            }
                        })
                .join();

        System.out.println("Dashboard listener is running.");

        // Keep the dashboard connection alive.
        Thread.currentThread().join();
    }
}