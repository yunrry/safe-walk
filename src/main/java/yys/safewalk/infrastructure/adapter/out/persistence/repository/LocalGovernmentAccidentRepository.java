package yys.safewalk.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.LocalGovernmentAccidentEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 지자체별 사고다발지역정보 Repository
 */
@Repository
public interface LocalGovernmentAccidentRepository extends JpaRepository<LocalGovernmentAccidentEntity, Long> {

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

    // ===== 지자체별 특화 조회 메서드 =====

    /**
     * 지자체별 TOP3 지역 조회
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.sidoSggNm = :sidoSggNm 
        AND l.searchYearCd = :year
        ORDER BY l.lgTop3Rank ASC
        """)
    List<LocalGovernmentAccidentEntity> findTop3ByLocalGovernment(@Param("sidoSggNm") String sidoSggNm,
                                                                  @Param("year") String year);

    /**
     * 지자체별 1순위 지역만 조회
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.lgTop3Rank = 1 
        AND l.searchYearCd = :year
        ORDER BY l.riskScore DESC
        """)
    List<LocalGovernmentAccidentEntity> findTop1AreasAllLocalGovernments(@Param("year") String year);

    /**
     * 특정 순위의 지역들 조회
     */
    List<LocalGovernmentAccidentEntity> findByLgTop3RankAndSearchYearCdOrderByRiskScoreDesc(
            Integer lgTop3Rank, String searchYearCd);

    /**
     * 지자체별 위험지역 개수 확인
     */
    @Query("""
        SELECT l.sidoSggNm, COUNT(l) 
        FROM LocalGovernmentAccidentEntity l 
        WHERE l.searchYearCd = :year
        GROUP BY l.sidoSggNm 
        ORDER BY COUNT(l) DESC
        """)
    List<Object[]> getTop3CountByLocalGovernment(@Param("year") String year);

    /**
     * 높은 상대적 위험지수를 가진 지역
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.relativeRiskIndex >= :threshold 
        ORDER BY l.relativeRiskIndex DESC
        """)
    List<LocalGovernmentAccidentEntity> findHighRelativeRiskAreas(@Param("threshold") BigDecimal threshold);

    /**
     * 지자체 커버리지가 높은 지역 (다발지역이 넓게 분포)
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.lgAreaCoverage >= :coverageThreshold 
        ORDER BY l.lgAreaCoverage DESC
        """)
    List<LocalGovernmentAccidentEntity> findHighCoverageAreas(@Param("coverageThreshold") BigDecimal coverageThreshold);

    // ===== 지역별 비교 분석 =====

    /**
     * 시도별 평균 위험점수
     */
    @Query("""
        SELECT SUBSTRING(l.sidoSggNm, 1, LOCATE(' ', l.sidoSggNm) - 1) as sido, 
               AVG(l.riskScore) as avgRiskScore
        FROM LocalGovernmentAccidentEntity l 
        WHERE l.searchYearCd = :year
        GROUP BY SUBSTRING(l.sidoSggNm, 1, LOCATE(' ', l.sidoSggNm) - 1)
        ORDER BY AVG(l.riskScore) DESC
        """)
    List<Object[]> getAverageRiskScoreBySido(@Param("year") String year);

    /**
     * 지자체별 총 사고건수 및 평균 위험도
     */
    @Query("""
        SELECT l.sidoSggNm, SUM(l.occrrnc_cnt) as totalAccidents, AVG(l.riskScore) as avgRisk
        FROM LocalGovernmentAccidentEntity l 
        WHERE l.searchYearCd = :year
        GROUP BY l.sidoSggNm 
        ORDER BY SUM(l.occrrnc_cnt) DESC
        """)
    List<Object[]> getAccidentStatisticsByLocalGovernment(@Param("year") String year);

