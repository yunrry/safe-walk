package yys.safewalk.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.RiskAreaEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 링크기반 사고위험지역정보 Repository
 */
@Repository
public interface RiskAreaRepository extends JpaRepository<RiskAreaEntity, Long> {

    // ===== 중복 체크 메서드 =====

    /**
     * 링크ID와 연도로 중복 체크
     */
    boolean existsByLinkIdAndSearchYearCd(String linkId, String searchYearCd);

    /**
     * 복합 키로 중복 체크
     */
    boolean existsByLinkIdAndSearchYearCdAndSidoCdAndGugunCd(
            String linkId, String searchYearCd, String sidoCd, String gugunCd);

    /**
     * 동일한 링크 데이터 존재 여부 체크
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM RiskAreaEntity r 
        WHERE r.linkId = :linkId 
        AND r.searchYearCd = :searchYearCd 
        AND r.sidoSggNm = :sidoSggNm
        """)
    boolean existsDuplicateLinkData(@Param("linkId") String linkId,
                                    @Param("searchYearCd") String searchYearCd,
                                    @Param("sidoSggNm") String sidoSggNm);

    // ===== 링크 기반 조회 메서드 =====

    /**
     * 링크ID로 조회
     */
    List<RiskAreaEntity> findByLinkIdOrderBySearchYearCdDesc(String linkId);

    /**
     * 도로명으로 조회
     */
    List<RiskAreaEntity> findByRoadNmContainingOrderByRiskScoreDesc(String roadNm);

    /**
     * 도로유형별 조회
     */
    List<RiskAreaEntity> findByRoadTypeOrderByRiskScoreDesc(RiskAreaEntity.RoadType roadType);

    /**
     * 도로등급별 조회
     */
    List<RiskAreaEntity> findByRoadGradeOrderByRiskScoreDesc(RiskAreaEntity.RoadGrade roadGrade);

    /**
     * 교통량수준별 조회
     */
    List<RiskAreaEntity> findByTrafficVolumeLevelOrderByRiskScoreDesc(
            RiskAreaEntity.TrafficVolumeLevel trafficVolumeLevel);

