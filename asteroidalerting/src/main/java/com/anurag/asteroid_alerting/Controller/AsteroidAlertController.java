package com.anurag.asteroid_alerting.Controller;

import com.anurag.asteroid_alerting.DTO.AlertRequest;
import com.anurag.asteroid_alerting.Service.AsteroidAlertingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/asteroid-alerting")
public class AsteroidAlertController {

    private final AsteroidAlertingService asteroidAlertingService;

    @Autowired
    public AsteroidAlertController(AsteroidAlertingService asteroidAlertingService) {
        this.asteroidAlertingService = asteroidAlertingService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/alerts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void alert(@RequestBody AlertRequest request) {
        asteroidAlertingService.alert(request.getEmail());
    }
}