    /**
     * 지자체내 순위별 통계
     */
    @Query("""
        SELECT l.lgTop3Rank, COUNT(l), AVG(l.riskScore), SUM(l.occrrnc_cnt)
        FROM LocalGovernmentAccidentEntity l 
        WHERE l.searchYearCd = :year
        GROUP BY l.lgTop3Rank 
        ORDER BY l.lgTop3Rank
        """)
    List<Object[]> getRankStatistics(@Param("year") String year);

    // ===== 일반 조회 메서드 =====

    /**
     * 연도별 조회
     */
    List<LocalGovernmentAccidentEntity> findBySearchYearCdOrderByRiskScoreDesc(String searchYearCd);

    /**
     * 지역별 조회
     */
    List<LocalGovernmentAccidentEntity> findBySidoSggNmContainingOrderByLgTop3RankAsc(String sidoSggNm);

    /**
     * 위험등급별 조회
     */
    List<LocalGovernmentAccidentEntity> findByRiskLevelOrderByRiskScoreDesc(
            LocalGovernmentAccidentEntity.RiskLevel riskLevel);

    /**
     * 고위험 지역 조회
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.riskLevel IN ('VERY_HIGH', 'HIGH') 
        ORDER BY l.riskScore DESC
        """)
    List<LocalGovernmentAccidentEntity> findHighRiskAreas();

    /**
     * 좌표 범위 내 조회
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.loCrd BETWEEN :minLon AND :maxLon 
        AND l.laCrd BETWEEN :minLat AND :maxLat
        ORDER BY l.riskScore DESC
        """)
    List<LocalGovernmentAccidentEntity> findByCoordinateRange(
            @Param("minLon") BigDecimal minLon, @Param("maxLon") BigDecimal maxLon,
            @Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat);

    /**
     * 특정 반경 내 조회
     */
    @Query(value = """
        SELECT * FROM local_government_accident l 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(l.la_crd)) * 
               cos(radians(l.lo_crd) - radians(:lon)) + 
               sin(radians(:lat)) * sin(radians(l.la_crd)))) <= :radiusKm
        ORDER BY l.risk_score DESC
        """, nativeQuery = true)
    List<LocalGovernmentAccidentEntity> findByLocationWithinRadius(
            @Param("lat") double lat, @Param("lon") double lon, @Param("radiusKm") double radiusKm);

    // ===== 통계 쿼리 =====

    /**
     * 연도별 총 사고건수
     */
    @Query("SELECT SUM(l.occrrnc_cnt) FROM LocalGovernmentAccidentEntity l WHERE l.searchYearCd = :year")
    Long getTotalAccidentCountByYear(@Param("year") String year);

    /**
     * 지역별 총 사고건수
     */
    @Query("SELECT SUM(l.occrrnc_cnt) FROM LocalGovernmentAccidentEntity l WHERE l.sidoSggNm LIKE %:region%")
    Long getTotalAccidentCountByRegion(@Param("region") String region);

    /**
     * 평균 사고밀도
     */
    @Query("SELECT AVG(l.accidentDensity) FROM LocalGovernmentAccidentEntity l WHERE l.accidentDensity > 0")
    Double getAverageAccidentDensity();

    /**
     * 평균 치사율
     */
    @Query("SELECT AVG(l.fatalityRate) FROM LocalGovernmentAccidentEntity l WHERE l.fatalityRate > 0")
    Double getAverageFatalityRate();

    /**
     * 상위 N개 위험지역
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        ORDER BY l.riskScore DESC 
        LIMIT :limit
        """)
    List<LocalGovernmentAccidentEntity> getTopRiskAreas(@Param("limit") int limit);

    // ===== 데이터 수집 관련 메서드 =====

    /**
     * 특정 지역의 수집 완료 여부 확인
     */
    @Query("""
        SELECT COUNT(l) > 0 FROM LocalGovernmentAccidentEntity l 
        WHERE l.sidoCd = :sidoCd AND l.gugunCd = :gugunCd AND l.searchYearCd = :year
        """)
    boolean isDataCollectedForRegion(@Param("sidoCd") String sidoCd,
                                     @Param("gugunCd") String gugunCd,
                                     @Param("year") String year);

