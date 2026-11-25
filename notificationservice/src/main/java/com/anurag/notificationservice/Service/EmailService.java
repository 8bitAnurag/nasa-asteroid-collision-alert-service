package com.anurag.notificationservice.Service;

import com.anurag.notificationservice.Event.AsteroidCollisionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {
    @Value("${email.service.from.email}")
    private String fromEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${email.webhook.url:}")
    private String emailWebhookUrl;

    @Value("${email.webhook.secret:}")
    private String emailWebhookSecret;

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an asteroid alert email via:
     * 1. EMAIL_WEBHOOK_URL (e.g., Google Apps Script Web App over HTTPS — no domain needed),
     * 2. BREVO_API_KEY (HTTPS API when an authenticated domain is configured), or
     * 3. Standard SMTP fallback for local development.
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

        final String toEmail = event.getRecipientEmail().trim();

        try {
            if (emailWebhookUrl != null && !emailWebhookUrl.isBlank()) {
                sendViaWebhook(toEmail, text);
                log.info("Asteroid alert email sent via HTTPS webhook to {}", toEmail);
            } else if (brevoApiKey != null && !brevoApiKey.isBlank()) {
                sendViaBrevo(toEmail, text);
                log.info("Asteroid alert email sent via Brevo HTTPS API to {}", toEmail);
            } else {
                sendViaSmtp(toEmail, text);
                log.info("Asteroid alert email sent via SMTP to {}", toEmail);
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new IllegalStateException("Email delivery failed: " + e.getMessage(), e);
        }
    }

    private void sendViaWebhook(final String toEmail, final String text) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", toEmail);
        payload.put("subject", "NASA Asteroid Collision Alert");
        payload.put("body", text);
        if (emailWebhookSecret != null && !emailWebhookSecret.isBlank()) {
            payload.put("secret", emailWebhookSecret.trim());
        }

        String json = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(emailWebhookUrl.trim()))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        // Google Apps Script executes doPost and responds with 302 redirect (or 200)
        if (status >= 400) {
            throw new IllegalStateException("Webhook returned HTTP " + status + ": " + response.body());
        }
        if (status == 302) {
            String location = response.headers().firstValue("location").orElse("");
            if (!location.isBlank()) {
                HttpRequest getResult = HttpRequest.newBuilder()
                        .uri(URI.create(location))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();
                HttpResponse<String> finalResp = httpClient.send(getResult, HttpResponse.BodyHandlers.ofString());
                if (finalResp.statusCode() >= 400 || finalResp.body().contains("\"ok\":false")) {
                    throw new IllegalStateException("Webhook failed: " + finalResp.body());
                }
            }
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
