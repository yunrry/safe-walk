package yys.safewalk.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.PedestrianAccidentEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 보행자 사고다발지역정보 Repository
 */
@Repository
public interface PedestrianAccidentRepository extends JpaRepository<PedestrianAccidentEntity, Long> {

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
     * 복합 키로 중복 체크 (가장 정확)
     */
    boolean existsByAfosIdAndSpotCdAndSearchYearCdAndSidoCdAndGugunCd(
            String afosId, String spotCd, String searchYearCd, String sidoCd, String gugunCd);

    /**
     * 동일한 데이터 존재 여부 체크
     */
    @Query("""
        SELECT COUNT(p) > 0 FROM PedestrianAccidentEntity p 
        WHERE p.afosFid = :afosFid 
        AND p.afosId = :afosId 
        AND p.searchYearCd = :searchYearCd
        """)
    boolean existsDuplicateData(@Param("afosFid") String afosFid,
                                @Param("afosId") String afosId,
                                @Param("searchYearCd") String searchYearCd);

    // ===== 조회 메서드 =====

    /**
     * 연도별 조회
     */
    List<PedestrianAccidentEntity> findBySearchYearCdOrderByOccrrncCntDesc(String searchYearCd);

    /**
     * 지역별 조회
     */
    List<PedestrianAccidentEntity> findBySidoSggNmContainingOrderByOccrrncCntDesc(String sidoSggNm);

    /**
     * 지역 + 연도별 조회
     */
    List<PedestrianAccidentEntity> findBySidoSggNmContainingAndSearchYearCdOrderByOccrrncCntDesc(
            String sidoSggNm, String searchYearCd);

    /**
     * 위험등급별 조회
     */
    List<PedestrianAccidentEntity> findByRiskLevelOrderByRiskScoreDesc(
            String riskLevel);

    /**
     * 고위험 지역 조회
     */
    @Query("""
        SELECT p FROM PedestrianAccidentEntity p 
        WHERE p.riskLevel IN ('VERY_HIGH', 'HIGH') 
        ORDER BY p.riskScore DESC
        """)
    List<PedestrianAccidentEntity> findHighRiskAreas();

    /**
     * 사망사고 발생 지역
     */
    List<PedestrianAccidentEntity> findByDthDnvCntGreaterThanOrderByDthDnvCntDesc(Integer dthDnvCnt);

    /**
     * 좌표 범위 내 조회 (지도 영역 검색)
     */
    @Query("""
        SELECT p FROM PedestrianAccidentEntity p 
        WHERE p.loCrd BETWEEN :minLon AND :maxLon 
        AND p.laCrd BETWEEN :minLat AND :maxLat
        ORDER BY p.riskScore DESC
        """)
    List<PedestrianAccidentEntity> findByCoordinateRange(
            @Param("minLon") BigDecimal minLon, @Param("maxLon") BigDecimal maxLon,
            @Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat);

    /**
     * 특정 반경 내 조회 (Haversine 공식 사용)
     */
    @Query(value = """
        SELECT * FROM pedestrian_accidents p 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(p.la_crd)) * 
               cos(radians(p.lo_crd) - radians(:lon)) + 
               sin(radians(:lat)) * sin(radians(p.la_crd)))) <= :radiusKm
        ORDER BY p.risk_score DESC
        """, nativeQuery = true)
    List<PedestrianAccidentEntity> findByLocationWithinRadius(
            @Param("lat") double lat, @Param("lon") double lon, @Param("radiusKm") double radiusKm);

    /**
     * 페이징된 고위험 지역 조회
     */
    @Query("""
        SELECT p FROM PedestrianAccidentEntity p 
        WHERE p.riskLevel IN ('VERY_HIGH', 'HIGH') 
        ORDER BY p.riskScore DESC
        """)
    Page<PedestrianAccidentEntity> findHighRiskAreasWithPaging(Pageable pageable);

    // ===== 통계 쿼리 =====

    /**
     * 연도별 총 사고건수
     */
    @Query("SELECT SUM(p.occrrnc_cnt) FROM PedestrianAccidentEntity p WHERE p.searchYearCd = :year")
    Long getTotalAccidentCountByYear(@Param("year") String year);

    /**
     * 지역별 총 사고건수
     */
    @Query("SELECT SUM(p.occrrnc_cnt) FROM PedestrianAccidentEntity p WHERE p.sidoSggNm LIKE %:region%")
    Long getTotalAccidentCountByRegion(@Param("region") String region);

