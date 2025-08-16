// application/port/out/external/dto/RiskAreaData.java
package yys.safewalk.application.port.out.external.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiskAreaData {

    /**
     * 사고위험지역명
     * 예: "서울특별시 강남구내에서 2023년도에 반경50m 이내 다른사고14건 이상 지역"
     */
    private String riskAreaName;

    /**
     * 총사고건수
     */
    private Integer totalAccidentCount;

    /**
     * 총사망자수
     */
    private Integer totalDeathCount;

    /**
     * 총중상자수
     */
    private Integer totalSeriousInjuryCount;

    /**
     * 총경상자수
     */
    private Integer totalMinorInjuryCount;

    /**
     * 총부상신고자수
     */
    private Integer totalInjuryReportCount;

    /**
     * 사고분석유형명 리스트
     * 예: ["기타", "안전거리 미확보", "U턴중"]
     */
    @Builder.Default
    private List<String> accidentAnalysisTypes = Collections.emptyList();

    /**
     * 중심점 UTMK X좌표 (EPSG 5179)
     */
    private Double centerPointUtmkX;

    /**
     * 중심점 UTMK Y좌표 (EPSG 5179)
     */
    private Double centerPointUtmkY;

    /**
     * 사고위험지역 폴리곤 정보 (WKT 형식, EPSG 5179)
     * 예: "POLYGON (( 959579.98710000 1944852.34250000, ... ))"
     */
    private String geometryWkt;

    /**
     * 데이터 수집 시간
     */
    @Builder.Default
    private LocalDateTime collectedAt = LocalDateTime.now();

    /**
     * 데이터 기준 연도
     */
    private String dataYear;

    /**
     * 위험도 등급 (계산된 값)
     */
    private String riskGrade;

    /**
     * 지역 구분 (시도-시군구)
     */
    private String regionCode;

    // ===========================================
    // 계산 메서드들
    // ===========================================

    /**
     * 총 사상자수 계산
     */
    public Integer getTotalCasualtyCount() {
        return safeInteger(totalDeathCount) +
                safeInteger(totalSeriousInjuryCount) +
                safeInteger(totalMinorInjuryCount) +
                safeInteger(totalInjuryReportCount);
    }

    /**
     * 치사율 계산 (%)
     */
    public Double calculateFatalityRate() {
        Integer totalCasualties = getTotalCasualtyCount();
        if (totalCasualties == 0) {
            return 0.0;
        }
        return (double) safeInteger(totalDeathCount) / totalCasualties * 100;
    }

    /**
     * 중상 이상 비율 계산 (%)
     */
    public Double calculateSeriousInjuryRate() {
        Integer totalCasualties = getTotalCasualtyCount();
        if (totalCasualties == 0) {
            return 0.0;
        }
        Integer seriousOrFatal = safeInteger(totalDeathCount) + safeInteger(totalSeriousInjuryCount);
        return (double) seriousOrFatal / totalCasualties * 100;
    }

    /**
     * 위험도 점수 계산
     * 사고건수 + (사망자수 * 10) + (중상자수 * 5) + (경상자수 * 2) + 부상신고자수
     */
    public Double calculateRiskScore() {
        return (double) safeInteger(totalAccidentCount) +
                (safeInteger(totalDeathCount) * 10.0) +
                (safeInteger(totalSeriousInjuryCount) * 5.0) +
                (safeInteger(totalMinorInjuryCount) * 2.0) +
                safeInteger(totalInjuryReportCount);
    }

    /**
     * 사고 심각도 등급 결정
     */
    public String calculateSeverityLevel() {
        Double riskScore = calculateRiskScore();
        Double fatalityRate = calculateFatalityRate();

        if (riskScore >= 100 || fatalityRate >= 10) {
            return "CRITICAL";
        } else if (riskScore >= 50 || fatalityRate >= 5) {
            return "HIGH";
        } else if (riskScore >= 20 || fatalityRate >= 2) {
            return "MEDIUM";
        } else if (riskScore >= 10 || fatalityRate >= 1) {
            return "LOW";
        } else {
            return "MINIMAL";
        }
    }

    /**
     * 주요 사고 원인 분석
     */
    public String getPrimaryAccidentCause() {
        if (accidentAnalysisTypes == null || accidentAnalysisTypes.isEmpty()) {
            return "원인 미상";
        }

        // 첫 번째 원인을 주요 원인으로 간주 (API에서 빈도순으로 정렬되어 있다고 가정)
        return accidentAnalysisTypes.get(0);
    }

    /**
     * 보행자 위험 요소 확인
     */
    public boolean hasPedestrianRiskFactors() {
        if (accidentAnalysisTypes == null) {
            return false;
        }

        return accidentAnalysisTypes.stream()
                .anyMatch(cause -> cause.contains("보행자") ||
                        cause.contains("횡단") ||
                        cause.contains("신호") ||
                        cause.contains("U턴"));
    }

    /**
     * 운전자 부주의 관련 위험 요소 확인
     */
    public boolean hasDriverNeglectFactors() {
        if (accidentAnalysisTypes == null) {
            return false;
        }

        return accidentAnalysisTypes.stream()
                .anyMatch(cause -> cause.contains("안전거리") ||
                        cause.contains("안전운전") ||
                        cause.contains("주의태만") ||
                        cause.contains("과속"));
    }

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return riskAreaName != null && !riskAreaName.trim().isEmpty() &&
                totalAccidentCount != null && totalAccidentCount > 0 &&
                hasValidCenterPoint();
    }

    /**
     * 중심점 좌표 유효성 확인
     */
    public boolean hasValidCenterPoint() {
        return centerPointUtmkX != null && centerPointUtmkY != null &&
                centerPointUtmkX > 0 && centerPointUtmkY > 0;
    }

    /**
     * 폴리곤 정보 존재 확인
     */
    public boolean hasGeometry() {
        return geometryWkt != null && !geometryWkt.trim().isEmpty() &&
                geometryWkt.startsWith("POLYGON");
    }

    /**
     * 사고 분석 정보 존재 확인
     */
    public boolean hasAccidentAnalysis() {
        return accidentAnalysisTypes != null && !accidentAnalysisTypes.isEmpty();
    }

    /**
     * 사상자 정보 존재 확인
     */
    public boolean hasCasualtyInfo() {
        return getTotalCasualtyCount() > 0;
    }

    // ===========================================
    // 지역 정보 추출 메서드들
    // ===========================================

    /**
     * 지역명에서 시도 추출
     */
    public String extractSiDo() {
        if (riskAreaName == null) {
            return null;
        }

        String[] parts = riskAreaName.split(" ");
        if (parts.length > 0) {
            String first = parts[0];
            if (first.endsWith("시") || first.endsWith("도")) {
                return first;
            }
        }
        return null;
    }

    /**
     * 지역명에서 시군구 추출
     */
    public String extractSiGunGu() {
        if (riskAreaName == null) {
            return null;
        }

        String[] parts = riskAreaName.split(" ");
        if (parts.length > 1) {
            String second = parts[1];
            if (second.endsWith("구") || second.endsWith("시") || second.endsWith("군")) {
                return second;
            }
        }
        return null;
    }

    /**
     * 연도 정보 추출
     */
    public String extractYear() {
        if (riskAreaName == null) {
            return null;
        }

        // "2023년도" 패턴 찾기
        String yearPattern = "\\d{4}년";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(yearPattern);
        java.util.regex.Matcher matcher = pattern.matcher(riskAreaName);

        if (matcher.find()) {
            return matcher.group().replace("년", "");
        }
        return null;
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
        return String.format("riskarea:%s:%s", dataYear, regionCode);
    }

    /**
     * 간단한 좌표 정보 문자열
     */
    public String getCenterPointSummary() {
        if (!hasValidCenterPoint()) {
            return "좌표정보 없음";
        }
        return String.format("UTMK(%.2f, %.2f)", centerPointUtmkX, centerPointUtmkY);
    }

    /**
     * 요약 정보 문자열
     */
    public String toSummary() {
        return String.format("%s - 사고:%d건, 사상자:%d명, 치사율:%.1f%%, 심각도:%s",
                extractSiGunGu() != null ? extractSiGunGu() : "지역미상",
                safeInteger(totalAccidentCount),
                getTotalCasualtyCount(),
                calculateFatalityRate(),
                calculateSeverityLevel());
    }

    @Override
    public String toString() {
        return String.format("RiskAreaData{region='%s', accidents=%d, casualties=%d, center=[%.2f, %.2f]}",
                extractSiGunGu(),
                safeInteger(totalAccidentCount),
                getTotalCasualtyCount(),
                centerPointUtmkX != null ? centerPointUtmkX : 0.0,
                centerPointUtmkY != null ? centerPointUtmkY : 0.0);
    }
}