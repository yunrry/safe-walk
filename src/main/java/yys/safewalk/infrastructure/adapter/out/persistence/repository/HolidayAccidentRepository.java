package yys.safewalk.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.HolidayAccidentEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 연휴기간별 사고다발지역정보 Repository
 */
@Repository
public interface HolidayAccidentRepository extends JpaRepository<HolidayAccidentEntity, Long> {

    // ===== 중복 체크 메서드 =====

    /**
     * 고유 식별자 조합으로 중복 체크
     */
    boolean existsByAfosIdAndSearchYearCd(String afosId, String searchYearCd);

    /**
     * 지점코드와 연도로 중복 체크
     */
    boolean existsBySpotCdAndSearchYearCd(String spotCd, String searchYearCd);

    /**
     * 복합 키로 중복 체크
     */
    boolean existsByAfosIdAndSpotCdAndSearchYearCdAndSidoCdAndGugunCd(
            String afosId, String spotCd, String searchYearCd, String sidoCd, String gugunCd);

    // ===== 연휴기간별 특화 조회 메서드 =====

    /**
     * 연휴기간 유형별 조회
     */
    List<HolidayAccidentEntity> findByHolidayPeriodTypeOrderByRiskScoreDesc(
            HolidayAccidentEntity.HolidayPeriodType holidayPeriodType);

    /**
     * 계절별 조회
     */
    List<HolidayAccidentEntity> findBySeasonalCategoryOrderByRiskScoreDesc(
            HolidayAccidentEntity.SeasonalCategory seasonalCategory);

    /**
     * 계절별 위험등급 조회
     */
    List<HolidayAccidentEntity> findBySeasonalRiskLevelOrderByRiskScoreDesc(
            HolidayAccidentEntity.SeasonalRiskLevel seasonalRiskLevel);

    /**
     * 대형 연휴 (설날, 추석) 위험지역
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.holidayPeriodType IN ('NEW_YEAR', 'CHUSEOK')
        ORDER BY h.riskScore DESC
        """)
    List<HolidayAccidentEntity> findMajorHolidayRiskAreas();

    /**
     * 관광철 (여름휴가, 봄/가을 주말) 위험지역
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.holidayPeriodType IN ('SUMMER_VACATION', 'SPRING_WEEKEND', 'AUTUMN_WEEKEND')
        ORDER BY h.tourismImpactScore DESC
        """)
    List<HolidayAccidentEntity> findTourismSeasonRiskAreas();

    /**
     * 높은 관광영향점수 지역
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.tourismImpactScore >= :threshold 
        ORDER BY h.tourismImpactScore DESC
        """)
    List<HolidayAccidentEntity> findHighTourismImpactAreas(@Param("threshold") BigDecimal threshold);

    /**
     * 높은 교통량 증가 지역
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.holidayTrafficMultiplier >= :multiplier 
        ORDER BY h.holidayTrafficMultiplier DESC
        """)
    List<HolidayAccidentEntity> findHighTrafficIncreaseAreas(@Param("multiplier") BigDecimal multiplier);

    /**
     * 연휴기간 + 지역별 조회
     */
    List<HolidayAccidentEntity> findByHolidayPeriodTypeAndSidoSggNmContainingOrderByRiskScoreDesc(
            HolidayAccidentEntity.HolidayPeriodType holidayPeriodType, String sidoSggNm);

    // ===== 계절별 분석 =====

    /**
     * 연휴기간별 통계
     */
    @Query("""
        SELECT h.holidayPeriodType, COUNT(h), AVG(h.riskScore), SUM(h.occrrnc_cnt)
        FROM HolidayAccidentEntity h 
        WHERE h.searchYearCd = :year
        GROUP BY h.holidayPeriodType 
        ORDER BY AVG(h.riskScore) DESC
        """)
    List<Object[]> getHolidayPeriodStatistics(@Param("year") String year);

    /**
     * 계절별 통계
     */
    @Query("""
        SELECT h.seasonalCategory, COUNT(h), AVG(h.riskScore), AVG(h.tourismImpactScore)
        FROM HolidayAccidentEntity h 
        WHERE h.searchYearCd = :year
        GROUP BY h.seasonalCategory 
        ORDER BY AVG(h.riskScore) DESC
        """)
    List<Object[]> getSeasonalStatistics(@Param("year") String year);

    /**
     * 계절별 위험등급 분포
     */
    @Query("""
        SELECT h.seasonalRiskLevel, COUNT(h)
        FROM HolidayAccidentEntity h 
        GROUP BY h.seasonalRiskLevel 
        ORDER BY COUNT(h) DESC
        """)
    List<Object[]> getSeasonalRiskLevelDistribution();

