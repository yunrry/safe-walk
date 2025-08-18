package yys.safewalk.application.usecase;

import yys.safewalk.application.port.in.GetEmdDetailQuery;
import yys.safewalk.application.port.in.dto.EmdDetailResponse;

public interface GetEmdDetailUseCase {
    EmdDetailResponse getEmdDetail(GetEmdDetailQuery query);
}