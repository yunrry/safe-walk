package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yys.safewalk.entity.PedestrianAccidentHotspotsEntity;

import java.math.BigDecimal;
import java.util.List;

public interface PedestrianAccidentHotspotsJpaRepository extends JpaRepository<PedestrianAccidentHotspotsEntity, Long> {

    @Query("SELECT SUM(p.accidentCount) FROM PedestrianAccidentHotspotsEntity p WHERE p.sidoCode LIKE CONCAT(:emdPrefix, '%')")
    Integer getTotalAccidentCountByEmdCode(@Param("emdPrefix") String emdPrefix);

    @Query("SELECT p FROM PedestrianAccidentHotspotsEntity p WHERE p.latitude BETWEEN :swLat AND :neLat AND p.longitude BETWEEN :swLng AND :neLng")
    List<PedestrianAccidentHotspotsEntity> findAccidentsInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLat") BigDecimal neLat,
            @Param("neLng") BigDecimal neLng
    );

    List<PedestrianAccidentHotspotsEntity> findBySidoCodeStartingWith(String emdPrefix);
}