    /**
     * 연도별 수집된 데이터 개수
     */
    @Query("SELECT COUNT(l) FROM LocalGovernmentAccidentEntity l WHERE l.searchYearCd = :year")
    Long getCollectedDataCountByYear(@Param("year") String year);

    /**
     * 최근 수집된 데이터 조회
     */
    List<LocalGovernmentAccidentEntity> findTop10ByOrderByCreatedAtDesc();

    /**
     * 수집 상태 확인용 - 지자체별 TOP3 완성도 체크
     */
    @Query("""
        SELECT l.sidoSggNm, l.searchYearCd, COUNT(l) 
        FROM LocalGovernmentAccidentEntity l 
        GROUP BY l.sidoSggNm, l.searchYearCd 
        HAVING COUNT(l) < 3
        ORDER BY l.searchYearCd DESC, l.sidoSggNm
        """)
    List<Object[]> getIncompleteTop3Areas();

    // ===== 복합 조건 검색 =====

    /**
     * 복합 조건 검색
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE (:region IS NULL OR l.sidoSggNm LIKE %:region%) 
        AND (:year IS NULL OR l.searchYearCd = :year) 
        AND (:rank IS NULL OR l.lgTop3Rank = :rank)
        AND (:minRiskScore IS NULL OR l.riskScore >= :minRiskScore) 
        AND (:riskLevel IS NULL OR l.riskLevel = :riskLevel)
        ORDER BY l.riskScore DESC
        """)
    Page<LocalGovernmentAccidentEntity> findByComplexConditions(
            @Param("region") String region,
            @Param("year") String year,
            @Param("rank") Integer rank,
            @Param("minRiskScore") BigDecimal minRiskScore,
            @Param("riskLevel") LocalGovernmentAccidentEntity.RiskLevel riskLevel,
            Pageable pageable);

    /**
     * 지점명으로 검색
     */
    List<LocalGovernmentAccidentEntity> findBySpotNmContainingIgnoreCaseOrderByLgTop3RankAsc(String spotNm);

    /**
     * 중복 데이터 조회
     */
    @Query("""
        SELECT l FROM LocalGovernmentAccidentEntity l 
        WHERE l.afosId IN (
            SELECT l2.afosId FROM LocalGovernmentAccidentEntity l2 
            GROUP BY l2.afosId 
            HAVING COUNT(l2.afosId) > 1
        )
        ORDER BY l.afosId, l.createdAt
        """)
    List<LocalGovernmentAccidentEntity> findDuplicateRecords();

    // ===== 분석용 메서드 =====

    /**
     * 지자체별 위험도 순위 분석
     */
    @Query("""
        SELECT l.sidoSggNm, 
               AVG(CASE WHEN l.lgTop3Rank = 1 THEN l.riskScore END) as rank1AvgRisk,
               AVG(CASE WHEN l.lgTop3Rank = 2 THEN l.riskScore END) as rank2AvgRisk,
               AVG(CASE WHEN l.lgTop3Rank = 3 THEN l.riskScore END) as rank3AvgRisk
        FROM LocalGovernmentAccidentEntity l 
        WHERE l.searchYearCd = :year
        GROUP BY l.sidoSggNm 
        ORDER BY rank1AvgRisk DESC
        """)
    List<Object[]> getRiskAnalysisByRank(@Param("year") String year);

    /**
     * 지자체 안전도 평가 (TOP3 평균 기준)
     */
    @Query("""
        SELECT l.sidoSggNm, 
               AVG(l.riskScore) as avgRisk,
               AVG(l.fatalityRate) as avgFatality,
               AVG(l.accidentDensity) as avgDensity,
               COUNT(l) as areaCount
        FROM LocalGovernmentAccidentEntity l 
        WHERE l.searchYearCd = :year
        GROUP BY l.sidoSggNm 
        ORDER BY AVG(l.riskScore) ASC
        """)
    List<Object[]> getLocalGovernmentSafetyEvaluation(@Param("year") String year);
}