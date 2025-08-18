package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.in.GetEmdInBoundsQuery;
import yys.safewalk.application.port.in.dto.EmdInBoundsResponse;
import yys.safewalk.application.port.out.EmdRepository;
import yys.safewalk.application.usecase.GetEmdInBoundsUseCase;
import yys.safewalk.domain.model.Emd;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmdService implements GetEmdInBoundsUseCase {

    private final EmdRepository emdRepository;

    @Override
    public List<EmdInBoundsResponse> getEmdInBounds(GetEmdInBoundsQuery query) {
        List<Emd> emds = emdRepository.findEmdInBounds(query.swLatLng(), query.neLatLng());

        return emds.stream()
                .map(emd -> new EmdInBoundsResponse(
                        emd.getName(),
                        emd.getTotalAccident(),
                        emd.getEmdCd(),
                        emd.getCenterPoint().latitude(),
                        emd.getCenterPoint().longitude()
                ))
                .toList();
    }
}