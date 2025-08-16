// application/port/out/external/dto/AccidentStatisticsData.java
package yys.safewalk.application.port.out.external.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccidentStatisticsData {

    /**
     * 기준년도
     */
    private String standardYear;

    /**
     * 사고분류명
     * 예: "전체사고", "어린이", "고령자", "보행자", "자전거", "야간" 등
     */
    private String accidentClassificationName;

    /**
     * 시도시군구명
     * 예: "서울특별시 중구", "부산광역시 해운대구"
     */
    private String regionName;

    /**
     * 사고건수
     */
    private Integer accidentCount;

    /**
     * 사고건수 구성비
     */
    private Double accidentCountRatio;

    /**
     * 사망자수
     */
    private Integer deathCount;

    /**
     * 사망자수 구성비
     */
    private Double deathCountRatio;

    /**
     * 치사율
     */
    private Double fatalityRate;

    /**
     * 부상자수
     */
    private Integer injuredPersonCount;

    /**
     * 부상자수 구성비
     */
    private Double injuredPersonCountRatio;

    /**
     * 해당 대상사고 총사고건수
     */
    private Integer totalAccidentCount;

    /**
     * 해당 대상사고 총사망자수
     */
    private Integer totalDeathCount;

    /**
     * 해당 대상사고 총부상자수
     */
    private Integer totalInjuredPersonCount;

    /**
     * 인구10만명당 사고건수
     */
    private Double accidentsPer100kPopulation;

    /**
     * 자동차1만대당 사고건수
     */
    private Double accidentsPer10kVehicles;

    // ===========================================
    // 법규위반별 사고건수 (8개 항목)
    // ===========================================

    /**
     * 과속 사고건수
     * 2021년부터 별도집계 (법규위반 항목에서 제외)
     */
    private Integer speedingCount;

    /**
     * 중앙선 침범 사고건수
     */
    private Integer centerLineViolationCount;

    /**
     * 신호위반 사고건수
     */
    private Integer signalViolationCount;

    /**
     * 안전거리 미확보 사고건수
     */
    private Integer safeDistanceViolationCount;

    /**
     * 안전운전 의무 불이행 사고건수
     */
    private Integer safeDrivingViolationCount;

    /**
     * 교차로 통행방법 위반 사고건수
     */
    private Integer intersectionViolationCount;

    /**
     * 보행자 보호의무 위반 사고건수
     */
    private Integer pedestrianProtectionViolationCount;

    /**
     * 기타 법규위반 사고건수
     */
    private Integer otherViolationCount;

    // ===========================================
    // 사고유형별 사고건수 (4개 항목)
    // ===========================================

    /**
     * 차대사람 사고건수
     */
    private Integer vehicleVsPedestrianCount;

    /**
     * 차대차 사고건수
     */
    private Integer vehicleVsVehicleCount;

    /**
     * 차량단독 사고건수
     */
    private Integer singleVehicleCount;

    /**
     * 철길건널목 사고건수
     */
    private Integer railwayCrossingCount;

    /**
     * 데이터 수집 시간
     */
    @Builder.Default
    private LocalDateTime collectedAt = LocalDateTime.now();

    // ===========================================
    // 계산 메서드들
    // ===========================================

    /**
     * 전체 법규위반 사고건수 계산
     */
    public Integer getTotalViolationCount() {
        return safeInteger(speedingCount) +
                safeInteger(centerLineViolationCount) +
                safeInteger(signalViolationCount) +
                safeInteger(safeDistanceViolationCount) +
                safeInteger(safeDrivingViolationCount) +
                safeInteger(intersectionViolationCount) +
                safeInteger(pedestrianProtectionViolationCount) +
                safeInteger(otherViolationCount);
    }

    /**
     * 전체 사고유형별 사고건수 계산
     */
    public Integer getTotalAccidentTypeCount() {
        return safeInteger(vehicleVsPedestrianCount) +
                safeInteger(vehicleVsVehicleCount) +
                safeInteger(singleVehicleCount) +
                safeInteger(railwayCrossingCount);
    }

    /**
     * 법규위반 사고 비율 계산 (%)
     */
    public Double getViolationAccidentRatio() {
        if (accidentCount == null || accidentCount == 0) {
            return 0.0;
        }
        return (double) getTotalViolationCount() / accidentCount * 100;
    }

    /**
     * 보행자 관련 사고 비율 계산 (%)
     */
    public Double getPedestrianAccidentRatio() {
        if (accidentCount == null || accidentCount == 0) {
            return 0.0;
        }
        int pedestrianRelated = safeInteger(vehicleVsPedestrianCount) +
                safeInteger(pedestrianProtectionViolationCount);
        return (double) pedestrianRelated / accidentCount * 100;
    }

    /**
     * 사고 심각도 지수 계산
     * (사망자수 * 10 + 부상자수 * 1) / 사고건수
     */
    public Double getAccidentSeverityIndex() {
        if (accidentCount == null || accidentCount == 0) {
            return 0.0;
        }
        int weightedCasualties = safeInteger(deathCount) * 10 +
                safeInteger(injuredPersonCount) * 1;
        return (double) weightedCasualties / accidentCount;
    }

    /**
     * 전국 대비 위험도 계산 (상대적 위험도)
     */
    public Double getRelativeRiskRatio() {
        if (totalAccidentCount == null || totalAccidentCount == 0) {
            return 0.0;
        }
        if (accidentCount == null) {
            return 0.0;
        }
        return (double) accidentCount / totalAccidentCount * 100;
    }

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return standardYear != null && !standardYear.trim().isEmpty() &&
                accidentClassificationName != null && !accidentClassificationName.trim().isEmpty() &&
                regionName != null && !regionName.trim().isEmpty() &&
                accidentCount != null && accidentCount >= 0;
    }

    /**
     * 인구/차량 통계가 있는지 확인 (전체사고만 제공)
     */
    public boolean hasPopulationVehicleStats() {
        return accidentsPer100kPopulation != null && accidentsPer10kVehicles != null;
    }

    /**
     * 법규위반 상세 통계가 있는지 확인 (전체사고만 제공)
     */
    public boolean hasViolationDetails() {
        return getTotalViolationCount() > 0;
    }

    /**
     * 사고유형 상세 통계가 있는지 확인 (전체사고만 제공)
     */
    public boolean hasAccidentTypeDetails() {
        return getTotalAccidentTypeCount() > 0;
    }

    /**
     * 어린이 관련 통계인지 확인
     */
    public boolean isChildrenRelated() {
        return accidentClassificationName != null &&
                (accidentClassificationName.contains("어린이") ||
                        accidentClassificationName.contains("스쿨존"));
    }

    /**
     * 고령자 관련 통계인지 확인
     */
    public boolean isElderlyRelated() {
        return accidentClassificationName != null &&
                accidentClassificationName.contains("고령");
    }

    /**
     * 보행자 관련 통계인지 확인
     */
    public boolean isPedestrianRelated() {
        return accidentClassificationName != null &&
                accidentClassificationName.contains("보행자");
    }

    // ===========================================
    // 유틸리티 메서드들
    // ===========================================

    /**
     * null-safe 정수 변환
     */
    private Integer safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * 캐시 키 생성
     */
    public String toCacheKey() {
        return String.format("statistics:%s:%s:%s",
                standardYear, regionName, accidentClassificationName);
    }

    /**
     * 요약 정보 문자열
     */
    public String toSummary() {
        return String.format("%s %s - 사고:%d건, 사망:%d명, 부상:%d명, 치사율:%.2f%%",
                regionName,
                accidentClassificationName,
                safeInteger(accidentCount),
                safeInteger(deathCount),
                safeInteger(injuredPersonCount),
                fatalityRate != null ? fatalityRate : 0.0);
    }

    @Override
    public String toString() {
        return String.format("AccidentStatisticsData{year='%s', region='%s', classification='%s', accidents=%d, deaths=%d}",
                standardYear, regionName, accidentClassificationName,
                safeInteger(accidentCount), safeInteger(deathCount));
    }
}