    /**
     * 위험등급별 개수
     */
    @Query("SELECT p.riskLevel, COUNT(p) FROM PedestrianAccidentEntity p GROUP BY p.riskLevel")
    List<Object[]> getRiskLevelStatistics();

    /**
     * 평균 치사율 계산
     */
    @Query("SELECT AVG(p.fatalityRate) FROM PedestrianAccidentEntity p WHERE p.fatalityRate > 0")
    Double getAverageFatalityRate();

    /**
     * 지역별 평균 위험점수
     */
    @Query("""
        SELECT p.sidoSggNm, AVG(p.riskScore) 
        FROM PedestrianAccidentEntity p 
        GROUP BY p.sidoSggNm 
        ORDER BY AVG(p.riskScore) DESC
        """)
    List<Object[]> getAverageRiskScoreByRegion();

    /**
     * 상위 N개 위험지역
     */
    @Query("""
        SELECT p FROM PedestrianAccidentEntity p 
        ORDER BY p.riskScore DESC 
        LIMIT :limit
        """)
    List<PedestrianAccidentEntity> getTopRiskAreas(@Param("limit") int limit);

    // ===== 데이터 수집 관련 메서드 =====

    /**
     * 특정 지역의 수집 완료 여부 확인
     */
    @Query("""
        SELECT COUNT(p) > 0 FROM PedestrianAccidentEntity p 
        WHERE p.sidoCd = :sidoCd AND p.gugunCd = :gugunCd AND p.searchYearCd = :year
        """)
    boolean isDataCollectedForRegion(@Param("sidoCd") String sidoCd,
                                     @Param("gugunCd") String gugunCd,
                                     @Param("year") String year);

    /**
     * 연도별 수집된 데이터 개수
     */
    @Query("SELECT COUNT(p) FROM PedestrianAccidentEntity p WHERE p.searchYearCd = :year")
    Long getCollectedDataCountByYear(@Param("year") String year);

    /**
     * 전체 수집된 데이터 개수
     */
    @Query("SELECT COUNT(p) FROM PedestrianAccidentEntity p")
    Long getTotalCollectedDataCount();

    /**
     * 최근 수집된 데이터 조회
     */
    List<PedestrianAccidentEntity> findTop10ByOrderByCreatedAtDesc();

    /**
     * 수집 상태 확인용 - 연도별 지역별 집계
     */
    @Query("""
        SELECT p.searchYearCd, p.sidoCd, p.gugunCd, COUNT(p) 
        FROM PedestrianAccidentEntity p 
        GROUP BY p.searchYearCd, p.sidoCd, p.gugunCd 
        ORDER BY p.searchYearCd DESC, p.sidoCd, p.gugunCd
        """)
    List<Object[]> getCollectionStatusSummary();

    // ===== 사용자 검색 메서드 =====

    /**
     * 지점명으로 검색
     */
    List<PedestrianAccidentEntity> findBySpotNmContainingIgnoreCaseOrderByRiskScoreDesc(String spotNm);

    /**
     * 복합 조건 검색
     */
    @Query("""
        SELECT p FROM PedestrianAccidentEntity p 
        WHERE (:region IS NULL OR p.sidoSggNm LIKE %:region%) 
        AND (:year IS NULL OR p.searchYearCd = :year) 
        AND (:minRiskScore IS NULL OR p.riskScore >= :minRiskScore) 
        AND (:riskLevel IS NULL OR p.riskLevel = :riskLevel)
        ORDER BY p.riskScore DESC
        """)
    Page<PedestrianAccidentEntity> findByComplexConditions(
            @Param("region") String region,
            @Param("year") String year,
            @Param("minRiskScore") BigDecimal minRiskScore,
            @Param("riskLevel") String riskLevel,
            Pageable pageable);

    /**
     * 중복 데이터 정리용 - 동일한 afosId 조회
     */
    @Query("""
        SELECT p FROM PedestrianAccidentEntity p 
        WHERE p.afosId IN (
            SELECT p2.afosId FROM PedestrianAccidentEntity p2 
            GROUP BY p2.afosId 
            HAVING COUNT(p2.afosId) > 1
        )
        ORDER BY p.afosId, p.createdAt
        """)
    List<PedestrianAccidentEntity> findDuplicateRecords();
}