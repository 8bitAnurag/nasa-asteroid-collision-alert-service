package com.anurag.asteroid_alerting.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CloseApproachData(

        @JsonProperty("close_approach_date_full")
        String closeApproachDate,

        @JsonProperty("relative_velocity")
        RelativeVelocity relativeVelocity,

        @JsonProperty("miss_distance")
        MissDistance missDistance
) {
    public record MissDistance(
            String kilometers
    ) {}

    public record RelativeVelocity(
            @JsonProperty("kilometers_per_hour")
            String velocity
    ) {}
}
