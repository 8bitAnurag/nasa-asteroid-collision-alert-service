package com.anurag.asteroid_alerting.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Asteroid(
         String id,
         String name,

        @JsonProperty("estimated_diameter")
        EstimatedDiameter estimatedDiameter,

        @JsonProperty("is_potentially_hazardous_asteroid")
        boolean isPotentiallyHazardous,

        @JsonProperty("close_approach_data")
         List<CloseApproachData> closeApproachData

) {}
