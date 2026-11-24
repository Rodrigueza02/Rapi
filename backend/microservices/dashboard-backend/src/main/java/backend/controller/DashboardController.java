
package backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    @GetMapping("/api/dashboard/status")
    public Map<String, String> status() {

        return Map.of(
                "service", "dashboard-backend",
                "status", "running"
        );
    }
}
