package yys.safewalk.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yys.safewalk.domain.riskarea.model.Location;
import yys.safewalk.application.port.in.GetNearbyRiskAreasUseCase;
import yys.safewalk.application.port.in.dto.RiskAreaDto;
import yys.safewalk.infrastructure.adapter.in.web.dto.RiskAreaResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk-areas")
@RequiredArgsConstructor
public class RiskAreaController {

    private final GetNearbyRiskAreasUseCase getNearbyRiskAreasUseCase;

    @GetMapping("/nearby")
    public ResponseEntity<List<RiskAreaResponse>> getNearbyRiskAreas(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "500") int radius
    ) {
        Location location = Location.of(latitude, longitude);
        List<RiskAreaDto> riskAreas = getNearbyRiskAreasUseCase
                .getNearbyRiskAreas(location, radius);

        return ResponseEntity.ok(RiskAreaResponse.from(riskAreas));
    }
}