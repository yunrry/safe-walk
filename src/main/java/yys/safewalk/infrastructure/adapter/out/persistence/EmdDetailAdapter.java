package yys.safewalk.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yys.safewalk.application.port.out.EmdDetailPort;
import yys.safewalk.domain.model.*;
import yys.safewalk.entity.AdministrativeLegalDongs;
import yys.safewalk.entity.ElderlyPedestrianAccidentHotspots;
import yys.safewalk.entity.EmdData;
import yys.safewalk.entity.PedestrianAccidentHotspots;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmdDetailAdapter implements EmdDetailPort {

    private final EmdJpaRepository emdJpaRepository;
    private final PedestrianAccidentHotspotsJpaRepository accidentJpaRepository;
    private final ElderlyPedestrianAccidentHotspotsJpaRepository elderlyAccidentJpaRepository;
    private final AdministrativeLegalDongsRepository administrativeLegalDongsRepository;

    @Override
    public Optional<EmdDetail> findByEmdCode(String emdCode) {
        System.out.println("🔍 DEBUG: findByEmdCode 호출됨 - emdCode: " + emdCode);
        
        // 1. AdministrativeLegalDongs에서 지역 정보 조회 (emdCode + "00" 형태로, codeType이 'H'가 아닌 것만)
        String searchCode = emdCode.endsWith("00") ? emdCode : emdCode + "00";
        System.out.println(" DEBUG: 검색할 코드: " + searchCode);
        
        // codeType이 'H'가 아닌 것만 조회
        Optional<AdministrativeLegalDongs> legalDongOpt = administrativeLegalDongsRepository.findByCodeAndCodeTypeNot(searchCode, "H");
        System.out.println("🔍 DEBUG: AdministrativeLegalDongs 조회 결과: " + (legalDongOpt.isPresent() ? "존재함" : "존재하지 않음"));
        
        if (legalDongOpt.isEmpty()) {
            System.out.println("❌ DEBUG: administrative_legal_dongs 테이블에 해당 코드가 없음 (codeType != 'H'): " + searchCode);
            return Optional.empty();
        }

        AdministrativeLegalDongs legalDong = legalDongOpt.get();
        System.out.println("✅ DEBUG: 지역 정보 - 시도: " + legalDong.getSido() + ", 시군구: " + legalDong.getSigungu() + ", 읍면동: " + legalDong.getEupMyeonDong() + ", codeType: " + legalDong.getCodeType());

        // 2. 해당 법정동의 사고 데이터 조회
        String emdPrefix = emdCode.substring(0, 8); // EMD_CD의 앞 8자리
        System.out.println("🔍 DEBUG: emdPrefix 계산: " + emdCode + " -> " + emdPrefix);
        
        List<PedestrianAccidentHotspots> accidents = accidentJpaRepository.findBySidoCodeStartingWith(emdPrefix);
        List<ElderlyPedestrianAccidentHotspots> elderlyAccidents = elderlyAccidentJpaRepository.findBySidoCodeStartingWith(emdPrefix);
        
        System.out.println(" DEBUG: 일반 사고 데이터 수: " + accidents.size());
        System.out.println("🔍 DEBUG: 고령자 사고 데이터 수: " + elderlyAccidents.size());

        // 3. 사고 데이터가 없는 경우에도 기본 정보 포함하여 반환
        if (accidents.isEmpty() && elderlyAccidents.isEmpty()) {
            System.out.println("✅ DEBUG: 사고 데이터 없음, 기본 정보만 반환");
            return Optional.of(new EmdDetail(
                    legalDong.getEupMyeonDong(),  // 읍면동명 (AdministrativeLegalDongs에서)
                    0,                             // totalAccident = 0
                    emdCode,                       // EMD_CD
                    null                           // accidents = null
            ));
        }

        // 4. 사고 데이터가 있는 경우 기존 로직 수행
        System.out.println("✅ DEBUG: 사고 데이터 있음, 상세 정보 포함하여 반환");
        
        // 총 사고 수 계산 (일반 + 고령자)
        Integer generalTotalAccident = accidents.stream()
                .mapToInt(accident -> accident.getAccidentCount() != null ? accident.getAccidentCount() : 0)
                .sum();

        Integer elderlyTotalAccident = elderlyAccidents.stream()
                .mapToInt(accident -> accident.getAccidentCount() != null ? accident.getAccidentCount() : 0)
                .sum();

        Integer totalAccident = generalTotalAccident + elderlyTotalAccident;
        System.out.println(" DEBUG: 총 사고 수: " + totalAccident + " (일반: " + generalTotalAccident + ", 고령자: " + elderlyTotalAccident + ")");

        // 사고 상세 정보 매핑
        List<AccidentDetail> accidentDetails = new ArrayList<>();

        // 일반 사고 데이터 추가
        List<AccidentDetail> generalAccidentDetails = accidents.stream()
                .map(this::mapToAccidentDetail)
                .collect(Collectors.toList());
        accidentDetails.addAll(generalAccidentDetails);

        // 고령자 사고 데이터 추가
        List<AccidentDetail> elderlyAccidentDetails = elderlyAccidents.stream()
                .map(this::mapToElderlyAccidentDetail)
                .collect(Collectors.toList());
        accidentDetails.addAll(elderlyAccidentDetails);

        return Optional.of(new EmdDetail(
                legalDong.getEupMyeonDong(),  // 읍면동명 (AdministrativeLegalDongs에서)
                totalAccident,
                emdCode,
                accidentDetails
        ));
    }

    // 고령자 사고 데이터 매핑 메서드 추가
    private AccidentDetail mapToElderlyAccidentDetail(ElderlyPedestrianAccidentHotspots elderlyHotspot) {
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
        final PedestrianAccidentHotspots generalAccident;
        final ElderlyPedestrianAccidentHotspots elderlyAccident;

        CombinedAccidentData(PedestrianAccidentHotspots generalAccident, ElderlyPedestrianAccidentHotspots elderlyAccident) {
            this.generalAccident = generalAccident;
            this.elderlyAccident = elderlyAccident;
        }
    }

    // 총 사고 수 계산 메서드
    private int getTotalAccidentCount(CombinedAccidentData combined) {
        int generalCount = 0;
        int elderlyCount = 0;

        if (combined.generalAccident != null && combined.generalAccident.getAccidentCount() != null) {
            generalCount = combined.generalAccident.getAccidentCount();
        }

        if (combined.elderlyAccident != null && combined.elderlyAccident.getAccidentCount() != null) {
            elderlyCount = combined.elderlyAccident.getAccidentCount();
        }

        return generalCount + elderlyCount;
    }

    // 통합된 사고 상세 정보 매핑 메서드
    private AccidentDetail mapToCombinedAccidentDetail(CombinedAccidentData combined) {
        // 기본 정보는 일반 사고 데이터를 우선, 없으면 고령자 사고 데이터 사용
        PedestrianAccidentHotspots primary = combined.generalAccident != null ?
                combined.generalAccident : null;
        ElderlyPedestrianAccidentHotspots elderly = combined.elderlyAccident;

        // ID 생성 (일반 사고 데이터 우선)
        String id;
        String pointName;
        BigDecimal latitude;
        BigDecimal longitude;

        if (primary != null) {
            id = "acc-" + primary.getPointCode();
            pointName = primary.getPointName();
            latitude = primary.getLatitude();
            longitude = primary.getLongitude();
        } else {
            id = "elderly-acc-" + elderly.getPointCode();
            pointName = elderly.getPointName();
            latitude = elderly.getLatitude();
            longitude = elderly.getLongitude();
        }

        // 위치명 추출
        String location = extractLocationFromPointName(pointName);

        // 통합된 사상자 정보 계산
        int totalAccidents = getTotalAccidentCount(combined);
        int totalDeaths = getSum(primary != null ? primary.getDeathCount() : null,
                elderly != null ? elderly.getDeathCount() : null);
        int totalSevere = getSum(primary != null ? primary.getSeriousInjuryCount() : null,
                elderly != null ? elderly.getSeriousInjuryCount() : null);
        int totalMinor = getSum(primary != null ? primary.getMinorInjuryCount() : null,
                elderly != null ? elderly.getMinorInjuryCount() : null);

        Casualties casualties = new Casualties(
                totalAccidents,
                totalDeaths,
                totalSevere,
                totalMinor
        );

        // 좌표 정보
        Coordinate point = new Coordinate(latitude, longitude);

        return new AccidentDetail(
                id,
                location,
                totalAccidents,
                casualties,
                point
        );
    }

    // null-safe 합계 계산 유틸리티 메서드
    private int getSum(Integer value1, Integer value2) {
        int sum = 0;
        if (value1 != null) sum += value1;
        if (value2 != null) sum += value2;
        return sum;
    }

    private AccidentDetail mapToAccidentDetail(PedestrianAccidentHotspots hotspot) {
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