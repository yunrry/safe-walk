package yys.safewalk.infrastructure.adapter.in.web.dto;

import yys.safewalk.application.port.in.dto.RiskAreaDto;

import java.util.List;

public class RiskAreaResponse {

    public static List<RiskAreaResponse> from(List<RiskAreaDto> riskAreas) {
        return List.of();
    }
}
