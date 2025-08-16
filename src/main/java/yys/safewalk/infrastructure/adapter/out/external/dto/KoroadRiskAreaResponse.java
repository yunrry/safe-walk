// infrastructure/adapter/out/external/dto/KoroadRiskAreaResponse.java
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
public class KoroadRiskAreaResponse extends KoroadBaseResponse {

    /**
     * 위험지역 데이터 항목들
     * 링크기반 사고위험지역정보 API 응답
     */
    @JsonProperty("items")
    private List<KoroadRiskAreaItem> items;

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
    public List<KoroadRiskAreaItem> getSafeItems() {
        return items != null ? items : Collections.emptyList();
    }

    /**
     * 유효한 아이템들만 필터링해서 반환
     */
    public List<KoroadRiskAreaItem> getValidItems() {
        return getSafeItems().stream()
                .filter(item -> item != null && item.isValid())
                .toList();
    }

    // ===========================================
    // 위험지역 분석 메서드들
    // ===========================================

    /**
     * 총 사고건수 합계 계산
     */
    public Integer getTotalAccidentCount() {
        return getSafeItems().stream()
                .mapToInt(item -> item.getTotAccCnt() != null ? item.getTotAccCnt() : 0)
                .sum();
    }

    /**
     * 총 사망자수 합계 계산
     */
    public Integer getTotalDeathCount() {
        return getSafeItems().stream()
                .mapToInt(item -> item.getTotDthDnvCnt() != null ? item.getTotDthDnvCnt() : 0)
                .sum();
    }

    /**
     * 총 사상자수 합계 계산
     */
    public Integer getTotalCasualtyCount() {
        return getSafeItems().stream()
                .mapToInt(item -> item.getTotalCasualtyCount())
                .sum();
    }

    /**
     * 평균 치사율 계산
     */
    public Double getAverageFatalityRate() {
        List<KoroadRiskAreaItem> validItems = getValidItems();
        if (validItems.isEmpty()) {
            return 0.0;
        }

        return validItems.stream()
                .mapToDouble(KoroadRiskAreaItem::calculateFatalityRate)
                .average()
                .orElse(0.0);
    }

    /**
     * 위험지역 심각도별 분포 계산
     */
    public Map<String, Long> getSeverityDistribution() {
        return getSafeItems().stream()
                .collect(Collectors.groupingBy(
                        KoroadRiskAreaItem::calculateSeverityLevel,
                        Collectors.counting()
                ));
    }

    /**
     * 가장 위험한 지역 반환 (위험도 점수 기준)
     */
    public KoroadRiskAreaItem getMostDangerousArea() {
        return getSafeItems().stream()
                .max((a, b) -> Double.compare(
                        a.calculateRiskScore(),
                        b.calculateRiskScore()))
                .orElse(null);
    }

    /**
     * 보행자 위험 요소가 있는 지역들 필터링
     */
    public List<KoroadRiskAreaItem> getPedestrianRiskAreas() {
        return getSafeItems().stream()
                .filter(KoroadRiskAreaItem::hasPedestrianRiskFactors)
                .toList();
    }

    /**
     * 운전자 부주의 관련 위험 지역들 필터링
     */
    public List<KoroadRiskAreaItem> getDriverNeglectAreas() {
        return getSafeItems().stream()
                .filter(KoroadRiskAreaItem::hasDriverNeglectFactors)
                .toList();
    }

    // ===========================================
    // 지역별 분석 메서드들
    // ===========================================

    /**
     * 시도별 위험지역 그룹화
     */
    public Map<String, List<KoroadRiskAreaItem>> groupBySiDo() {
        return getSafeItems().stream()
                .filter(item -> item.extractSiDo() != null)
                .collect(Collectors.groupingBy(KoroadRiskAreaItem::extractSiDo));
    }

