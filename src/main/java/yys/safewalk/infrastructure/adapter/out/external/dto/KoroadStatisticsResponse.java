// infrastructure/adapter/out/external/dto/KoroadStatisticsResponse.java
package yys.safewalk.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoroadStatisticsResponse extends KoroadBaseResponse {

    /**
     * 통계 데이터 항목들
     * 지자체별 대상사고통계 API 응답
     */
    @JsonProperty("items")
    private List<KoroadStatisticsItem> items;

    /**
     * 총 검색 건수
     */
    @JsonProperty("totalCount")
    private Integer totalCount;

    /**
     * 검색된 건수
     */
    @JsonProperty("numOfRows")
    private Integer numOfRows;

    /**
     * 페이지 번호
     */
    @JsonProperty("pageNo")
    private Integer pageNo;

    // ===========================================
    // 기본 데이터 접근 메서드들
    // ===========================================

    /**
     * 응답 데이터가 비어있는지 확인
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * 응답 데이터 개수 반환
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * null-safe items 반환
     */
    public List<KoroadStatisticsItem> getSafeItems() {
        return items != null ? items : Collections.emptyList();
    }

    /**
     * 유효한 아이템들만 필터링해서 반환
     */
    public List<KoroadStatisticsItem> getValidItems() {
        return getSafeItems().stream()
                .filter(item -> item != null && item.isValid())
                .toList();
    }

    // ===========================================
    // 통계 분석 메서드들
    // ===========================================

    /**
     * 전체사고 통계 아이템 반환
     */
    public KoroadStatisticsItem getOverallStatistics() {
        return getSafeItems().stream()
                .filter(item -> "전체사고".equals(item.getAccClNm()) ||
                        "전체".equals(item.getAccClNm()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 보행자 관련 통계 아이템들 반환
     */
    public List<KoroadStatisticsItem> getPedestrianStatistics() {
        return getSafeItems().stream()
                .filter(item -> item.getAccClNm() != null &&
                        (item.getAccClNm().contains("보행자") ||
                                item.getAccClNm().contains("어린이") ||
                                item.getAccClNm().contains("고령")))
                .toList();
    }

    /**
     * 사고분류별 통계 맵 반환
     */
    public Map<String, KoroadStatisticsItem> getStatisticsByClassification() {
        return getSafeItems().stream()
                .filter(item -> item.getAccClNm() != null)
                .collect(Collectors.toMap(
                        KoroadStatisticsItem::getAccClNm,
                        item -> item,
                        (existing, replacement) -> existing // 중복키 처리
                ));
    }

    /**
     * 총 사고건수 계산 (전체사고 기준)
     */
    public Integer getTotalAccidentCount() {
        KoroadStatisticsItem overall = getOverallStatistics();
        return overall != null ? overall.getAccCnt() : 0;
    }

    /**
     * 총 사망자수 계산 (전체사고 기준)
     */
    public Integer getTotalDeathCount() {
        KoroadStatisticsItem overall = getOverallStatistics();
        return overall != null ? overall.getDthDnvCnt() : 0;
    }

    /**
     * 총 부상자수 계산 (전체사고 기준)
     */
    public Integer getTotalInjuredCount() {
        KoroadStatisticsItem overall = getOverallStatistics();
        return overall != null ? overall.getInjpsnCnt() : 0;
    }

    /**
     * 전체 치사율 계산
     */
    public Double getOverallFatalityRate() {
        KoroadStatisticsItem overall = getOverallStatistics();
        return overall != null ? overall.getFtltRate() : 0.0;
    }

    // ===========================================
    // 보행자 안전 분석 메서드들
    // ===========================================

    /**
     * 보행자 사고 비율 계산 (%)
     */
    public Double getPedestrianAccidentRatio() {
        Integer totalAccidents = getTotalAccidentCount();
        if (totalAccidents == null || totalAccidents == 0) {
            return 0.0;
        }

        int pedestrianAccidents = getPedestrianStatistics().stream()
                .filter(item -> "보행자".equals(item.getAccClNm()))
                .mapToInt(item -> item.getAccCnt() != null ? item.getAccCnt() : 0)
                .sum();

        return (double) pedestrianAccidents / totalAccidents * 100;
    }

    /**
     * 고령자 사고 비율 계산 (%)
     */
    public Double getElderlyAccidentRatio() {
        Integer totalAccidents = getTotalAccidentCount();
        if (totalAccidents == null || totalAccidents == 0) {
            return 0.0;
        }

        int elderlyAccidents = getSafeItems().stream()
                .filter(item -> item.getAccClNm() != null &&
                        item.getAccClNm().contains("고령"))
                .mapToInt(item -> item.getAccCnt() != null ? item.getAccCnt() : 0)
                .sum();

        return (double) elderlyAccidents / totalAccidents * 100;
    }

    /**
     * 어린이 사고 비율 계산 (%)
     */
    public Double getChildrenAccidentRatio() {
        Integer totalAccidents = getTotalAccidentCount();
        if (totalAccidents == null || totalAccidents == 0) {
            return 0.0;
        }

        int childrenAccidents = getSafeItems().stream()
                .filter(item -> item.getAccClNm() != null &&
                        item.getAccClNm().contains("어린이"))
                .mapToInt(item -> item.getAccCnt() != null ? item.getAccCnt() : 0)
                .sum();

        return (double) childrenAccidents / totalAccidents * 100;
    }

    /**
     * 야간 사고 비율 계산 (%)
     */
    public Double getNightAccidentRatio() {
        Integer totalAccidents = getTotalAccidentCount();
        if (totalAccidents == null || totalAccidents == 0) {
            return 0.0;
        }

        int nightAccidents = getSafeItems().stream()
                .filter(item -> "야간".equals(item.getAccClNm()))
                .mapToInt(item -> item.getAccCnt() != null ? item.getAccCnt() : 0)
                .sum();

        return (double) nightAccidents / totalAccidents * 100;
    }

    // ===========================================
    // 법규위반 분석 메서드들 (전체사고만 제공)
    // ===========================================

    /**
     * 총 법규위반 사고건수 계산
     */
    public Integer getTotalViolationCount() {
        KoroadStatisticsItem overall = getOverallStatistics();
        if (overall == null) {
            return 0;
        }

        return safeInteger(overall.getCnt02701()) + // 과속
                safeInteger(overall.getCnt02702()) + // 중앙선침범
                safeInteger(overall.getCnt02703()) + // 신호위반
                safeInteger(overall.getCnt02704()) + // 안전거리미확보
                safeInteger(overall.getCnt02705()) + // 안전운전의무불이행
                safeInteger(overall.getCnt02706()) + // 교차로통행방법위반
                safeInteger(overall.getCnt02707()) + // 보행자보호의무위반
                safeInteger(overall.getCnt02799());   // 기타
    }

    /**
     * 보행자 보호의무 위반 비율 계산 (%)
     */
    public Double getPedestrianProtectionViolationRatio() {
        Integer totalViolations = getTotalViolationCount();
        if (totalViolations == 0) {
            return 0.0;
        }

        KoroadStatisticsItem overall = getOverallStatistics();
        if (overall == null) {
            return 0.0;
        }

        return (double) safeInteger(overall.getCnt02707()) / totalViolations * 100;
    }

    /**
     * 신호위반 사고 비율 계산 (%)
     */
    public Double getSignalViolationRatio() {
        Integer totalViolations = getTotalViolationCount();
        if (totalViolations == 0) {
            return 0.0;
        }

        KoroadStatisticsItem overall = getOverallStatistics();
        if (overall == null) {
            return 0.0;
        }

        return (double) safeInteger(overall.getCnt02703()) / totalViolations * 100;
    }

    // ===========================================
    // 사고유형 분석 메서드들 (전체사고만 제공)
    // ===========================================

    /**
     * 차대사람 사고 비율 계산 (%)
     */
    public Double getVehicleVsPedestrianRatio() {
        Integer totalAccidents = getTotalAccidentCount();
        if (totalAccidents == null || totalAccidents == 0) {
            return 0.0;
        }

        KoroadStatisticsItem overall = getOverallStatistics();
        if (overall == null) {
            return 0.0;
        }

        return (double) safeInteger(overall.getCnt01401()) / totalAccidents * 100;
    }

    /**
     * 차대차 사고 비율 계산 (%)
     */
    public Double getVehicleVsVehicleRatio() {
        Integer totalAccidents = getTotalAccidentCount();
        if (totalAccidents == null || totalAccidents == 0) {
            return 0.0;
        }

        KoroadStatisticsItem overall = getOverallStatistics();
        if (overall == null) {
            return 0.0;
        }

        return (double) safeInteger(overall.getCnt01402()) / totalAccidents * 100;
    }

    // ===========================================
    // 페이징 및 유효성 검증 메서드들
    // ===========================================

    /**
     * 다음 페이지가 있는지 확인
     */
    public boolean hasNextPage() {
        if (totalCount == null || numOfRows == null || pageNo == null) {
            return false;
        }
        return (pageNo * numOfRows) < totalCount;
    }

    /**
     * 전체 페이지 수 계산
     */
    public int getTotalPages() {
        if (totalCount == null || numOfRows == null || numOfRows == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / numOfRows);
    }

    /**
     * 통계 데이터 완성도 확인
     */
    public StatisticsCompleteness checkCompleteness() {
        if (isEmpty()) {
            return StatisticsCompleteness.NO_DATA;
        }

        boolean hasOverall = getOverallStatistics() != null;
        boolean hasPedestrian = !getPedestrianStatistics().isEmpty();
        boolean hasDetailedStats = hasOverall && getOverallStatistics().hasDetailedStats();

        if (hasOverall && hasPedestrian && hasDetailedStats) {
            return StatisticsCompleteness.COMPLETE;
        } else if (hasOverall && hasPedestrian) {
            return StatisticsCompleteness.PARTIAL;
        } else if (hasOverall) {
            return StatisticsCompleteness.BASIC;
        } else {
            return StatisticsCompleteness.INCOMPLETE;
        }
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
        return String.format("통계 요약 - 총 사고:%d건, 사망:%d명, 부상:%d명, 치사율:%.2f%%, 분류:%d개",
                getTotalAccidentCount(),
                getTotalDeathCount(),
                getTotalInjuredCount(),
                getOverallFatalityRate(),
                getItemCount());
    }

    /**
     * 보행자 안전 지표 요약
     */
    public String getPedestrianSafetySummary() {
        return String.format("보행자 안전 - 보행자사고:%.1f%%, 고령자사고:%.1f%%, 어린이사고:%.1f%%, 차대사람:%.1f%%",
                getPedestrianAccidentRatio(),
                getElderlyAccidentRatio(),
                getChildrenAccidentRatio(),
                getVehicleVsPedestrianRatio());
    }

    @Override
    public String toString() {
        return String.format("KoroadStatisticsResponse{resultCode='%s', totalCount=%d, itemCount=%d, completeness=%s}",
                getResultCode(),
                totalCount != null ? totalCount : 0,
                getItemCount(),
                checkCompleteness());
    }

    // ===========================================
    // 내부 열거형: 통계 완성도
    // ===========================================

    public enum StatisticsCompleteness {
        COMPLETE("완전", "전체사고, 보행자, 상세통계 모두 포함"),
        PARTIAL("부분", "전체사고와 보행자 통계 포함"),
        BASIC("기본", "전체사고 통계만 포함"),
        INCOMPLETE("불완전", "필수 통계 누락"),
        NO_DATA("데이터없음", "통계 데이터 없음");

        private final String description;
        private final String detail;

        StatisticsCompleteness(String description, String detail) {
            this.description = description;
            this.detail = detail;
        }

        public String getDescription() {
            return description;
        }

        public String getDetail() {
            return detail;
        }
    }
}