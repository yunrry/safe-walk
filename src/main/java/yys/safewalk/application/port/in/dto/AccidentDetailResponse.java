package yys.safewalk.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccidentDetailResponse {
    private final String id;
    private final String location;
    private final Integer accidentCount;
    private final CasualtiesResponse casualties;
    private final PointResponse point;
}