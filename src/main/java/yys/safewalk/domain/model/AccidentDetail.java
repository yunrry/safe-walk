package yys.safewalk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccidentDetail {
    private final String id;
    private final String location;
    private final Integer accidentCount;
    private final Casualties casualties;
    private final Coordinate point;
}