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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmdService implements GetEmdUseCase {

    private final EmdRepository emdRepository;

    @Override
    public List<EmdInBoundsResponse> getEmdInBounds(GetEmdInBoundsQuery query) {
        List<Emd> emds = emdRepository.findEmdInBounds(query.swLatLng(), query.neLatLng());

        return emds.stream()
                .collect(Collectors.toMap(
                        emd -> emd.getName() + "-" + emd.getEmdCd(), // key: name-emdCd 조합
                        Function.identity(), // value: emd 객체 자체
                        (existing, replacement) -> existing // 중복 시 기존 객체 유지
                ))
                .values()
                .stream()
                .map(emd -> new EmdInBoundsResponse(
                        emd.getName(),
                        emd.getTotalAccident(),
                        emd.getEmdCd(),
                        emd.getCenterPoint().latitude(),
                        emd.getCenterPoint().longitude()
                ))
                .toList();
    }

    @Override
    public List<EmdInBoundsResponse> getEmdBySidoCode(GetEmdBySidoCodeQuery query) {
        List<Emd> emds = emdRepository.findBySidoCode(query.sidoCode());

        return emds.stream()
                .collect(Collectors.toMap(
                        emd -> emd.getName() + "-" + emd.getEmdCd(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
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