// infrastructure/adapter/out/external/dto/KoroadRiskIndexResponse.java
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
public class KoroadRiskIndexResponse extends KoroadBaseResponse {

    /**
     * 위험지수 데이터 항목들
     * 세부링크 도로위험지수정보 API 응답
     */
    @JsonProperty("items")
    private List<KoroadRiskIndexItem> items;

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
    public List<KoroadRiskIndexItem> getSafeItems() {
        return items != null ? items : Collections.emptyList();
    }

    /**
     * 유효한 아이템들만 필터링해서 반환
     */
    public List<KoroadRiskIndexItem> getValidItems() {
        return getSafeItems().stream()
                .filter(item -> item != null && item.isValid())
                .toList();
    }

    // ===========================================
    // 위험지수 분석 메서드들
    // ===========================================

    /**
     * 전체 경로의 평균 위험지수 계산
     */
    public Double getAverageRiskIndex() {
        List<KoroadRiskIndexItem> validItems = getValidItems();
        if (validItems.isEmpty()) {
            return 0.0;
        }

        return validItems.stream()
                .filter(item -> item.getAnalsValue() != null)
                .mapToDouble(KoroadRiskIndexItem::getAnalsValue)
                .average()
                .orElse(0.0);
    }

    /**
     * 전체 경로의 평균 위험등급 계산
     */
    public Double getAverageRiskGrade() {
        List<KoroadRiskIndexItem> validItems = getValidItems();
        if (validItems.isEmpty()) {
            return 0.0;
        }

        return validItems.stream()
                .filter(item -> item.getAnalsGrd() != null)
                .mapToDouble(KoroadRiskIndexItem::getAnalsGrd)
                .average()
                .orElse(0.0);
    }

    /**
     * 최고 위험도 구간 반환
     */
    public KoroadRiskIndexItem getHighestRiskSegment() {
        return getSafeItems().stream()
                .filter(item -> item.getAnalsGrd() != null)
                .max((a, b) -> Integer.compare(a.getAnalsGrd(), b.getAnalsGrd()))
                .orElse(null);
    }

    /**
     * 최저 위험도 구간 반환
     */
    public KoroadRiskIndexItem getLowestRiskSegment() {
        return getSafeItems().stream()
                .filter(item -> item.getAnalsGrd() != null)
                .min((a, b) -> Integer.compare(a.getAnalsGrd(), b.getAnalsGrd()))
                .orElse(null);
    }

    /**
     * 위험등급별 구간 분포 계산
     */
    public Map<Integer, Long> getRiskGradeDistribution() {
        return getSafeItems().stream()
                .filter(item -> item.getAnalsGrd() != null)
                .collect(Collectors.groupingBy(
                        KoroadRiskIndexItem::getAnalsGrd,
                        Collectors.counting()
                ));
    }

    /**
     * 위험등급별 구간 분포 (문자열 레벨)
     */
    public Map<String, Long> getRiskLevelDistribution() {
        return getSafeItems().stream()
                .filter(item -> item.getAnalsGrd() != null)
                .collect(Collectors.groupingBy(
                        item -> convertGradeToLevel(item.getAnalsGrd()),
                        Collectors.counting()
                ));
    }

    /**
     * 위험 구간들만 필터링 (등급 3, 4)
     */
    public List<KoroadRiskIndexItem> getHighRiskSegments() {
        return getSafeItems().stream()
                .filter(item -> item.getAnalsGrd() != null &&
                        (item.getAnalsGrd() == 3 || item.getAnalsGrd() == 4))
                .toList();
    }

    /**
     * 안전 구간들만 필터링 (등급 1, 2)
     */
    public List<KoroadRiskIndexItem> getSafeSegments() {
        return getSafeItems().stream()
                .filter(item -> item.getAnalsGrd() != null &&
                        (item.getAnalsGrd() == 1 || item.getAnalsGrd() == 2))
                .toList();
    }

    // ===========================================
    // 경로 분석 메서드들
    // ===========================================

    /**
     * 전체 경로 길이 계산 (모든 구간 합계)
     */
    public Double getTotalRouteLength() {
        return getSafeItems().stream()
                .mapToDouble(item -> {
                    Double length = item.calculateSegmentLength();
                    return length != null ? length : 0.0;
                })
                .sum();
    }

    /**
     * 위험 구간의 총 길이 계산
     */
    public Double getHighRiskRouteLength() {
        return getHighRiskSegments().stream()
                .mapToDouble(item -> {
                    Double length = item.calculateSegmentLength();
                    return length != null ? length : 0.0;
                })
                .sum();
    }

