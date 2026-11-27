package com.anurag.asteroid_alerting.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.anurag.asteroid_alerting.Client.NasaClient;
import com.anurag.asteroid_alerting.DTO.Asteroid;
import com.anurag.asteroid_alerting.Event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AsteroidAlertingService {

	private final NasaClient nasaClient;
	private final ObjectProvider<KafkaTemplate<String, AsteroidCollisionEvent>> kafkaTemplateProvider;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${alert.delivery.mode:kafka}")
	private String deliveryMode;

	@Value("${notification.service.url:http://localhost:8081}")
	private String notificationServiceUrl;

	@Autowired
	public AsteroidAlertingService(
			NasaClient nasaClient,
			ObjectProvider<KafkaTemplate<String, AsteroidCollisionEvent>> kafkaTemplateProvider) {
		this.nasaClient = nasaClient;
		this.kafkaTemplateProvider = kafkaTemplateProvider;
	}

	private String normalizedNotificationBaseUrl() {
		String baseUrl = notificationServiceUrl.trim().replaceAll("/+$", "");
		if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
			baseUrl = "http://" + baseUrl;
		}
		return baseUrl;
	}

	/**
	 * When running in HTTP cloud mode (Render free tier), proactively wake
	 * notificationservice in the background as soon as this service starts.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void warmNotificationService() {
		if (!"http".equalsIgnoreCase(deliveryMode)) {
			return;
		}
		CompletableFuture.runAsync(() -> {
			try {
				String healthUrl = normalizedNotificationBaseUrl() + "/api/v1/notifications/health";
				log.info("Proactively waking notificationservice at {}", healthUrl);
				restTemplate.getForEntity(healthUrl, String.class);
			} catch (Exception e) {
				log.debug("Background notificationservice wakeup ping completed with: {}", e.getMessage());
			}
		});
	}

	public void alert(String recipientEmail) {
		log.info("Alerting service called for recipientEmail={} (mode={})", recipientEmail, deliveryMode);

		// Kick off a wakeup ping to notificationservice while fetching NASA NeoWs data
		if ("http".equalsIgnoreCase(deliveryMode)) {
			warmNotificationService();
		}

		final LocalDate fromDate = LocalDate.now();
		final LocalDate toDate = fromDate.plusDays(7);

		log.info("Getting asteroid alerts from {} to {}", fromDate, toDate);
		final List<Asteroid> asteroidList = nasaClient.getNeoAsteroids(fromDate, toDate);
		log.info("Retrieved Asteroid list of Size: {}", asteroidList.size());

		final List<Asteroid> dangerousAsteroids = asteroidList.stream()
				.filter(Asteroid::isPotentiallyHazardous)
				.toList();
		log.info("Found {} hazardous asteroids", dangerousAsteroids.size());

		StringBuilder body = new StringBuilder();
		if (dangerousAsteroids.isEmpty()) {
			body.append("NASA Asteroid Report (").append(fromDate).append(" to ").append(toDate).append(")\n");
			body.append("=====================================\n");
			body.append("Good news! Out of ").append(asteroidList.size())
					.append(" tracked Near-Earth Objects in the next 7 days, 0 are classified as potentially hazardous.\n");
			body.append("=====================================\n");
		} else {
			body.append("Asteroid Alert (").append(dangerousAsteroids.size()).append(" potentially hazardous NEOs):\n");
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
		}

		AsteroidCollisionEvent event = AsteroidCollisionEvent.builder()
				.recipientEmail(recipientEmail)
				.emailBody(body.toString())
				.build();

		if ("http".equalsIgnoreCase(deliveryMode)) {
			dispatchWithRetry(event);
			return;
		}

		KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
		if (kafkaTemplate == null) {
			throw new IllegalStateException("KafkaTemplate is unavailable while alert.delivery.mode=kafka");
		}
		kafkaTemplate.send("asteroid-alert", event);
		log.info("Asteroid Alert Sent to kafka topic for {} hazardous asteroids", dangerousAsteroids.size());
	}

	private void dispatchWithRetry(AsteroidCollisionEvent event) {
		String endpoint = normalizedNotificationBaseUrl() + "/api/v1/notifications/send";
		int maxAttempts = 3;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				restTemplate.postForLocation(endpoint, event);
				log.info("Asteroid Alert dispatched via HTTP to {} (attempt {})", endpoint, attempt);
				return;
			} catch (HttpServerErrorException | ResourceAccessException ex) {
				int code = (ex instanceof HttpServerErrorException hse) ? hse.getStatusCode().value() : 503;
				boolean retryable = (code == 502 || code == 503 || code == 504);
				if (!retryable || attempt == maxAttempts) {
					throw ex;
				}
				log.warn("Notification service returned HTTP {} (likely waking from sleep); retrying ({}/{})...",
						code, attempt, maxAttempts);
				try {
					Thread.sleep(4000L * attempt);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw ex;
				}
			}
		}
	}
}
