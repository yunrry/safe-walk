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
        List<AccidentDetailResponse> accidentResponses = null;

        if (emdDetail.getAccidents() != null) {
            accidentResponses = emdDetail.getAccidents().stream()
                    .map(accident -> new AccidentDetailResponse(
                            accident.getId(),
                            accident.getLocation(),
                            accident.getAccidentCount(),
                            new CasualtiesResponse(
                                    accident.getCasualties().getTotal(),
                                    accident.getCasualties().getDead(),
                                    accident.getCasualties().getSevere(),
                                    accident.getCasualties().getMinor()
                            ),
                            new PointResponse(
                                    accident.getPoint().latitude(),
                                    accident.getPoint().longitude()
                            )
                    ))
                    .collect(Collectors.toList());
        }

        return new EmdDetailResponse(
                emdDetail.getName(),
                emdDetail.getTotalAccident(),
                emdDetail.getEmdCode(),
                accidentResponses
        );
    }
}
