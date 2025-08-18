package yys.safewalk.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yys.safewalk.application.port.in.GetEmdInBoundsQuery;
import yys.safewalk.application.port.out.EmdRepository;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.domain.model.Emd;
import yys.safewalk.domain.model.Polygon;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmdServiceTest {

    @Mock
    private EmdRepository emdRepository;

    @InjectMocks
    private EmdService emdService;

    @Test
    void getEmdInBounds_성공() {
        // Given
        Coordinate sw = new Coordinate(BigDecimal.valueOf(35.820), BigDecimal.valueOf(129.200));
        Coordinate ne = new Coordinate(BigDecimal.valueOf(35.850), BigDecimal.valueOf(129.230));
        GetEmdInBoundsQuery query = new GetEmdInBoundsQuery(sw, ne);

        Emd emd = new Emd(
                "11110103",
                "황남동",
                new Coordinate(BigDecimal.valueOf(35.8339), BigDecimal.valueOf(129.2141)),
//                new Polygon("{}"),
                "",
                3
        );

        when(emdRepository.findEmdInBounds(sw, ne)).thenReturn(List.of(emd));

        // When
        var result = emdService.getEmdInBounds(query);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("황남동");
        assertThat(result.get(0).totalAccident()).isEqualTo(3);
    }
}