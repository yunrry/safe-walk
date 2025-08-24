package yys.safewalk.application.port.out;

import yys.safewalk.domain.model.PopularTouristSpots;

import java.util.Optional;

public interface LoadPopularTouristSpotPort {
    Optional<PopularTouristSpots> loadById(String touristSpotId);
}