    /**
     * 위험 구간 비율 계산 (%)
     */
    public Double getHighRiskRatio() {
        Double totalLength = getTotalRouteLength();
        if (totalLength == 0) {
            return 0.0;
        }

        Double highRiskLength = getHighRiskRouteLength();
        return (highRiskLength / totalLength) * 100;
    }

    /**
     * 경로의 전반적인 안전도 평가
     */
    public RouteSafetyLevel evaluateOverallSafety() {
        if (isEmpty()) {
            return RouteSafetyLevel.UNKNOWN;
        }

        Double avgGrade = getAverageRiskGrade();
        Double highRiskRatio = getHighRiskRatio();

        if (avgGrade >= 3.5 || highRiskRatio >= 50) {
            return RouteSafetyLevel.VERY_DANGEROUS;
        } else if (avgGrade >= 2.5 || highRiskRatio >= 30) {
            return RouteSafetyLevel.DANGEROUS;
        } else if (avgGrade >= 2.0 || highRiskRatio >= 15) {
            return RouteSafetyLevel.MODERATE_RISK;
        } else if (avgGrade >= 1.5 || highRiskRatio >= 5) {
            return RouteSafetyLevel.LOW_RISK;
        } else {
            return RouteSafetyLevel.SAFE;
        }
    }

    // ===========================================
    // 차종별 분석 메서드들
    // ===========================================

    /**
     * 차종별 위험도 그룹화 (응답에 차종 정보가 포함된 경우)
     */
    public Map<String, List<KoroadRiskIndexItem>> groupByVehicleType() {
        return getSafeItems().stream()
                .filter(item -> item.getVehicleType() != null)
                .collect(Collectors.groupingBy(KoroadRiskIndexItem::getVehicleType));
    }

    /**
     * 특정 차종의 평균 위험도 계산
     */
    public Double getAverageRiskForVehicleType(String vehicleType) {
        return getSafeItems().stream()
                .filter(item -> vehicleType.equals(item.getVehicleType()) &&
                        item.getAnalsGrd() != null)
                .mapToInt(KoroadRiskIndexItem::getAnalsGrd)
                .average()
                .orElse(0.0);
    }

    // ===========================================
    // 좌표 및 공간 분석 메서드들
    // ===========================================

    /**
     * 유효한 좌표를 가진 구간들만 필터링
     */
    public List<KoroadRiskIndexItem> getSegmentsWithValidCoordinates() {
        return getSafeItems().stream()
                .filter(KoroadRiskIndexItem::hasValidCoordinates)
                .toList();
    }

    /**
     * 경로의 시작점 반환
     */
    public KoroadRiskIndexItem.Coordinate getRouteStartPoint() {
        List<KoroadRiskIndexItem> validItems = getValidItems();
        if (validItems.isEmpty()) {
            return null;
        }

        KoroadRiskIndexItem firstItem = validItems.get(0);
        return firstItem.getStartPoint();
    }

    /**
     * 경로의 끝점 반환
     */
    public KoroadRiskIndexItem.Coordinate getRouteEndPoint() {
        List<KoroadRiskIndexItem> validItems = getValidItems();
        if (validItems.isEmpty()) {
            return null;
        }

        KoroadRiskIndexItem lastItem = validItems.get(validItems.size() - 1);
        return lastItem.getEndPoint();
    }