    /**
     * 시군구별 위험지역 그룹화
     */
    public Map<String, List<KoroadRiskAreaItem>> groupBySiGunGu() {
        return getSafeItems().stream()
                .filter(item -> item.extractSiGunGu() != null)
                .collect(Collectors.groupingBy(KoroadRiskAreaItem::extractSiGunGu));
    }

    /**
     * 특정 지역의 위험지역 개수 반환
     */
    public long getRegionRiskAreaCount(String regionName) {
        return getSafeItems().stream()
                .filter(item -> item.getRiskAreaName() != null &&
                        item.getRiskAreaName().contains(regionName))
                .count();
    }

    /**
     * 지역별 위험도 통계
     */
    public Map<String, RegionRiskStatistics> getRegionRiskStatistics() {
        return groupBySiGunGu().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateRegionStatistics(entry.getValue())
                ));
    }

    /**
     * 특정 지역의 위험도 통계 계산
     */
    private RegionRiskStatistics calculateRegionStatistics(List<KoroadRiskAreaItem> items) {
        int totalAreas = items.size();
        int totalAccidents = items.stream()
                .mapToInt(item -> item.getTotAccCnt() != null ? item.getTotAccCnt() : 0)
                .sum();
        int totalDeaths = items.stream()
                .mapToInt(item -> item.getTotDthDnvCnt() != null ? item.getTotDthDnvCnt() : 0)
                .sum();
        int totalCasualties = items.stream()
                .mapToInt(KoroadRiskAreaItem::getTotalCasualtyCount)
                .sum();

        double averageRiskScore = items.stream()
                .mapToDouble(KoroadRiskAreaItem::calculateRiskScore)
                .average()
                .orElse(0.0);

        return new RegionRiskStatistics(
                totalAreas, totalAccidents, totalDeaths,
                totalCasualties, averageRiskScore);
    }

    // ===========================================
    // 사고 원인 분석 메서드들
    // ===========================================

    /**
     * 전체 사고 분석 유형 수집
     */
    public Map<String, Long> getAccidentAnalysisTypeDistribution() {
        return getSafeItems().stream()
                .flatMap(item -> item.getAccidentAnalysisTypes().stream())
                .collect(Collectors.groupingBy(
                        type -> type,
                        Collectors.counting()
                ));
    }

    /**
     * 주요 사고 원인 Top N 반환
     */
    public List<Map.Entry<String, Long>> getTopAccidentCauses(int topN) {
        return getAccidentAnalysisTypeDistribution().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .toList();
    }

    /**
     * 보행자 관련 사고 원인 필터링
     */
    public Map<String, Long> getPedestrianRelatedCauses() {
        return getAccidentAnalysisTypeDistribution().entrySet().stream()
                .filter(entry -> entry.getKey().contains("보행자") ||
                        entry.getKey().contains("횡단") ||
                        entry.getKey().contains("신호") ||
                        entry.getKey().contains("U턴"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    // ===========================================
    // 좌표 및 공간 분석 메서드들
    // ===========================================

    /**
     * 유효한 좌표를 가진 지역들만 필터링
     */
    public List<KoroadRiskAreaItem> getAreasWithValidCoordinates() {
        return getSafeItems().stream()
                .filter(KoroadRiskAreaItem::hasValidCenterPoint)
                .toList();
    }

    /**
     * 폴리곤 정보를 가진 지역들만 필터링
     */
    public List<KoroadRiskAreaItem> getAreasWithGeometry() {
        return getSafeItems().stream()
                .filter(KoroadRiskAreaItem::hasGeometry)
                .toList();
    }

    /**
     * 공간정보 완성도 확인
     */
    public double getGeometryCompleteness() {
        if (isEmpty()) {
            return 0.0;
        }

        long withGeometry = getSafeItems().stream()
                .filter(KoroadRiskAreaItem::hasGeometry)
                .count();

        return (double) withGeometry / getItemCount() * 100;
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
     * 데이터 품질 체크
     */
    public RiskAreaDataQuality checkDataQuality() {
        if (isEmpty()) {
            return RiskAreaDataQuality.NO_DATA;
        }

        List<KoroadRiskAreaItem> validItems = getValidItems();
        double validRatio = (double) validItems.size() / getItemCount();
        double geometryRatio = getGeometryCompleteness() / 100.0;
        double avgCompleteness = (validRatio + geometryRatio) / 2.0;

        if (avgCompleteness >= 0.9) {
            return RiskAreaDataQuality.EXCELLENT;
        } else if (avgCompleteness >= 0.7) {
            return RiskAreaDataQuality.GOOD;
        } else if (avgCompleteness >= 0.5) {
            return RiskAreaDataQuality.FAIR;
        } else if (avgCompleteness >= 0.3) {
            return RiskAreaDataQuality.POOR;
        } else {
            return RiskAreaDataQuality.VERY_POOR;
        }
    }

    // ===========================================
    // 요약 및 리포트 메서드들
    // ===========================================

    /**
     * 위험지역 요약 정보
     */
    public String getRiskAreaSummary() {
        return String.format("위험지역 요약 - 총 %d개 지역, 사고:%d건, 사망:%d명, 사상자:%d명, 평균치사율:%.2f%%",
                getItemCount(),
                getTotalAccidentCount(),
                getTotalDeathCount(),
                getTotalCasualtyCount(),
                getAverageFatalityRate());
    }

    /**
     * 심각도별 분포 요약
     */
    public String getSeverityDistributionSummary() {
        Map<String, Long> distribution = getSeverityDistribution();
        return String.format("심각도 분포 - 위험:%d개, 높음:%d개, 보통:%d개, 낮음:%d개, 최소:%d개",
                distribution.getOrDefault("CRITICAL", 0L),
                distribution.getOrDefault("HIGH", 0L),
                distribution.getOrDefault("MEDIUM", 0L),
                distribution.getOrDefault("LOW", 0L),
                distribution.getOrDefault("MINIMAL", 0L));
    }

    /**
     * 주요 사고 원인 요약
     */
    public String getTopCausesSummary(int topN) {
        List<Map.Entry<String, Long>> topCauses = getTopAccidentCauses(topN);
        return topCauses.stream()
                .map(entry -> String.format("%s(%d건)", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return String.format("KoroadRiskAreaResponse{resultCode='%s', totalCount=%d, itemCount=%d, quality=%s}",
                getResultCode(),
                totalCount != null ? totalCount : 0,
                getItemCount(),
                checkDataQuality());
    }

    // ===========================================
    // 내부 클래스들
    // ===========================================

    /**
     * 지역별 위험도 통계
     */
    @Data
    @AllArgsConstructor
    public static class RegionRiskStatistics {
        private int totalAreas;
        private int totalAccidents;
        private int totalDeaths;
        private int totalCasualties;
        private double averageRiskScore;

        public double getFatalityRate() {
            return totalCasualties > 0 ? (double) totalDeaths / totalCasualties * 100 : 0.0;
        }

        public String getSummary() {
            return String.format("지역:%d개, 사고:%d건, 사망:%d명, 평균위험도:%.1f",
                    totalAreas, totalAccidents, totalDeaths, averageRiskScore);
        }
    }

    /**
     * 위험지역 데이터 품질 등급
     */
    public enum RiskAreaDataQuality {
        EXCELLENT("최고", "90% 이상의 완전한 데이터"),
        GOOD("양호", "70-90%의 양질 데이터"),
        FAIR("보통", "50-70%의 사용 가능한 데이터"),
        POOR("미흡", "30-50%의 제한적 데이터"),
        VERY_POOR("매우미흡", "30% 미만의 불완전한 데이터"),
        NO_DATA("데이터없음", "위험지역 데이터 없음");

        private final String description;
        private final String detail;

        RiskAreaDataQuality(String description, String detail) {
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