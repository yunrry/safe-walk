package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.in.dto.EmdResponse;
import yys.safewalk.application.port.in.dto.EmdSearchRequest;
import yys.safewalk.entity.AdministrativeLegalDongs;
import yys.safewalk.infrastructure.adapter.out.persistence.AdministrativeLegalDongsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdministrativeLegalDongService {

    private final AdministrativeLegalDongsRepository repository;

    public List<EmdResponse> searchRealtime(String query, int limit) {
        validateRealtimeQuery(query);

        log.debug("Searching realtime for query: {}, limit: {}", query, limit);

        List<AdministrativeLegalDongs> results = repository.findByEupMyeonDongStartingWith(
                query, PageRequest.of(0, limit));

        return results.stream()
                .map(this::toEmdResponse)
                .toList();
    }

    public List<EmdResponse> search(EmdSearchRequest request) {
        log.debug("Searching with request: {}", request);

        List<AdministrativeLegalDongs> results;

        if (request.sido() != null && request.sigungu() != null) {
            results = repository.findByEupMyeonDongAndSidoAndSigunguAndCodeTypeNot(
                    request.eupMyeonDong(), request.sido(), request.sigungu(), "H");
        } else if (request.sido() != null) {
            results = repository.findByEupMyeonDongAndSidoAndCodeTypeNot(
                    request.eupMyeonDong(), request.sido(), "H");
        } else {
            results = repository.findByEupMyeonDongAndCodeTypeNot(request.eupMyeonDong(), "H");
        }


        if (results.isEmpty()) {
            throw new IllegalArgumentException("해당 읍면동을 찾을 수 없습니다: " + request.eupMyeonDong());
        }


        return results.stream()
                .map(this::toEmdResponse)
                .toList();
    }

    public EmdResponse findByCode(String inputCode) {
        String code = inputCode.endsWith("00") ? inputCode : inputCode + "00";

        log.debug("Searching by code: {}", code);

        AdministrativeLegalDongs result = repository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("해당 코드의 행정구역을 찾을 수 없습니다: " + code));

        return toEmdResponse(result);
    }




    public List<EmdResponse> searchByEupMyeonDongOnly(String eupMyeonDong) {
        if (eupMyeonDong == null || eupMyeonDong.trim().isEmpty()) {
            throw new IllegalArgumentException("읍면동명은 필수입니다");
        }

        log.debug("Searching by eupMyeonDong only: {}", eupMyeonDong);

        List<AdministrativeLegalDongs> results = repository.findByEupMyeonDongOrderBySidoAndSigungu(eupMyeonDong.trim());

        return results.stream()
                .map(this::toEmdResponse)
                .toList();
    }





    private void validateRealtimeQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수입니다");
        }
        if (query.length() > 50) {
            throw new IllegalArgumentException("검색어는 50자를 초과할 수 없습니다");
        }
    }

    private EmdResponse toEmdResponse(AdministrativeLegalDongs entity) {
        // 코드에서 마지막 00 제거 (10자리 -> 8자리)
        String responseCode = entity.getCode();
        if (responseCode != null && responseCode.length() != 8) {
            responseCode = responseCode.substring(0, 8);
        }

        return new EmdResponse(
                entity.getId(),
                responseCode,
                entity.getSido(),
                entity.getSigungu(),
                entity.getEupMyeonDong(),
                entity.getSubLevel(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCodeType()
        );
    }
}