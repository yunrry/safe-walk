package yys.safewalk.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.ElderlyPedestrianAccidentEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 보행노인 사고다발지역정보 Repository
 */
@Repository
public interface ElderlyPedestrianAccidentRepository extends JpaRepository<ElderlyPedestrianAccidentEntity, Long> {

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

    /**
     * 동일한 데이터 존재 여부 체크
     */
    @Query("""
        SELECT COUNT(e) > 0 FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.afosFid = :afosFid 
        AND e.afosId = :afosId 
        AND e.searchYearCd = :searchYearCd
        """)
    boolean existsDuplicateData(@Param("afosFid") String afosFid,
                                @Param("afosId") String afosId,
                                @Param("searchYearCd") String searchYearCd);

    // ===== 노인 특화 조회 메서드 =====

    /**
     * 노인 고위험 지역 조회 (더 엄격한 기준)
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.riskLevel IN ('VERY_HIGH', 'HIGH') 
        OR e.elderlyAccidentDensity >= :densityThreshold
        ORDER BY e.riskScore DESC
        """)
    List<ElderlyPedestrianAccidentEntity> findElderlyHighRiskAreas(@Param("densityThreshold") BigDecimal densityThreshold);

    /**
     * 노인 사망사고 발생 지역
     */
    List<ElderlyPedestrianAccidentEntity> findByDthDnvCntGreaterThanOrderByDthDnvCntDesc(Integer dthDnvCnt);

    /**
     * 높은 치사율 지역 (노인 기준)
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.fatalityRate >= :fatalityThreshold 
        ORDER BY e.fatalityRate DESC
        """)
    List<ElderlyPedestrianAccidentEntity> findHighFatalityAreasForElderly(@Param("fatalityThreshold") BigDecimal fatalityThreshold);

    /**
     * 중상률이 높은 지역 (노인 기준)
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.seriousInjuryRate >= :seriousInjuryThreshold 
        ORDER BY e.seriousInjuryRate DESC
        """)
    List<ElderlyPedestrianAccidentEntity> findHighSeriousInjuryAreasForElderly(@Param("seriousInjuryThreshold") BigDecimal seriousInjuryThreshold);

    /**
     * 연도별 조회
     */
    List<ElderlyPedestrianAccidentEntity> findBySearchYearCdOrderByRiskScoreDesc(String searchYearCd);

    /**
     * 지역별 조회
     */
    List<ElderlyPedestrianAccidentEntity> findBySidoSggNmContainingOrderByRiskScoreDesc(String sidoSggNm);

    /**
     * 좌표 범위 내 노인 위험지역 조회
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.loCrd BETWEEN :minLon AND :maxLon 
        AND e.laCrd BETWEEN :minLat AND :maxLat
        ORDER BY e.riskScore DESC
        """)
    List<ElderlyPedestrianAccidentEntity> findElderlyRiskAreasByCoordinateRange(
            @Param("minLon") BigDecimal minLon, @Param("maxLon") BigDecimal maxLon,
            @Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat);

    /**
     * 특정 반경 내 노인 위험지역 조회
     */
    @Query(value = """
        SELECT * FROM elderly_pedestrian_accident e 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(e.la_crd)) * 
               cos(radians(e.lo_crd) - radians(:lon)) + 
               sin(radians(:lat)) * sin(radians(e.la_crd)))) <= :radiusKm
        ORDER BY e.risk_score DESC
        """, nativeQuery = true)
    List<ElderlyPedestrianAccidentEntity> findElderlyRiskAreasWithinRadius(
            @Param("lat") double lat, @Param("lon") double lon, @Param("radiusKm") double radiusKm);

    // ===== 노인 특화 통계 쿼리 =====

    /**
     * 노인 사고 총 건수 (연도별)
     */
    @Query("SELECT SUM(e.occrrnc_cnt) FROM ElderlyPedestrianAccidentEntity e WHERE e.searchYearCd = :year")
    Long getTotalElderlyAccidentCountByYear(@Param("year") String year);

    /**
     * 노인 평균 치사율
     */
    @Query("SELECT AVG(e.fatalityRate) FROM ElderlyPedestrianAccidentEntity e WHERE e.fatalityRate > 0")
    Double getAverageElderlyFatalityRate();

    /**
     * 노인 평균 중상률
     */
    @Query("SELECT AVG(e.seriousInjuryRate) FROM ElderlyPedestrianAccidentEntity e WHERE e.seriousInjuryRate > 0")
    Double getAverageElderlySeriousInjuryRate();

