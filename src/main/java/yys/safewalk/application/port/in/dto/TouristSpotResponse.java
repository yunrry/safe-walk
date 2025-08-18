package yys.safewalk.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TouristSpotResponse(
        @JsonProperty("spot_name")
        String spotName,

        @JsonProperty("sido_name")
        String sidoName,

        @JsonProperty("sigungu_name")
        String sigunguName,

        Double latitude,

        Double longitude
) {}