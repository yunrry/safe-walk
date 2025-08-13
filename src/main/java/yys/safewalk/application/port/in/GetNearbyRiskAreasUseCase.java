package yys.safewalk.application.port.in;

import yys.safewalk.application.port.in.dto.RiskAreaDto;
import java.util.List;
import javax.xml.stream.Location;


public interface GetNearbyRiskAreasUseCase {
    List<RiskAreaDto> getNearbyRiskAreas(Location location, int radius);
}