package com.anurag.notificationservice.Service;

import com.anurag.notificationservice.Entity.Notification;
import com.anurag.notificationservice.Event.AsteroidCollisionEvent;
import com.anurag.notificationservice.Repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @KafkaListener(topics = "asteroid-alert", groupId = "notification-service")
    public void alertEvent(String eventMessage) {
        try {
            AsteroidCollisionEvent notificationEvent = objectMapper.readValue(eventMessage, AsteroidCollisionEvent.class);
            log.info("Notification Event Received: {}", notificationEvent);

            final Notification notification = Notification.builder()
                    .asteroidName(notificationEvent.getAsteroidName())
                    .closeApproachDate(notificationEvent.getCloseApproachDate())
                    .estimatedDiameterAvgMeters(notificationEvent.getEstimatedDiameterAvgMeters())
                    .missDistanceKilometers(new BigDecimal(notificationEvent.getMissDistanceKilometers()))
                    .emailSent(false)
                    .build();

            final Notification savedNotification = notificationRepository.saveAndFlush(notification);
            log.info("Notification Saved: {}", savedNotification);

        } catch (JsonProcessingException e) {
            log.error("Error converting message to object", e);
        }
    }

    @Scheduled(fixedRate = 11000)
    public void sendAlertingEmail() {
        log.info("Triggering schedules job to send email alerts");
        emailService.sendAsteroidAlertEmail();
    }
}