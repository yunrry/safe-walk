package yys.safewalk.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import yys.safewalk.domain.model.Coordinate;

import java.math.BigDecimal;

public record TouristSpotsInStateResponse(

        @JsonProperty("id")
        String touristSpotId,
        @JsonProperty("sido_code")
        String sidoCode,
        @JsonProperty("mode")
        String mode,
        @JsonProperty("sido_name")
        String sidoName,
        @JsonProperty("sigungu_name")
        String sigunguName,
        @JsonProperty("spot_name")
        String spotName,
        @JsonProperty("category")
        String category,
        @JsonProperty("coordinate")
        Coordinate coordinate

) {
    public TouristSpotsInStateResponse {


    }

}
