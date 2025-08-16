// application/port/out/external/dto/AccidentData.java
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
public class AccidentData {

    /**
     * 다발지역 FID (공간정보 식별자)
     */
    private String afosId;

    /**
     * 다발지역 ID
     */
    private String afosAreaId;

    /**
     * 법정동 코드
     */
    private String legalDongCode;

    /**
     * 지점 코드
     */
    private String spotCode;

    /**
     * 시도시군구명
     * 예: "서울특별시 강남구", "부산광역시 해운대구"
     */
    private String regionName;

    /**
     * 지점명 (상세 위치)
     * 예: "서울특별시 강남구 역삼동 (선릉역사거리 부근)"
     */
    private String spotName;

    /**
     * 사고 건수
     */
    private Integer accidentCount;

    /**
     * 사상자 수 (총합)
     */
    private Integer casualtyCount;

    /**
     * 사망자 수
     */
    private Integer deathCount;

    /**
     * 중상자 수
     */
    private Integer seriousInjuryCount;

    /**
     * 경상자 수
     */
    private Integer minorInjuryCount;

    /**
     * 부상신고자 수
     */
    private Integer injuryReportCount;

    /**
     * 경도 (EPSG 4326)
     */
    private Double longitude;

    /**
     * 위도 (EPSG 4326)
     */
    private Double latitude;

    /**
     * 다발지역 폴리곤 정보 (GeoJSON 형태)
     */
    private String geometryJson;

    /**
     * API 유형
     * PEDESTRIAN, ELDERLY_PEDESTRIAN, LOCAL_GOVERNMENT, HOLIDAY
     */
    private String apiType;

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
     * 위험도 레벨 (계산된 값)
     */
    private String riskLevel;

    /**
     * 치사율 계산
     * 치사율 = (사망자수 / 사상자수) * 100
     */
    public Double calculateFatalityRate() {
        if (casualtyCount == null || casualtyCount == 0) {
            return 0.0;
        }
        if (deathCount == null) {
            return 0.0;
        }
        return (double) deathCount / casualtyCount * 100;
    }

    /**
     * 중상 이상 비율 계산
     * 중상 이상 비율 = ((사망자수 + 중상자수) / 사상자수) * 100
     */
    public Double calculateSeriousInjuryRate() {
        if (casualtyCount == null || casualtyCount == 0) {
            return 0.0;
        }
        int seriousTotal = (deathCount != null ? deathCount : 0) +
                (seriousInjuryCount != null ? seriousInjuryCount : 0);
        return (double) seriousTotal / casualtyCount * 100;
    }

    /**
     * 위험도 점수 계산 (사고건수 + 사상자수 + 치사율 가중치)
     */
    public Double calculateRiskScore() {
        double accidentWeight = (accidentCount != null ? accidentCount : 0) * 1.0;
        double casualtyWeight = (casualtyCount != null ? casualtyCount : 0) * 1.5;
        double fatalityWeight = calculateFatalityRate() * 2.0;

        return accidentWeight + casualtyWeight + fatalityWeight;
    }

    /**
     * 위치 정보가 유효한지 확인
     */
    public boolean hasValidLocation() {
        return longitude != null && latitude != null &&
                longitude >= -180.0 && longitude <= 180.0 &&
                latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * 사고 데이터가 유효한지 확인
     */
    public boolean isValid() {
        return afosId != null && !afosId.trim().isEmpty() &&
                regionName != null && !regionName.trim().isEmpty() &&
                spotName != null && !spotName.trim().isEmpty() &&
                accidentCount != null && accidentCount > 0 &&
                hasValidLocation();
    }

    /**
     * 보행자 관련 사고인지 확인
     */
    public boolean isPedestrianRelated() {
        return "PEDESTRIAN".equals(apiType) || "ELDERLY_PEDESTRIAN".equals(apiType);
    }

    /**
     * 고령자 관련 사고인지 확인
     */
    public boolean isElderlyRelated() {
        return "ELDERLY_PEDESTRIAN".equals(apiType);
    }

    /**
     * 연휴기간 사고인지 확인
     */
    public boolean isHolidayRelated() {
        return "HOLIDAY".equals(apiType);
    }

    /**
     * 간단한 위치 정보 문자열 반환
     */
    public String getLocationSummary() {
        if (!hasValidLocation()) {
            return "위치정보 없음";
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * 사고 심각도 등급 결정
     */
    public String getSeverityLevel() {
        Double riskScore = calculateRiskScore();

        if (riskScore >= 50) {
            return "VERY_HIGH";
        } else if (riskScore >= 30) {
            return "HIGH";
        } else if (riskScore >= 15) {
            return "MEDIUM";
        } else if (riskScore >= 5) {
            return "LOW";
        } else {
            return "VERY_LOW";
        }
    }

    /**
     * 캐시 키 생성
     */
    public String toCacheKey() {
        return String.format("accident:%s:%s", apiType, afosId);
    }

    /**
     * 요약 정보 문자열
     */
    public String toSummary() {
        return String.format("%s - 사고:%d건, 사상자:%d명, 치사율:%.1f%%",
                regionName,
                accidentCount != null ? accidentCount : 0,
                casualtyCount != null ? casualtyCount : 0,
                calculateFatalityRate());
    }

    @Override
    public String toString() {
        return String.format("AccidentData{afosId='%s', region='%s', accidents=%d, casualties=%d, location=[%.6f, %.6f]}",
                afosId, regionName,
                accidentCount != null ? accidentCount : 0,
                casualtyCount != null ? casualtyCount : 0,
                latitude != null ? latitude : 0.0,
                longitude != null ? longitude : 0.0);
    }
}