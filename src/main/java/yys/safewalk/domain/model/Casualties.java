package yys.safewalk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Casualties {
    private final Integer total;
    private final Integer dead;
    private final Integer severe;
    private final Integer minor;
}