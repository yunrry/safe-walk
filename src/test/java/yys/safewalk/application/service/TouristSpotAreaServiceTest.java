package yys.safewalk.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yys.safewalk.application.port.in.dto.TouristSpotAreaRequest;
import yys.safewalk.application.port.in.dto.TouristSpotResponse;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.entity.PopularTouristSpots;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TouristSpotAreaServiceTest {

    @Mock
    private PopularTouristSpotsRepository popularTouristSpotsRepository;

    @InjectMocks
    private TouristSpotAreaService touristSpotAreaService;

    @Test
    @DisplayName("지도 영역 내 관광지 조회 성공")
    void getTouristSpotsByArea_Success() {
        // Given
        Coordinate swCoordinate = new Coordinate(BigDecimal.valueOf(35.8242), BigDecimal.valueOf(129.2070));
        Coordinate neCoordinate = new Coordinate(BigDecimal.valueOf(35.8442), BigDecimal.valueOf(129.2270));

        PopularTouristSpots spot = PopularTouristSpots.builder()
                .spotName("동궁과월지")
                .sidoName("경상북도")
                .sigunguName("경주시")
                .latitude(BigDecimal.valueOf(35.8344))
                .longitude(BigDecimal.valueOf(129.2233))
                .build();

        when(popularTouristSpotsRepository.findByLatitudeBetweenAndLongitudeBetween(
                any(Double.class), any(Double.class), any(Double.class), any(Double.class)))
                .thenReturn(List.of(spot));

        // When
        List<TouristSpotResponse> result = touristSpotAreaService.getTouristSpotsByArea(swCoordinate, neCoordinate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).spotName()).isEqualTo("동궁과월지");
    }


}