package com.anurag.notificationservice.Controller;

import com.anurag.notificationservice.Event.AsteroidCollisionEvent;
import com.anurag.notificationservice.Service.EmailService;
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
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final EmailService emailService;

    @Autowired
    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@RequestBody AsteroidCollisionEvent event) {
        emailService.sendAsteroidAlertEmail(event);
    }
}
