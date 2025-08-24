package yys.safewalk.application.port.in;

import yys.safewalk.domain.model.TouristSpotAccidentResponse;

public interface GetTouristSpotAccidentsQuery {
    TouristSpotAccidentResponse getAccidentsInRadius(String spotId, Integer radiusKm);
}
