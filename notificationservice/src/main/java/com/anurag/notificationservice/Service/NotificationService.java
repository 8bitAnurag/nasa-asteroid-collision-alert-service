package com.anurag.notificationservice.Service;

import com.anurag.notificationservice.Event.AsteroidCollisionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "alert.delivery.mode", havingValue = "kafka", matchIfMissing = true)
public class NotificationService {

    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "asteroid-alert", groupId = "notification-service")
    public void alertEvent(String eventMessage) {
        try {
            AsteroidCollisionEvent notificationEvent =
                    objectMapper.readValue(eventMessage, AsteroidCollisionEvent.class);
            log.info("Notification Event Received: {}", notificationEvent);

            emailService.sendAsteroidAlertEmail(notificationEvent);

        } catch (JsonProcessingException e) {
            log.error("Error converting message to object", e);
        }
    }
}
