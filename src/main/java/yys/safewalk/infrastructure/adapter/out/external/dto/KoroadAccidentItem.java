// infrastructure/adapter/out/external/dto/KoroadAccidentItem.java
package yys.safewalk.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoroadAccidentItem {

    /**
     * 다발지역FID (공간정보 식별자)
     */
    @JsonProperty("afos_fid")
    private String afosFid;

    /**
     * 다발지역ID
     */
    @JsonProperty("afos_id")
    private String afosId;

    /**
     * 법정동코드
     */
    @JsonProperty("bjd_cd")
    private String bjdCd;

    /**
     * 지점코드
     */
    @JsonProperty("spot_cd")
    private String spotCd;

    /**
     * 시도시군구명
     * 예: "서울특별시 강남구"
     */
    @JsonProperty("sido_sgg_nm")
    private String sidoSggNm;

    /**
     * 지점명 (다발지역 지점의 위치)
     * 예: "서울특별시 강남구 역삼동 (선릉역사거리 부근)"
     */
    @JsonProperty("spot_nm")
    private String spotNm;

    /**
     * 사고건수
     */
    @JsonProperty("occrrnc_cnt")
    private Integer occrrncCnt;

    /**
     * 사상자수
     */
    @JsonProperty("caslt_cnt")
    private Integer casltCnt;

    /**
     * 사망자수
     */
    @JsonProperty("dth_dnv_cnt")
    private Integer dthDnvCnt;

    /**
     * 중상자수
     */
    @JsonProperty("se_dnv_cnt")
    private Integer seDnvCnt;

    /**
     * 경상자수
     */
    @JsonProperty("sl_dnv_cnt")
    private Integer slDnvCnt;

    /**
     * 부상신고자수
     */
    @JsonProperty("wnd_dnv_cnt")
    private Integer wndDnvCnt;

    /**
     * 경도 (EPSG 4326)
     */
    @JsonProperty("lo_crd")
    private Double loCrd;

    /**
     * 위도 (EPSG 4326)
     */
    @JsonProperty("la_crd")
    private Double laCrd;

    /**
     * 다발지역 폴리곤 정보 (EPSG 4326)
     * GeoJSON 형태 또는 WKT 형태
     */
    @JsonProperty("geom_json")
    private String geomJson;

    // ===========================================
    // 계산 메서드들
    // ===========================================

    /**
     * 치사율 계산 (%)
     * 치사율 = (사망자수 / 사상자수) * 100
     */
    public Double calculateFatalityRate() {
        if (casltCnt == null || casltCnt == 0) {
            return 0.0;
        }
        if (dthDnvCnt == null) {
            return 0.0;
        }
        return (double) dthDnvCnt / casltCnt * 100;
    }

    /**
     * 중상 이상 비율 계산 (%)
     * 중상 이상 비율 = ((사망자수 + 중상자수) / 사상자수) * 100
     */
    public Double calculateSeriousInjuryRate() {
        if (casltCnt == null || casltCnt == 0) {
            return 0.0;
        }
        int seriousTotal = safeInteger(dthDnvCnt) + safeInteger(seDnvCnt);
        return (double) seriousTotal / casltCnt * 100;
    }

    /**
     * 사고 심각도 점수 계산
     * 사고건수 + (사망자수 * 10) + (중상자수 * 5) + (경상자수 * 2) + 부상신고자수
     */
    public Double calculateSeverityScore() {
        return (double) safeInteger(occrrncCnt) +
                (safeInteger(dthDnvCnt) * 10.0) +
                (safeInteger(seDnvCnt) * 5.0) +
                (safeInteger(slDnvCnt) * 2.0) +
                safeInteger(wndDnvCnt);
    }

    /**
     * 사고 밀도 계산 (사고건수 / 사상자수)
     * 값이 높을수록 단일 사고당 피해가 적음 (경미한 사고 위주)
     */
    public Double calculateAccidentDensity() {
        if (casltCnt == null || casltCnt == 0) {
            return occrrncCnt != null ? occrrncCnt.doubleValue() : 0.0;
        }
        return occrrncCnt != null ? (double) occrrncCnt / casltCnt : 0.0;
    }

    // ===========================================
    // 데이터 분석 메서드들
    // ===========================================

    /**
     * 사고 심각도 등급 결정
     */
    public String getSeverityLevel() {
        Double severityScore = calculateSeverityScore();
        Double fatalityRate = calculateFatalityRate();

        if (severityScore >= 100 || fatalityRate >= 10) {
            return "CRITICAL";
        } else if (severityScore >= 50 || fatalityRate >= 5) {
            return "HIGH";
        } else if (severityScore >= 20 || fatalityRate >= 2) {
            return "MEDIUM";
        } else if (severityScore >= 10 || fatalityRate >= 1) {
            return "LOW";
        } else {
            return "MINIMAL";
        }
    }

    /**
     * 보행자 위험도가 높은 지역인지 판단
     */
    public boolean isHighPedestrianRisk() {
        // 사고건수가 많거나 사망/중상자 비율이 높은 경우
        int accidents = safeInteger(occrrncCnt);
        double seriousRate = calculateSeriousInjuryRate();

        return accidents >= 7 || seriousRate >= 30;
    }

    /**
     * 사상자 구성 분석
     */
    public CasualtyComposition analyzeCasualtyComposition() {
        int total = safeInteger(casltCnt);
        if (total == 0) {
            return new CasualtyComposition(0, 0, 0, 0);
        }

        double deathRatio = (double) safeInteger(dthDnvCnt) / total * 100;
        double seriousRatio = (double) safeInteger(seDnvCnt) / total * 100;
        double minorRatio = (double) safeInteger(slDnvCnt) / total * 100;
        double reportRatio = (double) safeInteger(wndDnvCnt) / total * 100;

        return new CasualtyComposition(deathRatio, seriousRatio, minorRatio, reportRatio);
    }

    // ===========================================
    // 지역 정보 분석 메서드들
    // ===========================================

    /**
     * 시도 정보 추출
     */
    public String extractSiDo() {
        if (sidoSggNm == null || sidoSggNm.trim().isEmpty()) {
            return null;
        }

        String[] parts = sidoSggNm.split(" ");
        return parts.length > 0 ? parts[0] : null;
    }

    /**
     * 시군구 정보 추출
     */
    public String extractSiGunGu() {
        if (sidoSggNm == null || sidoSggNm.trim().isEmpty()) {
            return null;
        }

        String[] parts = sidoSggNm.split(" ");
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * 상세 지역명 추출 (괄호 안의 내용)
     */
    public String extractDetailLocation() {
        if (spotNm == null) {
            return null;
        }

        int start = spotNm.indexOf('(');
        int end = spotNm.indexOf(')', start);

        if (start != -1 && end != -1 && end > start) {
            return spotNm.substring(start + 1, end);
        }

        return null;
    }

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

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 기본 데이터 유효성 검증
     */
    public boolean isValid() {
        return afosId != null && !afosId.trim().isEmpty() &&
                sidoSggNm != null && !sidoSggNm.trim().isEmpty() &&
                spotNm != null && !spotNm.trim().isEmpty() &&
                occrrncCnt != null && occrrncCnt > 0 &&
                hasValidCoordinates();
    }

    /**
     * 좌표 정보 유효성 확인
     */
    public boolean hasValidCoordinates() {
        return loCrd != null && laCrd != null &&
                loCrd >= -180.0 && loCrd <= 180.0 &&
                laCrd >= -90.0 && laCrd <= 90.0;
    }

    /**
     * 한국 영역 내 좌표인지 확인
     */
    public boolean isWithinKorea() {
        if (!hasValidCoordinates()) {
            return false;
        }

        // 한국 영역 대략적 경계
        return loCrd >= 124.0 && loCrd <= 132.0 &&
                laCrd >= 33.0 && laCrd <= 43.0;
    }

    /**
     * 사상자 정보 일관성 확인
     */
    public boolean hasCasualtyConsistency() {
        int totalCasualties = safeInteger(dthDnvCnt) + safeInteger(seDnvCnt) +
                safeInteger(slDnvCnt) + safeInteger(wndDnvCnt);

        // 총 사상자수와 개별 사상자수 합계가 일치하는지 확인
        return casltCnt != null && Math.abs(casltCnt - totalCasualties) <= 1;
    }

    /**
     * 지오메트리 정보 존재 확인
     */
    public boolean hasGeometry() {
        return geomJson != null && !geomJson.trim().isEmpty();
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
     * 좌표 정보 요약
     */
    public String getCoordinateSummary() {
        if (!hasValidCoordinates()) {
            return "좌표정보 없음";
        }
        return String.format("경도:%.6f, 위도:%.6f", loCrd, laCrd);
    }

    /**
     * 사고 정보 요약
     */
    public String getAccidentSummary() {
        return String.format("사고:%d건, 사상자:%d명, 사망:%d명, 중상:%d명",
                safeInteger(occrrncCnt),
                safeInteger(casltCnt),
                safeInteger(dthDnvCnt),
                safeInteger(seDnvCnt));
    }

    @Override
    public String toString() {
        return String.format("KoroadAccidentItem{afosId='%s', region='%s', accidents=%d, casualties=%d, location=[%.6f, %.6f]}",
                afosId,
                sidoSggNm,
                safeInteger(occrrncCnt),
                safeInteger(casltCnt),
                loCrd != null ? loCrd : 0.0,
                laCrd != null ? laCrd : 0.0);
    }

    // ===========================================
    // 내부 클래스: 사상자 구성 분석
    // ===========================================

    @Data
    @AllArgsConstructor
    public static class CasualtyComposition {
        private double deathRatio;
        private double seriousInjuryRatio;
        private double minorInjuryRatio;
        private double injuryReportRatio;

        public String getCompositionSummary() {
            return String.format("사망:%.1f%%, 중상:%.1f%%, 경상:%.1f%%, 신고:%.1f%%",
                    deathRatio, seriousInjuryRatio, minorInjuryRatio, injuryReportRatio);
        }
    }
}