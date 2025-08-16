// infrastructure/adapter/out/external/dto/KoroadStatisticsItem.java
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
public class KoroadStatisticsItem {

    /**
     * 기준년도
     */
    @JsonProperty("std_year")
    private String stdYear;

    /**
     * 사고분류명
     * 예: "전체사고", "어린이", "고령자", "보행자", "자전거", "야간" 등
     */
    @JsonProperty("acc_cl_nm")
    private String accClNm;

    /**
     * 시도시군구명
     * 예: "서울특별시 중구"
     */
    @JsonProperty("sido_sgg_nm")
    private String sidoSggNm;

    /**
     * 사고건수
     */
    @JsonProperty("acc_cnt")
    private Integer accCnt;

    /**
     * 사고건수 구성비
     */
    @JsonProperty("acc_cnt_cmrt")
    private Double accCntCmrt;

    /**
     * 사망자수
     */
    @JsonProperty("dth_dnv_cnt")
    private Integer dthDnvCnt;

    /**
     * 사망자수 구성비
     */
    @JsonProperty("dth_dnv_cnt_cmrt")
    private Double dthDnvCntCmrt;

    /**
     * 치사율
     */
    @JsonProperty("ftlt_rate")
    private Double ftltRate;

    /**
     * 부상자수
     */
    @JsonProperty("injpsn_cnt")
    private Integer injpsnCnt;

    /**
     * 부상자수 구성비
     */
    @JsonProperty("injpsn_cnt_cmrt")
    private Double injpsnCntCmrt;

    /**
     * 해당 대상사고 총사고건수
     */
    @JsonProperty("tot_acc_cnt")
    private Integer totAccCnt;

    /**
     * 해당 대상사고 총사망자수
     */
    @JsonProperty("tot_dth_dnv_cnt")
    private Integer totDthDnvCnt;

    /**
     * 해당 대상사고 총부상자수
     */
    @JsonProperty("tot_injpsn_cnt")
    private Integer totInjpsnCnt;

    /**
     * 인구10만명당 사고건수 (전체사고만 제공)
     */
    @JsonProperty("pop_100k")
    private Double pop100k;

    /**
     * 자동차1만대당 사고건수 (전체사고만 제공)
     */
    @JsonProperty("car_10k")
    private Double car10k;

    // ===========================================
    // 법규위반 사고건수 (8개 항목, 전체사고만 제공)
    // ===========================================

    /**
     * 과속 사고건수
     * 2021년부터 별도집계 (법규위반 항목에서 제외)
     */
    @JsonProperty("cnt_027_01")
    private Integer cnt02701;

    /**
     * 중앙선 침범 사고건수
     */
    @JsonProperty("cnt_027_02")
    private Integer cnt02702;

    /**
     * 신호위반 사고건수
     */
    @JsonProperty("cnt_027_03")
    private Integer cnt02703;

    /**
     * 안전거리 미확보 사고건수
     */
    @JsonProperty("cnt_027_04")
    private Integer cnt02704;

    /**
     * 안전운전 의무 불이행 사고건수
     */
    @JsonProperty("cnt_027_05")
    private Integer cnt02705;

    /**
     * 교차로 통행방법 위반 사고건수
     */
    @JsonProperty("cnt_027_06")
    private Integer cnt02706;

    /**
     * 보행자 보호의무 위반 사고건수
     */
    @JsonProperty("cnt_027_07")
    private Integer cnt02707;

    /**
     * 기타 법규위반 사고건수
     */
    @JsonProperty("cnt_027_99")
    private Integer cnt02799;

    // ===========================================
    // 사고유형별 사고건수 (4개 항목, 전체사고만 제공)
    // ===========================================

    /**
     * 차대사람 사고건수
     */
    @JsonProperty("cnt_014_01")
    private Integer cnt01401;

    /**
     * 차대차 사고건수
     */
    @JsonProperty("cnt_014_02")
    private Integer cnt01402;

    /**
     * 차량단독 사고건수
     */
    @JsonProperty("cnt_014_03")
    private Integer cnt01403;

    /**
     * 철길건널목 사고건수
     */
    @JsonProperty("cnt_014_04")
    private Integer cnt01404;

    // ===========================================
    // 계산 메서드들
    // ===========================================

    /**
     * 총 사상자수 계산 (사망자 + 부상자)
     */
    public Integer getTotalCasualtyCount() {
        return safeInteger(dthDnvCnt) + safeInteger(injpsnCnt);
    }

