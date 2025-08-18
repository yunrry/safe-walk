package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yys.safewalk.entity.PopularTouristSpots;

import java.util.List;

@Repository
public interface PopularTouristSpotsRepository extends JpaRepository<PopularTouristSpots, Long> {

    List<PopularTouristSpots> findByLongitudeIsNullOrLatitudeIsNull();

    List<PopularTouristSpots> findBySidoNameAndSigunguName(String sidoName, String sigunguName);
}