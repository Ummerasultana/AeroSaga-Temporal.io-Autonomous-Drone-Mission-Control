
package in.aerosaga.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "in.aerosaga")
public class TelemetryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelemetryApplication.class, args);
    }
}