    /**
     * 고속도로 링크 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.roadType = 'HIGHWAY'
        ORDER BY r.riskScore DESC
        """)
    List<RiskAreaEntity> findHighwayLinks();

    /**
     * 도시부 도로 링크 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.roadType IN ('URBAN', 'LOCAL_STREET')
        ORDER BY r.riskScore DESC
        """)
    List<RiskAreaEntity> findUrbanRoadLinks();

    /**
     * 고교통량 링크 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.trafficVolumeLevel IN ('VERY_HIGH', 'HIGH')
        ORDER BY r.trafficFlowRisk DESC
        """)
    List<RiskAreaEntity> findHighTrafficVolumeLinks();

    /**
     * 고속도로 링크 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.speedLimit >= :speedThreshold
        ORDER BY r.speedLimit DESC, r.riskScore DESC
        """)
    List<RiskAreaEntity> findHighSpeedLinks(@Param("speedThreshold") Integer speedThreshold);

    // ===== 위험도 분석 =====

    /**
     * 고위험 링크 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.riskLevel IN ('VERY_HIGH', 'HIGH') 
        ORDER BY r.riskScore DESC
        """)
    List<RiskAreaEntity> findHighRiskLinks();

    /**
     * km당 사고건수 상위 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.accidentPerKm >= :threshold 
        ORDER BY r.accidentPerKm DESC
        """)
    List<RiskAreaEntity> findHighAccidentDensityLinks(@Param("threshold") BigDecimal threshold);

    /**
     * 높은 치사율 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.fatalityRate >= :fatalityThreshold 
        ORDER BY r.fatalityRate DESC
        """)
    List<RiskAreaEntity> findHighFatalityLinks(@Param("fatalityThreshold") BigDecimal fatalityThreshold);

    /**
     * 낮은 도로안전지수 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.roadSafetyIndex <= :safetyThreshold 
        ORDER BY r.roadSafetyIndex ASC
        """)
    List<RiskAreaEntity> findLowSafetyIndexLinks(@Param("safetyThreshold") BigDecimal safetyThreshold);

    /**
     * 높은 교통흐름위험 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.trafficFlowRisk >= :trafficRiskThreshold 
        ORDER BY r.trafficFlowRisk DESC
        """)
    List<RiskAreaEntity> findHighTrafficFlowRiskLinks(@Param("trafficRiskThreshold") BigDecimal trafficRiskThreshold);

    /**
     * 높은 인프라위험 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.infrastructureRisk >= :infraRiskThreshold 
        ORDER BY r.infrastructureRisk DESC
        """)
    List<RiskAreaEntity> findHighInfrastructureRiskLinks(@Param("infraRiskThreshold") BigDecimal infraRiskThreshold);

    // ===== 인프라 기반 조회 =====

    /**
     * 신호등이 많은 링크 (복잡한 교차로)
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.trafficSignalCount >= :signalThreshold 
        ORDER BY r.trafficSignalCount DESC, r.riskScore DESC
        """)
    List<RiskAreaEntity> findComplexIntersectionLinks(@Param("signalThreshold") Integer signalThreshold);

    /**
     * 횡단보도가 많은 링크 (보행자 위험 구간)
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.crosswalkCount >= :crosswalkThreshold 
        ORDER BY r.crosswalkCount DESC, r.riskScore DESC
        """)
    List<RiskAreaEntity> findPedestrianRiskLinks(@Param("crosswalkThreshold") Integer crosswalkThreshold);

    /**
     * 버스정류장이 많은 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.busStopCount >= :busStopThreshold 
        ORDER BY r.busStopCount DESC, r.riskScore DESC
        """)
    List<RiskAreaEntity> findBusStopDenseLinks(@Param("busStopThreshold") Integer busStopThreshold);

    /**
     * 보행자 고위험 링크 (횡단보도 + 버스정류장 + 고위험)
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.crosswalkCount > 0 
        AND r.busStopCount > 0 
        AND r.riskLevel IN ('VERY_HIGH', 'HIGH')
        ORDER BY r.riskScore DESC
        """)
    List<RiskAreaEntity> findPedestrianHighRiskLinks();

    // ===== 지역별 조회 =====

    /**
     * 연도별 조회
     */
    List<RiskAreaEntity> findBySearchYearCdOrderByRiskScoreDesc(String searchYearCd);

    /**
     * 지역별 조회
     */
    List<RiskAreaEntity> findBySidoSggNmContainingOrderByRiskScoreDesc(String sidoSggNm);

    /**
     * 위험등급별 조회
     */
    List<RiskAreaEntity> findByRiskLevelOrderByRiskScoreDesc(RiskAreaEntity.RiskLevel riskLevel);

    /**
     * 좌표 범위 내 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.loCrd BETWEEN :minLon AND :maxLon 
        AND r.laCrd BETWEEN :minLat AND :maxLat
        ORDER BY r.riskScore DESC
        """)
    List<RiskAreaEntity> findByCoordinateRange(
            @Param("minLon") BigDecimal minLon, @Param("maxLon") BigDecimal maxLon,
            @Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat);

    /**
     * 특정 반경 내 링크 조회
     */
    @Query(value = """
        SELECT * FROM risk_area r 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(r.la_crd)) * 
               cos(radians(r.lo_crd) - radians(:lon)) + 
               sin(radians(:lat)) * sin(radians(r.la_crd)))) <= :radiusKm
        ORDER BY r.risk_score DESC
        """, nativeQuery = true)
    List<RiskAreaEntity> findByLocationWithinRadius(
            @Param("lat") double lat, @Param("lon") double lon, @Param("radiusKm") double radiusKm);

    // ===== 통계 쿼리 =====

    /**
     * 도로유형별 통계
     */
    @Query("""
        SELECT r.roadType, 
               COUNT(r) as linkCount,
               AVG(r.riskScore) as avgRiskScore,
               SUM(r.occrrnc_cnt) as totalAccidents,
               AVG(r.accidentPerKm) as avgAccidentPerKm
        FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year
        GROUP BY r.roadType 
        ORDER BY AVG(r.riskScore) DESC
        """)
    List<Object[]> getRoadTypeStatistics(@Param("year") String year);

