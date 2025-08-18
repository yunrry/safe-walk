package yys.safewalk.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetEmdDetailQuery {
    private final String emdCode;
}