    /**
     * 지역별 노인 사고 밀도 평균
     */
    @Query("""
        SELECT e.sidoSggNm, AVG(e.elderlyAccidentDensity) 
        FROM ElderlyPedestrianAccidentEntity e 
        GROUP BY e.sidoSggNm 
        ORDER BY AVG(e.elderlyAccidentDensity) DESC
        """)
    List<Object[]> getAverageElderlyAccidentDensityByRegion();

    /**
     * 위험등급별 노인 사고지역 통계
     */
    @Query("SELECT e.riskLevel, COUNT(e) FROM ElderlyPedestrianAccidentEntity e GROUP BY e.riskLevel")
    List<Object[]> getElderlyRiskLevelStatistics();

    /**
     * 상위 N개 노인 위험지역
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        ORDER BY e.riskScore DESC 
        LIMIT :limit
        """)
    List<ElderlyPedestrianAccidentEntity> getTopElderlyRiskAreas(@Param("limit") int limit);

    // ===== 데이터 수집 관련 메서드 =====

    /**
     * 특정 지역의 노인 데이터 수집 완료 여부 확인
     */
    @Query("""
        SELECT COUNT(e) > 0 FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.sidoCd = :sidoCd AND e.gugunCd = :gugunCd AND e.searchYearCd = :year
        """)
    boolean isElderlyDataCollectedForRegion(@Param("sidoCd") String sidoCd,
                                            @Param("gugunCd") String gugunCd,
                                            @Param("year") String year);

    /**
     * 노인 데이터 수집 현황
     */
    @Query("SELECT COUNT(e) FROM ElderlyPedestrianAccidentEntity e WHERE e.searchYearCd = :year")
    Long getElderlyCollectedDataCountByYear(@Param("year") String year);

    /**
     * 최근 수집된 노인 데이터
     */
    List<ElderlyPedestrianAccidentEntity> findTop10ByOrderByCreatedAtDesc();

    // ===== 노인 안전 분석 메서드 =====

    /**
     * 노인에게 매우 위험한 지역 (복합 조건)
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE (e.fatalityRate >= :fatalityThreshold OR e.seriousInjuryRate >= :seriousThreshold)
        AND e.riskLevel IN ('VERY_HIGH', 'HIGH')
        ORDER BY e.riskScore DESC
        """)
    List<ElderlyPedestrianAccidentEntity> findCriticalElderlyRiskAreas(
            @Param("fatalityThreshold") BigDecimal fatalityThreshold,
            @Param("seriousThreshold") BigDecimal seriousThreshold);

    /**
     * 노인 사고 핫스팟 분석 (밀도 기준)
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.elderlyAccidentDensity >= (
            SELECT AVG(e2.elderlyAccidentDensity) * :multiplier 
            FROM ElderlyPedestrianAccidentEntity e2
        )
        ORDER BY e.elderlyAccidentDensity DESC
        """)
    List<ElderlyPedestrianAccidentEntity> findElderlyAccidentHotspots(@Param("multiplier") double multiplier);

    /**
     * 복합 조건 검색 (노인 특화)
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE (:region IS NULL OR e.sidoSggNm LIKE %:region%) 
        AND (:year IS NULL OR e.searchYearCd = :year) 
        AND (:minRiskScore IS NULL OR e.riskScore >= :minRiskScore) 
        AND (:riskLevel IS NULL OR e.riskLevel = :riskLevel)
        AND (:minFatalityRate IS NULL OR e.fatalityRate >= :minFatalityRate)
        ORDER BY e.riskScore DESC
        """)
    Page<ElderlyPedestrianAccidentEntity> findElderlyRiskAreasByComplexConditions(
            @Param("region") String region,
            @Param("year") String year,
            @Param("minRiskScore") BigDecimal minRiskScore,
            @Param("riskLevel") ElderlyPedestrianAccidentEntity.RiskLevel riskLevel,
            @Param("minFatalityRate") BigDecimal minFatalityRate,
            Pageable pageable);

    /**
     * 노인 데이터 중복 체크
     */
    @Query("""
        SELECT e FROM ElderlyPedestrianAccidentEntity e 
        WHERE e.afosId IN (
            SELECT e2.afosId FROM ElderlyPedestrianAccidentEntity e2 
            GROUP BY e2.afosId 
            HAVING COUNT(e2.afosId) > 1
        )
        ORDER BY e.afosId, e.createdAt
        """)
    List<ElderlyPedestrianAccidentEntity> findElderlyDuplicateRecords();

    /**
     * 지점명으로 노인 위험지역 검색
     */
    List<ElderlyPedestrianAccidentEntity> findBySpotNmContainingIgnoreCaseOrderByRiskScoreDesc(String spotNm);
}