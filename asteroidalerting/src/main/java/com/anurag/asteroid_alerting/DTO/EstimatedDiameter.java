package com.anurag.asteroid_alerting.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstimatedDiameter(
        @JsonProperty("meters")
        DiameterRange meters
) {
   public record DiameterRange(
            @JsonProperty("estimated_diameter_min")
            Double minDiameter,
            @JsonProperty("estimated_diameter_max")
            Double maxDiameter
    ) {}
}