    /**
     * 전체 법규위반 사고건수 계산
     */
    public Integer getTotalViolationCount() {
        return safeInteger(cnt02701) + // 과속
                safeInteger(cnt02702) + // 중앙선침범
                safeInteger(cnt02703) + // 신호위반
                safeInteger(cnt02704) + // 안전거리미확보
                safeInteger(cnt02705) + // 안전운전의무불이행
                safeInteger(cnt02706) + // 교차로통행방법위반
                safeInteger(cnt02707) + // 보행자보호의무위반
                safeInteger(cnt02799);   // 기타
    }

    /**
     * 전체 사고유형별 사고건수 계산
     */
    public Integer getTotalAccidentTypeCount() {
        return safeInteger(cnt01401) + // 차대사람
                safeInteger(cnt01402) + // 차대차
                safeInteger(cnt01403) + // 차량단독
                safeInteger(cnt01404);   // 철길건널목
    }

    /**
     * 사고 심각도 지수 계산
     * (사망자수 * 10 + 부상자수 * 1) / 사고건수
     */
    public Double getAccidentSeverityIndex() {
        if (accCnt == null || accCnt == 0) {
            return 0.0;
        }
        int weightedCasualties = safeInteger(dthDnvCnt) * 10 + safeInteger(injpsnCnt) * 1;
        return (double) weightedCasualties / accCnt;
    }

    /**
     * 전국 대비 사고 비율 계산 (%)
     */
    public Double getNationalAccidentRatio() {
        if (totAccCnt == null || totAccCnt == 0) {
            return 0.0;
        }
        if (accCnt == null) {
            return 0.0;
        }
        return (double) accCnt / totAccCnt * 100;
    }

    /**
     * 전국 대비 사망자 비율 계산 (%)
     */
    public Double getNationalDeathRatio() {
        if (totDthDnvCnt == null || totDthDnvCnt == 0) {
            return 0.0;
        }
        if (dthDnvCnt == null) {
            return 0.0;
        }
        return (double) dthDnvCnt / totDthDnvCnt * 100;
    }

    // ===========================================
    // 법규위반 분석 메서드들
    // ===========================================

    /**
     * 법규위반 사고 비율 계산 (%)
     */
    public Double getViolationAccidentRatio() {
        if (accCnt == null || accCnt == 0) {
            return 0.0;
        }
        return (double) getTotalViolationCount() / accCnt * 100;
    }

    /**
     * 보행자 관련 법규위반 사고건수 계산
     */
    public Integer getPedestrianRelatedViolationCount() {
        return safeInteger(cnt02703) + // 신호위반
                safeInteger(cnt02707);   // 보행자보호의무위반
    }

    /**
     * 주요 법규위반 유형 반환
     */
    public String getPrimaryViolationType() {
        if (!hasDetailedStats()) {
            return "상세통계 없음";
        }

        int maxCount = 0;
        String primaryType = "없음";

        if (safeInteger(cnt02701) > maxCount) { maxCount = cnt02701; primaryType = "과속"; }
        if (safeInteger(cnt02702) > maxCount) { maxCount = cnt02702; primaryType = "중앙선침범"; }
        if (safeInteger(cnt02703) > maxCount) { maxCount = cnt02703; primaryType = "신호위반"; }
        if (safeInteger(cnt02704) > maxCount) { maxCount = cnt02704; primaryType = "안전거리미확보"; }
        if (safeInteger(cnt02705) > maxCount) { maxCount = cnt02705; primaryType = "안전운전의무불이행"; }
        if (safeInteger(cnt02706) > maxCount) { maxCount = cnt02706; primaryType = "교차로통행방법위반"; }
        if (safeInteger(cnt02707) > maxCount) { maxCount = cnt02707; primaryType = "보행자보호의무위반"; }
        if (safeInteger(cnt02799) > maxCount) { primaryType = "기타"; }

        return primaryType;
    }

    // ===========================================
    // 사고유형 분석 메서드들
    // ===========================================

    /**
     * 보행자 관련 사고 비율 계산 (%)
     */
    public Double getPedestrianAccidentRatio() {
        if (accCnt == null || accCnt == 0) {
            return 0.0;
        }
        int pedestrianRelated = safeInteger(cnt01401) + // 차대사람
                safeInteger(cnt02707);   // 보행자보호의무위반
        return (double) pedestrianRelated / accCnt * 100;
    }

    /**
     * 차대사람 사고 비율 계산 (%)
     */
    public Double getVehicleVsPedestrianRatio() {
        if (accCnt == null || accCnt == 0) {
            return 0.0;
        }
        return (double) safeInteger(cnt01401) / accCnt * 100;
    }

