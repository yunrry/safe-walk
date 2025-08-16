package yys.safewalk.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.AccidentStatisticsEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 지자체별 대상사고통계 Repository
 */
@Repository
public interface AccidentStatisticsRepository extends JpaRepository<AccidentStatisticsEntity, Long> {

    // ===== 중복 체크 메서드 =====

    /**
     * 지자체명과 연도로 중복 체크
     */
    boolean existsBySidoSggNmAndSearchYearCd(String sidoSggNm, String searchYearCd);

    /**
     * 시도/시군구 코드와 연도로 중복 체크
     */
    boolean existsBySidoCdAndGugunCdAndSearchYearCd(String sidoCd, String gugunCd, String searchYearCd);

    /**
     * 특정 지자체의 특정 연도 데이터 존재 여부
     */
    @Query("""
        SELECT COUNT(a) > 0 FROM AccidentStatisticsEntity a 
        WHERE a.sidoSggNm = :sidoSggNm AND a.searchYearCd = :year
        """)
    boolean existsStatisticsData(@Param("sidoSggNm") String sidoSggNm, @Param("year") String year);

    // ===== 기본 조회 메서드 =====

    /**
     * 지자체명으로 조회
     */
    List<AccidentStatisticsEntity> findBySidoSggNmOrderBySearchYearCdDesc(String sidoSggNm);

    /**
     * 연도별 조회
     */
    List<AccidentStatisticsEntity> findBySearchYearCdOrderByTotalOccrrncCntDesc(String searchYearCd);

    /**
     * 지자체명과 연도로 조회
     */
    Optional<AccidentStatisticsEntity> findBySidoSggNmAndSearchYearCd(String sidoSggNm, String searchYearCd);

    /**
     * 지역유형별 조회
     */
    List<AccidentStatisticsEntity> findByRegionTypeOrderByTotalOccrrncCntDesc(
            AccidentStatisticsEntity.RegionType regionType);

    /**
     * 도시화수준별 조회
     */
    List<AccidentStatisticsEntity> findByUrbanLevelOrderByAccidentPerPopulationDesc(
            AccidentStatisticsEntity.UrbanLevel urbanLevel);

    /**
     * 위험등급별 조회
     */
    List<AccidentStatisticsEntity> findByRiskLevelOrderByRiskScoreDesc(
            AccidentStatisticsEntity.RiskLevel riskLevel);

    // ===== 순위 기반 조회 =====

    /**
     * 전국 상위 N개 위험 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        ORDER BY a.totalOccrrncCnt DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopRiskLocalGovernments(@Param("year") String year, @Param("limit") int limit);

    /**
     * 인구대비 사고율 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.accidentPerPopulation > 0
        ORDER BY a.accidentPerPopulation DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopAccidentRatePerPopulation(@Param("year") String year, @Param("limit") int limit);

    /**
     * 치사율 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.fatalityRate > 0
        ORDER BY a.fatalityRate DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopFatalityRateAreas(@Param("year") String year, @Param("limit") int limit);

    /**
     * 지역내 순위별 조회
     */
    List<AccidentStatisticsEntity> findByRegionalRankAndSearchYearCdOrderByTotalOccrrncCntDesc(
            Integer regionalRank, String searchYearCd);

    /**
     * 전국 순위별 조회
     */
    List<AccidentStatisticsEntity> findByNationalRankAndSearchYearCdOrderByNationalRankAsc(
            Integer nationalRank, String searchYearCd);

    // ===== 통계 분석 쿼리 =====

    /**
     * 전국 총 사고건수 (연도별)
     */
    @Query("SELECT SUM(a.totalOccrrncCnt) FROM AccidentStatisticsEntity a WHERE a.searchYearCd = :year")
    Long getNationalTotalAccidentCount(@Param("year") String year);

    /**
     * 전국 총 사상자수 (연도별)
     */
    @Query("SELECT SUM(a.totalCasltCnt) FROM AccidentStatisticsEntity a WHERE a.searchYearCd = :year")
    Long getNationalTotalCasualtyCount(@Param("year") String year);

    /**
     * 전국 총 사망자수 (연도별)
     */
    @Query("SELECT SUM(a.totalDthDnvCnt) FROM AccidentStatisticsEntity a WHERE a.searchYearCd = :year")
    Long getNationalTotalDeathCount(@Param("year") String year);

    /**
     * 전국 평균 치사율
     */
    @Query("SELECT AVG(a.fatalityRate) FROM AccidentStatisticsEntity a WHERE a.fatalityRate > 0 AND a.searchYearCd = :year")
    Double getNationalAverageFatalityRate(@Param("year") String year);

