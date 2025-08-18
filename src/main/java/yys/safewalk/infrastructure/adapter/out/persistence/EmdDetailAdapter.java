package yys.safewalk.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yys.safewalk.application.port.out.EmdDetailPort;
import yys.safewalk.domain.model.*;
import yys.safewalk.entity.EmdData;
import yys.safewalk.entity.PedestrianAccidentHotspots;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmdDetailAdapter implements EmdDetailPort {

    private final EmdJpaRepository emdJpaRepository;
    private final PedestrianAccidentHotspotsJpaRepository accidentJpaRepository;

    @Override
    public Optional<EmdDetail> findByEmdCode(String emdCode) {
        // 1. 법정동 기본 정보 조회
        Optional<EmdData> emdDataOpt = emdJpaRepository.findByEmdCd(emdCode);
        if (emdDataOpt.isEmpty()) {
            return Optional.empty();
        }

        EmdData emdData = emdDataOpt.get();

        // 2. 해당 법정동의 사고 데이터 조회
        String emdPrefix = emdCode.substring(0, 8); // EMD_CD의 앞 8자리
        List<PedestrianAccidentHotspots> accidents = accidentJpaRepository.findBySidoCodeStartingWith(emdPrefix);

        // 3. 총 사고 수 계산
        Integer totalAccident = accidents.stream()
                .mapToInt(accident -> accident.getAccidentCount() != null ? accident.getAccidentCount() : 0)
                .sum();

        // 4. 사고 상세 정보 매핑
        List<AccidentDetail> accidentDetails = accidents.stream()
                .map(this::mapToAccidentDetail)
                .collect(Collectors.toList());

        return Optional.of(new EmdDetail(
                emdData.getEmdKorNm(),
                totalAccident,
                emdCode,
                accidentDetails
        ));
    }

    private AccidentDetail mapToAccidentDetail(PedestrianAccidentHotspots hotspot) {
        // ID 생성 (점 코드 또는 FID 활용)
        String id = "acc-" + hotspot.getPointCode();

        // 위치명에서 괄호 안 내용만 추출
        String location = extractLocationFromPointName(hotspot.getPointName());

        // 사상자 정보 매핑
        Casualties casualties = new Casualties(
                hotspot.getAccidentCount(),  // total <- accident_count
                hotspot.getDeathCount(),     // dead <- death_count
                hotspot.getSeriousInjuryCount(), // severe <- serious_injury_count
                hotspot.getMinorInjuryCount()    // minor <- minor_injury_count
        );

        // 좌표 정보
        Coordinate point = new Coordinate(
                hotspot.getLatitude(),
                hotspot.getLongitude()
        );

        return new AccidentDetail(
                id,
                location,
                hotspot.getAccidentCount(),
                casualties,
                point
        );
    }

    private String extractLocationFromPointName(String pointName) {
        if (pointName == null) {
            return "위치 정보 없음";
        }

        // "서울특별시 종로구 낙원동(낙원상가 부근)" -> "낙원상가 부근"
        int startIndex = pointName.indexOf('(');
        int endIndex = pointName.indexOf(')', startIndex);

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return pointName.substring(startIndex + 1, endIndex);
        }

        // 괄호가 없으면 전체 이름 반환
        return pointName;
    }
}