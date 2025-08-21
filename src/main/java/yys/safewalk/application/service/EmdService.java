package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.in.GetEmdBySidoCodeQuery;
import yys.safewalk.application.port.in.GetEmdInBoundsQuery;
import yys.safewalk.application.port.in.dto.EmdInBoundsResponse;
import yys.safewalk.application.port.out.EmdRepository;
import yys.safewalk.application.usecase.GetEmdUseCase;
import yys.safewalk.domain.model.Emd;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.infrastructure.adapter.out.persistence.AdministrativeLegalDongsAdapter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmdService implements GetEmdUseCase {

    private final AdministrativeLegalDongsAdapter administrativeLegalDongsAdapter; // AdministrativeLegalDongsAdapter 주입됨

    @Override
    @Transactional(readOnly = true)
    public List<EmdInBoundsResponse> getEmdInBounds(GetEmdInBoundsQuery query) {
        // AdministrativeLegalDongsAdapter를 통해 좌표 범위 내 법정동 조회
        List<Emd> emds = administrativeLegalDongsAdapter.findEmdInBounds(
                query.swLatLng(), 
                query.neLatLng()
        );

        return emds.stream()
                .map(this::mapToEmdInBoundsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmdInBoundsResponse> getEmdBySidoCode(GetEmdBySidoCodeQuery query) {
        // AdministrativeLegalDongsAdapter를 통해 시도별 법정동 조회
        List<Emd> emds = administrativeLegalDongsAdapter.findBySidoCode(query.sidoCode());

        return emds.stream()
                .map(this::mapToEmdInBoundsResponse)
                .collect(Collectors.toList());
    }

    private EmdInBoundsResponse mapToEmdInBoundsResponse(Emd emd) {
        return new EmdInBoundsResponse(
                emd.getName(),
                emd.getTotalAccident(),
                emd.getEmdCd(),
                emd.getCenterPoint().latitude(),
                emd.getCenterPoint().longitude()
        );
    }
}