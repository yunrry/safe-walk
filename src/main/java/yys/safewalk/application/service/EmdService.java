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
import yys.safewalk.infrastructure.adapter.out.persistence.AdministrativeLegalDongsRepository;
import yys.safewalk.entity.AdministrativeLegalDongs;
import yys.safewalk.domain.model.Coordinate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmdService implements GetEmdUseCase {

    private final EmdRepository emdRepository;
    private final AdministrativeLegalDongsRepository administrativeLegalDongsRepository;

    @Override
    public List<EmdInBoundsResponse> getEmdInBounds(GetEmdInBoundsQuery query) {
        // AdministrativeLegalDongs에서 지도 영역 내 법정동 조회 (codeType != 'H')
        List<AdministrativeLegalDongs> legalDongs = administrativeLegalDongsRepository
                .findByLatitudeBetweenAndLongitudeBetweenAndCodeTypeNot(
                        query.swLatLng().latitude(),
                        query.neLatLng().latitude(),
                        query.swLatLng().longitude(),
                        query.neLatLng().longitude(),
                        "H"
                );

        return legalDongs.stream()
                .map(this::mapToEmdInBoundsResponse)
                .toList();
    }

    @Override
    public List<EmdInBoundsResponse> getEmdBySidoCode(GetEmdBySidoCodeQuery query) {
        // AdministrativeLegalDongs에서 시도별 법정동 조회 (codeType != 'H')
        List<AdministrativeLegalDongs> legalDongs = administrativeLegalDongsRepository
                .findBySidoStartingWithAndCodeTypeNot(query.sidoCode(), "H");

        return legalDongs.stream()
                .map(this::mapToEmdInBoundsResponse)
                .toList();
    }
    
    /**
     * AdministrativeLegalDongs를 EmdInBoundsResponse로 변환
     */
    private EmdInBoundsResponse mapToEmdInBoundsResponse(AdministrativeLegalDongs legalDong) {
        // 사고 데이터 조회 (emdCode의 앞 8자리로 조회)
        String emdPrefix = legalDong.getCode().substring(0, 8);
        
        // TODO: 사고 데이터 조회 로직 추가 필요
        // 현재는 기본값 0으로 설정
        Integer totalAccident = 0;
        
        return new EmdInBoundsResponse(
                legalDong.getEupMyeonDong(),  // 읍면동명
                totalAccident,                 // 사고 건수 (기본값 0)
                emdPrefix,                     // EMD_CD (앞 8자리)
                legalDong.getLatitude(),       // 위도
                legalDong.getLongitude()       // 경도
        );
    }
}