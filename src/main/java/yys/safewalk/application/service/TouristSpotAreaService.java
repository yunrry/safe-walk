package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.in.dto.TouristSpotResponse;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsJPARepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristSpotAreaService {

    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;

    public List<TouristSpotResponse> getTouristSpotsByArea(Coordinate swCoordinate, Coordinate neCoordinate) {
        validateCoordinates(swCoordinate, neCoordinate);

        return popularTouristSpotsJPARepository.findByLatitudeBetweenAndLongitudeBetween(
                        swCoordinate.latitude().doubleValue(),
                        neCoordinate.latitude().doubleValue(),
                        swCoordinate.longitude().doubleValue(),
                        neCoordinate.longitude().doubleValue()
                )
                .stream()
                .map(spot -> new TouristSpotResponse(
                        spot.getTouristSpotId(),
                        spot.getSpotName(),
                        spot.getSidoName(),
                        spot.getSigunguName(),
                        spot.getCategory(),
                        new Coordinate(
                        spot.getLatitude(),
                        spot.getLongitude()
                        )
                ))
                .toList();
    }

    private void validateCoordinates(Coordinate swCoordinate, Coordinate neCoordinate) {
        if (swCoordinate.latitude().compareTo(neCoordinate.latitude()) >= 0 ||
                swCoordinate.longitude().compareTo(neCoordinate.longitude()) >= 0) {
            throw new IllegalArgumentException("남서쪽 좌표는 북동쪽 좌표보다 작아야 합니다");
        }
    }
}