    /**
     * 시도별 통계 집계
     */
    @Query("""
        SELECT SUBSTRING(a.sidoSggNm, 1, LOCATE(' ', a.sidoSggNm) - 1) as sido,
               COUNT(a) as areaCount,
               SUM(a.totalOccrrncCnt) as totalAccidents,
               AVG(a.fatalityRate) as avgFatalityRate,
               AVG(a.accidentPerPopulation) as avgAccidentRate
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        GROUP BY SUBSTRING(a.sidoSggNm, 1, LOCATE(' ', a.sidoSggNm) - 1)
        ORDER BY SUM(a.totalOccrrncCnt) DESC
        """)
    List<Object[]> getSidoStatistics(@Param("year") String year);

    /**
     * 지역유형별 통계
     */
    @Query("""
        SELECT a.regionType, 
               COUNT(a) as areaCount,
               AVG(a.totalOccrrncCnt) as avgAccidents,
               AVG(a.fatalityRate) as avgFatalityRate,
               AVG(a.accidentPerPopulation) as avgAccidentRate
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        GROUP BY a.regionType 
        ORDER BY AVG(a.totalOccrrncCnt) DESC
        """)
    List<Object[]> getRegionTypeStatistics(@Param("year") String year);

    /**
     * 도시화수준별 통계
     */
    @Query("""
        SELECT a.urbanLevel,
               COUNT(a) as areaCount,
               AVG(a.totalOccrrncCnt) as avgAccidents,
               AVG(a.accidentDensity) as avgDensity,
               AVG(a.accidentPerPopulation) as avgAccidentRate
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        GROUP BY a.urbanLevel 
        ORDER BY AVG(a.accidentDensity) DESC
        """)
    List<Object[]> getUrbanLevelStatistics(@Param("year") String year);

    // ===== 교통약자 관련 통계 =====

    /**
     * 보행자 사고 비율 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.pedestrianAccidentRatio > 0
        ORDER BY a.pedestrianAccidentRatio DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopPedestrianAccidentRatioAreas(@Param("year") String year, @Param("limit") int limit);

    /**
     * 노인 사고 비율 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.elderlyAccidentRatio > 0
        ORDER BY a.elderlyAccidentRatio DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopElderlyAccidentRatioAreas(@Param("year") String year, @Param("limit") int limit);

    /**
     * 어린이 사고 비율 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.childAccidentRatio > 0
        ORDER BY a.childAccidentRatio DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopChildAccidentRatioAreas(@Param("year") String year, @Param("limit") int limit);

    /**
     * 교통약자 사고 비율 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.vulnerableRoadUsersRatio > 0
        ORDER BY a.vulnerableRoadUsersRatio DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopVulnerableRoadUsersRatioAreas(@Param("year") String year, @Param("limit") int limit);

    /**
     * 교통약자별 평균 사고 비율
     */
    @Query("""
        SELECT AVG(a.pedestrianAccidentRatio) as avgPedestrianRatio,
               AVG(a.elderlyAccidentRatio) as avgElderlyRatio,
               AVG(a.childAccidentRatio) as avgChildRatio,
               AVG(a.vulnerableRoadUsersRatio) as avgVulnerableRatio
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        """)
    Object[] getAverageVulnerableRoadUsersRatios(@Param("year") String year);

    // ===== 추세 분석 =====

    /**
     * 개선 추세 지자체 (전년대비 감소)
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year 
        AND a.yearOverYearChange < 0
        ORDER BY a.yearOverYearChange ASC
        """)
    List<AccidentStatisticsEntity> getImprovingAreas(@Param("year") String year);

    /**
     * 악화 추세 지자체 (전년대비 증가)
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year 
        AND a.yearOverYearChange > 0
        ORDER BY a.yearOverYearChange DESC
        """)
    List<AccidentStatisticsEntity> getWorseningAreas(@Param("year") String year);

    /**
     * 안전개선점수 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.safetyImprovementScore > 0
        ORDER BY a.safetyImprovementScore DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopSafetyImprovementAreas(@Param("year") String year, @Param("limit") int limit);

    /**
     * 연도별 추세 분석
     */
    @Query("""
        SELECT a.trendDirection, COUNT(a), AVG(a.yearOverYearChange)
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        GROUP BY a.trendDirection
        """)
    List<Object[]> getTrendAnalysis(@Param("year") String year);

    // ===== 인구 기반 분석 =====

    /**
     * 인구 규모별 통계
     */
    @Query("""
        SELECT 
            CASE 
                WHEN a.populationSize >= 1000000 THEN '100만 이상'
                WHEN a.populationSize >= 500000 THEN '50만-100만'
                WHEN a.populationSize >= 100000 THEN '10만-50만'
                WHEN a.populationSize >= 50000 THEN '5만-10만'
                ELSE '5만 미만'
            END as populationGroup,
            COUNT(a) as areaCount,
            AVG(a.accidentPerPopulation) as avgAccidentRate,
            AVG(a.fatalityRate) as avgFatalityRate
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.populationSize > 0
        GROUP BY 
            CASE 
                WHEN a.populationSize >= 1000000 THEN '100만 이상'
                WHEN a.populationSize >= 500000 THEN '50만-100만'
                WHEN a.populationSize >= 100000 THEN '10만-50만'
                WHEN a.populationSize >= 50000 THEN '5만-10만'
                ELSE '5만 미만'
            END
        ORDER BY AVG(a.accidentPerPopulation) DESC
        """)
    List<Object[]> getPopulationGroupStatistics(@Param("year") String year);

