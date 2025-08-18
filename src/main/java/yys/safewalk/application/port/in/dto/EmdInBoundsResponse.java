package yys.safewalk.application.port.in.dto;

import java.math.BigDecimal;

public record EmdInBoundsResponse(
        String name,
        Integer totalAccident,
        String EMD_CD,
        BigDecimal latitude,
        BigDecimal longitude
) {}