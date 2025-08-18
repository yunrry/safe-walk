package yys.safewalk.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CasualtiesResponse {
    private final Integer total;
    private final Integer dead;
    private final Integer severe;
    private final Integer minor;
}