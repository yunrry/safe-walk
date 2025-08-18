package yys.safewalk.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PointResponse {
    private final BigDecimal lat;
    private final BigDecimal lng;
}