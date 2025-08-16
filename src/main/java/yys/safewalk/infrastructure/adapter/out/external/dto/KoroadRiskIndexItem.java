// infrastructure/adapter/out/external/dto/KoroadRiskIndexItem.java
package yys.safewalk.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoroadRiskIndexItem {

    /**
     * 순번
     */
    @JsonProperty("index")
    private Integer index;

    /**
     * 구간좌표 (EPSG 4326)
     * 예: "(128.84327445041353 37.837255381378334, 128.8434883231929 37.83715261967514)"
     */
    @JsonProperty("line_string")
    private String lineString;

    /**
     * 도로위험도지수값
     */
    @JsonProperty("anals_value")
    private Double analsValue;

    /**
     * 도로위험도등급
     * 1: 안전, 2: 주의, 3: 심각, 4: 위험
     */
    @JsonProperty("anals_grd")
    private Integer analsGrd;

    /**
     * 차종 코드 (요청 시 전달된 값, 응답에는 포함되지 않을 수 있음)
     * 01: 승용차, 02: 버스, 03: 택시, 04: 화물차
     */
    private String vehicleType;

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
     * 구간 길이 계산 (하버사인 공식 사용, 미터 단위)
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
        return (bearingDeg + 360) % 360;
    }

    // ===========================================
    // 위험도 분석 메서드들
    // ===========================================

    /**
     * 위험도 등급을 문자열로 반환
     */
    public String getRiskLevelDescription() {
        if (analsGrd == null) {
            return "알 수 없음";
        }

        return switch (analsGrd) {
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
        if (analsGrd == null) {
            return "#808080"; // 회색
        }

        return switch (analsGrd) {
            case 1 -> "#00FF00"; // 녹색 (안전)
            case 2 -> "#FFFF00"; // 노란색 (주의)
            case 3 -> "#FF8000"; // 주황색 (심각)
            case 4 -> "#FF0000"; // 빨간색 (위험)
            default -> "#808080"; // 회색
        };
    }

    /**
     * 위험도 점수 계산 (0-100)
     */
    public Double calculateRiskScore() {
        if (analsGrd == null) {
            return 0.0;
        }

        // 등급을 0-100 점수로 변환
        return switch (analsGrd) {
            case 1 -> 25.0;  // 안전
            case 2 -> 50.0;  // 주의
            case 3 -> 75.0;  // 심각
            case 4 -> 100.0; // 위험
            default -> 0.0;
        };
    }

    /**
     * 위험도 우선순위 반환 (숫자가 높을수록 우선순위 높음)
     */
    public Integer getRiskPriority() {
        if (analsGrd == null) {
            return 0;
        }

        return switch (analsGrd) {
            case 4 -> 4; // 위험 (최우선)
            case 3 -> 3; // 심각
            case 2 -> 2; // 주의
            case 1 -> 1; // 안전
            default -> 0;
        };
    }

    /**
     * 보행자에게 위험한 구간인지 확인
     */
    public boolean isDangerousForPedestrians() {
        return analsGrd != null && analsGrd >= 3; // 심각(3) 이상
    }

    /**
     * 주의가 필요한 구간인지 확인
     */
    public boolean requiresCaution() {
        return analsGrd != null && analsGrd >= 2; // 주의(2) 이상
    }

    /**
     * 안전한 구간인지 확인
     */
    public boolean isSafeSegment() {
        return analsGrd != null && analsGrd == 1;
    }

    // ===========================================
    // 차종 관련 메서드들
    // ===========================================

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
     * 대중교통 차종인지 확인
     */
    public boolean isPublicTransport() {
        return "02".equals(vehicleType) || "03".equals(vehicleType);
    }

    /**
     * 상업용 차량인지 확인
     */
    public boolean isCommercialVehicle() {
        return "02".equals(vehicleType) || "03".equals(vehicleType) || "04".equals(vehicleType);
    }

    /**
     * 대형차량인지 확인 (보행자에게 더 위험)
     */
    public boolean isLargeVehicle() {
        return "02".equals(vehicleType) || "04".equals(vehicleType);
    }

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return lineString != null && !lineString.trim().isEmpty() &&
                analsGrd != null && analsGrd >= 1 && analsGrd <= 4 &&
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
     * 한국 영역 내 좌표인지 확인
     */
    public boolean isWithinKorea() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.isEmpty()) {
            return false;
        }

        // 한국 영역 대략적 경계
        return coords.stream().allMatch(coord ->
                coord.getLongitude() >= 124.0 && coord.getLongitude() <= 132.0 &&
                        coord.getLatitude() >= 33.0 && coord.getLatitude() <= 43.0);
    }

    /**
     * 구간 길이가 적절한지 확인 (너무 짧거나 길지 않은지)
     */
    public boolean hasReasonableLength() {
        Double length = calculateSegmentLength();
        // 1m ~ 10km 사이를 적절한 범위로 간주
        return length >= 1.0 && length <= 10000.0;
    }

    /**
     * 위험지수값과 등급이 일치하는지 확인
     */
    public boolean hasConsistentRiskData() {
        if (analsValue == null || analsGrd == null) {
            return false;
        }

        // 일반적으로 위험지수값이 높을수록 등급도 높아야 함
        // 정확한 매핑 기준은 API 문서 확인 필요
        return analsValue >= 0 && analsGrd >= 1 && analsGrd <= 4;
    }

    // ===========================================
    // 구간 특성 분석 메서드들
    // ===========================================

    /**
     * 직선 구간인지 확인 (좌표점이 2개)
     */
    public boolean isStraightSegment() {
        return parseCoordinates().size() == 2;
    }

    /**
     * 곡선 구간인지 확인 (좌표점이 3개 이상)
     */
    public boolean isCurvedSegment() {
        return parseCoordinates().size() > 2;
    }

    /**
     * 짧은 구간인지 확인 (100m 미만)
     */
    public boolean isShortSegment() {
        Double length = calculateSegmentLength();
        return length != null && length < 100.0;
    }

    /**
     * 긴 구간인지 확인 (1km 이상)
     */
    public boolean isLongSegment() {
        Double length = calculateSegmentLength();
        return length != null && length >= 1000.0;
    }

    // ===========================================
    // 유틸리티 메서드들
    // ===========================================

    /**
     * 좌표 정보 요약
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
     * 구간 정보 요약
     */
    public String getSegmentSummary() {
        Double length = calculateSegmentLength();
        Double bearing = calculateSegmentBearing();

        return String.format("길이:%.0fm, 방향:%.0f°, 위험도:%s",
                length != null ? length : 0.0,
                bearing != null ? bearing : 0.0,
                getRiskLevelDescription());
    }

    /**
     * 캐시 키 생성
     */
    public String toCacheKey() {
        return String.format("riskindex:%s:%s:%d",
                vehicleType != null ? vehicleType : "unknown",
                lineString != null ? String.valueOf(lineString.hashCode()) : "0",
                index != null ? index : 0);
    }

    @Override
    public String toString() {
        return String.format("KoroadRiskIndexItem{index=%d, grade=%d(%s), vehicle=%s, length=%.0fm}",
                index != null ? index : 0,
                analsGrd != null ? analsGrd : 0,
                getRiskLevelDescription(),
                getVehicleTypeName(),
                calculateSegmentLength());
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

        /**
         * 좌표가 유효한지 확인
         */
        public boolean isValid() {
            return longitude >= -180.0 && longitude <= 180.0 &&
                    latitude >= -90.0 && latitude <= 90.0;
        }

        /**
         * 한국 영역 내 좌표인지 확인
         */
        public boolean isWithinKorea() {
            return longitude >= 124.0 && longitude <= 132.0 &&
                    latitude >= 33.0 && latitude <= 43.0;
        }

        @Override
        public String toString() {
            return String.format("[%.6f, %.6f]", longitude, latitude);
        }
    }
}