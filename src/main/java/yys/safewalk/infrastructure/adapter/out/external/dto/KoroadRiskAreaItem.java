// infrastructure/adapter/out/external/dto/KoroadRiskAreaItem.java
package yys.safewalk.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoroadRiskAreaItem {

    /**
     * 사고위험지역명
     * 예: "서울특별시 강남구내에서 2023년도에 반경50m 이내 다른사고14건 이상 지역"
     */
    @JsonProperty("acc_risk_area_nm")
    private String accRiskAreaNm;

    /**
     * 총사고건수
     */
    @JsonProperty("tot_acc_cnt")
    private Integer totAccCnt;

    /**
     * 총사망자수
     */
    @JsonProperty("tot_dth_dnv_cnt")
    private Integer totDthDnvCnt;

    /**
     * 총중상자수
     */
    @JsonProperty("tot_se_dnv_cnt")
    private Integer totSeDnvCnt;

    /**
     * 총경상자수
     */
    @JsonProperty("tot_sl_dnv_cnt")
    private Integer totSlDnvCnt;

    /**
     * 총부상신고자수
     */
    @JsonProperty("tot_wnd_dnv_cnt")
    private Integer totWndDnvCnt;

    /**
     * 사고분석유형명
     * JSON 배열 형태: ["기타","안전거리 미확보", "U턴중"]
     */
    @JsonProperty("cause_anals_ty_nm")
    private String causeAnalsTyNm;

    /**
     * 중심점 UTMK X좌표 (EPSG 5179)
     */
    @JsonProperty("cntpnt_utmk_x_crd")
    private Double cntpntUtmkXCrd;

    /**
     * 중심점 UTMK Y좌표 (EPSG 5179)
     */
    @JsonProperty("cntpnt_utmk_y_crd")
    private Double cntpntUtmkYCrd;

    /**
     * 사고위험지역 폴리곤 정보 (WKT 형식, EPSG 5179)
     * 예: "POLYGON (( 959579.98710000 1944852.34250000, ... ))"
     */
    @JsonProperty("geom_wkt")
    private String geomWkt;

    // ===========================================
    // 계산 메서드들
    // ===========================================

    /**
     * 총 사상자수 계산
     */
    public Integer getTotalCasualtyCount() {
        return safeInteger(totDthDnvCnt) +
                safeInteger(totSeDnvCnt) +
                safeInteger(totSlDnvCnt) +
                safeInteger(totWndDnvCnt);
    }

    /**
     * 치사율 계산 (%)
     */
    public Double calculateFatalityRate() {
        Integer totalCasualties = getTotalCasualtyCount();
        if (totalCasualties == 0) {
            return 0.0;
        }
        return (double) safeInteger(totDthDnvCnt) / totalCasualties * 100;
    }

    /**
     * 중상 이상 비율 계산 (%)
     */
    public Double calculateSeriousInjuryRate() {
        Integer totalCasualties = getTotalCasualtyCount();
        if (totalCasualties == 0) {
            return 0.0;
        }
        Integer seriousOrFatal = safeInteger(totDthDnvCnt) + safeInteger(totSeDnvCnt);
        return (double) seriousOrFatal / totalCasualties * 100;
    }

    /**
     * 위험도 점수 계산
     * 사고건수 + (사망자수 * 10) + (중상자수 * 5) + (경상자수 * 2) + 부상신고자수
     */
    public Double calculateRiskScore() {
        return (double) safeInteger(totAccCnt) +
                (safeInteger(totDthDnvCnt) * 10.0) +
                (safeInteger(totSeDnvCnt) * 5.0) +
                (safeInteger(totSlDnvCnt) * 2.0) +
                safeInteger(totWndDnvCnt);
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
     * 사고 밀도 계산 (사고건수 / 사상자수)
     */
    public Double calculateAccidentDensity() {
        Integer totalCasualties = getTotalCasualtyCount();
        if (totalCasualties == 0) {
            return totAccCnt != null ? totAccCnt.doubleValue() : 0.0;
        }
        return totAccCnt != null ? (double) totAccCnt / totalCasualties : 0.0;
    }

    // ===========================================
    // 사고 원인 분석 메서드들
    // ===========================================

    /**
     * 사고분석유형 문자열을 리스트로 파싱
     */
    public List<String> getAccidentAnalysisTypes() {
        if (causeAnalsTyNm == null || causeAnalsTyNm.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // JSON 배열 형태: ["기타","안전거리 미확보", "U턴중"]
            String cleaned = causeAnalsTyNm
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .trim();

            if (cleaned.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> types = new ArrayList<>();
            String[] parts = cleaned.split("\\s*,\\s*");

            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    types.add(trimmed);
                }
            }

            return types;
        } catch (Exception e) {
            // 파싱 실패시 원본 문자열을 단일 항목으로 반환
            return Collections.singletonList(causeAnalsTyNm);
        }
    }

    /**
     * 주요 사고 원인 분석 (첫 번째 원인을 주요 원인으로 간주)
     */
    public String getPrimaryAccidentCause() {
        List<String> causes = getAccidentAnalysisTypes();
        return causes.isEmpty() ? "원인 미상" : causes.get(0);
    }

    /**
     * 보행자 위험 요소 확인
     */
    public boolean hasPedestrianRiskFactors() {
        List<String> causes = getAccidentAnalysisTypes();
        if (causes.isEmpty()) {
            return false;
        }

        return causes.stream()
                .anyMatch(cause -> cause.contains("보행자") ||
                        cause.contains("횡단") ||
                        cause.contains("신호") ||
                        cause.contains("U턴") ||
                        cause.contains("유턴"));
    }

    /**
     * 운전자 부주의 관련 위험 요소 확인
     */
    public boolean hasDriverNeglectFactors() {
        List<String> causes = getAccidentAnalysisTypes();
        if (causes.isEmpty()) {
            return false;
        }

        return causes.stream()
                .anyMatch(cause -> cause.contains("안전거리") ||
                        cause.contains("안전운전") ||
                        cause.contains("주의태만") ||
                        cause.contains("과속") ||
                        cause.contains("중앙선"));
    }

    /**
     * 교통법규 위반 관련 위험 요소 확인
     */
    public boolean hasTrafficViolationFactors() {
        List<String> causes = getAccidentAnalysisTypes();
        if (causes.isEmpty()) {
            return false;
        }

        return causes.stream()
                .anyMatch(cause -> cause.contains("신호위반") ||
                        cause.contains("중앙선") ||
                        cause.contains("속도위반") ||
                        cause.contains("통행방법"));
    }

    // ===========================================
    // 지역 정보 추출 메서드들
    // ===========================================

    /**
     * 지역명에서 시도 추출
     */
    public String extractSiDo() {
        if (accRiskAreaNm == null) {
            return null;
        }

        String[] parts = accRiskAreaNm.split(" ");
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
        if (accRiskAreaNm == null) {
            return null;
        }

        String[] parts = accRiskAreaNm.split(" ");
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
        if (accRiskAreaNm == null) {
            return null;
        }

        // "2023년도" 패턴 찾기
        Pattern yearPattern = Pattern.compile("(\\d{4})년");
        Matcher matcher = yearPattern.matcher(accRiskAreaNm);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 반경 정보 추출 (미터 단위)
     */
    public Integer extractRadius() {
        if (accRiskAreaNm == null) {
            return null;
        }

        // "반경50m" 패턴 찾기
        Pattern radiusPattern = Pattern.compile("반경(\\d+)m");
        Matcher matcher = radiusPattern.matcher(accRiskAreaNm);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 최소 사고건수 기준 추출
     */
    public Integer extractMinimumAccidentCount() {
        if (accRiskAreaNm == null) {
            return null;
        }

        // "다른사고14건 이상" 패턴 찾기
        Pattern accidentPattern = Pattern.compile("다른사고(\\d+)건\\s*이상");
        Matcher matcher = accidentPattern.matcher(accRiskAreaNm);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return accRiskAreaNm != null && !accRiskAreaNm.trim().isEmpty() &&
                totAccCnt != null && totAccCnt > 0 &&
                hasValidCenterPoint();
    }

    /**
     * 중심점 좌표 유효성 확인
     */
    public boolean hasValidCenterPoint() {
        return cntpntUtmkXCrd != null && cntpntUtmkYCrd != null &&
                cntpntUtmkXCrd > 0 && cntpntUtmkYCrd > 0;
    }

    /**
     * 폴리곤 정보 존재 확인
     */
    public boolean hasGeometry() {
        return geomWkt != null && !geomWkt.trim().isEmpty() &&
                geomWkt.toUpperCase().startsWith("POLYGON");
    }

    /**
     * 사고 분석 정보 존재 확인
     */
    public boolean hasAccidentAnalysis() {
        return !getAccidentAnalysisTypes().isEmpty();
    }

    /**
     * 사상자 정보 존재 확인
     */
    public boolean hasCasualtyInfo() {
        return getTotalCasualtyCount() > 0;
    }

    /**
     * 한국 좌표계(UTMK) 범위 내 확인
     */
    public boolean isWithinKoreaUTMK() {
        if (!hasValidCenterPoint()) {
            return false;
        }

        // 한국 UTMK 좌표계 대략적 범위
        // X: 200,000 ~ 700,000, Y: 1,100,000 ~ 2,000,000
        return cntpntUtmkXCrd >= 200000 && cntpntUtmkXCrd <= 700000 &&
                cntpntUtmkYCrd >= 1100000 && cntpntUtmkYCrd <= 2000000;
    }

    // ===========================================
    // 지역별 특성 분석 메서드들
    // ===========================================

    /**
     * 서울 지역인지 확인
     */
    public boolean isSeoulArea() {
        String sido = extractSiDo();
        return sido != null && sido.contains("서울");
    }

    /**
     * 광역시 지역인지 확인
     */
    public boolean isMetropolitanArea() {
        String sido = extractSiDo();
        return sido != null && sido.contains("광역시");
    }

    /**
     * 도 단위 지역인지 확인
     */
    public boolean isProvinceArea() {
        String sido = extractSiDo();
        return sido != null && sido.endsWith("도");
    }

    /**
     * 관광지역 여부 추정 (지역명 기반)
     */
    public boolean isPossibleTouristArea() {
        if (accRiskAreaNm == null) {
            return false;
        }

        return accRiskAreaNm.contains("제주") ||
                accRiskAreaNm.contains("강릉") ||
                accRiskAreaNm.contains("부산") ||
                accRiskAreaNm.contains("경주") ||
                accRiskAreaNm.contains("속초");
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
     * 간단한 좌표 정보 문자열
     */
    public String getCenterPointSummary() {
        if (!hasValidCenterPoint()) {
            return "좌표정보 없음";
        }
        return String.format("UTMK(%.0f, %.0f)", cntpntUtmkXCrd, cntpntUtmkYCrd);
    }

    /**
     * 사상자 구성 요약
     */
    public String getCasualtySummary() {
        return String.format("사망:%d명, 중상:%d명, 경상:%d명, 신고:%d명",
                safeInteger(totDthDnvCnt),
                safeInteger(totSeDnvCnt),
                safeInteger(totSlDnvCnt),
                safeInteger(totWndDnvCnt));
    }

    /**
     * 위험지역 설정 기준 요약
     */
    public String getCriteriaSummary() {
        Integer radius = extractRadius();
        Integer minAccidents = extractMinimumAccidentCount();
        String year = extractYear();

        return String.format("기준: %s년 반경%dm 내 %d건 이상",
                year != null ? year : "미상",
                radius != null ? radius : 0,
                minAccidents != null ? minAccidents : 0);
    }

    /**
     * 요약 정보 문자열
     */
    public String toSummary() {
        return String.format("%s - 사고:%d건, 사상자:%d명, 치사율:%.1f%%, 심각도:%s",
                extractSiGunGu() != null ? extractSiGunGu() : "지역미상",
                safeInteger(totAccCnt),
                getTotalCasualtyCount(),
                calculateFatalityRate(),
                calculateSeverityLevel());
    }

    @Override
    public String toString() {
        return String.format("KoroadRiskAreaItem{region='%s', year='%s', accidents=%d, casualties=%d, center=[%.0f, %.0f]}",
                extractSiGunGu(),
                extractYear(),
                safeInteger(totAccCnt),
                getTotalCasualtyCount(),
                cntpntUtmkXCrd != null ? cntpntUtmkXCrd : 0.0,
                cntpntUtmkYCrd != null ? cntpntUtmkYCrd : 0.0);
    }
}