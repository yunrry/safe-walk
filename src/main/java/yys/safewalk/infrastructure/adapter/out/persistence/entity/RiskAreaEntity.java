package yys.safewalk.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 링크기반 사고위험지역정보 API 엔티티
 * 도로 링크 기반의 사고위험지역 정보를 저장
 *
 * API 정보: 링크기반 사고위험지역정보 API
 * 데이터 제공기간: 도로 링크별 위험지역 데이터
 */
@Entity
@Table(
        name = "risk_area",
        indexes = {
                @Index(name = "idx_risk_sido_sgg", columnList = "sido_sgg_nm"),
                @Index(name = "idx_risk_year", columnList = "search_year_cd"),
                @Index(name = "idx_risk_occurrence_cnt", columnList = "occrrnc_cnt"),
                @Index(name = "idx_risk_casualty_cnt", columnList = "caslt_cnt"),
                @Index(name = "idx_risk_death_cnt", columnList = "dth_dnv_cnt"),
                @Index(name = "idx_risk_coordinates", columnList = "lo_crd, la_crd"),
                @Index(name = "idx_risk_link_id", columnList = "link_id"),
                @Index(name = "idx_risk_road_name", columnList = "road_nm"),
                @Index(name = "idx_risk_fatality_rate", columnList = "fatality_rate"),
                @Index(name = "idx_risk_score", columnList = "risk_score"),
                @Index(name = "idx_risk_created_at", columnList = "created_at"),
                @Index(name = "idx_risk_road_type", columnList = "road_type"),
                @Index(name = "idx_risk_traffic_volume", columnList = "traffic_volume_level"),
                @Index(name = "idx_risk_link_length", columnList = "link_length"),
                @Index(name = "idx_risk_composite", columnList = "search_year_cd, sido_sgg_nm, risk_level"),
                @Index(name = "idx_risk_road_analysis", columnList = "road_type, traffic_volume_level, risk_level")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("링크기반 사고위험지역정보")
public class RiskAreaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("기본키")
    private Long id;

    // ===== API 원본 필드 =====

    @Column(name = "link_id", nullable = false, length = 20)
    @Comment("링크ID - 도로 링크 식별자")
    private String linkId;

    @Column(name = "road_nm", nullable = false, length = 100)
    @Comment("도로명 - 해당 링크의 도로명")
    private String roadNm;

    @Column(name = "sido_sgg_nm", nullable = false, length = 40)
    @Comment("시도시군구명 - 링크가 위치한 시도시군구명")
    private String sidoSggNm;

    @Column(name = "start_node_nm", length = 100)
    @Comment("시작노드명 - 링크 시작지점명")
    private String startNodeNm;

    @Column(name = "end_node_nm", length = 100)
    @Comment("종료노드명 - 링크 종료지점명")
    private String endNodeNm;

    @Column(name = "occrrnc_cnt", nullable = false)
    @Comment("사고건수 - 해당 링크에서 발생한 사고건수")
    private Integer occrrnc_cnt;

    @Column(name = "caslt_cnt", nullable = false)
    @Comment("사상자수 - 해당 링크에서 발생한 사상자수")
    private Integer casltCnt;

    @Column(name = "dth_dnv_cnt", nullable = false)
    @Comment("사망자수 - 해당 링크에서 발생한 사망자수")
    private Integer dthDnvCnt;

    @Column(name = "se_dnv_cnt", nullable = false)
    @Comment("중상자수 - 해당 링크에서 발생한 중상자수")
    private Integer seDnvCnt;

    @Column(name = "sl_dnv_cnt", nullable = false)
    @Comment("경상자수 - 해당 링크에서 발생한 경상자수")
    private Integer slDnvCnt;

    @Column(name = "wnd_dnv_cnt", nullable = false)
    @Comment("부상신고자수 - 해당 링크에서 발생한 부상신고자수")
    private Integer wndDnvCnt;

    @Column(name = "lo_crd", nullable = false, precision = 16, scale = 12)
    @Comment("경도 - 링크 중심점의 경도(EPSG 4326)")
    private BigDecimal loCrd;

    @Column(name = "la_crd", nullable = false, precision = 15, scale = 12)
    @Comment("위도 - 링크 중심점의 위도(EPSG 4326)")
    private BigDecimal laCrd;

    @Column(name = "link_length", precision = 10, scale = 2)
    @Comment("링크길이 - 도로 링크의 길이(미터)")
    private BigDecimal linkLength;

    @Column(name = "road_width", precision = 8, scale = 2)
    @Comment("도로폭 - 도로의 폭(미터)")
    private BigDecimal roadWidth;

    @Column(name = "lane_count")
    @Comment("차선수 - 해당 도로의 차선수")
    private Integer laneCount;

    // ===== 요청 파라미터 정보 =====

    @Column(name = "search_year_cd", nullable = false, length = 7)
    @Comment("검색연도코드 - API 요청시 사용한 연도")
    private String searchYearCd;

    @Column(name = "sido_cd", length = 2)
    @Comment("시도코드 - API 요청시 사용한 법정동 시도 코드")
    private String sidoCd;

    @Column(name = "gugun_cd", length = 3)
    @Comment("시군구코드 - API 요청시 사용한 법정동 시군구 코드")
    private String gugunCd;

    // ===== 링크 특화 필드 =====

    @Enumerated(EnumType.STRING)
    @Column(name = "road_type", length = 20)
    @Comment("도로유형 - HIGHWAY(고속도로), NATIONAL(국도), PROVINCIAL(지방도), LOCAL(시군구도), URBAN(도시부도로)")
    private RoadType roadType;

    @Enumerated(EnumType.STRING)
    @Column(name = "road_grade", length = 20)
    @Comment("도로등급 - GRADE_1(1급), GRADE_2(2급), GRADE_3(3급), GRADE_4(4급)")
    private RoadGrade roadGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_volume_level", length = 20)
    @Comment("교통량수준 - VERY_HIGH(매우높음), HIGH(높음), MEDIUM(보통), LOW(낮음), VERY_LOW(매우낮음)")
    private TrafficVolumeLevel trafficVolumeLevel;

    @Column(name = "speed_limit")
    @Comment("제한속도 - 해당 링크의 제한속도(km/h)")
    private Integer speedLimit;

    @Column(name = "traffic_signal_count")
    @Comment("신호등수 - 링크 구간 내 신호등 개수")
    private Integer trafficSignalCount;

    @Column(name = "crosswalk_count")
    @Comment("횡단보도수 - 링크 구간 내 횡단보도 개수")
    private Integer crosswalkCount;

    @Column(name = "bus_stop_count")
    @Comment("버스정류장수 - 링크 구간 내 버스정류장 개수")
    private Integer busStopCount;

    // ===== 계산된 필드 =====

    @Column(name = "fatality_rate", precision = 5, scale = 2)
    @Comment("치사율 - 사망자수/사상자수 * 100")
    private BigDecimal fatalityRate;

    @Column(name = "serious_injury_rate", precision = 5, scale = 2)
    @Comment("중상률 - 중상자수/사상자수 * 100")
    private BigDecimal seriousInjuryRate;

    @Column(name = "casualty_per_accident", precision = 5, scale = 2)
    @Comment("사고당 사상자수 - 사상자수/사고건수")
    private BigDecimal casualtyPerAccident;

    @Column(name = "accident_per_km", precision = 8, scale = 2)
    @Comment("km당 사고건수 - 사고건수/(링크길이/1000)")
    private BigDecimal accidentPerKm;

    @Column(name = "risk_score", precision = 8, scale = 2)
    @Comment("위험도 점수 - 링크 특성을 반영한 종합 위험도 계산 점수")
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Comment("위험등급 - VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW")
    private RiskLevel riskLevel;

    @Column(name = "road_safety_index", precision = 8, scale = 2)
    @Comment("도로안전지수 - 도로 인프라 안전성 지수")
    private BigDecimal roadSafetyIndex;

    @Column(name = "traffic_flow_risk", precision = 8, scale = 2)
    @Comment("교통흐름위험도 - 교통량과 속도를 고려한 위험도")
    private BigDecimal trafficFlowRisk;

    @Column(name = "infrastructure_risk", precision = 8, scale = 2)
    @Comment("인프라위험도 - 도로 인프라 시설물 기반 위험도")
    private BigDecimal infrastructureRisk;

    // ===== 메타데이터 =====

    @Column(name = "api_result_code", length = 2)
    @Comment("API 결과코드 - 00:성공, 03:데이터없음, 10:파라미터오류, 99:알수없는오류")
    private String apiResultCode;

    @Column(name = "api_result_msg", length = 100)
    @Comment("API 결과메시지 - API 호출 결과 메시지")
    private String apiResultMsg;

    @Column(name = "total_count")
    @Comment("총건수 - 검색결과 총건수")
    private Integer totalCount;

    @Column(name = "page_no")
    @Comment("페이지번호")
    private Integer pageNo;

    @Column(name = "num_of_rows")
    @Comment("검색건수")
    private Integer numOfRows;

    @Column(name = "data_collection_date")
    @Comment("데이터 수집일자")
    private LocalDateTime dataCollectionDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Comment("수정일시")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    @Comment("생성자")
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    @Comment("수정자")
    private String updatedBy;

    // ===== 열거형 정의 =====

    public enum RoadType {
        HIGHWAY("고속도로"),
        NATIONAL("국도"),
        PROVINCIAL("지방도"),
        LOCAL("시군구도"),
        URBAN("도시부도로"),
        ARTERIAL("간선도로"),
        COLLECTOR("집산도로"),
        LOCAL_STREET("국지도로");

        private final String description;

        RoadType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RoadGrade {
        GRADE_1("1급도로"),
        GRADE_2("2급도로"),
        GRADE_3("3급도로"),
        GRADE_4("4급도로"),
        SPECIAL("특수도로");

        private final String description;

        RoadGrade(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TrafficVolumeLevel {
        VERY_HIGH("매우높음"),
        HIGH("높음"),
        MEDIUM("보통"),
        LOW("낮음"),
        VERY_LOW("매우낮음");

        private final String description;

        TrafficVolumeLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RiskLevel {
        VERY_HIGH("매우 높음"),
        HIGH("높음"),
        MEDIUM("보통"),
        LOW("낮음"),
        VERY_LOW("매우 낮음");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ===== 생성자 및 빌더 메서드 =====

    @Builder
    public RiskAreaEntity(
            String linkId, String roadNm, String sidoSggNm, String startNodeNm, String endNodeNm,
            Integer occrrnc_cnt, Integer casltCnt, Integer dthDnvCnt,
            Integer seDnvCnt, Integer slDnvCnt, Integer wndDnvCnt,
            BigDecimal loCrd, BigDecimal laCrd, BigDecimal linkLength, BigDecimal roadWidth,
            Integer laneCount, String searchYearCd, String sidoCd, String gugunCd,
            RoadType roadType, RoadGrade roadGrade, TrafficVolumeLevel trafficVolumeLevel,
            Integer speedLimit, Integer trafficSignalCount, Integer crosswalkCount, Integer busStopCount,
            String apiResultCode, String apiResultMsg, Integer totalCount,
            Integer pageNo, Integer numOfRows, LocalDateTime dataCollectionDate,
            String createdBy, String updatedBy) {

        this.linkId = linkId;
        this.roadNm = roadNm;
        this.sidoSggNm = sidoSggNm;
        this.startNodeNm = startNodeNm;
        this.endNodeNm = endNodeNm;
        this.occrrnc_cnt = occrrnc_cnt;
        this.casltCnt = casltCnt;
        this.dthDnvCnt = dthDnvCnt;
        this.seDnvCnt = seDnvCnt;
        this.slDnvCnt = slDnvCnt;
        this.wndDnvCnt = wndDnvCnt;
        this.loCrd = loCrd;
        this.laCrd = laCrd;
        this.linkLength = linkLength;
        this.roadWidth = roadWidth;
        this.laneCount = laneCount;
        this.searchYearCd = searchYearCd;
        this.sidoCd = sidoCd;
        this.gugunCd = gugunCd;
        this.roadType = roadType;
        this.roadGrade = roadGrade;
        this.trafficVolumeLevel = trafficVolumeLevel;
        this.speedLimit = speedLimit;
        this.trafficSignalCount = trafficSignalCount;
        this.crosswalkCount = crosswalkCount;
        this.busStopCount = busStopCount;
        this.apiResultCode = apiResultCode;
        this.apiResultMsg = apiResultMsg;
        this.totalCount = totalCount;
        this.pageNo = pageNo;
        this.numOfRows = numOfRows;
        this.dataCollectionDate = dataCollectionDate;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;

        // 계산된 필드들 자동 계산
        calculateDerivedFields();
    }

    // ===== 계산 메서드 =====

    @PrePersist
    @PreUpdate
    public void calculateDerivedFields() {
        if (casltCnt != null && casltCnt > 0) {
            // 치사율 계산
            this.fatalityRate = BigDecimal.valueOf(dthDnvCnt != null ? dthDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(casltCnt), 2, RoundingMode.HALF_UP);

            // 중상률 계산
            this.seriousInjuryRate = BigDecimal.valueOf(seDnvCnt != null ? seDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(casltCnt), 2, RoundingMode.HALF_UP);
        } else {
            this.fatalityRate = BigDecimal.ZERO;
            this.seriousInjuryRate = BigDecimal.ZERO;
        }

        if (occrrnc_cnt != null && occrrnc_cnt > 0) {
            // 사고당 사상자수 계산
            this.casualtyPerAccident = BigDecimal.valueOf(casltCnt != null ? casltCnt : 0)
                    .divide(BigDecimal.valueOf(occrrnc_cnt), 2, RoundingMode.HALF_UP);
        } else {
            this.casualtyPerAccident = BigDecimal.ZERO;
        }

        // km당 사고건수 계산
        calculateAccidentPerKm();

        // 링크 기반 위험도 점수 계산
        calculateLinkRiskScore();

        // 위험등급 결정
        determineRiskLevel();

        // 도로안전지수 계산
        calculateRoadSafetyIndex();

        // 교통흐름위험도 계산
        calculateTrafficFlowRisk();

        // 인프라위험도 계산
        calculateInfrastructureRisk();
    }

    private void calculateAccidentPerKm() {
        if (linkLength != null && linkLength.compareTo(BigDecimal.ZERO) > 0 && occrrnc_cnt != null) {
            // 링크길이를 km로 변환하여 km당 사고건수 계산
            BigDecimal lengthInKm = linkLength.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            this.accidentPerKm = BigDecimal.valueOf(occrrnc_cnt)
                    .divide(lengthInKm, 2, RoundingMode.HALF_UP);
        } else {
            this.accidentPerKm = BigDecimal.ZERO;
        }
    }

    private void calculateLinkRiskScore() {
        if (occrrnc_cnt == null || casltCnt == null) {
            this.riskScore = BigDecimal.ZERO;
            return;
        }

        // 링크 기반 위험도 계산
        BigDecimal accidentWeight = BigDecimal.valueOf(occrrnc_cnt).multiply(BigDecimal.valueOf(3.0));
        BigDecimal casualtyWeight = BigDecimal.valueOf(casltCnt).multiply(BigDecimal.valueOf(2.5));
        BigDecimal deathWeight = BigDecimal.valueOf(dthDnvCnt != null ? dthDnvCnt : 0).multiply(BigDecimal.valueOf(15.0));
        BigDecimal seriousInjuryWeight = BigDecimal.valueOf(seDnvCnt != null ? seDnvCnt : 0).multiply(BigDecimal.valueOf(8.0));

        // 도로유형별 가중치
        BigDecimal roadTypeWeight = getRoadTypeWeight();

        // 교통량 가중치
        BigDecimal trafficWeight = getTrafficVolumeWeight();

        // 속도 가중치
        BigDecimal speedWeight = getSpeedWeight();

        this.riskScore = accidentWeight
                .add(casualtyWeight)
                .add(deathWeight)
                .add(seriousInjuryWeight)
                .multiply(roadTypeWeight)
                .multiply(trafficWeight)
                .multiply(speedWeight)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRoadTypeWeight() {
        if (roadType == null) return BigDecimal.ONE;

        switch (roadType) {
            case HIGHWAY:
                return BigDecimal.valueOf(2.5); // 고속도로 높은 위험
            case ARTERIAL:
            case NATIONAL:
                return BigDecimal.valueOf(2.0); // 간선도로/국도 높은 위험
            case URBAN:
            case PROVINCIAL:
                return BigDecimal.valueOf(1.5); // 도시부/지방도 중간 위험
            case COLLECTOR:
            case LOCAL:
                return BigDecimal.valueOf(1.2); // 집산/지방도로 낮은 위험
            case LOCAL_STREET:
                return BigDecimal.valueOf(1.0); // 국지도로 기본 위험
            default:
                return BigDecimal.ONE;
        }
    }

    private BigDecimal getTrafficVolumeWeight() {
        if (trafficVolumeLevel == null) return BigDecimal.ONE;

        switch (trafficVolumeLevel) {
            case VERY_HIGH:
                return BigDecimal.valueOf(2.0);
            case HIGH:
                return BigDecimal.valueOf(1.6);
            case MEDIUM:
                return BigDecimal.valueOf(1.3);
            case LOW:
                return BigDecimal.valueOf(1.1);
            case VERY_LOW:
                return BigDecimal.valueOf(1.0);
            default:
                return BigDecimal.ONE;
        }
    }

    private BigDecimal getSpeedWeight() {
        if (speedLimit == null) return BigDecimal.ONE;

        // 속도가 높을수록 위험도 증가
        if (speedLimit >= 100) return BigDecimal.valueOf(2.0); // 고속도로
        if (speedLimit >= 80) return BigDecimal.valueOf(1.7);  // 자동차전용도로
        if (speedLimit >= 60) return BigDecimal.valueOf(1.4);  // 간선도로
        if (speedLimit >= 50) return BigDecimal.valueOf(1.2);  // 도시부도로
        return BigDecimal.ONE; // 저속도로
    }

    private void determineRiskLevel() {
        if (riskScore == null) {
            this.riskLevel = RiskLevel.VERY_LOW;
            return;
        }

        // 링크 기반 위험등급 결정
        if (riskScore.compareTo(BigDecimal.valueOf(500)) >= 0) {
            this.riskLevel = RiskLevel.VERY_HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(300)) >= 0) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(150)) >= 0) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else if (riskScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            this.riskLevel = RiskLevel.LOW;
        } else {
            this.riskLevel = RiskLevel.VERY_LOW;
        }
    }

    private void calculateRoadSafetyIndex() {
        // 도로 인프라 안전성 지수 계산
        BigDecimal baseScore = BigDecimal.valueOf(100);

        // 차선수 점수 (차선이 많을수록 안전성 증가)
        BigDecimal laneScore = BigDecimal.ZERO;
        if (laneCount != null) {
            laneScore = BigDecimal.valueOf(laneCount * 5).min(BigDecimal.valueOf(20));
        }

        // 도로폭 점수 (넓을수록 안전성 증가)
        BigDecimal widthScore = BigDecimal.ZERO;
        if (roadWidth != null) {
            widthScore = roadWidth.multiply(BigDecimal.valueOf(2)).min(BigDecimal.valueOf(30));
        }

        // 신호등 점수 (적절한 신호등은 안전성 증가)
        BigDecimal signalScore = BigDecimal.ZERO;
        if (trafficSignalCount != null && linkLength != null && linkLength.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal signalDensity = BigDecimal.valueOf(trafficSignalCount)
                    .divide(linkLength.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
            signalScore = signalDensity.multiply(BigDecimal.valueOf(10)).min(BigDecimal.valueOf(25));
        }

        this.roadSafetyIndex = baseScore
                .add(laneScore)
                .add(widthScore)
                .add(signalScore)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void calculateTrafficFlowRisk() {
        if (trafficVolumeLevel == null && speedLimit == null) {
            this.trafficFlowRisk = BigDecimal.ZERO;
            return;
        }

        // 교통량 위험점수
        BigDecimal trafficRisk = BigDecimal.ZERO;
        if (trafficVolumeLevel != null) {
            switch (trafficVolumeLevel) {
                case VERY_HIGH:
                    trafficRisk = BigDecimal.valueOf(80);
                    break;
                case HIGH:
                    trafficRisk = BigDecimal.valueOf(60);
                    break;
                case MEDIUM:
                    trafficRisk = BigDecimal.valueOf(40);
                    break;
                case LOW:
                    trafficRisk = BigDecimal.valueOf(20);
                    break;
                case VERY_LOW:
                    trafficRisk = BigDecimal.valueOf(10);
                    break;
            }
        }

        // 속도 위험점수
        BigDecimal speedRisk = BigDecimal.ZERO;
        if (speedLimit != null) {
            speedRisk = BigDecimal.valueOf(speedLimit).multiply(BigDecimal.valueOf(0.8));
        }

        this.trafficFlowRisk = trafficRisk.add(speedRisk).setScale(2, RoundingMode.HALF_UP);
    }

    private void calculateInfrastructureRisk() {
        BigDecimal baseRisk = BigDecimal.valueOf(10);

        // 횡단보도 밀도 위험 (높을수록 위험 증가)
        BigDecimal crosswalkRisk = BigDecimal.ZERO;
        if (crosswalkCount != null && linkLength != null && linkLength.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal crosswalkDensity = BigDecimal.valueOf(crosswalkCount)
                    .divide(linkLength.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
            crosswalkRisk = crosswalkDensity.multiply(BigDecimal.valueOf(15));
        }

        // 버스정류장 밀도 위험 (높을수록 위험 증가)
        BigDecimal busStopRisk = BigDecimal.ZERO;
        if (busStopCount != null && linkLength != null && linkLength.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal busStopDensity = BigDecimal.valueOf(busStopCount)
                    .divide(linkLength.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
            busStopRisk = busStopDensity.multiply(BigDecimal.valueOf(12));
        }

        this.infrastructureRisk = baseRisk
                .add(crosswalkRisk)
                .add(busStopRisk)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ===== 비즈니스 메서드 =====

    public boolean isHighRiskLink() {
        return riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.HIGH;
    }

    public boolean isHighwayLink() {
        return roadType == RoadType.HIGHWAY;
    }

    public boolean isUrbanRoad() {
        return roadType == RoadType.URBAN || roadType == RoadType.LOCAL_STREET;
    }

    public boolean hasHighTrafficVolume() {
        return trafficVolumeLevel == TrafficVolumeLevel.VERY_HIGH ||
                trafficVolumeLevel == TrafficVolumeLevel.HIGH;
    }

    public boolean isHighSpeedRoad() {
        return speedLimit != null && speedLimit >= 80;
    }

    public boolean hasFatalAccident() {
        return dthDnvCnt != null && dthDnvCnt > 0;
    }

    public boolean hasHighAccidentDensity() {
        return accidentPerKm != null && accidentPerKm.compareTo(BigDecimal.valueOf(5.0)) >= 0;
    }

    public boolean hasGoodRoadSafety() {
        return roadSafetyIndex != null && roadSafetyIndex.compareTo(BigDecimal.valueOf(120)) >= 0;
    }

    public boolean hasHighTrafficFlowRisk() {
        return trafficFlowRisk != null && trafficFlowRisk.compareTo(BigDecimal.valueOf(100)) >= 0;
    }

    public boolean hasHighInfrastructureRisk() {
        return infrastructureRisk != null && infrastructureRisk.compareTo(BigDecimal.valueOf(50)) >= 0;
    }

    public boolean isComplexIntersectionArea() {
        return (trafficSignalCount != null && trafficSignalCount >= 2) ||
                (crosswalkCount != null && crosswalkCount >= 3);
    }

    public boolean isPedestrianHighRiskArea() {
        return crosswalkCount != null && crosswalkCount > 0 &&
                busStopCount != null && busStopCount > 0 &&
                isHighRiskLink();
    }

    public double[] getCoordinates() {
        if (loCrd != null && laCrd != null) {
            return new double[]{loCrd.doubleValue(), laCrd.doubleValue()};
        }
        return new double[]{0.0, 0.0};
    }

    public String getLinkRiskSummary() {
        return String.format(
                "링크위험분석 - 도로: %s(%s), 위험등급: %s, 사고건수: %d건, 사망자: %d명, km당사고: %.1f건",
                roadNm,
                roadType != null ? roadType.getDescription() : "미분류",
                riskLevel.getDescription(),
                occrrnc_cnt != null ? occrrnc_cnt : 0,
                dthDnvCnt != null ? dthDnvCnt : 0,
                accidentPerKm != null ? accidentPerKm.doubleValue() : 0.0
        );
    }

    public String getRoadCharacteristics() {
        return String.format(
                "도로특성 - 유형: %s, 등급: %s, 길이: %.0fm, 차선: %d개, 제한속도: %dkm/h, 교통량: %s",
                roadType != null ? roadType.getDescription() : "미분류",
                roadGrade != null ? roadGrade.getDescription() : "미분류",
                linkLength != null ? linkLength.doubleValue() : 0.0,
                laneCount != null ? laneCount : 0,
                speedLimit != null ? speedLimit : 0,
                trafficVolumeLevel != null ? trafficVolumeLevel.getDescription() : "미분류"
        );
    }

    public String getInfrastructureAnalysis() {
        return String.format(
                "인프라분석 - 신호등: %d개, 횡단보도: %d개, 버스정류장: %d개, 도로안전지수: %.1f점, 인프라위험도: %.1f점",
                trafficSignalCount != null ? trafficSignalCount : 0,
                crosswalkCount != null ? crosswalkCount : 0,
                busStopCount != null ? busStopCount : 0,
                roadSafetyIndex != null ? roadSafetyIndex.doubleValue() : 0.0,
                infrastructureRisk != null ? infrastructureRisk.doubleValue() : 0.0
        );
    }

    public String getTrafficFlowAnalysis() {
        return String.format(
                "교통흐름분석 - 교통량수준: %s, 제한속도: %dkm/h, 교통흐름위험도: %.1f점, 치사율: %.1f%%",
                trafficVolumeLevel != null ? trafficVolumeLevel.getDescription() : "미분류",
                speedLimit != null ? speedLimit : 0,
                trafficFlowRisk != null ? trafficFlowRisk.doubleValue() : 0.0,
                fatalityRate != null ? fatalityRate.doubleValue() : 0.0
        );
    }

    public String getLinkSectionInfo() {
        return String.format(
                "링크구간정보 - 시작: %s, 종료: %s, 링크ID: %s, 위치: %s",
                startNodeNm != null ? startNodeNm : "미상",
                endNodeNm != null ? endNodeNm : "미상",
                linkId,
                sidoSggNm
        );
    }
}