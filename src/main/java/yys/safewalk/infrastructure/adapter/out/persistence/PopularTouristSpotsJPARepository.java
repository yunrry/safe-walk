package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.entity.PopularTouristSpots;

import java.util.List;

@Repository
public interface PopularTouristSpotsJPARepository extends JpaRepository<PopularTouristSpots, Long> {

    List<PopularTouristSpots> findByLongitudeIsNullOrLatitudeIsNull();

    List<PopularTouristSpots> findBySidoNameAndSigunguName(String sidoName, String sigunguName);

    @Query("SELECT p FROM PopularTouristSpots p " +
            "WHERE p.latitude BETWEEN :swLat AND :neLat " +
            "AND p.longitude BETWEEN :swLng AND :neLng " +
            "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
    List<PopularTouristSpots> findByLatitudeBetweenAndLongitudeBetween(
            @Param("swLat") Double swLat,
            @Param("neLat") Double neLat,
            @Param("swLng") Double swLng,
            @Param("neLng") Double neLng
    );

    /**
     * 시군구명이 null인 관광지 조회
     */
    List<PopularTouristSpots> findBySigunguNameIsNull();

    /**
     * 시도코드와 모드로 관광지 조회 (rank 순으로 정렬)
     */
    @Query("SELECT p FROM PopularTouristSpots p " +
            "WHERE p.sidoCode = :sidoCode AND p.mode = :mode " +
            "ORDER BY p.rank ASC")
    List<PopularTouristSpots> findBySidoCodeAndMode(
            @Param("sidoCode") String sidoCode, 
            @Param("mode") String mode
    );
}