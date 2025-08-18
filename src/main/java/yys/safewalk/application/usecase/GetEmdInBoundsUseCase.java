package yys.safewalk.application.usecase;

import yys.safewalk.application.port.in.GetEmdInBoundsQuery;
import yys.safewalk.application.port.in.dto.EmdInBoundsResponse;

import java.util.List;

public interface GetEmdInBoundsUseCase {
    List<EmdInBoundsResponse> getEmdInBounds(GetEmdInBoundsQuery query);
}