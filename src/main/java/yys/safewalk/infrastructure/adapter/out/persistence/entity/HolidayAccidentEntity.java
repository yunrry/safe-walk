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
 * 연휴기간별 사고다발지역정보 API 엔티티
 * 연휴기간 중 발생한 교통사고 다발지역 정보를 저장 (설날, 추석, 여름휴가철, 봄철주말, 단풍철주말)
 *
 * API 정보: https://opendata.koroad.or.kr/data/rest/frequentzone/tmzon
 * 데이터 제공기간: 2015~2023
 */
@Entity
@Table(
        name = "holiday_accident",
        indexes = {
                @Index(name = "idx_holiday_sido_sgg", columnList = "sido_sgg_nm"),
                @Index(name = "idx_holiday_year", columnList = "search_year_cd"),
                @Index(name = "idx_holiday_occurrence_cnt", columnList = "occrrnc_cnt"),
                @Index(name = "idx_holiday_casualty_cnt", columnList = "caslt_cnt"),
                @Index(name = "idx_holiday_death_cnt", columnList = "dth_dnv_cnt"),
                @Index(name = "idx_holiday_coordinates", columnList = "lo_crd, la_crd"),
                @Index(name = "idx_holiday_spot_cd", columnList = "spot_cd"),
                @Index(name = "idx_holiday_bjd_cd", columnList = "bjd_cd"),
                @Index(name = "idx_holiday_fatality_rate", columnList = "fatality_rate"),
                @Index(name = "idx_holiday_risk_score", columnList = "risk_score"),
                @Index(name = "idx_holiday_created_at", columnList = "created_at"),
                @Index(name = "idx_holiday_period_type", columnList = "holiday_period_type"),
                @Index(name = "idx_holiday_seasonal_risk", columnList = "seasonal_risk_level"),
                @Index(name = "idx_holiday_tourism_impact", columnList = "tourism_impact_score"),
                @Index(name = "idx_holiday_composite", columnList = "search_year_cd, sido_sgg_nm, holiday_period_type"),
                @Index(name = "idx_holiday_season_risk", columnList = "holiday_period_type, risk_level, occrrnc_cnt")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("연휴기간별 사고다발지역정보")
public class HolidayAccidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("기본키")
    private Long id;

    // ===== API 원본 필드 =====

    @Column(name = "afos_fid", nullable = false, length = 22)
    @Comment("다발지역FID - 다발지역에 대한 공간정보 식별자")
    private String afosFid;

    @Column(name = "afos_id", nullable = false, length = 7)
    @Comment("다발지역ID - 다발지역 식별자")
    private String afosId;

    @Column(name = "bjd_cd", nullable = false, length = 10)
    @Comment("법정동코드")
    private String bjdCd;

    @Column(name = "spot_cd", nullable = false, length = 10)
    @Comment("지점코드 - 사고다발지역 목록 내의 지점코드")
    private String spotCd;

    @Column(name = "sido_sgg_nm", nullable = false, length = 40)
    @Comment("시도시군구명 - 지점의 시도시군구명")
    private String sidoSggNm;

    @Column(name = "spot_nm", nullable = false, length = 200)
    @Comment("지점명 - 다발지역 지점의 위치")
    private String spotNm;

    @Column(name = "occrrnc_cnt", nullable = false)
    @Comment("사고건수 - 연휴기간 다발지역 내 사고건수")
    private Integer occrrnc_cnt;

    @Column(name = "caslt_cnt", nullable = false)
    @Comment("사상자수 - 연휴기간 다발지역 내 사상자수")
    private Integer casltCnt;

    @Column(name = "dth_dnv_cnt", nullable = false)
    @Comment("사망자수 - 연휴기간 다발지역 내 사망자수")
    private Integer dthDnvCnt;

    @Column(name = "se_dnv_cnt", nullable = false)
    @Comment("중상자수 - 연휴기간 다발지역 내 중상자수")
    private Integer seDnvCnt;

    @Column(name = "sl_dnv_cnt", nullable = false)
    @Comment("경상자수 - 연휴기간 다발지역 내 경상자수")
    private Integer slDnvCnt;

    @Column(name = "wnd_dnv_cnt", nullable = false)
    @Comment("부상신고자수 - 연휴기간 다발지역 내 부상신고자수")
    private Integer wndDnvCnt;

    @Column(name = "lo_crd", nullable = false, precision = 16, scale = 12)
    @Comment("경도 - 다발지역지점 중심점의 경도(EPSG 4326)")
    private BigDecimal loCrd;

    @Column(name = "la_crd", nullable = false, precision = 15, scale = 12)
    @Comment("위도 - 다발지역의 중심점의 위도(EPSG 4326)")
    private BigDecimal laCrd;

    @Column(name = "geom_json", columnDefinition = "TEXT")
    @Comment("다발지역폴리곤 - 다발지역 지점의 폴리곤 정보 (EPSG 4326)")
    private String geomJson;

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

    // ===== 연휴기간 특화 필드 =====

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_period_type", length = 20)
    @Comment("연휴기간 유형 - NEW_YEAR(설날), CHUSEOK(추석), SUMMER_VACATION(여름휴가철), SPRING_WEEKEND(4월봄철주말), AUTUMN_WEEKEND(10월단풍철주말)")
    private HolidayPeriodType holidayPeriodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "seasonal_category", length = 10)
    @Comment("계절 구분 - SPRING(봄), SUMMER(여름), AUTUMN(가을), WINTER(겨울)")
    private SeasonalCategory seasonalCategory;

    @Column(name = "tourism_impact_score", precision = 8, scale = 2)
    @Comment("관광영향점수 - 연휴기간 관광지 교통량 증가에 따른 위험도")
    private BigDecimal tourismImpactScore;

    @Column(name = "holiday_traffic_multiplier", precision = 5, scale = 2)
    @Comment("연휴 교통량 배수 - 평상시 대비 연휴기간 교통량 증가 배수")
    private BigDecimal holidayTrafficMultiplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "seasonal_risk_level", length = 10)
    @Comment("계절별 위험등급 - 계절 특성을 반영한 위험등급")
    private SeasonalRiskLevel seasonalRiskLevel;

    // ===== 계산된 필드 =====

    @Column(name = "fatality_rate", precision = 5, scale = 2)
    @Comment("치사율 - 사망자수/사상자수 * 100 (연휴기간 기준)")
    private BigDecimal fatalityRate;

    @Column(name = "serious_injury_rate", precision = 5, scale = 2)
    @Comment("중상률 - 중상자수/사상자수 * 100 (연휴기간 기준)")
    private BigDecimal seriousInjuryRate;

    @Column(name = "casualty_per_accident", precision = 5, scale = 2)
    @Comment("사고당 사상자수 - 사상자수/사고건수 (연휴기간 기준)")
    private BigDecimal casualtyPerAccident;

    @Column(name = "risk_score", precision = 8, scale = 2)
    @Comment("위험도 점수 - 연휴기간 특성을 반영한 종합 위험도 계산 점수")
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Comment("위험등급 - VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW")
    private RiskLevel riskLevel;

    @Column(name = "holiday_accident_density", precision = 8, scale = 2)
    @Comment("연휴 사고밀도 - 반경 100m 내 연휴기간 사고 밀도")
    private BigDecimal holidayAccidentDensity;

    @Column(name = "weekend_risk_factor", precision = 5, scale = 2)
    @Comment("주말 위험계수 - 주말 연휴의 위험도 가중치")
    private BigDecimal weekendRiskFactor;

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

    public enum HolidayPeriodType {
        NEW_YEAR("설날"),
        CHUSEOK("추석"),
        SUMMER_VACATION("여름휴가철"),
        SPRING_WEEKEND("4월봄철주말"),
        AUTUMN_WEEKEND("10월단풍철주말"),
        UNKNOWN("미분류");

        private final String description;

        HolidayPeriodType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SeasonalCategory {
        SPRING("봄"),
        SUMMER("여름"),
        AUTUMN("가을"),
        WINTER("겨울");

        private final String description;

        SeasonalCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SeasonalRiskLevel {
        PEAK_TOURISM("관광성수기"),
        HIGH_TRAFFIC("교통혼잡기"),
        NORMAL_HOLIDAY("일반연휴"),
        LOW_RISK("저위험기"),
        MINIMAL_RISK("최저위험기");

        private final String description;

        SeasonalRiskLevel(String description) {
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
    public HolidayAccidentEntity(
            String afosFid, String afosId, String bjdCd, String spotCd,
            String sidoSggNm, String spotNm, Integer occrrnc_cnt, Integer casltCnt,
            Integer dthDnvCnt, Integer seDnvCnt, Integer slDnvCnt, Integer wndDnvCnt,
            BigDecimal loCrd, BigDecimal laCrd, String geomJson,
            String searchYearCd, String sidoCd, String gugunCd,
            HolidayPeriodType holidayPeriodType, SeasonalCategory seasonalCategory,
            String apiResultCode, String apiResultMsg, Integer totalCount,
            Integer pageNo, Integer numOfRows, LocalDateTime dataCollectionDate,
            String createdBy, String updatedBy) {

        this.afosFid = afosFid;
        this.afosId = afosId;
        this.bjdCd = bjdCd;
        this.spotCd = spotCd;
        this.sidoSggNm = sidoSggNm;
        this.spotNm = spotNm;
        this.occrrnc_cnt = occrrnc_cnt;
        this.casltCnt = casltCnt;
        this.dthDnvCnt = dthDnvCnt;
        this.seDnvCnt = seDnvCnt;
        this.slDnvCnt = slDnvCnt;
        this.wndDnvCnt = wndDnvCnt;
        this.loCrd = loCrd;
        this.laCrd = laCrd;
        this.geomJson = geomJson;
        this.searchYearCd = searchYearCd;
        this.sidoCd = sidoCd;
        this.gugunCd = gugunCd;
        this.holidayPeriodType = holidayPeriodType;
        this.seasonalCategory = seasonalCategory;
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

        // 연휴기간별 자동 분류
        determineHolidayPeriodType();

        // 계절 구분 결정
        determineSeasonalCategory();

        // 연휴 위험도 점수 계산
        calculateHolidayRiskScore();

        // 위험등급 결정
        determineRiskLevel();

        // 연휴 사고 밀도 계산
        calculateHolidayAccidentDensity();

        // 관광영향점수 계산
        calculateTourismImpactScore();

        // 연휴 교통량 배수 계산
        calculateHolidayTrafficMultiplier();

        // 주말 위험계수 계산
        calculateWeekendRiskFactor();

        // 계절별 위험등급 결정
        determineSeasonalRiskLevel();
    }

    private void determineHolidayPeriodType() {
        if (holidayPeriodType != null) return;

        // 기본적으로 지역명이나 다른 정보로부터 연휴기간 유형을 추정
        // 실제로는 API 요청 파라미터나 별도 필드가 필요할 수 있음
        this.holidayPeriodType = HolidayPeriodType.UNKNOWN;
    }

    private void determineSeasonalCategory() {
        if (holidayPeriodType == null) {
            this.seasonalCategory = null;
            return;
        }

        switch (holidayPeriodType) {
            case NEW_YEAR:
                this.seasonalCategory = SeasonalCategory.WINTER;
                break;
            case SPRING_WEEKEND:
                this.seasonalCategory = SeasonalCategory.SPRING;
                break;
            case SUMMER_VACATION:
                this.seasonalCategory = SeasonalCategory.SUMMER;
                break;
            case CHUSEOK:
            case AUTUMN_WEEKEND:
                this.seasonalCategory = SeasonalCategory.AUTUMN;
                break;
            default:
                this.seasonalCategory = null;
        }
    }

    private void calculateHolidayRiskScore() {
        if (occrrnc_cnt == null || casltCnt == null) {
            this.riskScore = BigDecimal.ZERO;
            return;
        }

        // 연휴기간 특성을 반영한 위험도 계산
        BigDecimal accidentWeight = BigDecimal.valueOf(occrrnc_cnt).multiply(BigDecimal.valueOf(4.0)); // 연휴기간 가중치 증가
        BigDecimal casualtyWeight = BigDecimal.valueOf(casltCnt).multiply(BigDecimal.valueOf(3.0));
        BigDecimal deathWeight = BigDecimal.valueOf(dthDnvCnt != null ? dthDnvCnt : 0).multiply(BigDecimal.valueOf(20.0)); // 연휴기간 사망사고 가중치 대폭 증가
        BigDecimal seriousInjuryWeight = BigDecimal.valueOf(seDnvCnt != null ? seDnvCnt : 0).multiply(BigDecimal.valueOf(8.0));

        // 연휴기간 유형별 가중치
        BigDecimal holidayTypeWeight = getHolidayTypeWeight();

        this.riskScore = accidentWeight
                .add(casualtyWeight)
                .add(deathWeight)
                .add(seriousInjuryWeight)
                .multiply(holidayTypeWeight)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getHolidayTypeWeight() {
        if (holidayPeriodType == null) return BigDecimal.ONE;

        switch (holidayPeriodType) {
            case NEW_YEAR:
            case CHUSEOK:
                return BigDecimal.valueOf(2.0); // 대형 연휴 높은 가중치
            case SUMMER_VACATION:
                return BigDecimal.valueOf(1.8); // 여름휴가철 높은 가중치
            case SPRING_WEEKEND:
            case AUTUMN_WEEKEND:
                return BigDecimal.valueOf(1.5); // 계절 주말 중간 가중치
            default:
                return BigDecimal.ONE;
        }
    }

    private void determineRiskLevel() {
        if (riskScore == null) {
            this.riskLevel = RiskLevel.VERY_LOW;
            return;
        }

        // 연휴기간 특성을 반영한 더 높은 기준 적용
        if (riskScore.compareTo(BigDecimal.valueOf(400)) >= 0) {
            this.riskLevel = RiskLevel.VERY_HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(250)) >= 0) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(120)) >= 0) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else if (riskScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            this.riskLevel = RiskLevel.LOW;
        } else {
            this.riskLevel = RiskLevel.VERY_LOW;
        }
    }

    private void calculateHolidayAccidentDensity() {
        if (occrrnc_cnt == null) {
            this.holidayAccidentDensity = BigDecimal.ZERO;
            return;
        }

        // 반경 100m 내 연휴기간 사고 밀도 계산 (π * 100² = 31,416 ㎡)
        BigDecimal area = BigDecimal.valueOf(31416); // 100m 반경 면적 (㎡)
        this.holidayAccidentDensity = BigDecimal.valueOf(occrrnc_cnt)
                .multiply(BigDecimal.valueOf(1000000)) // ㎢ 당 사고 수로 변환
                .divide(area, 2, RoundingMode.HALF_UP);
    }

    private void calculateTourismImpactScore() {
        if (occrrnc_cnt == null) {
            this.tourismImpactScore = BigDecimal.ZERO;
            return;
        }

        // 관광지역의 연휴기간 교통량 증가를 반영한 점수
        BigDecimal baseScore = BigDecimal.valueOf(occrrnc_cnt).multiply(BigDecimal.valueOf(10));
        BigDecimal holidayMultiplier = getHolidayTypeWeight();

        this.tourismImpactScore = baseScore
                .multiply(holidayMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void calculateHolidayTrafficMultiplier() {
        if (holidayPeriodType == null) {
            this.holidayTrafficMultiplier = BigDecimal.ONE;
            return;
        }

        // 연휴기간별 평상시 대비 교통량 증가 배수
        switch (holidayPeriodType) {
            case NEW_YEAR:
            case CHUSEOK:
                this.holidayTrafficMultiplier = BigDecimal.valueOf(3.5); // 350% 증가
                break;
            case SUMMER_VACATION:
                this.holidayTrafficMultiplier = BigDecimal.valueOf(2.8); // 280% 증가
                break;
            case SPRING_WEEKEND:
            case AUTUMN_WEEKEND:
                this.holidayTrafficMultiplier = BigDecimal.valueOf(2.2); // 220% 증가
                break;
            default:
                this.holidayTrafficMultiplier = BigDecimal.valueOf(1.5); // 150% 증가
        }
    }

    private void calculateWeekendRiskFactor() {
        if (holidayPeriodType == null) {
            this.weekendRiskFactor = BigDecimal.ONE;
            return;
        }

        // 주말 연휴의 위험도 가중치
        switch (holidayPeriodType) {
            case SPRING_WEEKEND:
            case AUTUMN_WEEKEND:
                this.weekendRiskFactor = BigDecimal.valueOf(1.8); // 주말 특화 위험
                break;
            case SUMMER_VACATION:
                this.weekendRiskFactor = BigDecimal.valueOf(1.6); // 여름휴가 주말 위험
                break;
            default:
                this.weekendRiskFactor = BigDecimal.valueOf(1.3); // 일반 연휴 주말 위험
        }
    }

    private void determineSeasonalRiskLevel() {
        if (holidayPeriodType == null || riskLevel == null) {
            this.seasonalRiskLevel = SeasonalRiskLevel.MINIMAL_RISK;
            return;
        }

        // 연휴기간과 위험등급을 종합하여 계절별 위험등급 결정
        if (riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.HIGH) {
            switch (holidayPeriodType) {
                case SUMMER_VACATION:
                case NEW_YEAR:
                case CHUSEOK:
                    this.seasonalRiskLevel = SeasonalRiskLevel.PEAK_TOURISM;
                    break;
                default:
                    this.seasonalRiskLevel = SeasonalRiskLevel.HIGH_TRAFFIC;
            }
        } else if (riskLevel == RiskLevel.MEDIUM) {
            this.seasonalRiskLevel = SeasonalRiskLevel.NORMAL_HOLIDAY;
        } else if (riskLevel == RiskLevel.LOW) {
            this.seasonalRiskLevel = SeasonalRiskLevel.LOW_RISK;
        } else {
            this.seasonalRiskLevel = SeasonalRiskLevel.MINIMAL_RISK;
        }
    }

    // ===== 비즈니스 메서드 =====

    public boolean isMajorHoliday() {
        return holidayPeriodType == HolidayPeriodType.NEW_YEAR ||
                holidayPeriodType == HolidayPeriodType.CHUSEOK;
    }

    public boolean isTourismSeason() {
        return holidayPeriodType == HolidayPeriodType.SUMMER_VACATION ||
                holidayPeriodType == HolidayPeriodType.SPRING_WEEKEND ||
                holidayPeriodType == HolidayPeriodType.AUTUMN_WEEKEND;
    }

    public boolean isHighRiskHolidayArea() {
        return riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.HIGH;
    }

    public boolean hasFatalAccident() {
        return dthDnvCnt != null && dthDnvCnt > 0;
    }

    public boolean isHighTourismImpact() {
        return tourismImpactScore != null && tourismImpactScore.compareTo(BigDecimal.valueOf(200)) >= 0;
    }

    public boolean isHighTrafficIncrease() {
        return holidayTrafficMultiplier != null && holidayTrafficMultiplier.compareTo(BigDecimal.valueOf(3.0)) >= 0;
    }

    public double[] getCoordinates() {
        if (loCrd != null && laCrd != null) {
            return new double[]{loCrd.doubleValue(), laCrd.doubleValue()};
        }
        return new double[]{0.0, 0.0};
    }

    public String getHolidayRiskSummary() {
        return String.format(
                "연휴기간 위험지역 - 연휴유형: %s, 계절: %s, 위험등급: %s, 사고건수: %d건, 사망자: %d명, 관광영향: %.1f점",
                holidayPeriodType != null ? holidayPeriodType.getDescription() : "미분류",
                seasonalCategory != null ? seasonalCategory.getDescription() : "미분류",
                riskLevel.getDescription(),
                occrrnc_cnt != null ? occrrnc_cnt : 0,
                dthDnvCnt != null ? dthDnvCnt : 0,
                tourismImpactScore != null ? tourismImpactScore.doubleValue() : 0.0
        );
    }

    public String getSeasonalRiskAnalysis() {
        return String.format(
                "계절별 위험분석 - 계절위험등급: %s, 교통량증가: %.1f배, 주말위험계수: %.1f, 사고밀도: %.1f건/㎢",
                seasonalRiskLevel != null ? seasonalRiskLevel.getDescription() : "미분류",
                holidayTrafficMultiplier != null ? holidayTrafficMultiplier.doubleValue() : 1.0,
                weekendRiskFactor != null ? weekendRiskFactor.doubleValue() : 1.0,
                holidayAccidentDensity != null ? holidayAccidentDensity.doubleValue() : 0.0
        );
    }
}