package yys.safewalk.application.port.in;

import yys.safewalk.application.port.in.dto.RiskAreaDto;
import yys.safewalk.domain.riskarea.model.Location;

import java.util.List;



public interface GetNearbyRiskAreasUseCase {
    List<RiskAreaDto> getNearbyRiskAreas(Location location, int radius);
}