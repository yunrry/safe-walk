package yys.safewalk.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class EmdDetailResponse {
    private final String name;
    private final Integer totalAccident;
    private final String EMD_CD;
    private final List<AccidentDetailResponse> accidents;
}