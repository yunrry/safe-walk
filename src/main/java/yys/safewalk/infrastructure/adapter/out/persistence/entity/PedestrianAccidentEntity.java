// infrastructure/adapter/out/persistence/entity/PedestrianAccidentEntity.java
package yys.safewalk.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 보행자 사고다발지역정보 API 원본 데이터 저장용 JPA Entity
 * API Endpoint: /frequentzone/pedstrians
 */
@Entity
@Table(name = "pedestrian_accidents",
        indexes = {
                @Index(name = "idx_afos_id", columnList = "afosId"),
                @Index(name = "idx_region", columnList = "sidoSggNm"),
                @Index(name = "idx_data_year", columnList = "dataYear"),
                @Index(name = "idx_location", columnList = "latitude, longitude"),
                @Index(name = "idx_accident_count", columnList = "occrrncCnt"),
                @Index(name = "idx_collected_at", columnList = "collectedAt")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Comment("보행자 사고다발지역정보 API 원본 데이터")
public class PedestrianAccidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("자동 증가 ID")
    private Long id;

    // ===========================================
    // API 응답 필드들 (원본 그대로)
    // ===========================================

    @Column(name = "afos_fid", length = 50)
    @Comment("다발지역FID (공간정보 식별자)")
    private String afosFid;

    @Column(name = "afos_id", length = 20, nullable = false)
    @Comment("다발지역ID")
    private String afosId;

    @Column(name = "bjd_cd", length = 20)
    @Comment("법정동코드")
    private String bjdCd;

    @Column(name = "spot_cd", length = 20)
    @Comment("지점코드")
    private String spotCd;

    @Column(name = "sido_sgg_nm", length = 100, nullable = false)
    @Comment("시도시군구명")
    private String sidoSggNm;

    @Column(name = "spot_nm", length = 200, nullable = false)
    @Comment("지점명 (다발지역 지점의 위치)")
    private String spotNm;

    @Column(name = "occrrnc_cnt")
    @Comment("사고건수")
    private Integer occrrncCnt;

    @Column(name = "caslt_cnt")
    @Comment("사상자수")
    private Integer casltCnt;

    @Column(name = "dth_dnv_cnt")
    @Comment("사망자수")
    private Integer dthDnvCnt;

    @Column(name = "se_dnv_cnt")
    @Comment("중상자수")
    private Integer seDnvCnt;

    @Column(name = "sl_dnv_cnt")
    @Comment("경상자수")
    private Integer slDnvCnt;

    @Column(name = "wnd_dnv_cnt")
    @Comment("부상신고자수")
    private Integer wndDnvCnt;

    @Column(name = "longitude", precision = 15, scale = 12)
    @Comment("경도 (EPSG 4326)")
    private Double longitude;

    @Column(name = "latitude", precision = 15, scale = 12)
    @Comment("위도 (EPSG 4326)")
    private Double latitude;

    @Column(name = "geom_json", columnDefinition = "LONGTEXT")
    @Comment("다발지역 폴리곤 정보 (GeoJSON 형태)")
    private String geomJson;

    // ===========================================
    // 메타데이터 필드들
    // ===========================================

    @Column(name = "data_year", length = 4, nullable = false)
    @Comment("데이터 기준 연도")
    private String dataYear;

    @Column(name = "sido_code", length = 2)
    @Comment("시도 코드 (추출된 값)")
    private String sidoCode;

    @Column(name = "gugun_code", length = 3)
    @Comment("시군구 코드 (추출된 값)")
    private String gugunCode;

    @Column(name = "api_result_code", length = 2)
    @Comment("API 호출 결과 코드")
    private String apiResultCode;

    @Column(name = "api_result_msg", length = 200)
    @Comment("API 호출 결과 메시지")
    private String apiResultMsg;

    @CreationTimestamp
    @Column(name = "collected_at", nullable = false, updatable = false)
    @Comment("데이터 수집 시간")
    private LocalDateTime collectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("레코드 생성 시간")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Comment("레코드 수정 시간")
    private LocalDateTime updatedAt;

    // ===========================================
    // 계산된 필드들 (성능 최적화용)
    // ===========================================

    @Column(name = "fatality_rate", precision = 5, scale = 2)
    @Comment("치사율 (계산된 값)")
    private Double fatalityRate;

    @Column(name = "serious_injury_rate", precision = 5, scale = 2)
    @Comment("중상 이상 비율 (계산된 값)")
    private Double seriousInjuryRate;

    @Column(name = "risk_score", precision = 8, scale = 2)
    @Comment("위험도 점수 (계산된 값)")
    private Double riskScore;

    @Column(name = "risk_level", length = 20)
    @Comment("위험도 레벨 (계산된 값)")
    private String riskLevel;



    // ===========================================
    // JPA 라이프사이클 메서드들
    // ===========================================

    @PrePersist
    @PreUpdate
    private void calculateDerivedFields() {
        // 치사율 계산
        if (casltCnt != null && casltCnt > 0 && dthDnvCnt != null) {
            this.fatalityRate = (double) dthDnvCnt / casltCnt * 100;
        } else {
            this.fatalityRate = 0.0;
        }

        // 중상 이상 비율 계산
        if (casltCnt != null && casltCnt > 0) {
            int seriousTotal = (dthDnvCnt != null ? dthDnvCnt : 0) +
                    (seDnvCnt != null ? seDnvCnt : 0);
            this.seriousInjuryRate = (double) seriousTotal / casltCnt * 100;
        } else {
            this.seriousInjuryRate = 0.0;
        }

        // 위험도 점수 계산
        this.riskScore = calculateRiskScore();

        // 위험도 레벨 결정
        this.riskLevel = determineRiskLevel();
    }

    // ===========================================
    // 비즈니스 로직 메서드들
    // ===========================================

    /**
     * 위험도 점수 계산
     */
    private Double calculateRiskScore() {
        double score = 0.0;

        if (occrrncCnt != null) score += occrrncCnt;
        if (dthDnvCnt != null) score += dthDnvCnt * 10.0;
        if (seDnvCnt != null) score += seDnvCnt * 5.0;
        if (slDnvCnt != null) score += slDnvCnt * 2.0;
        if (wndDnvCnt != null) score += wndDnvCnt;

        return score;
    }

    /**
     * 위험도 레벨 결정
     */
    private String determineRiskLevel() {
        if (fatalityRate == null || riskScore == null) {
            return "UNKNOWN";
        }

        if (riskScore >= 100 || fatalityRate >= 10) {
            return "VERY_DANGER";
        } else if (riskScore >= 50 || fatalityRate >= 5) {
            return "DANGER";
        } else if (riskScore >= 20 || fatalityRate >= 2) {
            return "CAUTION";
        } else {
            return "SAFE";
        }
    }

    /**
     * 데이터 유효성 검증
     */
    public boolean isValid() {
        return afosId != null && !afosId.trim().isEmpty() &&
                sidoSggNm != null && !sidoSggNm.trim().isEmpty() &&
                spotNm != null && !spotNm.trim().isEmpty() &&
                occrrncCnt != null && occrrncCnt > 0 &&
                hasValidCoordinates();
    }

    /**
     * 좌표 유효성 확인
     */
    public boolean hasValidCoordinates() {
        return longitude != null && latitude != null &&
                longitude >= -180.0 && longitude <= 180.0 &&
                latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * 한국 영역 내 좌표인지 확인
     */
    public boolean isWithinKorea() {
        if (!hasValidCoordinates()) {
            return false;
        }
        return longitude >= 124.0 && longitude <= 132.0 &&
                latitude >= 33.0 && latitude <= 43.0;
    }

    /**
     * 보행자 위험구간인지 확인 (기준: 사고 7건 이상)
     */
    public boolean isPedestrianDangerousArea() {
        return occrrncCnt != null && occrrncCnt >= 7;
    }

    /**
     * 고위험 구간인지 확인
     */
    public boolean isHighRiskArea() {
        return "DANGER".equals(riskLevel) || "VERY_DANGER".equals(riskLevel);
    }

    /**
     * 최신 데이터인지 확인 (1개월 이내)
     */
    public boolean isRecentData() {
        return collectedAt != null &&
                collectedAt.isAfter(LocalDateTime.now().minusMonths(1));
    }

    /**
     * API 호출이 성공했는지 확인
     */
    public boolean isApiCallSuccessful() {
        return "00".equals(apiResultCode);
    }

    // ===========================================
    // 편의 메서드들
    // ===========================================

    /**
     * 지역 코드 설정 (시도, 시군구 분리)
     */
    public void setRegionCodes(String sido, String gugun) {
        this.sidoCode = sido;
        this.gugunCode = gugun;
    }

    /**
     * API 응답 정보 설정
     */
    public void setApiResponseInfo(String resultCode, String resultMsg) {
        this.apiResultCode = resultCode;
        this.apiResultMsg = resultMsg;
    }

    /**
     * 총 사상자수 계산
     */
    public Integer getTotalCasualtyCount() {
        int total = 0;
        if (dthDnvCnt != null) total += dthDnvCnt;
        if (seDnvCnt != null) total += seDnvCnt;
        if (slDnvCnt != null) total += slDnvCnt;
        if (wndDnvCnt != null) total += wndDnvCnt;
        return total;
    }

    /**
     * 지역명 요약 (시군구만 추출)
     */
    public String getRegionSummary() {
        if (sidoSggNm == null) {
            return "알 수 없음";
        }
        String[] parts = sidoSggNm.split(" ");
        return parts.length > 1 ? parts[1] : sidoSggNm;
    }

    /**
     * 좌표 요약 정보
     */
    public String getLocationSummary() {
        if (!hasValidCoordinates()) {
            return "좌표정보 없음";
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * 사고 정보 요약
     */
    public String getAccidentSummary() {
        return String.format("사고:%d건, 사상자:%d명, 사망:%d명",
                occrrncCnt != null ? occrrncCnt : 0,
                casltCnt != null ? casltCnt : 0,
                dthDnvCnt != null ? dthDnvCnt : 0);
    }

    @Override
    public String toString() {
        return String.format("PedestrianAccidentEntity{id=%d, afosId='%s', region='%s', accidents=%d, coordinates=[%.6f, %.6f]}",
                id, afosId, sidoSggNm,
                occrrncCnt != null ? occrrncCnt : 0,
                latitude != null ? latitude : 0.0,
                longitude != null ? longitude : 0.0);
    }

//    public enum RiskLevel {
//        VERY_HIGH("매우 높음"),
//        HIGH("높음"),
//        MEDIUM("보통"),
//        LOW("낮음"),
//        VERY_LOW("매우 낮음");
//
//        private final String description;
//
//        RiskLevel(String description) {
//            this.description = description;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//    }
}