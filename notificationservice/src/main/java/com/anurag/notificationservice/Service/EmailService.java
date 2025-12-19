package com.anurag.notificationservice.Service;


import com.anurag.notificationservice.Event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    @Value("${email.service.from.email}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a single asteroid alert email to the recipient specified in the event.
     * This is now completely stateless – no database lookups.
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
            sendEmail(event.getRecipientEmail(), text);
            log.info("Asteroid alert email sent to {}", event.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send email due to: ", e);
        }
    }

    private void sendEmail(final String toEmail,final String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setText(text);
        message.setSubject("Nasa Asteroid Collision Alert");
        mailSender.send(message);
    }
}
