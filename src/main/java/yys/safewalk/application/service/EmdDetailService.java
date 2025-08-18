package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yys.safewalk.application.port.in.GetEmdDetailQuery;
import yys.safewalk.application.port.in.dto.*;
import yys.safewalk.application.port.out.EmdDetailPort;
import yys.safewalk.application.usecase.GetEmdDetailUseCase;
import yys.safewalk.domain.model.EmdDetail;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmdDetailService implements GetEmdDetailUseCase {

    private final EmdDetailPort emdDetailPort;

    @Override
    public EmdDetailResponse getEmdDetail(GetEmdDetailQuery query) {
        EmdDetail emdDetail = emdDetailPort.findByEmdCode(query.getEmdCode())
                .orElseThrow(() -> new IllegalArgumentException("법정동을 찾을 수 없습니다: " + query.getEmdCode()));

        return mapToResponse(emdDetail);
    }

    private EmdDetailResponse mapToResponse(EmdDetail emdDetail) {
        List<AccidentDetailResponse> accidents = emdDetail.getAccidents().stream()
                .map(accident -> {
                    Integer dead = accident.getCasualties().getDead();
                    Integer severe = accident.getCasualties().getSevere();
                    Integer minor = accident.getCasualties().getMinor();

                    // total = dead + severe + minor
                    Integer total = (dead != null ? dead : 0) +
                            (severe != null ? severe : 0) +
                            (minor != null ? minor : 0);

                    return new AccidentDetailResponse(
                            accident.getId(),
                            accident.getLocation(),
                            accident.getAccidentCount(),
                            new CasualtiesResponse(
                                    total,  // 계산된 값
                                    dead,
                                    severe,
                                    minor
                            ),
                            new PointResponse(
                                    accident.getPoint().latitude(),
                                    accident.getPoint().longitude()
                            )
                    );
                })
                .collect(Collectors.toList());

        return new EmdDetailResponse(
                emdDetail.getName(),
                emdDetail.getTotalAccident(),
                emdDetail.getEmdCode(),
                accidents
        );
    }
}