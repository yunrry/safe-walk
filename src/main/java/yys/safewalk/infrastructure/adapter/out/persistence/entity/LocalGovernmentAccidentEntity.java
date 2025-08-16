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
 * 지자체별 사고다발지역정보 API 엔티티
 * 지자체별 교통사고 다발지역 정보를 저장 (반경 150m 내, 지자체별 대상사고 3건 이상인 상위 3개 지점)
 *
 * API 정보: https://opendata.koroad.or.kr/data/rest/frequentzone/lg
 * 데이터 제공기간: 2017~2023
 */
@Entity
@Table(
        name = "local_government_accident",
        indexes = {
                @Index(name = "idx_lg_sido_sgg", columnList = "sido_sgg_nm"),
                @Index(name = "idx_lg_year", columnList = "search_year_cd"),
                @Index(name = "idx_lg_occurrence_cnt", columnList = "occrrnc_cnt"),
                @Index(name = "idx_lg_casualty_cnt", columnList = "caslt_cnt"),
                @Index(name = "idx_lg_death_cnt", columnList = "dth_dnv_cnt"),
                @Index(name = "idx_lg_coordinates", columnList = "lo_crd, la_crd"),
                @Index(name = "idx_lg_spot_cd", columnList = "spot_cd"),
                @Index(name = "idx_lg_bjd_cd", columnList = "bjd_cd"),
                @Index(name = "idx_lg_fatality_rate", columnList = "fatality_rate"),
                @Index(name = "idx_lg_risk_score", columnList = "risk_score"),
                @Index(name = "idx_lg_created_at", columnList = "created_at"),
                @Index(name = "idx_lg_top3_rank", columnList = "lg_top3_rank"),
                @Index(name = "idx_lg_composite", columnList = "search_year_cd, sido_sgg_nm, occrrnc_cnt"),
                @Index(name = "idx_lg_regional", columnList = "sido_cd, gugun_cd, lg_top3_rank")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("지자체별 사고다발지역정보")
public class LocalGovernmentAccidentEntity {

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
    @Comment("사고건수 - 다발지역 내 전체 교통사고건수")
    private Integer occrrnc_cnt;

    @Column(name = "caslt_cnt", nullable = false)
    @Comment("사상자수 - 다발지역 내 전체 사상자수")
    private Integer casltCnt;

    @Column(name = "dth_dnv_cnt", nullable = false)
    @Comment("사망자수 - 다발지역 내 사망자수")
    private Integer dthDnvCnt;

    @Column(name = "se_dnv_cnt", nullable = false)
    @Comment("중상자수 - 다발지역 내 중상자수")
    private Integer seDnvCnt;

    @Column(name = "sl_dnv_cnt", nullable = false)
    @Comment("경상자수 - 다발지역 내 경상자수")
    private Integer slDnvCnt;

    @Column(name = "wnd_dnv_cnt", nullable = false)
    @Comment("부상신고자수 - 다발지역 내 부상신고자수")
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

    // ===== 지자체별 특화 필드 =====

    @Column(name = "lg_top3_rank")
    @Comment("지자체내 TOP3 순위 - 해당 지자체 내에서의 위험도 순위 (1~3)")
    private Integer lgTop3Rank;

    @Column(name = "lg_total_spots")
    @Comment("지자체내 총 다발지역수 - 해당 지자체의 전체 다발지역 수")
    private Integer lgTotalSpots;

    @Column(name = "lg_area_coverage", precision = 5, scale = 2)
    @Comment("지자체 커버리지 - 반경 150m 기준 지역 커버리지 비율")
    private BigDecimal lgAreaCoverage;

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

    @Column(name = "risk_score", precision = 8, scale = 2)
    @Comment("위험도 점수 - 종합 위험도 계산 점수")
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Comment("위험등급 - VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW")
    private RiskLevel riskLevel;

    @Column(name = "accident_density", precision = 8, scale = 2)
    @Comment("사고 밀도 - 반경 150m 내 사고 밀도")
    private BigDecimal accidentDensity;

    @Column(name = "relative_risk_index", precision = 8, scale = 2)
    @Comment("상대적 위험지수 - 지자체 내 다른 지역 대비 상대적 위험도")
    private BigDecimal relativeRiskIndex;

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
    public LocalGovernmentAccidentEntity(
            String afosFid, String afosId, String bjdCd, String spotCd,
            String sidoSggNm, String spotNm, Integer occrrnc_cnt, Integer casltCnt,
            Integer dthDnvCnt, Integer seDnvCnt, Integer slDnvCnt, Integer wndDnvCnt,
            BigDecimal loCrd, BigDecimal laCrd, String geomJson,
            String searchYearCd, String sidoCd, String gugunCd,
            Integer lgTop3Rank, Integer lgTotalSpots,
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
        this.lgTop3Rank = lgTop3Rank;
        this.lgTotalSpots = lgTotalSpots;
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

        // 위험도 점수 계산
        calculateRiskScore();

        // 위험등급 결정
        determineRiskLevel();

        // 사고 밀도 계산 (반경 150m 기준)
        calculateAccidentDensity();

        // 상대적 위험지수 계산
        calculateRelativeRiskIndex();

        // 지자체 커버리지 계산
        calculateAreaCoverage();
    }

    private void calculateRiskScore() {
        if (occrrnc_cnt == null || casltCnt == null) {
            this.riskScore = BigDecimal.ZERO;
            return;
        }

        // 지자체별 위험도 계산 (TOP3 지역 특성 반영)
        BigDecimal accidentWeight = BigDecimal.valueOf(occrrnc_cnt).multiply(BigDecimal.valueOf(2.5));
        BigDecimal casualtyWeight = BigDecimal.valueOf(casltCnt).multiply(BigDecimal.valueOf(2.0));
        BigDecimal deathWeight = BigDecimal.valueOf(dthDnvCnt != null ? dthDnvCnt : 0).multiply(BigDecimal.valueOf(10.0));
        BigDecimal seriousInjuryWeight = BigDecimal.valueOf(seDnvCnt != null ? seDnvCnt : 0).multiply(BigDecimal.valueOf(5.0));

        // TOP3 순위 가중치 (순위가 높을수록 가중치 증가)
        BigDecimal rankWeight = BigDecimal.ONE;
        if (lgTop3Rank != null) {
            rankWeight = BigDecimal.valueOf(4 - lgTop3Rank); // 1순위=3.0, 2순위=2.0, 3순위=1.0
        }

        this.riskScore = accidentWeight
                .add(casualtyWeight)
                .add(deathWeight)
                .add(seriousInjuryWeight)
                .multiply(rankWeight)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void determineRiskLevel() {
        if (riskScore == null) {
            this.riskLevel = RiskLevel.VERY_LOW;
            return;
        }

        // 지자체별 TOP3 지역 기준으로 조정된 등급
        if (riskScore.compareTo(BigDecimal.valueOf(300)) >= 0) {
            this.riskLevel = RiskLevel.VERY_HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(200)) >= 0) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(100)) >= 0) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else if (riskScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            this.riskLevel = RiskLevel.LOW;
        } else {
            this.riskLevel = RiskLevel.VERY_LOW;
        }
    }

    private void calculateAccidentDensity() {
        if (occrrnc_cnt == null) {
            this.accidentDensity = BigDecimal.ZERO;
            return;
        }

        // 반경 150m 내 사고 밀도 계산 (π * 150² = 70,686 ㎡)
        BigDecimal area = BigDecimal.valueOf(70686); // 150m 반경 면적 (㎡)
        this.accidentDensity = BigDecimal.valueOf(occrrnc_cnt)
                .multiply(BigDecimal.valueOf(1000000)) // ㎢ 당 사고 수로 변환
                .divide(area, 2, RoundingMode.HALF_UP);
    }

    private void calculateRelativeRiskIndex() {
        if (lgTop3Rank == null || lgTotalSpots == null) {
            this.relativeRiskIndex = BigDecimal.ZERO;
            return;
        }

        // 지자체 내 상대적 위험지수 (순위가 높을수록 높은 지수)
        BigDecimal rankScore = BigDecimal.valueOf(4 - lgTop3Rank); // 1순위=3, 2순위=2, 3순위=1
        BigDecimal totalSpotsFactor = BigDecimal.valueOf(lgTotalSpots).multiply(BigDecimal.valueOf(0.1));

        this.relativeRiskIndex = rankScore
                .multiply(BigDecimal.valueOf(100))
                .add(totalSpotsFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void calculateAreaCoverage() {
        if (lgTotalSpots == null || lgTotalSpots == 0) {
            this.lgAreaCoverage = BigDecimal.ZERO;
            return;
        }

        // 반경 150m 기준 커버리지 계산 (단순화된 계산)
        BigDecimal singleSpotCoverage = BigDecimal.valueOf(7.07); // 150m 반경이 커버하는 대략적 비율
        this.lgAreaCoverage = singleSpotCoverage
                .multiply(BigDecimal.valueOf(lgTotalSpots))
                .min(BigDecimal.valueOf(100)) // 최대 100%
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ===== 비즈니스 메서드 =====

    public boolean isTop1InLocalGovernment() {
        return lgTop3Rank != null && lgTop3Rank == 1;
    }

    public boolean isTopRankedArea() {
        return lgTop3Rank != null && lgTop3Rank <= 2;
    }

    public boolean isHighRiskArea() {
        return riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.HIGH;
    }

    public boolean hasFatalAccident() {
        return dthDnvCnt != null && dthDnvCnt > 0;
    }

    public boolean hasHighAccidentDensity() {
        return accidentDensity != null && accidentDensity.compareTo(BigDecimal.valueOf(100)) >= 0;
    }

    public double[] getCoordinates() {
        if (loCrd != null && laCrd != null) {
            return new double[]{loCrd.doubleValue(), laCrd.doubleValue()};
        }
        return new double[]{0.0, 0.0};
    }

    public String getLocalGovernmentRiskSummary() {
        return String.format(
                "지자체별 위험지역 - 순위: %d위, 위험등급: %s, 사고건수: %d건, 사망자: %d명, 사상자: %d명",
                lgTop3Rank != null ? lgTop3Rank : 0,
                riskLevel.getDescription(),
                occrrnc_cnt != null ? occrrnc_cnt : 0,
                dthDnvCnt != null ? dthDnvCnt : 0,
                casltCnt != null ? casltCnt : 0
        );
    }
}