    /**
     * 교통량수준별 통계
     */
    @Query("""
        SELECT r.trafficVolumeLevel,
               COUNT(r) as linkCount,
               AVG(r.riskScore) as avgRiskScore,
               AVG(r.trafficFlowRisk) as avgTrafficRisk
        FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year
        GROUP BY r.trafficVolumeLevel 
        ORDER BY AVG(r.riskScore) DESC
        """)
    List<Object[]> getTrafficVolumeStatistics(@Param("year") String year);

    /**
     * 속도별 통계
     */
    @Query("""
        SELECT 
            CASE 
                WHEN r.speedLimit >= 100 THEN '100km/h 이상'
                WHEN r.speedLimit >= 80 THEN '80-99km/h'
                WHEN r.speedLimit >= 60 THEN '60-79km/h'
                WHEN r.speedLimit >= 50 THEN '50-59km/h'
                ELSE '50km/h 미만'
            END as speedGroup,
            COUNT(r) as linkCount,
            AVG(r.riskScore) as avgRiskScore,
            AVG(r.fatalityRate) as avgFatalityRate
        FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year AND r.speedLimit > 0
        GROUP BY 
            CASE 
                WHEN r.speedLimit >= 100 THEN '100km/h 이상'
                WHEN r.speedLimit >= 80 THEN '80-99km/h'
                WHEN r.speedLimit >= 60 THEN '60-79km/h'
                WHEN r.speedLimit >= 50 THEN '50-59km/h'
                ELSE '50km/h 미만'
            END
        ORDER BY AVG(r.riskScore) DESC
        """)
    List<Object[]> getSpeedGroupStatistics(@Param("year") String year);

    /**
     * 연도별 총 사고건수
     */
    @Query("SELECT SUM(r.occrrnc_cnt) FROM RiskAreaEntity r WHERE r.searchYearCd = :year")
    Long getTotalAccidentCountByYear(@Param("year") String year);

    /**
     * 평균 km당 사고건수
     */
    @Query("SELECT AVG(r.accidentPerKm) FROM RiskAreaEntity r WHERE r.accidentPerKm > 0 AND r.searchYearCd = :year")
    Double getAverageAccidentPerKm(@Param("year") String year);

    /**
     * 평균 도로안전지수
     */
    @Query("SELECT AVG(r.roadSafetyIndex) FROM RiskAreaEntity r WHERE r.roadSafetyIndex > 0 AND r.searchYearCd = :year")
    Double getAverageRoadSafetyIndex(@Param("year") String year);

    /**
     * 상위 N개 위험 링크
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year
        ORDER BY r.riskScore DESC 
        LIMIT :limit
        """)
    List<RiskAreaEntity> getTopRiskLinks(@Param("year") String year, @Param("limit") int limit);

    // ===== 데이터 수집 관련 메서드 =====

    /**
     * 특정 지역의 링크 데이터 수집 완료 여부 확인
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM RiskAreaEntity r 
        WHERE r.sidoCd = :sidoCd AND r.gugunCd = :gugunCd AND r.searchYearCd = :year
        """)
    boolean isLinkDataCollectedForRegion(@Param("sidoCd") String sidoCd,
                                         @Param("gugunCd") String gugunCd,
                                         @Param("year") String year);

    /**
     * 연도별 수집된 링크 데이터 개수
     */
    @Query("SELECT COUNT(r) FROM RiskAreaEntity r WHERE r.searchYearCd = :year")
    Long getLinkCollectedDataCountByYear(@Param("year") String year);

    /**
     * 최근 수집된 링크 데이터
     */
    List<RiskAreaEntity> findTop10ByOrderByCreatedAtDesc();

    // ===== 복합 조건 검색 =====