    /**
     * 지역별 연휴기간 위험도 분석
     */
    @Query("""
        SELECT h.sidoSggNm, h.holidayPeriodType, AVG(h.riskScore), AVG(h.tourismImpactScore)
        FROM HolidayAccidentEntity h 
        WHERE h.searchYearCd = :year
        GROUP BY h.sidoSggNm, h.holidayPeriodType 
        ORDER BY h.sidoSggNm, AVG(h.riskScore) DESC
        """)
    List<Object[]> getRegionalHolidayRiskAnalysis(@Param("year") String year);

    // ===== 일반 조회 메서드 =====

    /**
     * 연도별 조회
     */
    List<HolidayAccidentEntity> findBySearchYearCdOrderByRiskScoreDesc(String searchYearCd);

    /**
     * 지역별 조회
     */
    List<HolidayAccidentEntity> findBySidoSggNmContainingOrderByRiskScoreDesc(String sidoSggNm);

    /**
     * 위험등급별 조회
     */
    List<HolidayAccidentEntity> findByRiskLevelOrderByRiskScoreDesc(
            HolidayAccidentEntity.RiskLevel riskLevel);

    /**
     * 고위험 연휴지역 조회
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.riskLevel IN ('VERY_HIGH', 'HIGH') 
        OR h.seasonalRiskLevel IN ('PEAK_TOURISM', 'HIGH_TRAFFIC')
        ORDER BY h.riskScore DESC
        """)
    List<HolidayAccidentEntity> findHighRiskHolidayAreas();

    /**
     * 좌표 범위 내 조회
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.loCrd BETWEEN :minLon AND :maxLon 
        AND h.laCrd BETWEEN :minLat AND :maxLat
        ORDER BY h.riskScore DESC
        """)
    List<HolidayAccidentEntity> findByCoordinateRange(
            @Param("minLon") BigDecimal minLon, @Param("maxLon") BigDecimal maxLon,
            @Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat);

    /**
     * 특정 반경 내 연휴 위험지역 조회
     */
    @Query(value = """
        SELECT * FROM holiday_accident h 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(h.la_crd)) * 
               cos(radians(h.lo_crd) - radians(:lon)) + 
               sin(radians(:lat)) * sin(radians(h.la_crd)))) <= :radiusKm
        ORDER BY h.risk_score DESC
        """, nativeQuery = true)
    List<HolidayAccidentEntity> findByLocationWithinRadius(
            @Param("lat") double lat, @Param("lon") double lon, @Param("radiusKm") double radiusKm);

    // ===== 통계 쿼리 =====

    /**
     * 연휴기간별 총 사고건수
     */
    @Query("""
        SELECT h.holidayPeriodType, SUM(h.occrrnc_cnt) 
        FROM HolidayAccidentEntity h 
        WHERE h.searchYearCd = :year
        GROUP BY h.holidayPeriodType
        """)
    List<Object[]> getTotalAccidentCountByHolidayPeriod(@Param("year") String year);

    /**
     * 연도별 총 연휴 사고건수
     */
    @Query("SELECT SUM(h.occrrnc_cnt) FROM HolidayAccidentEntity h WHERE h.searchYearCd = :year")
    Long getTotalHolidayAccidentCountByYear(@Param("year") String year);

    /**
     * 평균 관광영향점수
     */
    @Query("SELECT AVG(h.tourismImpactScore) FROM HolidayAccidentEntity h WHERE h.tourismImpactScore > 0")
    Double getAverageTourismImpactScore();

    /**
     * 평균 교통량 증가배수
     */
    @Query("SELECT AVG(h.holidayTrafficMultiplier) FROM HolidayAccidentEntity h WHERE h.holidayTrafficMultiplier > 0")
    Double getAverageTrafficMultiplier();

    /**
     * 연휴 사고밀도 평균
     */
    @Query("SELECT AVG(h.holidayAccidentDensity) FROM HolidayAccidentEntity h WHERE h.holidayAccidentDensity > 0")
    Double getAverageHolidayAccidentDensity();

    /**
     * 상위 N개 연휴 위험지역
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        ORDER BY h.riskScore DESC 
        LIMIT :limit
        """)
    List<HolidayAccidentEntity> getTopHolidayRiskAreas(@Param("limit") int limit);

    // ===== 데이터 수집 관련 메서드 =====

    /**
     * 특정 지역의 연휴 데이터 수집 완료 여부 확인
     */
    @Query("""
        SELECT COUNT(h) > 0 FROM HolidayAccidentEntity h 
        WHERE h.sidoCd = :sidoCd AND h.gugunCd = :gugunCd AND h.searchYearCd = :year
        """)
    boolean isHolidayDataCollectedForRegion(@Param("sidoCd") String sidoCd,
                                            @Param("gugunCd") String gugunCd,
                                            @Param("year") String year);

