package com.anurag.asteroid_alerting.Service;


import com.anurag.asteroid_alerting.Client.NasaClient;
import com.anurag.asteroid_alerting.DTO.Asteroid;
import com.anurag.asteroid_alerting.Event.AsteroidCollisionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AsteroidAlertingService {

    private final NasaClient nasaClient;
    private final KafkaTemplate<String , AsteroidCollisionEvent> kafkaTemplate;

    @Autowired
    public AsteroidAlertingService(NasaClient nasaClient, KafkaTemplate<String , AsteroidCollisionEvent> kafkaTemplate){
        this.nasaClient = nasaClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alert(){
        log.info("Alerting service called");
        final LocalDate fromDate =  LocalDate.now();
        final LocalDate toDate = fromDate.plusDays(7);

        log.info("Getting asteroid alerts from {} to {}", fromDate, toDate);
       final List<Asteroid> asteroidList = nasaClient.getNeoAsteroids(fromDate,toDate);
       log.info("Retrieved Asteroid list of Size: {}", asteroidList.size());

       final List<Asteroid> dangerousAsteroids = asteroidList.stream()
               .filter(Asteroid::isPotentiallyHazardous)
               .toList();
       log.info("Found {} hazardous asteroids", dangerousAsteroids.size() );


        final List<AsteroidCollisionEvent> asteroidCollisionEventList =
                createEventListOfDangerousAsteroids(dangerousAsteroids);

        log.info("Sending {} asteroids alerts to Kafka", asteroidCollisionEventList.size());
        asteroidCollisionEventList.forEach(event ->{
            kafkaTemplate.send("asteroid-alert", event);
            log.info("Asteroid Alert Sent to kafka topic: {}", event);
        });
    }

    public List<AsteroidCollisionEvent> createEventListOfDangerousAsteroids(List<Asteroid> dangerousAsteroids){
        return dangerousAsteroids.stream()
                .map(asteroid ->{
                    if (asteroid.isPotentiallyHazardous()) {
                        return AsteroidCollisionEvent.builder()
                                .asteroidName(asteroid.name())
                                .closeApproachDate(asteroid.closeApproachData().getFirst().closeApproachDate().toString())
                                .missDistanceKilometers(asteroid.closeApproachData().getFirst().missDistance().kilometers())
                                .estimatedDiameterAvgMeters((asteroid.estimatedDiameter().meters().minDiameter() +
                                        asteroid.estimatedDiameter().meters().maxDiameter()) / 2)
                                .build();
                    }
                    return null;
                })
                .toList();
    }



}