    /**
     * 주요 사고유형 반환
     */
    public String getPrimaryAccidentType() {
        if (!hasDetailedStats()) {
            return "상세통계 없음";
        }

        int maxCount = 0;
        String primaryType = "없음";

        if (safeInteger(cnt01401) > maxCount) { maxCount = cnt01401; primaryType = "차대사람"; }
        if (safeInteger(cnt01402) > maxCount) { maxCount = cnt01402; primaryType = "차대차"; }
        if (safeInteger(cnt01403) > maxCount) { maxCount = cnt01403; primaryType = "차량단독"; }
        if (safeInteger(cnt01404) > maxCount) { primaryType = "철길건널목"; }

        return primaryType;
    }

    // ===========================================
    // 분류 및 지역 분석 메서드들
    // ===========================================

    /**
     * 전체사고 통계인지 확인
     */
    public boolean isOverallStatistics() {
        return "전체사고".equals(accClNm) || "전체".equals(accClNm);
    }

    /**
     * 어린이 관련 통계인지 확인
     */
    public boolean isChildrenRelated() {
        return accClNm != null &&
                (accClNm.contains("어린이") || accClNm.contains("스쿨존"));
    }

    /**
     * 고령자 관련 통계인지 확인
     */
    public boolean isElderlyRelated() {
        return accClNm != null && accClNm.contains("고령");
    }

    /**
     * 보행자 관련 통계인지 확인
     */
    public boolean isPedestrianRelated() {
        return accClNm != null && accClNm.contains("보행자");
    }

    /**
     * 야간 관련 통계인지 확인
     */
    public boolean isNightTimeRelated() {
        return "야간".equals(accClNm);
    }

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

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 기본 데이터 유효성 검증
     */
    public boolean isValid() {
        return stdYear != null && !stdYear.trim().isEmpty() &&
                accClNm != null && !accClNm.trim().isEmpty() &&
                sidoSggNm != null && !sidoSggNm.trim().isEmpty() &&
                accCnt != null && accCnt >= 0;
    }

    /**
     * 인구/차량 통계가 있는지 확인 (전체사고만 제공)
     */
    public boolean hasPopulationVehicleStats() {
        return pop100k != null && car10k != null;
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
     * 상세 통계가 모두 있는지 확인
     */
    public boolean hasDetailedStats() {
        return hasPopulationVehicleStats() &&
                hasViolationDetails() &&
                hasAccidentTypeDetails();
    }

    /**
     * 데이터 일관성 확인
     */
    public boolean hasDataConsistency() {
        // 사고건수와 사상자수 관계 확인
        if (accCnt != null && (dthDnvCnt != null || injpsnCnt != null)) {
            int totalCasualties = getTotalCasualtyCount();
            // 사고건수보다 사상자수가 현저히 많은 경우는 비정상
            return totalCasualties <= accCnt * 5; // 사고당 최대 5명 정도로 제한
        }
        return true;
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
     * 통계 요약 정보
     */
    public String getStatisticsSummary() {
        return String.format("%s %s - 사고:%d건, 사망:%d명, 부상:%d명, 치사율:%.2f%%",
                sidoSggNm,
                accClNm,
                safeInteger(accCnt),
                safeInteger(dthDnvCnt),
                safeInteger(injpsnCnt),
                ftltRate != null ? ftltRate : 0.0);
    }

    /**
     * 위험도 등급 평가
     */
    public String evaluateRiskLevel() {
        Double severityIndex = getAccidentSeverityIndex();
        Double fatalityRate = ftltRate != null ? ftltRate : 0.0;

        if (severityIndex >= 5.0 || fatalityRate >= 3.0) {
            return "매우높음";
        } else if (severityIndex >= 3.0 || fatalityRate >= 2.0) {
            return "높음";
        } else if (severityIndex >= 2.0 || fatalityRate >= 1.5) {
            return "보통";
        } else if (severityIndex >= 1.0 || fatalityRate >= 1.0) {
            return "낮음";
        } else {
            return "매우낮음";
        }
    }

    @Override
    public String toString() {
        return String.format("KoroadStatisticsItem{year='%s', region='%s', classification='%s', accidents=%d, deaths=%d, risk='%s'}",
                stdYear,
                sidoSggNm,
                accClNm,
                safeInteger(accCnt),
                safeInteger(dthDnvCnt),
                evaluateRiskLevel());
    }
}