    /**
     * 복합 조건 검색
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE (:region IS NULL OR r.sidoSggNm LIKE %:region%) 
        AND (:year IS NULL OR r.searchYearCd = :year) 
        AND (:roadName IS NULL OR r.roadNm LIKE %:roadName%)
        AND (:roadType IS NULL OR r.roadType = :roadType)
        AND (:trafficLevel IS NULL OR r.trafficVolumeLevel = :trafficLevel)
        AND (:minRiskScore IS NULL OR r.riskScore >= :minRiskScore) 
        AND (:riskLevel IS NULL OR r.riskLevel = :riskLevel)
        AND (:minSpeedLimit IS NULL OR r.speedLimit >= :minSpeedLimit)
        ORDER BY r.riskScore DESC
        """)
    Page<RiskAreaEntity> findByComplexConditions(
            @Param("region") String region,
            @Param("year") String year,
            @Param("roadName") String roadName,
            @Param("roadType") RiskAreaEntity.RoadType roadType,
            @Param("trafficLevel") RiskAreaEntity.TrafficVolumeLevel trafficLevel,
            @Param("minRiskScore") BigDecimal minRiskScore,
            @Param("riskLevel") RiskAreaEntity.RiskLevel riskLevel,
            @Param("minSpeedLimit") Integer minSpeedLimit,
            Pageable pageable);

    /**
     * 도로명 + 노드명으로 검색
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.roadNm LIKE %:roadName%
        OR r.startNodeNm LIKE %:nodeName%
        OR r.endNodeNm LIKE %:nodeName%
        ORDER BY r.riskScore DESC
        """)
    List<RiskAreaEntity> findByRoadOrNodeName(@Param("roadName") String roadName, @Param("nodeName") String nodeName);

    /**
     * 중복 데이터 조회
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.linkId IN (
            SELECT r2.linkId FROM RiskAreaEntity r2 
            GROUP BY r2.linkId, r2.searchYearCd
            HAVING COUNT(r2.linkId) > 1
        )
        ORDER BY r.linkId, r.searchYearCd, r.createdAt
        """)
    List<RiskAreaEntity> findDuplicateRecords();

    // ===== 특수 분석 메서드 =====

    /**
     * 링크 길이별 사고 분석
     */
    @Query("""
        SELECT 
            CASE 
                WHEN r.linkLength >= 1000 THEN '1km 이상'
                WHEN r.linkLength >= 500 THEN '500m-1km'
                WHEN r.linkLength >= 200 THEN '200m-500m'
                ELSE '200m 미만'
            END as lengthGroup,
            COUNT(r) as linkCount,
            AVG(r.accidentPerKm) as avgAccidentPerKm,
            AVG(r.riskScore) as avgRiskScore
        FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year AND r.linkLength > 0
        GROUP BY 
            CASE 
                WHEN r.linkLength >= 1000 THEN '1km 이상'
                WHEN r.linkLength >= 500 THEN '500m-1km'
                WHEN r.linkLength >= 200 THEN '200m-500m'
                ELSE '200m 미만'
            END
        ORDER BY AVG(r.accidentPerKm) DESC
        """)
    List<Object[]> getLinkLengthAnalysis(@Param("year") String year);

    /**
     * 도로 안전도 개선이 필요한 링크 (낮은 안전지수 + 높은 위험도)
     */
    @Query("""
        SELECT r FROM RiskAreaEntity r 
        WHERE r.roadSafetyIndex <= :safetyThreshold
        AND r.riskScore >= :riskThreshold
        ORDER BY r.riskScore DESC, r.roadSafetyIndex ASC
        """)
    List<RiskAreaEntity> findLinksNeedingImprovement(
            @Param("safetyThreshold") BigDecimal safetyThreshold,
            @Param("riskThreshold") BigDecimal riskThreshold);

    /**
     * 차선수별 위험도 분석
     */
    @Query("""
        SELECT r.laneCount, 
               COUNT(r) as linkCount,
               AVG(r.riskScore) as avgRiskScore,
               AVG(r.fatalityRate) as avgFatalityRate
        FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year AND r.laneCount > 0
        GROUP BY r.laneCount 
        ORDER BY r.laneCount
        """)
    List<Object[]> getLaneCountRiskAnalysis(@Param("year") String year);

    /**
     * 교통흐름위험과 인프라위험 상관관계 분석
     */
    @Query("""
        SELECT r.trafficFlowRisk, r.infrastructureRisk, COUNT(r), AVG(r.riskScore)
        FROM RiskAreaEntity r 
        WHERE r.searchYearCd = :year 
        AND r.trafficFlowRisk IS NOT NULL 
        AND r.infrastructureRisk IS NOT NULL
        GROUP BY r.trafficFlowRisk, r.infrastructureRisk
        ORDER BY AVG(r.riskScore) DESC
        """)
    List<Object[]> getTrafficInfraRiskCorrelation(@Param("year") String year);
}