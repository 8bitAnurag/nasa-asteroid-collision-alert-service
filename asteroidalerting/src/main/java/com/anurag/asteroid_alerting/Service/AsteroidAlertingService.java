package com.anurag.asteroid_alerting.Service;

import java.time.LocalDate;
import java.util.List;

import com.anurag.asteroid_alerting.Client.NasaClient;
import com.anurag.asteroid_alerting.DTO.Asteroid;
import com.anurag.asteroid_alerting.Event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsteroidAlertingService {
	
	private final NasaClient nasaClient;
	private final KafkaTemplate<String , AsteroidCollisionEvent> kafkaTemplate;
	
	@Autowired
	public AsteroidAlertingService(NasaClient nasaClient,
			KafkaTemplate<String , AsteroidCollisionEvent> kafkaTemplate){
		this.nasaClient = nasaClient;
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void alert(String recipientEmail){
		log.info("Alerting service called for recipientEmail={}", recipientEmail);
		
		final LocalDate fromDate =  LocalDate.now();
		final LocalDate toDate = fromDate.plusDays(7);
		
		log.info("Getting asteroid alerts from {} to {}", fromDate, toDate);
		final List<Asteroid> asteroidList = nasaClient.getNeoAsteroids(fromDate,toDate);
		log.info("Retrieved Asteroid list of Size: {}", asteroidList.size());
		
		final List<Asteroid> dangerousAsteroids = asteroidList.stream()
				.filter(Asteroid::isPotentiallyHazardous)
				.toList();
		log.info("Found {} hazardous asteroids", dangerousAsteroids.size() );
		
		if (dangerousAsteroids.isEmpty()) {
			log.info("No hazardous asteroids found, not sending any email");
			return;
		}
		
		// Build ONE email body with all hazardous asteroids
		StringBuilder body = new StringBuilder();
		body.append("Asteroid Alert:\n");
		body.append("=====================================\n");
		dangerousAsteroids.forEach(asteroid -> {
			body.append("Asteroid Name: ").append(asteroid.name()).append("\n");
			body.append("Close Approach Date: ")
					.append(asteroid.closeApproachData().getFirst().closeApproachDate()).append("\n");
			body.append("Estimated Diameter Avg Meters: ")
					.append((asteroid.estimatedDiameter().meters().minDiameter()
							+ asteroid.estimatedDiameter().meters().maxDiameter()) / 2).append("\n");
			body.append("Miss Distance Kilometers: ")
					.append(asteroid.closeApproachData().getFirst().missDistance().kilometers()).append("\n");
			body.append("=====================================\n");
		});
		
		AsteroidCollisionEvent event = AsteroidCollisionEvent.builder()
				.recipientEmail(recipientEmail)
				.emailBody(body.toString())
				.build();
		
		kafkaTemplate.send("asteroid-alert", event);
		log.info("Asteroid Alert Sent to kafka topic for {} hazardous asteroids", dangerousAsteroids.size());
	}
}
