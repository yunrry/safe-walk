package yys.safewalk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmdDetail {
    private final String name;
    private final Integer totalAccident;
    private final String emdCode;
    private final List<AccidentDetail> accidents;
}