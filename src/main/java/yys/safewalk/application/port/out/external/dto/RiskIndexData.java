// application/port/out/external/dto/RiskIndexData.java
package yys.safewalk.application.port.out.external.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiskIndexData {

    /**
     * 순번
     */
    private Integer index;

    /**
     * 구간좌표 (EPSG 4326)
     * 예: "(128.84327445041353 37.837255381378334, 128.8434883231929 37.83715261967514)"
     */
    private String lineString;

    /**
     * 도로위험도지수값
     */
    private Double analysisValue;

    /**
     * 도로위험도등급
     * 1: 안전, 2: 주의, 3: 심각, 4: 위험
     */
    private Integer analysisGrade;

    /**
     * 위험도 레벨 (enum 문자열)
     * SAFE, CAUTION, DANGER, VERY_DANGER, UNKNOWN
     */
    private String riskLevel;

    /**
     * 데이터 수집 시간
     */
    @Builder.Default
    private LocalDateTime collectedAt = LocalDateTime.now();

    /**
     * 차종 코드
     * 01: 승용차, 02: 버스, 03: 택시, 04: 화물차
     */
    private String vehicleType;

    /**
     * 구간 길이 (미터, 계산된 값)
     */
    private Double segmentLength;

    /**
     * 구간 방향 (도, 계산된 값)
     */
    private Double segmentBearing;

    // ===========================================
    // 좌표 파싱 및 계산 메서드들
    // ===========================================

    /**
     * LineString에서 좌표 리스트 추출
     */
    public List<Coordinate> parseCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();

        if (lineString == null || lineString.trim().isEmpty()) {
            return coordinates;
        }

        try {
            // LineString 형태: "(lon1 lat1, lon2 lat2, ...)" 또는 "LineString(lon1 lat1, lon2 lat2, ...)"
            String cleanedString = lineString
                    .replaceAll("LineString\\s*\\(", "")
                    .replaceAll("^\\(", "")
                    .replaceAll("\\)$", "")
                    .trim();

            String[] coordPairs = cleanedString.split(",");

            for (String coordPair : coordPairs) {
                String[] lonLat = coordPair.trim().split("\\s+");
                if (lonLat.length >= 2) {
                    double lon = Double.parseDouble(lonLat[0]);
                    double lat = Double.parseDouble(lonLat[1]);
                    coordinates.add(new Coordinate(lon, lat));
                }
            }
        } catch (Exception e) {
            // 파싱 실패시 빈 리스트 반환
        }

        return coordinates;
    }

    /**
     * 시작점 좌표 반환
     */
    public Coordinate getStartPoint() {
        List<Coordinate> coords = parseCoordinates();
        return coords.isEmpty() ? null : coords.get(0);
    }

    /**
     * 끝점 좌표 반환
     */
    public Coordinate getEndPoint() {
        List<Coordinate> coords = parseCoordinates();
        return coords.isEmpty() ? null : coords.get(coords.size() - 1);
    }

    /**
     * 중심점 좌표 계산
     */
    public Coordinate getCenterPoint() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.isEmpty()) {
            return null;
        }

        double totalLon = coords.stream().mapToDouble(Coordinate::getLongitude).sum();
        double totalLat = coords.stream().mapToDouble(Coordinate::getLatitude).sum();

        return new Coordinate(totalLon / coords.size(), totalLat / coords.size());
    }

    /**
     * 구간 길이 계산 (하버사인 공식 사용)
     */
    public Double calculateSegmentLength() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.size() < 2) {
            return 0.0;
        }

        double totalLength = 0.0;
        for (int i = 0; i < coords.size() - 1; i++) {
            totalLength += calculateDistance(coords.get(i), coords.get(i + 1));
        }

        this.segmentLength = totalLength;
        return totalLength;
    }

    /**
     * 두 좌표 간 거리 계산 (하버사인 공식, 미터 단위)
     */
    private double calculateDistance(Coordinate coord1, Coordinate coord2) {
        final double R = 6371000; // 지구 반지름 (미터)

        double lat1Rad = Math.toRadians(coord1.getLatitude());
        double lat2Rad = Math.toRadians(coord2.getLatitude());
        double deltaLatRad = Math.toRadians(coord2.getLatitude() - coord1.getLatitude());
        double deltaLonRad = Math.toRadians(coord2.getLongitude() - coord1.getLongitude());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 구간 방향 계산 (베어링, 도 단위)
     */
    public Double calculateSegmentBearing() {
        Coordinate start = getStartPoint();
        Coordinate end = getEndPoint();

        if (start == null || end == null) {
            return null;
        }

        double lat1Rad = Math.toRadians(start.getLatitude());
        double lat2Rad = Math.toRadians(end.getLatitude());
        double deltaLonRad = Math.toRadians(end.getLongitude() - start.getLongitude());

        double x = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double y = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);

        double bearingRad = Math.atan2(x, y);
        double bearingDeg = Math.toDegrees(bearingRad);

        // 0-360도로 정규화
        this.segmentBearing = (bearingDeg + 360) % 360;
        return this.segmentBearing;
    }

    // ===========================================
    // 위험도 분석 메서드들
    // ===========================================

    /**
     * 위험도 등급을 문자열로 반환
     */
    public String getRiskLevelDescription() {
        if (analysisGrade == null) {
            return "알 수 없음";
        }

        return switch (analysisGrade) {
            case 1 -> "안전";
            case 2 -> "주의";
            case 3 -> "심각";
            case 4 -> "위험";
            default -> "알 수 없음";
        };
    }

    /**
     * 위험도 색상 코드 반환 (UI용)
     */
    public String getRiskColorCode() {
        if (analysisGrade == null) {
            return "#808080"; // 회색
        }

        return switch (analysisGrade) {
            case 1 -> "#00FF00"; // 녹색 (안전)
            case 2 -> "#FFFF00"; // 노란색 (주의)
            case 3 -> "#FF8000"; // 주황색 (심각)
            case 4 -> "#FF0000"; // 빨간색 (위험)
            default -> "#808080"; // 회색
        };
    }

    /**
     * 차종명 반환
     */
    public String getVehicleTypeName() {
        if (vehicleType == null) {
            return "알 수 없음";
        }

        return switch (vehicleType) {
            case "01" -> "승용차";
            case "02" -> "버스";
            case "03" -> "택시";
            case "04" -> "화물차";
            default -> "알 수 없음";
        };
    }

    /**
     * 위험도 점수 계산 (0-100)
     */
    public Double calculateRiskScore() {
        if (analysisGrade == null) {
            return 0.0;
        }

        // 등급을 0-100 점수로 변환
        return switch (analysisGrade) {
            case 1 -> 25.0;  // 안전
            case 2 -> 50.0;  // 주의
            case 3 -> 75.0;  // 심각
            case 4 -> 100.0; // 위험
            default -> 0.0;
        };
    }

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return lineString != null && !lineString.trim().isEmpty() &&
                analysisGrade != null && analysisGrade >= 1 && analysisGrade <= 4 &&
                !parseCoordinates().isEmpty();
    }

    /**
     * 좌표 정보 유효성 확인
     */
    public boolean hasValidCoordinates() {
        List<Coordinate> coords = parseCoordinates();
        return !coords.isEmpty() &&
                coords.stream().allMatch(coord ->
                        coord.getLongitude() >= -180 && coord.getLongitude() <= 180 &&
                                coord.getLatitude() >= -90 && coord.getLatitude() <= 90);
    }

    /**
     * 실시간 데이터인지 확인 (수집 시간 기준)
     */
    public boolean isRealTimeData() {
        if (collectedAt == null) {
            return false;
        }

        // 1시간 이내 데이터를 실시간으로 간주
        return collectedAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    // ===========================================
    // 유틸리티 메서드들
    // ===========================================

    /**
     * 캐시 키 생성
     */
    public String toCacheKey() {
        return String.format("riskindex:%s:%s", vehicleType, lineString.hashCode());
    }

    /**
     * 좌표 요약 정보
     */
    public String getCoordinateSummary() {
        Coordinate start = getStartPoint();
        Coordinate end = getEndPoint();

        if (start == null || end == null) {
            return "좌표정보 없음";
        }

        return String.format("시작점[%.6f, %.6f] → 끝점[%.6f, %.6f]",
                start.getLongitude(), start.getLatitude(),
                end.getLongitude(), end.getLatitude());
    }

    /**
     * 요약 정보 문자열
     */
    public String toSummary() {
        return String.format("위험지수 - 등급:%s, 차종:%s, 길이:%.0fm, 방향:%.0f°",
                getRiskLevelDescription(),
                getVehicleTypeName(),
                segmentLength != null ? segmentLength : calculateSegmentLength(),
                segmentBearing != null ? segmentBearing : calculateSegmentBearing());
    }

    @Override
    public String toString() {
        return String.format("RiskIndexData{index=%d, grade=%d(%s), vehicle=%s, length=%.0fm}",
                index != null ? index : 0,
                analysisGrade != null ? analysisGrade : 0,
                getRiskLevelDescription(),
                getVehicleTypeName(),
                segmentLength != null ? segmentLength : 0.0);
    }

    // ===========================================
    // 내부 클래스: 좌표
    // ===========================================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Coordinate {
        private double longitude;
        private double latitude;
    }
}