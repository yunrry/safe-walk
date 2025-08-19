package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yys.safewalk.domain.model.Emd;
import yys.safewalk.entity.EmdData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EmdJpaRepository extends JpaRepository<EmdData, Long> {

    // EmdJpaRepository 수정
//    @Query("""
//    SELECT e.emdCd, e.emdKorNm, e.polygon, a.latitude, a.longitude,
//    COALESCE((SELECT SUM(p.accidentCount)
//              FROM PedestrianAccidentHotspots p
//              WHERE p.sidoCode LIKE CONCAT(SUBSTRING(e.emdCd, 1, 8), '%')), 0) as totalAccident
//    FROM EmdData e
//    LEFT JOIN AdministrativeLegalDongs a ON SUBSTRING(e.emdCd, 1, 8) = SUBSTRING(a.code, 1, 8)
//    WHERE a.latitude BETWEEN :swLat AND :neLat
//    AND a.longitude BETWEEN :swLng AND :neLng
//    """)
//    List<Object[]> findEmdDataInBounds(@Param("swLat") BigDecimal swLat, @Param("swLng") BigDecimal swLng,
//                                       @Param("neLat") BigDecimal neLat, @Param("neLng") BigDecimal neLng);


    @Query("""

            SELECT e.emdCd, e.emdKorNm, a.latitude, a.longitude,
    COALESCE((SELECT SUM(p.accidentCount) 
              FROM PedestrianAccidentHotspots p 
              WHERE p.sidoCode LIKE CONCAT(SUBSTRING(e.emdCd, 1, 8), '%')), 0) as totalAccident
    FROM EmdData e
    LEFT JOIN AdministrativeLegalDongs a ON SUBSTRING(e.emdCd, 1, 8) = SUBSTRING(a.code, 1, 8)
    WHERE a.latitude BETWEEN :swLat AND :neLat
    AND a.longitude BETWEEN :swLng AND :neLng
    """)
    List<Object[]> findEmdDataInBounds(@Param("swLat") BigDecimal swLat, @Param("swLng") BigDecimal swLng,
                                       @Param("neLat") BigDecimal neLat, @Param("neLng") BigDecimal neLng);


    Optional<EmdData> findByEmdCd(String emdCd);

    @Query("SELECT e.emdCd, e.emdKorNm, a.latitude, a.longitude, " +
            "COALESCE(SUM(p.accidentCount), 0) as totalAccident " +
            "FROM EmdData e " +
            "LEFT JOIN AdministrativeLegalDongs a ON SUBSTRING(e.emdCd, 1, 8) = SUBSTRING(a.code, 1, 8) " +
            "LEFT JOIN PedestrianAccidentHotspots p ON SUBSTRING(e.emdCd, 1, 8) = SUBSTRING(p.sidoCode, 1, 8) " +
            "WHERE SUBSTRING(e.emdCd, 1, 4) = :sidoCode " +
            "GROUP BY e.emdCd, e.emdKorNm, a.latitude, a.longitude")
    List<Object[]> findEmdDataBySidoCode(@Param("sidoCode") String sidoCode);

    }