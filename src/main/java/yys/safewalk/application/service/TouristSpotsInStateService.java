package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.in.dto.TouristSpotsInStateResponse;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsJPARepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TouristSpotsInStateService {

    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;

    public List<TouristSpotsInStateResponse> getTouristSpotsInState(String sidoCode, String mode) {

        return popularTouristSpotsJPARepository.findBySidoCodeAndMode(sidoCode, mode)
                .stream()
                .map(spot -> new TouristSpotsInStateResponse(
                        spot.getTouristSpotId(),
                        spot.getSidoCode(),
                        spot.getMode(),
                        spot.getRank(),
                        spot.getSidoName(),
                        spot.getSigunguName(),
                        spot.getSpotName(),
                        spot.getCategory(),
                        new Coordinate(spot.getLatitude() != null ? spot.getLatitude() : null,
                                spot.getLongitude() != null ? spot.getLongitude() : null
                                )

                ))
                .toList();
    }

}
