package yys.safewalk.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yys.safewalk.application.port.out.EmdRepository;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.domain.model.Emd;
import yys.safewalk.entity.AdministrativeLegalDongs;
import yys.safewalk.entity.ElderlyPedestrianAccidentHotspots;
import yys.safewalk.entity.PedestrianAccidentHotspots;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AdministrativeLegalDongsAdapter implements EmdRepository {

    private final AdministrativeLegalDongsRepository administrativeLegalDongsRepository;
    private final PedestrianAccidentHotspotsJpaRepository accidentJpaRepository;
    private final ElderlyPedestrianAccidentHotspotsJpaRepository elderlyAccidentJpaRepository;

    @Override
    public List<Emd> findEmdInBounds(Coordinate swCoordinate, Coordinate neCoordinate) {
        // 좌표 범위로 AdministrativeLegalDongs 조회 (H 타입 제외)
        List<AdministrativeLegalDongs> legalDongs = administrativeLegalDongsRepository
                .findByLatitudeBetweenAndLongitudeBetweenAndCodeTypeNot(
                        swCoordinate.latitude(),
                        neCoordinate.latitude(),
                        swCoordinate.longitude(),
                        neCoordinate.longitude(),
                        "H"
                );

        return legalDongs.stream()
                .map(this::mapToEmd)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Emd> findBySidoCode(String sidoCode) {
        // 시도 코드로 AdministrativeLegalDongs 조회 (H 타입 제외)
        List<AdministrativeLegalDongs> legalDongs = administrativeLegalDongsRepository
                .findBySidoAndCodeTypeNot(sidoCode, "H");

        return legalDongs.stream()
                .map(this::mapToEmd)
                .filter(Objects::nonNull)
                .toList();
    }

    private Emd mapToEmd(AdministrativeLegalDongs legalDong) {
        String emdCd = legalDong.getCode();
        String emdKorNm = legalDong.getEupMyeonDong();
        BigDecimal latitude = legalDong.getLatitude();
        BigDecimal longitude = legalDong.getLongitude();

        // 좌표가 null인 경우 제외
        if (latitude == null || longitude == null) {
            return null;
        }

        // EMD 코드가 null이거나 8자리 미만인 경우 기본값 반환
        if (emdCd == null || emdCd.length() < 8) {
            Coordinate centerPoint = new Coordinate(latitude, longitude);
            return new Emd(
                    emdCd != null ? emdCd : "",
                    emdKorNm != null ? emdKorNm : "",
                    centerPoint,
                    "",
                    0
            );
        }

        // 일반 사고 데이터 조회 (emdCd 앞 8자리로 조회)
        String emdPrefix = emdCd.substring(0, 8);
        Integer generalAccident = accidentJpaRepository.getTotalAccidentCountByEmdCode(emdPrefix);
        if (generalAccident == null) {
            generalAccident = 0;
        }

        // 고령자 사고 데이터 조회
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
