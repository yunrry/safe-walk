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
 * 보행노인 사고다발지역정보 API 엔티티
 * 65세 이상 보행노인 교통사고 다발지역 정보를 저장
 *
 * API 정보: https://opendata.koroad.or.kr/data/rest/frequentzone/oldman
 * 데이터 제공기간: 2012~2023
 */
@Entity
@Table(
        name = "elderly_pedestrian_accident",
        indexes = {
                @Index(name = "idx_elderly_sido_sgg", columnList = "sido_sgg_nm"),
                @Index(name = "idx_elderly_year", columnList = "search_year_cd"),
                @Index(name = "idx_elderly_occurrence_cnt", columnList = "occrrnc_cnt"),
                @Index(name = "idx_elderly_casualty_cnt", columnList = "caslt_cnt"),
                @Index(name = "idx_elderly_death_cnt", columnList = "dth_dnv_cnt"),
                @Index(name = "idx_elderly_coordinates", columnList = "lo_crd, la_crd"),
                @Index(name = "idx_elderly_spot_cd", columnList = "spot_cd"),
                @Index(name = "idx_elderly_bjd_cd", columnList = "bjd_cd"),
                @Index(name = "idx_elderly_fatality_rate", columnList = "fatality_rate"),
                @Index(name = "idx_elderly_risk_score", columnList = "risk_score"),
                @Index(name = "idx_elderly_created_at", columnList = "created_at"),
                @Index(name = "idx_elderly_composite", columnList = "search_year_cd, sido_sgg_nm, occrrnc_cnt")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("보행노인 사고다발지역정보")
public class ElderlyPedestrianAccidentEntity {

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
    @Comment("사고건수 - 다발지역 내 65세 이상 보행노인 사고건수")
    private Integer occrrnc_cnt;

    @Column(name = "caslt_cnt", nullable = false)
    @Comment("사상자수 - 다발지역 내 65세 이상 보행노인 사상자수")
    private Integer casltCnt;

    @Column(name = "dth_dnv_cnt", nullable = false)
    @Comment("사망자수 - 다발지역 내 65세 이상 보행노인 사망자수")
    private Integer dthDnvCnt;

    @Column(name = "se_dnv_cnt", nullable = false)
    @Comment("중상자수 - 다발지역 내 65세 이상 보행노인 중상자수")
    private Integer seDnvCnt;

    @Column(name = "sl_dnv_cnt", nullable = false)
    @Comment("경상자수 - 다발지역 내 65세 이상 보행노인 경상자수")
    private Integer slDnvCnt;

    @Column(name = "wnd_dnv_cnt", nullable = false)
    @Comment("부상신고자수 - 다발지역 내 65세 이상 보행노인 부상신고자수")
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

    // ===== 계산된 필드 =====

    @Column(name = "fatality_rate", precision = 5, scale = 2)
    @Comment("치사율 - 사망자수/사상자수 * 100 (65세 이상 보행노인 기준)")
    private BigDecimal fatalityRate;

    @Column(name = "serious_injury_rate", precision = 5, scale = 2)
    @Comment("중상률 - 중상자수/사상자수 * 100 (65세 이상 보행노인 기준)")
    private BigDecimal seriousInjuryRate;

    @Column(name = "casualty_per_accident", precision = 5, scale = 2)
    @Comment("사고당 사상자수 - 사상자수/사고건수 (65세 이상 보행노인 기준)")
    private BigDecimal casualtyPerAccident;

    @Column(name = "risk_score", precision = 8, scale = 2)
    @Comment("위험도 점수 - 종합 위험도 계산 점수 (65세 이상 보행노인 기준)")
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Comment("위험등급 - VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW")
    private RiskLevel riskLevel;

    @Column(name = "elderly_accident_density", precision = 8, scale = 2)
    @Comment("노인 사고 밀도 - 65세 이상 보행노인 사고의 지역별 밀도")
    private BigDecimal elderlyAccidentDensity;

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
    public ElderlyPedestrianAccidentEntity(
            String afosFid, String afosId, String bjdCd, String spotCd,
            String sidoSggNm, String spotNm, Integer occrrnc_cnt, Integer casltCnt,
            Integer dthDnvCnt, Integer seDnvCnt, Integer slDnvCnt, Integer wndDnvCnt,
            BigDecimal loCrd, BigDecimal laCrd, String geomJson,
            String searchYearCd, String sidoCd, String gugunCd,
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

    /**
     * 계산된 필드들을 자동으로 계산합니다.
     */
    @PrePersist
    @PreUpdate
    public void calculateDerivedFields() {
        if (casltCnt != null && casltCnt > 0) {
            // 치사율 계산 (65세 이상 보행노인 기준)
            this.fatalityRate = BigDecimal.valueOf(dthDnvCnt != null ? dthDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(casltCnt), 2, RoundingMode.HALF_UP);

            // 중상률 계산 (65세 이상 보행노인 기준)
            this.seriousInjuryRate = BigDecimal.valueOf(seDnvCnt != null ? seDnvCnt : 0)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(casltCnt), 2, RoundingMode.HALF_UP);
        } else {
            this.fatalityRate = BigDecimal.ZERO;
            this.seriousInjuryRate = BigDecimal.ZERO;
        }

        if (occrrnc_cnt != null && occrrnc_cnt > 0) {
            // 사고당 사상자수 계산 (65세 이상 보행노인 기준)
            this.casualtyPerAccident = BigDecimal.valueOf(casltCnt != null ? casltCnt : 0)
                    .divide(BigDecimal.valueOf(occrrnc_cnt), 2, RoundingMode.HALF_UP);
        } else {
            this.casualtyPerAccident = BigDecimal.ZERO;
        }

        // 65세 이상 보행노인 위험도 점수 계산
        calculateElderlyRiskScore();

        // 위험등급 결정
        determineRiskLevel();

        // 노인 사고 밀도 계산 (단순화된 계산)
        calculateElderlyAccidentDensity();
    }

    /**
     * 65세 이상 보행노인 전용 위험도 점수를 계산합니다.
     * 노인 보행자의 특성을 고려하여 가중치를 조정합니다.
     */
    private void calculateElderlyRiskScore() {
        if (occrrnc_cnt == null || casltCnt == null) {
            this.riskScore = BigDecimal.ZERO;
            return;
        }

        // 65세 이상 보행노인 위험도 계산 (노인 특성 반영)
        BigDecimal accidentWeight = BigDecimal.valueOf(occrrnc_cnt).multiply(BigDecimal.valueOf(3.0)); // 사고건수 가중치 증가
        BigDecimal casualtyWeight = BigDecimal.valueOf(casltCnt).multiply(BigDecimal.valueOf(2.5)); // 사상자수 가중치 증가
        BigDecimal deathWeight = BigDecimal.valueOf(dthDnvCnt != null ? dthDnvCnt : 0).multiply(BigDecimal.valueOf(15.0)); // 사망자 가중치 대폭 증가
        BigDecimal seriousInjuryWeight = BigDecimal.valueOf(seDnvCnt != null ? seDnvCnt : 0).multiply(BigDecimal.valueOf(8.0)); // 중상자 가중치 증가

        this.riskScore = accidentWeight
                .add(casualtyWeight)
                .add(deathWeight)
                .add(seriousInjuryWeight)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 위험등급을 결정합니다. (65세 이상 보행노인 기준으로 조정)
     */
    private void determineRiskLevel() {
        if (riskScore == null) {
            this.riskLevel = RiskLevel.VERY_LOW;
            return;
        }

        // 노인 보행자를 위한 더 엄격한 기준 적용
        if (riskScore.compareTo(BigDecimal.valueOf(200)) >= 0) {
            this.riskLevel = RiskLevel.VERY_HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(120)) >= 0) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else if (riskScore.compareTo(BigDecimal.valueOf(20)) >= 0) {
            this.riskLevel = RiskLevel.LOW;
        } else {
            this.riskLevel = RiskLevel.VERY_LOW;
        }
    }

    /**
     * 65세 이상 보행노인 사고 밀도를 계산합니다.
     */
    private void calculateElderlyAccidentDensity() {
        if (occrrnc_cnt == null) {
            this.elderlyAccidentDensity = BigDecimal.ZERO;
            return;
        }

        // 단순화된 밀도 계산 (실제로는 지역 면적 정보가 필요)
        // 현재는 사고건수를 기준으로 상대적 밀도 계산
        this.elderlyAccidentDensity = BigDecimal.valueOf(occrrnc_cnt)
                .multiply(BigDecimal.valueOf(10.0)) // 노인 사고의 상대적 위험도 반영
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 해당 지역이 65세 이상 보행노인에게 고위험 지역인지 확인합니다.
     */
    public boolean isHighRiskForElderly() {
        return riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.HIGH;
    }

    /**
     * 해당 지역이 65세 이상 보행노인 사망사고 발생 지역인지 확인합니다.
     */
    public boolean hasFatalAccident() {
        return dthDnvCnt != null && dthDnvCnt > 0;
    }

    /**
     * 해당 지역의 65세 이상 보행노인 중상 사고 비율이 높은지 확인합니다.
     */
    public boolean hasHighSeriousInjuryRate() {
        return seriousInjuryRate != null && seriousInjuryRate.compareTo(BigDecimal.valueOf(50)) >= 0;
    }

    /**
     * 좌표 기반 거리 계산을 위한 위도/경도 반환
     */
    public double[] getCoordinates() {
        if (loCrd != null && laCrd != null) {
            return new double[]{loCrd.doubleValue(), laCrd.doubleValue()};
        }
        return new double[]{0.0, 0.0};
    }

    /**
     * 노인 보행자 전용 위험 정보 요약
     */
    public String getElderlyRiskSummary() {
        return String.format(
                "65세 이상 보행노인 위험지역 - 위험등급: %s, 사고건수: %d건, 사망자: %d명, 중상자: %d명, 치사율: %.1f%%",
                riskLevel.getDescription(),
                occrrnc_cnt != null ? occrrnc_cnt : 0,
                dthDnvCnt != null ? dthDnvCnt : 0,
                seDnvCnt != null ? seDnvCnt : 0,
                fatalityRate != null ? fatalityRate.doubleValue() : 0.0
        );
    }
}