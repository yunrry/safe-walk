package yys.safewalk.domain.riskarea.port;

import yys.safewalk.domain.riskarea.model.RiskArea;
import yys.safewalk.domain.riskarea.model.Location;
import yys.safewalk.domain.riskarea.model.RiskLevel;
import yys.safewalk.domain.riskarea.model.Region;

import java.util.List;
import java.util.Optional;

public interface RiskAreaRepository {
    List<RiskArea> findNearbyRiskAreas(Location location, int radius);
    Optional<RiskArea> findById(Long id);
    void save(RiskArea riskArea);
    void delete(Long id);

    // 도메인 특화 쿼리 메서드들
    List<RiskArea> findByRiskLevelGreaterThan(RiskLevel level);
    List<RiskArea> findByRegion(Region region);
    boolean existsByLocationWithinRadius(Location location, int radius);
}