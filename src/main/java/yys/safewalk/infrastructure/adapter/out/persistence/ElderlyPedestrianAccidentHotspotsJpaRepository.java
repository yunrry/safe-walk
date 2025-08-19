package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yys.safewalk.entity.ElderlyPedestrianAccidentHotspots;
import yys.safewalk.entity.PedestrianAccidentHotspots;

import java.math.BigDecimal;
import java.util.List;

public interface ElderlyPedestrianAccidentHotspotsJpaRepository extends JpaRepository<ElderlyPedestrianAccidentHotspots, Long> {

    @Query("SELECT SUM(e.accidentCount) FROM ElderlyPedestrianAccidentHotspots e WHERE e.sidoCode LIKE CONCAT(:emdPrefix, '%')")
    Integer getTotalAccidentCountByEmdCode(@Param("emdPrefix") String emdPrefix);

    @Query("SELECT e FROM ElderlyPedestrianAccidentHotspots e WHERE e.latitude BETWEEN :swLat AND :neLat AND e.longitude BETWEEN :swLng AND :neLng")
    List<ElderlyPedestrianAccidentHotspots> findAccidentsInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLat") BigDecimal neLat,
            @Param("neLng") BigDecimal neLng
    );

    List<ElderlyPedestrianAccidentHotspots> findBySidoCodeStartingWith(String sidoCodePrefix);
}