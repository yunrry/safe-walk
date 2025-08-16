// application/port/out/external/dto/RouteInfo.java
package yys.safewalk.application.port.out.external.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteInfo {

    /**
     * 링크경로 (LineString 형태, EPSG 4326)
     * 예: "LineString(126.9877427718531 37.571846624073224, 126.9878281347612 37.572345363517954)"
     */
    @NotBlank(message = "링크경로는 필수입니다")
    private String lineString;

    /**
     * 차종구분 코드
     * 01: 승용차, 02: 버스, 03: 택시, 04: 화물차
     */
    @NotBlank(message = "차종구분은 필수입니다")
    @Pattern(regexp = "0[1-4]", message = "차종구분은 01~04 사이여야 합니다")
    private String vehicleType;

    /**
     * 경로명 (옵션)
     */
    private String routeName;

    /**
     * 경로 설명 (옵션)
     */
    private String description;

    /**
     * 예상 소요 시간 (분)
     */
    private Integer estimatedDurationMinutes;

    /**
     * 경로 총 거리 (미터)
     */
    private Double totalDistanceMeters;

    /**
     * 출발지명
     */
    private String startLocationName;

    /**
     * 도착지명
     */
    private String endLocationName;

    // ===========================================
    // 정적 팩토리 메서드들
    // ===========================================

    /**
     * 두 좌표점을 연결하는 기본 경로 생성
     */
    public static RouteInfo createSimpleRoute(double startLon, double startLat,
                                              double endLon, double endLat,
                                              String vehicleType) {
        String lineString = String.format("LineString(%.10f %.10f, %.10f %.10f)",
                startLon, startLat, endLon, endLat);

        return RouteInfo.builder()
                .lineString(lineString)
                .vehicleType(vehicleType)
                .build();
    }

    /**
     * 다중 좌표점을 연결하는 경로 생성
     */
    public static RouteInfo createMultiPointRoute(List<Coordinate> coordinates, String vehicleType) {
        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException("최소 2개 이상의 좌표가 필요합니다");
        }

        String coordString = coordinates.stream()
                .map(coord -> String.format("%.10f %.10f", coord.getLongitude(), coord.getLatitude()))
                .collect(Collectors.joining(", "));

        String lineString = "LineString(" + coordString + ")";

        return RouteInfo.builder()
                .lineString(lineString)
                .vehicleType(vehicleType)
                .build();
    }

    /**
     * 승용차용 경로 생성
     */
    public static RouteInfo forCar(String lineString) {
        return RouteInfo.builder()
                .lineString(lineString)
                .vehicleType("01")
                .build();
    }

    /**
     * 버스용 경로 생성
     */
    public static RouteInfo forBus(String lineString) {
        return RouteInfo.builder()
                .lineString(lineString)
                .vehicleType("02")
                .build();
    }

    /**
     * 택시용 경로 생성
     */
    public static RouteInfo forTaxi(String lineString) {
        return RouteInfo.builder()
                .lineString(lineString)
                .vehicleType("03")
                .build();
    }

    /**
     * 화물차용 경로 생성
     */
    public static RouteInfo forTruck(String lineString) {
        return RouteInfo.builder()
                .lineString(lineString)
                .vehicleType("04")
                .build();
    }

    // ===========================================
    // 좌표 파싱 및 분석 메서드들
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
            // LineString 형태에서 좌표 추출
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
     * 중간점들 반환 (시작점과 끝점 제외)
     */
    public List<Coordinate> getWaypoints() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.size() <= 2) {
            return new ArrayList<>();
        }
        return coords.subList(1, coords.size() - 1);
    }

    /**
     * 경로 총 거리 계산 (하버사인 공식)
     */
    public Double calculateTotalDistance() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < coords.size() - 1; i++) {
            totalDistance += calculateDistance(coords.get(i), coords.get(i + 1));
        }

        this.totalDistanceMeters = totalDistance;
        return totalDistance;
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
     * 경로의 경계 상자 계산 (Bounding Box)
     */
    public BoundingBox calculateBoundingBox() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.isEmpty()) {
            return null;
        }

        double minLon = coords.stream().mapToDouble(Coordinate::getLongitude).min().orElse(0);
        double maxLon = coords.stream().mapToDouble(Coordinate::getLongitude).max().orElse(0);
        double minLat = coords.stream().mapToDouble(Coordinate::getLatitude).min().orElse(0);
        double maxLat = coords.stream().mapToDouble(Coordinate::getLatitude).max().orElse(0);

        return new BoundingBox(minLon, minLat, maxLon, maxLat);
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
     * 보행자에게 위험한 차종인지 확인
     */
    public boolean isDangerousTopedestrians() {
        // 버스와 화물차는 보행자에게 더 위험
        return "02".equals(vehicleType) || "04".equals(vehicleType);
    }

    /**
     * 대중교통 차종인지 확인
     */
    public boolean isPublicTransport() {
        return "02".equals(vehicleType) || "03".equals(vehicleType);
    }

    // ===========================================
    // 유효성 검증 메서드들
    // ===========================================

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return lineString != null && !lineString.trim().isEmpty() &&
                vehicleType != null && vehicleType.matches("0[1-4]") &&
                !parseCoordinates().isEmpty() &&
                parseCoordinates().size() >= 2;
    }

    /**
     * 좌표가 한국 영역 내에 있는지 확인
     */
    public boolean isWithinKorea() {
        List<Coordinate> coords = parseCoordinates();
        if (coords.isEmpty()) {
            return false;
        }

        // 한국 영역 대략적 경계 (위경도)
        // 경도: 124°~132°, 위도: 33°~43°
        return coords.stream().allMatch(coord ->
                coord.getLongitude() >= 124.0 && coord.getLongitude() <= 132.0 &&
                        coord.getLatitude() >= 33.0 && coord.getLatitude() <= 43.0);
    }

    /**
     * 경로 길이가 적절한지 확인 (너무 짧거나 길지 않은지)
     */
    public boolean hasReasonableLength() {
        Double distance = calculateTotalDistance();
        // 10m ~ 100km 사이를 적절한 범위로 간주
        return distance >= 10.0 && distance <= 100000.0;
    }

    // ===========================================
    // 유틸리티 메서드들
    // ===========================================

    /**
     * API 요청용 LineString 반환 (인코딩 처리)
     */
    public String getEncodedLineString() {
        if (lineString == null) {
            return null;
        }
        // URL 인코딩이 필요한 경우 처리
        return lineString.replace(" ", "%20").replace(",", "%2C");
    }

    /**
     * 캐시 키 생성
     */
    public String toCacheKey() {
        return String.format("route:%s:%s", vehicleType, lineString.hashCode());
    }

    /**
     * 경로 요약 정보
     */
    public String toSummary() {
        Coordinate start = getStartPoint();
        Coordinate end = getEndPoint();
        Double distance = calculateTotalDistance();

        return String.format("경로 - 차종:%s, 거리:%.0fm, 시작점[%.6f,%.6f] → 끝점[%.6f,%.6f]",
                getVehicleTypeName(),
                distance != null ? distance : 0.0,
                start != null ? start.getLongitude() : 0.0,
                start != null ? start.getLatitude() : 0.0,
                end != null ? end.getLongitude() : 0.0,
                end != null ? end.getLatitude() : 0.0);
    }

    @Override
    public String toString() {
        return String.format("RouteInfo{vehicle=%s(%s), points=%d, distance=%.0fm}",
                vehicleType, getVehicleTypeName(),
                parseCoordinates().size(),
                totalDistanceMeters != null ? totalDistanceMeters : 0.0);
    }

    // ===========================================
    // 내부 클래스들
    // ===========================================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Coordinate {
        private double longitude;
        private double latitude;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoundingBox {
        private double minLongitude;
        private double minLatitude;
        private double maxLongitude;
        private double maxLatitude;

        /**
         * 경계 상자의 중심점 반환
         */
        public Coordinate getCenter() {
            return new Coordinate(
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