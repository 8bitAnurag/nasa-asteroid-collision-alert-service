package com.anurag.notificationservice.Service;


import com.anurag.notificationservice.Repository.NotificationRepository;
import com.anurag.notificationservice.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.anurag.notificationservice.Entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmailService {
    @Value("${email.service.from.email}")
    private String fromEmail;

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(NotificationRepository notificationRepository,
                        UserRepository userRepository,
                        JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Async
    public void sendAsteroidAlertEmail() {
        final String text = createEmailText();

        if(text == null) {
            log.info("No asteroids to send email alerts for at {}", LocalDateTime.now());
            return;
        }
        final List<String> toEmails = userRepository.findAllEmailsAndNotificationEnabled();

        if(toEmails.isEmpty()){
            log.info("No users to send email alerts to");
            return;
        }
        toEmails.forEach(toEmail -> {
            try {
                sendEmail(toEmail, text);

                Thread.sleep(11000); // Wait 2 seconds between emails to fix Rate Limit error
            } catch (Exception e) {
                log.error("Failed to send email due to: ", e);
            }
        });
        log.info("Email sent to: #{} users", toEmails.size());


    }
    private void sendEmail(final String toEmail,final String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setText(text);
        message.setSubject("Nasa Asteroid Collision Alert");
        mailSender.send(message);
    }
        private String createEmailText(){
            // check if there are any asteroids to send alerts for
            List<Notification> notificationList = notificationRepository.findByEmailSent(false);

            if(notificationList.isEmpty()) {
                return null;
            }

            StringBuilder emailText = new StringBuilder();
            emailText.append("Asteroid Alert: \n");
            emailText.append("=====================================\n");

            notificationList.forEach(notification -> {
                emailText.append("Asteroid Name: ").append(notification.getAsteroidName()).append("\n");
                emailText.append("Close Approach Date: ").append(notification.getCloseApproachDate()).append("\n");
                emailText.append("Estimated Diameter Avg Meters: ").append(notification.getEstimatedDiameterAvgMeters()).append("\n");
                emailText.append("Miss Distance Kilometers: ").append(notification.getMissDistanceKilometers()).append("\n");
                emailText.append("=====================================\n");
                notification.setEmailSent(true);
                notificationRepository.save(notification);
            });

            return emailText.toString();
        }
    }

