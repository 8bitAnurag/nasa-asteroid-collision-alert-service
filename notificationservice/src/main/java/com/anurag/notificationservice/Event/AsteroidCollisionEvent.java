package com.anurag.notificationservice.Event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsteroidCollisionEvent {
    private String recipientEmail;
    /**
     * Full email body containing details for all hazardous asteroids.
     * This is prepared by the asteroid-alerting service so we only send one email.
     */
    private String emailBody;
}
