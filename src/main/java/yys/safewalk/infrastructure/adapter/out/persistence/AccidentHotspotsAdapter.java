package yys.safewalk.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yys.safewalk.application.port.out.LoadAccidentHotspotsPort;
import yys.safewalk.domain.model.*;
import yys.safewalk.entity.PedestrianAccidentHotspotsEntity;
import yys.safewalk.entity.ElderlyPedestrianAccidentHotspotsEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccidentHotspotsAdapter implements LoadAccidentHotspotsPort {

    private final PedestrianAccidentHotspotsJpaRepository pedestrianRepository;
    private final ElderlyPedestrianAccidentHotspotsJpaRepository elderlyRepository;

    @Override
    public List<AccidentDetail> findAccidentsInRadius(BigDecimal centerLat, BigDecimal centerLng, Integer radiusKm) {
        log.debug("반경 내 교통사고 조회: center=({}, {}), radius={}km", centerLat, centerLng, radiusKm);

        
        // 반경 계산 (대략적인 계산)
        BigDecimal radiusDegrees = BigDecimal.valueOf(radiusKm / 111.0); // 1도 ≈ 111km
        
        BigDecimal swLat = centerLat.subtract(radiusDegrees);
        BigDecimal neLat = centerLat.add(radiusDegrees);
        BigDecimal swLng = centerLng.subtract(radiusDegrees);
        BigDecimal neLng = centerLng.add(radiusDegrees);
        
        // 보행자 교통사고 조회
        List<PedestrianAccidentHotspotsEntity> pedestrianAccidents =
                pedestrianRepository.findAccidentsInBounds(swLat, swLng, neLat, neLng);

        
        // 노인 보행자 교통사고 조회
        List<ElderlyPedestrianAccidentHotspotsEntity> elderlyAccidents =
                elderlyRepository.findAccidentsInBounds(swLat, swLng, neLat, neLng);

        
        log.debug("반경 내 교통사고 조회 완료: 보행자 {}건, 노인보행자 {}건, 총 {}건", 
                pedestrianAccidents.size(), elderlyAccidents.size());


        // 3. 사고 데이터가 없는 경우에도 기본 정보 포함하여 반환
        if (pedestrianAccidents.isEmpty() && elderlyAccidents.isEmpty()) {
            return Collections.emptyList();                           // accidents = null
        }

        // 4. 사고 데이터가 있는 경우 기존 로직 수행

        // 총 사고 수 계산 (일반 + 고령자)
        Integer generalTotalAccident = pedestrianAccidents.stream()
                .mapToInt(accident -> accident.getAccidentCount() != null ? accident.getAccidentCount() : 0)
                .sum();

        Integer elderlyTotalAccident = elderlyAccidents.stream()
                .mapToInt(accident -> accident.getAccidentCount() != null ? accident.getAccidentCount() : 0)
                .sum();

        Integer totalAccident = generalTotalAccident + elderlyTotalAccident;

        // 사고 상세 정보 매핑
        List<AccidentDetail> accidentDetails = new ArrayList<>();

        // 일반 사고 데이터 추가
        List<AccidentDetail> generalAccidentDetails = pedestrianAccidents.stream()
                .map(this::mapToAccidentDetail)
                .collect(Collectors.toList());
        accidentDetails.addAll(generalAccidentDetails);

        // 고령자 사고 데이터 추가
        List<AccidentDetail> elderlyAccidentDetails = elderlyAccidents.stream()
                .map(this::mapToElderlyAccidentDetail)
                .collect(Collectors.toList());
        accidentDetails.addAll(elderlyAccidentDetails);

        return accidentDetails;


    }

    private AccidentDetail mapToElderlyAccidentDetail(ElderlyPedestrianAccidentHotspotsEntity elderlyHotspot) {
        // ID 생성 (고령자 사고 구분을 위해 prefix 추가)
        String id = elderlyHotspot.getAccidentHotspotFid().toString();

        // 위치명에서 괄호 안 내용만 추출
        String location = extractLocationFromPointName(elderlyHotspot.getPointName());

        // 사상자 정보 매핑
        Casualties casualties = new Casualties(
                elderlyHotspot.getAccidentCount(),      // total <- accident_count
                elderlyHotspot.getDeathCount(),         // dead <- death_count
                elderlyHotspot.getSeriousInjuryCount(), // severe <- serious_injury_count
                elderlyHotspot.getMinorInjuryCount()    // minor <- minor_injury_count
        );

        // 좌표 정보
        Coordinate point = new Coordinate(
                elderlyHotspot.getLatitude(),
                elderlyHotspot.getLongitude()
        );

        return new AccidentDetail(
                id,
                location,
                elderlyHotspot.getAccidentCount(),
                casualties,
                point
        );
    }

    // 내부 클래스: 통합된 사고 데이터
    private static class CombinedAccidentData {
        final PedestrianAccidentHotspotsEntity generalAccident;
        final ElderlyPedestrianAccidentHotspotsEntity elderlyAccident;

        CombinedAccidentData(PedestrianAccidentHotspotsEntity generalAccident, ElderlyPedestrianAccidentHotspotsEntity elderlyAccident) {
            this.generalAccident = generalAccident;
            this.elderlyAccident = elderlyAccident;
        }
    }



    // null-safe 합계 계산 유틸리티 메서드
    private int getSum(Integer value1, Integer value2) {
        int sum = 0;
        if (value1 != null) sum += value1;
        if (value2 != null) sum += value2;
        return sum;
    }

    private AccidentDetail mapToAccidentDetail(PedestrianAccidentHotspotsEntity hotspot) {
        // ID 생성 (점 코드 또는 FID 활용)
        String id = hotspot.getAccidentHotspotFid().toString();

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

        // 마지막 여는 괄호와 마지막 닫는 괄호를 찾아서 추출
        int startIndex = pointName.lastIndexOf('(');
        int endIndex = pointName.lastIndexOf(')');

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return pointName.substring(startIndex + 1, endIndex);
        }

        // 괄호가 없으면 전체 이름 반환
        return pointName;
    }
}