    /**
     * 사고밀도 상위 지자체
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year AND a.accidentDensity > 0
        ORDER BY a.accidentDensity DESC 
        LIMIT :limit
        """)
    List<AccidentStatisticsEntity> getTopAccidentDensityAreas(@Param("year") String year, @Param("limit") int limit);

    // ===== 복합 조건 검색 =====

    /**
     * 복합 조건 검색
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE (:region IS NULL OR a.sidoSggNm LIKE %:region%) 
        AND (:year IS NULL OR a.searchYearCd = :year) 
        AND (:regionType IS NULL OR a.regionType = :regionType)
        AND (:urbanLevel IS NULL OR a.urbanLevel = :urbanLevel)
        AND (:minAccidentCount IS NULL OR a.totalOccrrncCnt >= :minAccidentCount)
        AND (:maxAccidentCount IS NULL OR a.totalOccrrncCnt <= :maxAccidentCount)
        AND (:minFatalityRate IS NULL OR a.fatalityRate >= :minFatalityRate)
        AND (:riskLevel IS NULL OR a.riskLevel = :riskLevel)
        ORDER BY a.totalOccrrncCnt DESC
        """)
    Page<AccidentStatisticsEntity> findByComplexConditions(
            @Param("region") String region,
            @Param("year") String year,
            @Param("regionType") AccidentStatisticsEntity.RegionType regionType,
            @Param("urbanLevel") AccidentStatisticsEntity.UrbanLevel urbanLevel,
            @Param("minAccidentCount") Integer minAccidentCount,
            @Param("maxAccidentCount") Integer maxAccidentCount,
            @Param("minFatalityRate") BigDecimal minFatalityRate,
            @Param("riskLevel") AccidentStatisticsEntity.RiskLevel riskLevel,
            Pageable pageable);

    // ===== 데이터 수집 관련 메서드 =====

    /**
     * 특정 연도 데이터 수집 완료 여부
     */
    @Query("SELECT COUNT(a) FROM AccidentStatisticsEntity a WHERE a.searchYearCd = :year")
    Long getCollectedStatisticsCountByYear(@Param("year") String year);

    /**
     * 최근 수집된 통계 데이터
     */
    List<AccidentStatisticsEntity> findTop10ByOrderByCreatedAtDesc();

    /**
     * 수집 상태 요약
     */
    @Query("""
        SELECT a.searchYearCd, COUNT(a) as dataCount, 
               MIN(a.createdAt) as firstCollection,
               MAX(a.createdAt) as lastCollection
        FROM AccidentStatisticsEntity a 
        GROUP BY a.searchYearCd 
        ORDER BY a.searchYearCd DESC
        """)
    List<Object[]> getCollectionStatusSummary();

    /**
     * 중복 데이터 조회
     */
    @Query("""
        SELECT a FROM AccidentStatisticsEntity a 
        WHERE a.sidoSggNm IN (
            SELECT a2.sidoSggNm FROM AccidentStatisticsEntity a2 
            WHERE a2.searchYearCd = a.searchYearCd
            GROUP BY a2.sidoSggNm, a2.searchYearCd
            HAVING COUNT(a2.sidoSggNm) > 1
        )
        ORDER BY a.sidoSggNm, a.searchYearCd, a.createdAt
        """)
    List<AccidentStatisticsEntity> findDuplicateRecords();

    /**
     * 지자체명으로 검색
     */
    List<AccidentStatisticsEntity> findBySidoSggNmContainingIgnoreCaseOrderByTotalOccrrncCntDesc(String sidoSggNm);

    // ===== 특수 분석 메서드 =====

    /**
     * 종합 안전도 평가 (여러 지표 종합)
     */
    @Query("""
        SELECT a.sidoSggNm,
               a.totalOccrrncCnt,
               a.fatalityRate,
               a.accidentPerPopulation,
               a.vulnerableRoadUsersRatio,
               a.safetyImprovementScore,
               RANK() OVER (ORDER BY a.totalOccrrncCnt ASC, a.fatalityRate ASC, a.accidentPerPopulation ASC) as safetyRank
        FROM AccidentStatisticsEntity a 
        WHERE a.searchYearCd = :year
        ORDER BY safetyRank
        """)
    List<Object[]> getComprehensiveSafetyEvaluation(@Param("year") String year);

    /**
     * 지자체별 다년도 비교 (특정 지자체)
     */
    @Query("""
        SELECT a.searchYearCd, a.totalOccrrncCnt, a.fatalityRate, a.accidentPerPopulation
        FROM AccidentStatisticsEntity a 
        WHERE a.sidoSggNm = :sidoSggNm
        ORDER BY a.searchYearCd
        """)
    List<Object[]> getMultiYearComparison(@Param("sidoSggNm") String sidoSggNm);
}