package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.entity.PopularTouristSpots;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsRepository;
import yys.safewalk.infrastructure.external.KakaoMapApiClient;


import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TouristSpotCoordinateService {

    private final PopularTouristSpotsRepository repository;
    private final KakaoMapApiClient kakaoMapApiClient;

    @Transactional
    public void updateAllCoordinates() {
        List<PopularTouristSpots> spotsWithoutCoordinates = repository.findByLongitudeIsNullOrLatitudeIsNull();

        log.info("좌표가 없는 관광지 {}개 발견", spotsWithoutCoordinates.size());

        int successCount = 0;
        int failCount = 0;

        for (PopularTouristSpots spot : spotsWithoutCoordinates) {
            try {
                updateCoordinate(spot);
                successCount++;

                // API 호출 제한을 위한 딜레이
                Thread.sleep(100);

            } catch (Exception e) {
                log.error("좌표 업데이트 실패: id={}, name={}, error={}",
                        spot.getId(), spot.getSpotName(), e.getMessage());
                failCount++;
            }
        }

        log.info("좌표 업데이트 완료: 성공={}, 실패={}", successCount, failCount);
    }

    @Transactional
    public void updateCoordinate(PopularTouristSpots spot) {
        String searchQuery = buildSearchQuery(spot);

        kakaoMapApiClient.searchLocation(searchQuery)
                .ifPresentOrElse(
                        document -> {
                            spot.setLongitude(document.getLongitude());
                            spot.setLatitude(document.getLatitude());
                            repository.save(spot);

                            log.debug("좌표 업데이트 성공: {} -> ({}, {})",
                                    searchQuery, document.getLongitude(), document.getLatitude());
                        },
                        () -> {
                            // 검색어를 다르게 시도
                            String alternativeQuery = spot.getSpotName();
                            kakaoMapApiClient.searchLocation(alternativeQuery)
                                    .ifPresentOrElse(
                                            document -> {
                                                spot.setLongitude(document.getLongitude());
                                                spot.setLatitude(document.getLatitude());
                                                repository.save(spot);

                                                log.debug("대체 검색으로 좌표 업데이트 성공: {} -> ({}, {})",
                                                        alternativeQuery, document.getLongitude(), document.getLatitude());
                                            },
                                            () -> log.warn("좌표 찾기 실패: {}", spot.getSpotName())
                                    );
                        }
                );
    }

    private String buildSearchQuery(PopularTouristSpots spot) {
        // "경상북도 경주시 불국사" 형태로 검색어 구성
        StringBuilder query = new StringBuilder();

        if (spot.getSidoName() != null) {
            query.append(spot.getSidoName()).append(" ");
        }
        if (spot.getSigunguName() != null) {
            query.append(spot.getSigunguName()).append(" ");
        }
        if (spot.getSpotName() != null) {
            query.append(spot.getSpotName());
        }

        return query.toString().trim();
    }
}