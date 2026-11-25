package com.anurag.notificationservice.Service;

import com.anurag.notificationservice.Event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {
    @Value("${email.service.from.email}")
    private String fromEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an asteroid alert email via Brevo HTTPS API (when configured for cloud hosts
     * like Render Free Tier that block outbound SMTP ports) or falls back to SMTP.
     */
    public void sendAsteroidAlertEmail(final AsteroidCollisionEvent event) {
        if (event.getRecipientEmail() == null || event.getRecipientEmail().isBlank()) {
            log.warn("Recipient email is missing in event: {}", event);
            return;
        }

        final String text = event.getEmailBody();
        if (text == null || text.isBlank()) {
            log.warn("Email body is empty for event: {}", event);
            return;
        }

        try {
            if (brevoApiKey != null && !brevoApiKey.isBlank()) {
                sendViaBrevo(event.getRecipientEmail().trim(), text);
                log.info("Asteroid alert email sent via Brevo HTTPS API to {}", event.getRecipientEmail());
            } else {
                sendViaSmtp(event.getRecipientEmail().trim(), text);
                log.info("Asteroid alert email sent via SMTP to {}", event.getRecipientEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", event.getRecipientEmail(), e.getMessage(), e);
            throw new IllegalStateException("Email delivery failed: " + e.getMessage(), e);
        }
    }

    private void sendViaBrevo(final String toEmail, final String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey.trim());

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", "NASA Asteroid Alerts", "email", fromEmail.trim()),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "NASA Asteroid Collision Alert",
                "textContent", text
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
    }

    private void sendViaSmtp(final String toEmail, final String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setText(text);
        message.setSubject("Nasa Asteroid Collision Alert");
        mailSender.send(message);
    }
}
