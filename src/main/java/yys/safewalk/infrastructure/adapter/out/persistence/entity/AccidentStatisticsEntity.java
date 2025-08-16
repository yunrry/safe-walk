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
 * 지자체별 대상사고통계 API 엔티티
 * 지자체별 교통사고 통계 정보를 저장
 *
 * API 정보: 지자체별 대상사고통계 API
 * 데이터 제공기간: 연도별 통계 데이터
 */
@Entity
@Table(
        name = "accident_statistics",
        indexes = {
                @Index(name = "idx_stats_sido_sgg", columnList = "sido_sgg_nm"),
                @Index(name = "idx_stats_year", columnList = "search_year_cd"),
                @Index(name = "idx_stats_occurrence_cnt", columnList = "total_occrrnc_cnt"),
                @Index(name = "idx_stats_casualty_cnt", columnList = "total_caslt_cnt"),
                @Index(name = "idx_stats_death_cnt", columnList = "total_dth_dnv_cnt"),
                @Index(name = "idx_stats_fatality_rate", columnList = "fatality_rate"),
                @Index(name = "idx_stats_risk_score", columnList = "risk_score"),
                @Index(name = "idx_stats_created_at", columnList = "created_at"),
                @Index(name = "idx_stats_region_rank", columnList = "regional_rank"),
                @Index(name = "idx_stats_population", columnList = "population_size"),
                @Index(name = "idx_stats_composite", columnList = "search_year_cd, sido_sgg_nm, total_occrrnc_cnt"),
                @Index(name = "idx_stats_regional", columnList = "sido_cd, gugun_cd, regional_rank"),
                @Index(name = "idx_stats_demographics", columnList = "population_size, accident_per_population")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("지자체별 대상사고통계")
public class AccidentStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("기본키")
    private Long id;

    // ===== 지자체 기본 정보 =====

    @Column(name = "sido_sgg_nm", nullable = false, length = 40)
    @Comment("시도시군구명 - 지자체명")
    private String sidoSggNm;

    @Column(name = "sido_cd", length = 2)
    @Comment("시도코드 - 법정동 시도 코드")
    private String sidoCd;

    @Column(name = "gugun_cd", length = 3)
    @Comment("시군구코드 - 법정동 시군구 코드")
    private String gugunCd;

    @Column(name = "search_year_cd", nullable = false, length = 7)
    @Comment("통계연도코드 - 통계 기준 연도")
    private String searchYearCd;

    // ===== 사고 통계 원본 필드 =====

    @Column(name = "total_occrrnc_cnt", nullable = false)
    @Comment("총 사고건수 - 지자체 내 전체 교통사고건수")
    private Integer totalOccrrncCnt;

    @Column(name = "total_caslt_cnt", nullable = false)
    @Comment("총 사상자수 - 지자체 내 전체 사상자수")
    private Integer totalCasltCnt;

    @Column(name = "total_dth_dnv_cnt", nullable = false)
    @Comment("총 사망자수 - 지자체 내 사망자수")
    private Integer totalDthDnvCnt;

    @Column(name = "total_se_dnv_cnt", nullable = false)
    @Comment("총 중상자수 - 지자체 내 중상자수")
    private Integer totalSeDnvCnt;

    @Column(name = "total_sl_dnv_cnt", nullable = false)
    @Comment("총 경상자수 - 지자체 내 경상자수")
    private Integer totalSlDnvCnt;

    @Column(name = "total_wnd_dnv_cnt", nullable = false)
    @Comment("총 부상신고자수 - 지자체 내 부상신고자수")
    private Integer totalWndDnvCnt;

    // ===== 세분화된 사고 통계 =====

    @Column(name = "pedestrian_occrrnc_cnt")
    @Comment("보행자 사고건수 - 보행자 관련 사고건수")
    private Integer pedestrianOccrrncCnt;

    @Column(name = "pedestrian_caslt_cnt")
    @Comment("보행자 사상자수 - 보행자 관련 사상자수")
    private Integer pedestrianCasltCnt;

    @Column(name = "pedestrian_dth_dnv_cnt")
    @Comment("보행자 사망자수 - 보행자 관련 사망자수")
    private Integer pedestrianDthDnvCnt;

    @Column(name = "elderly_occrrnc_cnt")
    @Comment("노인 사고건수 - 65세 이상 관련 사고건수")
    private Integer elderlyOccrrncCnt;

    @Column(name = "elderly_caslt_cnt")
    @Comment("노인 사상자수 - 65세 이상 관련 사상자수")
    private Integer elderlyCasltCnt;

    @Column(name = "elderly_dth_dnv_cnt")
    @Comment("노인 사망자수 - 65세 이상 관련 사망자수")
    private Integer elderlyDthDnvCnt;

    @Column(name = "child_occrrnc_cnt")
    @Comment("어린이 사고건수 - 12세 이하 관련 사고건수")
    private Integer childOccrrncCnt;

    @Column(name = "child_caslt_cnt")
    @Comment("어린이 사상자수 - 12세 이하 관련 사상자수")
    private Integer childCasltCnt;

    @Column(name = "child_dth_dnv_cnt")
    @Comment("어린이 사망자수 - 12세 이하 관련 사망자수")
    private Integer childDthDnvCnt;

    // ===== 지자체 특성 정보 =====

    @Column(name = "population_size")
    @Comment("인구수 - 지자체 전체 인구수")
    private Long populationSize;

    @Column(name = "area_size", precision = 10, scale = 2)
    @Comment("면적 - 지자체 면적 (㎢)")
    private BigDecimal areaSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", length = 20)
    @Comment("지역유형 - METROPOLITAN(광역시), PROVINCE(도), SPECIAL(특별시/특별자치시)")
    private RegionType regionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "urban_level", length = 20)
    @Comment("도시화수준 - URBAN(도시), SUBURBAN(준도시), RURAL(농촌)")
    private UrbanLevel urbanLevel;

    @Column(name = "regional_rank")
    @Comment("지역내 순위 - 사고건수 기준 시도 내 순위")
    private Integer regionalRank;

    @Column(name = "national_rank")
    @Comment("전국 순위 - 사고건수 기준 전국 순위")
    private Integer nationalRank;

    // ===== 계산된 통계 필드 =====

    @Column(name = "fatality_rate", precision = 5, scale = 2)
    @Comment("치사율 - 사망자수/사상자수 * 100")
    private BigDecimal fatalityRate;

    @Column(name = "serious_injury_rate", precision = 5, scale = 2)
    @Comment("중상률 - 중상자수/사상자수 * 100")
    private BigDecimal seriousInjuryRate;

    @Column(name = "casualty_per_accident", precision = 5, scale = 2)
    @Comment("사고당 사상자수 - 사상자수/사고건수")
    private BigDecimal casualtyPerAccident;

    @Column(name = "accident_per_population", precision = 8, scale = 2)
    @Comment("인구 10만명당 사고건수 - (사고건수/인구수) * 100000")
    private BigDecimal accidentPerPopulation;

    @Column(name = "death_per_population", precision = 8, scale = 2)
    @Comment("인구 10만명당 사망자수 - (사망자수/인구수) * 100000")
    private BigDecimal deathPerPopulation;

    @Column(name = "accident_density", precision = 8, scale = 2)
    @Comment("사고밀도 - 면적 1㎢당 사고건수")
    private BigDecimal accidentDensity;

    @Column(name = "risk_score", precision = 8, scale = 2)
    @Comment("위험도 점수 - 종합 위험도 계산 점수")
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Comment("위험등급 - VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW")
    private RiskLevel riskLevel;

    // ===== 세부 비율 통계 =====

    @Column(name = "pedestrian_accident_ratio", precision = 5, scale = 2)
    @Comment("보행자 사고비율 - 보행자사고/전체사고 * 100")
    private BigDecimal pedestrianAccidentRatio;

    @Column(name = "elderly_accident_ratio", precision = 5, scale = 2)
    @Comment("노인 사고비율 - 노인사고/전체사고 * 100")
    private BigDecimal elderlyAccidentRatio;

    @Column(name = "child_accident_ratio", precision = 5, scale = 2)
    @Comment("어린이 사고비율 - 어린이사고/전체사고 * 100")
    private BigDecimal childAccidentRatio;

    @Column(name = "vulnerable_road_users_ratio", precision = 5, scale = 2)
    @Comment("교통약자 사고비율 - (보행자+노인+어린이)사고/전체사고 * 100")
    private BigDecimal vulnerableRoadUsersRatio;

    // ===== 추세 분석 필드 =====

    @Column(name = "year_over_year_change", precision = 8, scale = 2)
    @Comment("전년대비 증감률 - 전년 대비 사고건수 증감률 (%)")
    private BigDecimal yearOverYearChange;

    @Column(name = "trend_direction", length = 10)
    @Comment("추세방향 - INCREASING(증가), DECREASING(감소), STABLE(안정)")
    private String trendDirection;

    @Column(name = "safety_improvement_score", precision = 8, scale = 2)
    @Comment("안전개선점수 - 교통안전 개선도 점수")
    private BigDecimal safetyImprovementScore;

    // ===== 메타데이터 =====

    @Column(name = "api_result_code", length = 2)
    @Comment("API 결과코드 - 00:성공, 03:데이터없음, 10:파라미터오류, 99:알수없는오류")
    private String apiResultCode;

    @Column(name = "api_result_msg", length = 100)
    @Comment("API 결과메시지 - API 호출 결과 메시지")
    private String apiResultMsg;

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

    public enum RegionType {
        METROPOLITAN("광역시"),
        PROVINCE("도"),
        SPECIAL("특별시/특별자치시");

        private final String description;

        RegionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum UrbanLevel {
        URBAN("도시"),
        SUBURBAN("준도시"),
        RURAL("농촌");

        private final String description;

        UrbanLevel(String description) {
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
    public AccidentStatisticsEntity(
            String sidoSggNm, String sidoCd, String gugunCd, String searchYearCd,
            Integer totalOccrrncCnt, Integer totalCasltCnt, Integer totalDthDnvCnt,
            Integer totalSeDnvCnt, Integer totalSlDnvCnt, Integer totalWndDnvCnt,
            Integer pedestrianOccrrncCnt, Integer pedestrianCasltCnt, Integer pedestrianDthDnvCnt,
            Integer elderlyOccrrncCnt, Integer elderlyCasltCnt, Integer elderlyDthDnvCnt,
            Integer childOccrrncCnt, Integer childCasltCnt, Integer childDthDnvCnt,
            Long populationSize, BigDecimal areaSize, RegionType regionType, UrbanLevel urbanLevel,
            Integer regionalRank, Integer nationalRank, BigDecimal yearOverYearChange,
            String trendDirection, String apiResultCode, String apiResultMsg,
            LocalDateTime dataCollectionDate, String createdBy, String updatedBy) {

        this.sidoSggNm = sidoSggNm;
        this.sidoCd = sidoCd;
        this.gugunCd = gugunCd;
        this.searchYearCd = searchYearCd;
        this.totalOccrrncCnt = totalOccrrncCnt;
        this.totalCasltCnt = totalCasltCnt;
        this.totalDthDnvCnt = totalDthDnvCnt;
        this.totalSeDnvCnt = totalSeDnvCnt;
        this.totalSlDnvCnt = totalSlDnvCnt;
        this.totalWndDnvCnt = totalWndDnvCnt;
        this.pedestrianOccrrncCnt = pedestrianOccrrncCnt;
        this.pedestrianCasltCnt = pedestrianCasltCnt;
        this.pedestrianDthDnvCnt = pedestrianDthDnvCnt;
        this.elderlyOccrrncCnt = elderlyOccrrncCnt;
        this.elderlyCasltCnt = elderlyCasltCnt;
        this.elderlyDthDnvCnt = elderlyDthDnvCnt;
        this.childOccrrncCnt = childOccrrncCnt;
        this.childCasltCnt = childCasltCnt;
        this.childDthDnvCnt = childDthDnvCnt;
        this.populationSize = populationSize;
        this.areaSize = areaSize;
        this.regionType = regionType;
        this.urbanLevel = urbanLevel;
        this.regionalRank = regionalRank;
        this.nationalRank = nationalRank;
        this.yearOverYearChange = yearOverYearChange;
        this.trendDirection = trendDirection;
        this.apiResultCode = apiResultCode;
        this.apiResultMsg = apiResultMsg;
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
        // 기본 비율 계산
        calculateBasicRatios();

        // 인구 기반 통계 계산
        calculatePopulationBasedStats();

        // 면적 기반 통계 계산
        calculateAreaBasedStats();

        // 세부 비율 통계 계산
        calculateDetailedRatios();

        // 위험도 점수 계산
        calculateRiskScore();

        // 위험등급 결정
        determineRiskLevel();

        // 안전개선점수 계산
        calculateSafetyImprovementScore();
    }

    private void calculateBasicRatios() {
        if (totalCasltCnt != null && totalCasltCnt > 0) {
            // 치사율 계산
            this.fatalityRate = BigDecimal.valueOf(totalDthDnvCnt != null ? totalDthDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCasltCnt), 2, RoundingMode.HALF_UP);

            // 중상률 계산
            this.seriousInjuryRate = BigDecimal.valueOf(totalSeDnvCnt != null ? totalSeDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCasltCnt), 2, RoundingMode.HALF_UP);
        } else {
            this.fatalityRate = BigDecimal.ZERO;
            this.seriousInjuryRate = BigDecimal.ZERO;
        }

        if (totalOccrrncCnt != null && totalOccrrncCnt > 0) {
            // 사고당 사상자수 계산
            this.casualtyPerAccident = BigDecimal.valueOf(totalCasltCnt != null ? totalCasltCnt : 0)
                    .divide(BigDecimal.valueOf(totalOccrrncCnt), 2, RoundingMode.HALF_UP);
        } else {
            this.casualtyPerAccident = BigDecimal.ZERO;
        }
    }

    private void calculatePopulationBasedStats() {
        if (populationSize != null && populationSize > 0) {
            // 인구 10만명당 사고건수
            this.accidentPerPopulation = BigDecimal.valueOf(totalOccrrncCnt != null ? totalOccrrncCnt : 0)
                    .multiply(BigDecimal.valueOf(100000))
                    .divide(BigDecimal.valueOf(populationSize), 2, RoundingMode.HALF_UP);

            // 인구 10만명당 사망자수
            this.deathPerPopulation = BigDecimal.valueOf(totalDthDnvCnt != null ? totalDthDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100000))
                    .divide(BigDecimal.valueOf(populationSize), 2, RoundingMode.HALF_UP);
        } else {
            this.accidentPerPopulation = BigDecimal.ZERO;
            this.deathPerPopulation = BigDecimal.ZERO;
        }
    }

    private void calculateAreaBasedStats() {
        if (areaSize != null && areaSize.compareTo(BigDecimal.ZERO) > 0) {
            // 면적 1㎢당 사고건수
            this.accidentDensity = BigDecimal.valueOf(totalOccrrncCnt != null ? totalOccrrncCnt : 0)
                    .divide(areaSize, 2, RoundingMode.HALF_UP);
        } else {
            this.accidentDensity = BigDecimal.ZERO;
        }
    }

    private void calculateDetailedRatios() {
        if (totalOccrrncCnt != null && totalOccrrncCnt > 0) {
            // 보행자 사고비율
            this.pedestrianAccidentRatio = BigDecimal.valueOf(pedestrianOccrrncCnt != null ? pedestrianOccrrncCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOccrrncCnt), 2, RoundingMode.HALF_UP);

            // 노인 사고비율
            this.elderlyAccidentRatio = BigDecimal.valueOf(elderlyOccrrncCnt != null ? elderlyOccrrncCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOccrrncCnt), 2, RoundingMode.HALF_UP);

            // 어린이 사고비율
            this.childAccidentRatio = BigDecimal.valueOf(childOccrrncCnt != null ? childOccrrncCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOccrrncCnt), 2, RoundingMode.HALF_UP);

            // 교통약자 사고비율
            int vulnerableAccidents = (pedestrianOccrrncCnt != null ? pedestrianOccrrncCnt : 0) +
                    (elderlyOccrrncCnt != null ? elderlyOccrrncCnt : 0) +
                    (childOccrrncCnt != null ? childOccrrncCnt : 0);
            this.vulnerableRoadUsersRatio = BigDecimal.valueOf(vulnerableAccidents)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOccrrncCnt), 2, RoundingMode.HALF_UP);
        } else {
            this.pedestrianAccidentRatio = BigDecimal.ZERO;
            this.elderlyAccidentRatio = BigDecimal.ZERO;
            this.childAccidentRatio = BigDecimal.ZERO;
            this.vulnerableRoadUsersRatio = BigDecimal.ZERO;
        }
    }

    private void calculateRiskScore() {
        if (totalOccrrncCnt == null || totalCasltCnt == null) {
            this.riskScore = BigDecimal.ZERO;
            return;
        }

        // 지자체별 통계 특성을 반영한 위험도 계산
        BigDecimal accidentWeight = BigDecimal.valueOf(totalOccrrncCnt).multiply(BigDecimal.valueOf(1.0));
        BigDecimal casualtyWeight = BigDecimal.valueOf(totalCasltCnt).multiply(BigDecimal.valueOf(1.5));
        BigDecimal deathWeight = BigDecimal.valueOf(totalDthDnvCnt != null ? totalDthDnvCnt : 0).multiply(BigDecimal.valueOf(8.0));
        BigDecimal seriousInjuryWeight = BigDecimal.valueOf(totalSeDnvCnt != null ? totalSeDnvCnt : 0).multiply(BigDecimal.valueOf(3.0));

        // 인구밀도 및 지역특성 가중치
        BigDecimal populationWeight = getPopulationWeight();
        BigDecimal urbanWeight = getUrbanWeight();

        this.riskScore = accidentWeight
                .add(casualtyWeight)
                .add(deathWeight)
                .add(seriousInjuryWeight)
                .multiply(populationWeight)
                .multiply(urbanWeight)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getPopulationWeight() {
        if (populationSize == null) return BigDecimal.ONE;

        // 인구 규모별 가중치
        if (populationSize >= 1000000) return BigDecimal.valueOf(1.5); // 100만 이상
        if (populationSize >= 500000) return BigDecimal.valueOf(1.3);  // 50만 이상
        if (populationSize >= 100000) return BigDecimal.valueOf(1.1);  // 10만 이상
        return BigDecimal.ONE;
    }

    private BigDecimal getUrbanWeight() {
        if (urbanLevel == null) return BigDecimal.ONE;

        switch (urbanLevel) {
            case URBAN:
                return BigDecimal.valueOf(1.4); // 도시지역 높은 가중치
            case SUBURBAN:
                return BigDecimal.valueOf(1.2); // 준도시 중간 가중치
            case RURAL:
                return BigDecimal.valueOf(1.0); // 농촌지역 기본 가중치
            default:
                return BigDecimal.ONE;
        }
    }

    private void determineRiskLevel() {
        if (riskScore == null) {
            this.riskLevel = RiskLevel.VERY_LOW;
            return;
        }

        // 지자체별 통계 기준으로 조정된 등급
        if (riskScore.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            this.riskLevel = RiskLevel.VERY_HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(2000)) >= 0) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else if (riskScore.compareTo(BigDecimal.valueOf(500)) >= 0) {
            this.riskLevel = RiskLevel.LOW;
        } else {
            this.riskLevel = RiskLevel.VERY_LOW;
        }
    }

    private void calculateSafetyImprovementScore() {
        if (yearOverYearChange == null) {
            this.safetyImprovementScore = BigDecimal.valueOf(50); // 중간값
            return;
        }

        // 전년대비 증감률을 바탕으로 안전개선점수 계산 (감소할수록 높은 점수)
        if (yearOverYearChange.compareTo(BigDecimal.valueOf(-20)) <= 0) {
            this.safetyImprovementScore = BigDecimal.valueOf(95); // 매우 좋은 개선
        } else if (yearOverYearChange.compareTo(BigDecimal.valueOf(-10)) <= 0) {
            this.safetyImprovementScore = BigDecimal.valueOf(80); // 좋은 개선
        } else if (yearOverYearChange.compareTo(BigDecimal.valueOf(-5)) <= 0) {
            this.safetyImprovementScore = BigDecimal.valueOf(70); // 개선
        } else if (yearOverYearChange.compareTo(BigDecimal.valueOf(5)) <= 0) {
            this.safetyImprovementScore = BigDecimal.valueOf(50); // 현상유지
        } else if (yearOverYearChange.compareTo(BigDecimal.valueOf(10)) <= 0) {
            this.safetyImprovementScore = BigDecimal.valueOf(30); // 악화
        } else {
            this.safetyImprovementScore = BigDecimal.valueOf(10); // 심각한 악화
        }
    }

    // ===== 비즈니스 메서드 =====

    public boolean isHighRiskRegion() {
        return riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.HIGH;
    }

    public boolean isMetropolitanArea() {
        return regionType == RegionType.METROPOLITAN || regionType == RegionType.SPECIAL;
    }

    public boolean hasHighFatalityRate() {
        return fatalityRate != null && fatalityRate.compareTo(BigDecimal.valueOf(3.0)) >= 0;
    }

    public boolean hasHighVulnerableRoadUsersRatio() {
        return vulnerableRoadUsersRatio != null && vulnerableRoadUsersRatio.compareTo(BigDecimal.valueOf(40.0)) >= 0;
    }

    public boolean isImprovingTrend() {
        return "DECREASING".equals(trendDirection) ||
                (yearOverYearChange != null && yearOverYearChange.compareTo(BigDecimal.ZERO) < 0);
    }

    public boolean isWorseningTrend() {
        return "INCREASING".equals(trendDirection) ||
                (yearOverYearChange != null && yearOverYearChange.compareTo(BigDecimal.valueOf(10.0)) >= 0);
    }

    public boolean hasHighAccidentDensity() {
        return accidentDensity != null && accidentDensity.compareTo(BigDecimal.valueOf(50.0)) >= 0;
    }

    public boolean isTopRankedInRegion() {
        return regionalRank != null && regionalRank <= 3;
    }

    public boolean isTopRankedNationally() {
        return nationalRank != null && nationalRank <= 10;
    }

    public boolean hasGoodSafetyScore() {
        return safetyImprovementScore != null && safetyImprovementScore.compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    public String getStatisticsSummary() {
        return String.format(
                "지자체 사고통계 - 지역: %s, 위험등급: %s, 사고건수: %d건, 사망자: %d명, 치사율: %.1f%%, 지역순위: %d위",
                sidoSggNm,
                riskLevel.getDescription(),
                totalOccrrncCnt != null ? totalOccrrncCnt : 0,
                totalDthDnvCnt != null ? totalDthDnvCnt : 0,
                fatalityRate != null ? fatalityRate.doubleValue() : 0.0,
                regionalRank != null ? regionalRank : 0
        );
    }

    public String getPopulationAnalysis() {
        return String.format(
                "인구 기반 분석 - 인구: %s명, 10만명당 사고: %.1f건, 10만명당 사망: %.1f명, 사고밀도: %.1f건/㎢",
                populationSize != null ? String.format("%,d", populationSize) : "미상",
                accidentPerPopulation != null ? accidentPerPopulation.doubleValue() : 0.0,
                deathPerPopulation != null ? deathPerPopulation.doubleValue() : 0.0,
                accidentDensity != null ? accidentDensity.doubleValue() : 0.0
        );
    }

    public String getVulnerableRoadUsersAnalysis() {
        return String.format(
                "교통약자 분석 - 보행자: %.1f%%, 노인: %.1f%%, 어린이: %.1f%%, 교통약자전체: %.1f%%",
                pedestrianAccidentRatio != null ? pedestrianAccidentRatio.doubleValue() : 0.0,
                elderlyAccidentRatio != null ? elderlyAccidentRatio.doubleValue() : 0.0,
                childAccidentRatio != null ? childAccidentRatio.doubleValue() : 0.0,
                vulnerableRoadUsersRatio != null ? vulnerableRoadUsersRatio.doubleValue() : 0.0
        );
    }

    public String getTrendAnalysis() {
        return String.format(
                "추세 분석 - 전년대비: %s%.1f%%, 추세: %s, 안전개선점수: %.1f점",
                yearOverYearChange != null && yearOverYearChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "",
                yearOverYearChange != null ? yearOverYearChange.doubleValue() : 0.0,
                trendDirection != null ? trendDirection : "미상",
                safetyImprovementScore != null ? safetyImprovementScore.doubleValue() : 0.0
        );
    }

    public String getRegionalComparison() {
        return String.format(
                "지역 비교 - 지역유형: %s, 도시화수준: %s, 지역순위: %d위, 전국순위: %d위",
                regionType != null ? regionType.getDescription() : "미분류",
                urbanLevel != null ? urbanLevel.getDescription() : "미분류",
                regionalRank != null ? regionalRank : 0,
                nationalRank != null ? nationalRank : 0
        );
    }
}