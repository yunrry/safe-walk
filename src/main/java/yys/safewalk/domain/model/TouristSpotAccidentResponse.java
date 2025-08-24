package yys.safewalk.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TouristSpotAccidentResponse(
    @JsonProperty("name") String name,
    @JsonProperty("spotId") String spotId,
    @JsonProperty("totalAccident") Integer totalAccident,
    @JsonProperty("accidents") List<AccidentDetail> accidents
) {
    public TouristSpotAccidentResponse {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("관광지명은 필수입니다");
        }
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("관광지 ID는 필수입니다");
        }
    }
}
