package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.in.dto.EmdResponse;
import yys.safewalk.application.port.in.dto.EmdSearchRequest;
import yys.safewalk.application.port.in.dto.TouristSpotSearchResponse;
import yys.safewalk.domain.model.Coordinate;
import yys.safewalk.domain.model.PopularTouristSpots;
import yys.safewalk.entity.AdministrativeLegalDongs;
import yys.safewalk.entity.PopularTouristSpotsEntity;
import yys.safewalk.infrastructure.adapter.out.persistence.AdministrativeLegalDongsRepository;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsJPARepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TouristSpotSearchService {

    private final PopularTouristSpotsJPARepository popularTouristSpotsJPARepository;

    public List<TouristSpotSearchResponse> searchRealtime(String query, int limit) {
        validateRealtimeQuery(query);

        log.debug("Searching realtime for query: {}, limit: {}", query, limit);

        List<PopularTouristSpotsEntity> results = popularTouristSpotsJPARepository.findBySpotNameStartingWith(
                query, PageRequest.of(0, limit));

        return results.stream()
                .map(this::toTouristSpotSearchResponse)
                .collect(Collectors.toMap(
                        TouristSpotSearchResponse::touristSpotId,  //id 키로 사용
                        response -> response,  // 값은 TouristSpotSearchResponse 그대로
                        (existing, replacement) -> existing  // 중복 시 기존 값 유지
                ))
                .values()
                .stream()
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

    private TouristSpotSearchResponse toTouristSpotSearchResponse(PopularTouristSpotsEntity entity) {

        return new TouristSpotSearchResponse(
                entity.getTouristSpotId(),
                entity.getSpotName(),
                entity.getSidoCode(),
                entity.getSidoName(),
                entity.getSigunguName(),
                entity.getCategory(),
                new Coordinate(entity.getLatitude(),
                        entity.getLongitude())
        );
    }
}