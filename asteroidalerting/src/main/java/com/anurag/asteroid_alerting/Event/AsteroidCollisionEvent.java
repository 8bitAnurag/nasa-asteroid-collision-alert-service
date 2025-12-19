package com.anurag.asteroid_alerting.Event;

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
	private String emailBody;
}
