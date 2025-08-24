package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yys.safewalk.entity.ElderlyPedestrianAccidentHotspotsEntity;

import java.math.BigDecimal;
import java.util.List;

public interface ElderlyPedestrianAccidentHotspotsJpaRepository extends JpaRepository<ElderlyPedestrianAccidentHotspotsEntity, Long> {

    @Query("SELECT SUM(e.accidentCount) FROM ElderlyPedestrianAccidentHotspotsEntity e WHERE e.sidoCode LIKE CONCAT(:emdPrefix, '%')")
    Integer getTotalAccidentCountByEmdCode(@Param("emdPrefix") String emdPrefix);

    @Query("SELECT e FROM ElderlyPedestrianAccidentHotspotsEntity e WHERE e.latitude BETWEEN :swLat AND :neLat AND e.longitude BETWEEN :swLng AND :neLng")
    List<ElderlyPedestrianAccidentHotspotsEntity> findAccidentsInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLat") BigDecimal neLat,
            @Param("neLng") BigDecimal neLng
    );

    List<ElderlyPedestrianAccidentHotspotsEntity> findBySidoCodeStartingWith(String sidoCodePrefix);
}