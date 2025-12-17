package com.anurag.asteroid_alerting.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record NasaNeoResponse(
        @JsonProperty("near_earth_objects")
        Map<String, List<Asteroid>> nearEarthObjects,

        @JsonProperty("elements_count")
        Long totalAsteroids
) {}