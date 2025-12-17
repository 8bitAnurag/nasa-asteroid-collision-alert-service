package com.anurag.asteroid_alerting.Client;

import com.anurag.asteroid_alerting.DTO.Asteroid;
import com.anurag.asteroid_alerting.DTO.NasaNeoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class NasaClient {

    @Value("${nasa.neo.api.url}")
    private String nasaNeoApiUrl;

    @Value("${nasa.api.key}")
    private String nasaApiKey;

    public List<Asteroid> getNeoAsteroids(final LocalDate fromDate, final LocalDate toDate) {
        final RestTemplate restTemplate = new RestTemplate();

        final NasaNeoResponse nasaNeoResponse =
                restTemplate.getForObject(getUrl(fromDate, toDate), NasaNeoResponse.class);

        final List<Asteroid> asteroidList = new ArrayList<>();
        if (nasaNeoResponse != null) {
            asteroidList.addAll(nasaNeoResponse.nearEarthObjects().values().stream().flatMap(List::stream).toList());
        }
        return asteroidList;
    }

        public String getUrl(final LocalDate fromDate, final LocalDate toDate){
            return UriComponentsBuilder.fromUriString(nasaNeoApiUrl)
                    .queryParam("start_date", fromDate)
                    .queryParam("end_date", toDate)
                    .queryParam("api_key" , nasaApiKey)
                    .toUriString();
        }
    }