    /**
     * 연도별 수집된 연휴 데이터 개수
     */
    @Query("SELECT COUNT(h) FROM HolidayAccidentEntity h WHERE h.searchYearCd = :year")
    Long getHolidayCollectedDataCountByYear(@Param("year") String year);

    /**
     * 최근 수집된 연휴 데이터
     */
    List<HolidayAccidentEntity> findTop10ByOrderByCreatedAtDesc();

    // ===== 복합 조건 검색 =====

    /**
     * 복합 조건 검색 (연휴 특화)
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE (:region IS NULL OR h.sidoSggNm LIKE %:region%) 
        AND (:year IS NULL OR h.searchYearCd = :year) 
        AND (:holidayType IS NULL OR h.holidayPeriodType = :holidayType)
        AND (:season IS NULL OR h.seasonalCategory = :season)
        AND (:minRiskScore IS NULL OR h.riskScore >= :minRiskScore) 
        AND (:riskLevel IS NULL OR h.riskLevel = :riskLevel)
        AND (:minTourismScore IS NULL OR h.tourismImpactScore >= :minTourismScore)
        ORDER BY h.riskScore DESC
        """)
    Page<HolidayAccidentEntity> findByComplexConditions(
            @Param("region") String region,
            @Param("year") String year,
            @Param("holidayType") HolidayAccidentEntity.HolidayPeriodType holidayType,
            @Param("season") HolidayAccidentEntity.SeasonalCategory season,
            @Param("minRiskScore") BigDecimal minRiskScore,
            @Param("riskLevel") HolidayAccidentEntity.RiskLevel riskLevel,
            @Param("minTourismScore") BigDecimal minTourismScore,
            Pageable pageable);

    /**
     * 지점명으로 검색
     */
    List<HolidayAccidentEntity> findBySpotNmContainingIgnoreCaseOrderByRiskScoreDesc(String spotNm);

    /**
     * 중복 데이터 조회
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.afosId IN (
            SELECT h2.afosId FROM HolidayAccidentEntity h2 
            GROUP BY h2.afosId 
            HAVING COUNT(h2.afosId) > 1
        )
        ORDER BY h.afosId, h.createdAt
        """)
    List<HolidayAccidentEntity> findDuplicateRecords();

    // ===== 특수 분석 메서드 =====

    /**
     * 관광지 위험도 분석 (높은 관광영향 + 높은 위험도)
     */
    @Query("""
        SELECT h FROM HolidayAccidentEntity h 
        WHERE h.tourismImpactScore >= :tourismThreshold
        AND h.riskScore >= :riskThreshold
        AND h.seasonalRiskLevel IN ('PEAK_TOURISM', 'HIGH_TRAFFIC')
        ORDER BY h.tourismImpactScore DESC
        """)
    List<HolidayAccidentEntity> findTourismHighRiskAreas(
            @Param("tourismThreshold") BigDecimal tourismThreshold,
            @Param("riskThreshold") BigDecimal riskThreshold);

    /**
     * 연휴별 지역 안전도 비교
     */
    @Query("""
        SELECT h.sidoSggNm, 
               AVG(CASE WHEN h.holidayPeriodType = 'NEW_YEAR' THEN h.riskScore END) as newYearRisk,
               AVG(CASE WHEN h.holidayPeriodType = 'CHUSEOK' THEN h.riskScore END) as chuseokRisk,
               AVG(CASE WHEN h.holidayPeriodType = 'SUMMER_VACATION' THEN h.riskScore END) as summerRisk
        FROM HolidayAccidentEntity h 
        WHERE h.searchYearCd = :year
        GROUP BY h.sidoSggNm 
        ORDER BY h.sidoSggNm
        """)
    List<Object[]> getHolidayRiskComparisonByRegion(@Param("year") String year);

    /**
     * 교통량 증가 위험도 상관관계 분석
     */
    @Query("""
        SELECT h.holidayTrafficMultiplier, AVG(h.riskScore), COUNT(h)
        FROM HolidayAccidentEntity h 
        WHERE h.holidayTrafficMultiplier IS NOT NULL
        GROUP BY h.holidayTrafficMultiplier 
        ORDER BY h.holidayTrafficMultiplier
        """)
    List<Object[]> getTrafficMultiplierRiskCorrelation();
}