package yys.safewalk.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yys.safewalk.entity.PopularTouristSpots;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsJPARepository;

public interface PopularTouristSpotsRepository extends PopularTouristSpotsJPARepository {

}
