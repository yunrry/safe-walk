package yys.safewalk.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import yys.safewalk.application.port.out.EmdRepository;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.domain.model.Emd;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class EmdAdapter implements EmdRepository {

    private final EmdJpaRepository emdJpaRepository;
    private final PedestrianAccidentHotspotsJpaRepository accidentJpaRepository;
    private final ElderlyPedestrianAccidentHotspotsJpaRepository elderlyAccidentJpaRepository;

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

    @Override
    public List<Emd> findBySidoCode(String sidoCode) {
        List<Object[]> results = emdJpaRepository.findEmdDataBySidoCode(sidoCode);

        return results.stream()
                .map(this::mapToEmd)
                .filter(Objects::nonNull)
                .filter(emd -> emd.getTotalAccident() > 0)
                .toList();
    }

    private Emd mapToEmd(Object[] row) {
        String emdCd = (String) row[0];
        String emdKorNm = (String) row[1];
        BigDecimal latitude = (BigDecimal) row[2];
        BigDecimal longitude = (BigDecimal) row[3];
        Integer generalAccident = ((Number) row[4]).intValue();
        if (latitude == null || longitude == null) {
            return null;
        }

        // 고령자 사고 데이터 추가 조회
        String emdPrefix = emdCd.substring(0, 8);
        Integer elderlyAccident = elderlyAccidentJpaRepository.getTotalAccidentCountByEmdCode(emdPrefix);
        if (elderlyAccident == null) {
            elderlyAccident = 0;
        }

        // 총 사고 수 = 일반 사고 + 고령자 사고
        Integer totalAccident = generalAccident + elderlyAccident;

        Coordinate centerPoint = new Coordinate(latitude, longitude);

        return new Emd(
                emdCd,
                emdKorNm,
                centerPoint,
                "",
                totalAccident
        );
    }
}