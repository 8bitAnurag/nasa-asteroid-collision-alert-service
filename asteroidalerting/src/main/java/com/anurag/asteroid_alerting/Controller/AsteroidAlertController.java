package com.anurag.asteroid_alerting.Controller;


import com.anurag.asteroid_alerting.Service.AsteroidAlertingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asteroid-alerting")
public class AsteroidAlertController {

    private final AsteroidAlertingService asteroidAlertingService;

    @Autowired
    public AsteroidAlertController(AsteroidAlertingService asteroidAlertingService) {
        this.asteroidAlertingService = asteroidAlertingService;
    }

    @PostMapping("/alerts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void alert(){
        asteroidAlertingService.alert();
    }
}