    /**
     * 경로의 경계 상자 계산
     */
    public RouteBoundingBox calculateRouteBoundingBox() {
        List<KoroadRiskIndexItem.Coordinate> allCoords = getSafeItems().stream()
                .flatMap(item -> item.parseCoordinates().stream())
                .toList();

        if (allCoords.isEmpty()) {
            return null;
        }

        double minLon = allCoords.stream().mapToDouble(KoroadRiskIndexItem.Coordinate::getLongitude).min().orElse(0);
        double maxLon = allCoords.stream().mapToDouble(KoroadRiskIndexItem.Coordinate::getLongitude).max().orElse(0);
        double minLat = allCoords.stream().mapToDouble(KoroadRiskIndexItem.Coordinate::getLatitude).min().orElse(0);
        double maxLat = allCoords.stream().mapToDouble(KoroadRiskIndexItem.Coordinate::getLatitude).max().orElse(0);

        return new RouteBoundingBox(minLon, minLat, maxLon, maxLat);
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
     * 데이터 품질 체크
     */
    public RiskIndexDataQuality checkDataQuality() {
        if (isEmpty()) {
            return RiskIndexDataQuality.NO_DATA;
        }

        List<KoroadRiskIndexItem> validItems = getValidItems();
        double validRatio = (double) validItems.size() / getItemCount();

        long coordValidCount = getSafeItems().stream()
                .filter(KoroadRiskIndexItem::hasValidCoordinates)
                .count();
        double coordRatio = (double) coordValidCount / getItemCount();

        double avgQuality = (validRatio + coordRatio) / 2.0;

        if (avgQuality >= 0.9) {
            return RiskIndexDataQuality.EXCELLENT;
        } else if (avgQuality >= 0.7) {
            return RiskIndexDataQuality.GOOD;
        } else if (avgQuality >= 0.5) {
            return RiskIndexDataQuality.FAIR;
        } else {
            return RiskIndexDataQuality.POOR;
        }
    }

    // ===========================================
    // 유틸리티 메서드들
    // ===========================================

    /**
     * 위험등급 숫자를 문자열 레벨로 변환
     */
    private String convertGradeToLevel(Integer grade) {
        if (grade == null) {
            return "UNKNOWN";
        }

        return switch (grade) {
            case 1 -> "SAFE";
            case 2 -> "CAUTION";
            case 3 -> "DANGER";
            case 4 -> "VERY_DANGER";
            default -> "UNKNOWN";
        };
    }

    /**
     * 위험지수 요약 정보
     */
    public String getRiskIndexSummary() {
        return String.format("위험지수 요약 - 구간:%d개, 평균등급:%.1f, 위험구간:%.1f%%, 총길이:%.0fm, 전체안전도:%s",
                getItemCount(),
                getAverageRiskGrade(),
                getHighRiskRatio(),
                getTotalRouteLength(),
                evaluateOverallSafety().getDescription());
    }

    /**
     * 위험등급 분포 요약
     */
    public String getRiskDistributionSummary() {
        Map<String, Long> distribution = getRiskLevelDistribution();
        return String.format("위험분포 - 안전:%d개, 주의:%d개, 위험:%d개, 매우위험:%d개",
                distribution.getOrDefault("SAFE", 0L),
                distribution.getOrDefault("CAUTION", 0L),
                distribution.getOrDefault("DANGER", 0L),
                distribution.getOrDefault("VERY_DANGER", 0L));
    }

    @Override
    public String toString() {
        return String.format("KoroadRiskIndexResponse{resultCode='%s', segments=%d, avgGrade=%.1f, safety=%s}",
                getResultCode(),
                getItemCount(),
                getAverageRiskGrade(),
                evaluateOverallSafety());
    }

    // ===========================================
    // 내부 클래스들
    // ===========================================

    /**
     * 경로 안전도 등급
     */
    public enum RouteSafetyLevel {
        SAFE("안전", "대부분 안전한 구간"),
        LOW_RISK("낮은위험", "일부 주의 구간 포함"),
        MODERATE_RISK("보통위험", "주의가 필요한 구간들"),
        DANGEROUS("위험", "많은 위험 구간 포함"),
        VERY_DANGEROUS("매우위험", "대부분 위험한 구간"),
        UNKNOWN("알수없음", "위험도 평가 불가");

        private final String description;
        private final String detail;

        RouteSafetyLevel(String description, String detail) {
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

    /**
     * 위험지수 데이터 품질 등급
     */
    public enum RiskIndexDataQuality {
        EXCELLENT("최고", "90% 이상의 완전한 데이터"),
        GOOD("양호", "70-90%의 양질 데이터"),
        FAIR("보통", "50-70%의 사용 가능한 데이터"),
        POOR("미흡", "50% 미만의 불완전한 데이터"),
        NO_DATA("데이터없음", "위험지수 데이터 없음");

        private final String description;
        private final String detail;

        RiskIndexDataQuality(String description, String detail) {
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

    /**
     * 경로 경계 상자
     */
    @Data
    @AllArgsConstructor
    public static class RouteBoundingBox {
        private double minLongitude;
        private double minLatitude;
        private double maxLongitude;
        private double maxLatitude;

        /**
         * 경계 상자의 중심점 반환
         */
        public KoroadRiskIndexItem.Coordinate getCenter() {
            return new KoroadRiskIndexItem.Coordinate(
                    (minLongitude + maxLongitude) / 2.0,
                    (minLatitude + maxLatitude) / 2.0
            );
        }

        /**
         * 경계 상자의 너비 (경도 차이)
         */
        public double getWidth() {
            return maxLongitude - minLongitude;
        }

        /**
         * 경계 상자의 높이 (위도 차이)
         */
        public double getHeight() {
            return maxLatitude - minLatitude;
        }
    }
}