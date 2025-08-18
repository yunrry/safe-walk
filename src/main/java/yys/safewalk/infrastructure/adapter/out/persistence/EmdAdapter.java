package yys.safewalk.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import yys.safewalk.application.port.out.EmdRepository;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.domain.model.Emd;
import yys.safewalk.domain.model.Polygon;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmdAdapter implements EmdRepository {


    private final EmdJpaRepository emdJpaRepository;
    private final PedestrianAccidentHotspotsJpaRepository accidentJpaRepository;

    @Override
    public List<Emd> findEmdInBounds(Coordinate swCoordinate, Coordinate neCoordinate) {
        List<Object[]> results = emdJpaRepository.findEmdDataInBounds(
                swCoordinate.latitude(), swCoordinate.longitude(),
                neCoordinate.latitude(), neCoordinate.longitude()
        );

        return results.stream()
                .map(this::mapToEmd)
                .toList();
    }

    private Emd mapToEmd(Object[] row) {
        String emdCd = (String) row[0];
        String emdKorNm = (String) row[1];
//        String polygonJson = (String) row[2];
        BigDecimal latitude = (BigDecimal) row[2];
        BigDecimal longitude = (BigDecimal) row[3];
        Integer totalAccident = ((Number) row[4]).intValue();

        Coordinate centerPoint = new Coordinate(latitude, longitude);

        return new Emd(
                emdCd,
                emdKorNm,
                centerPoint,
//                new Polygon(polygonJson),
                "",
                totalAccident
        );
    }


}