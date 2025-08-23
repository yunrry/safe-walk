package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.entity.PopularTouristSpots;
import yys.safewalk.infrastructure.adapter.out.persistence.PopularTouristSpotsRepository;
import yys.safewalk.infrastructure.external.KakaoMapApiClient;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import jakarta.annotation.PreDestroy;

@Slf4j
@Service
@RequiredArgsConstructor
public class TouristSpotSigunguUpdateService {

    private final PopularTouristSpotsRepository repository;
    private final KakaoMapApiClient kakaoMapApiClient;
    
    // 병렬 처리를 위한 스레드 풀
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Transactional
    public void updateAllSigunguNames() {
        List<PopularTouristSpots> spotsWithoutSigungu = repository.findBySigunguNameIsNull();

        log.info("시군구명이 없는 관광지 {}개 발견", spotsWithoutSigungu.size());

        // 배치 크기 설정
        int batchSize = 50;
        int totalBatches = (int) Math.ceil((double) spotsWithoutSigungu.size() / batchSize);
        
        int successCount = 0;
        int failCount = 0;

        // 배치 단위로 병렬 처리
        for (int i = 0; i < totalBatches; i++) {
            int startIndex = i * batchSize;
            int endIndex = Math.min(startIndex + batchSize, spotsWithoutSigungu.size());
            List<PopularTouristSpots> batch = spotsWithoutSigungu.subList(startIndex, endIndex);
            
            log.info("배치 {}/{} 처리 중... ({}-{})", i + 1, totalBatches, startIndex + 1, endIndex);
            
            // 병렬 처리 (CompletableFuture 사용)
            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(spot -> CompletableFuture.runAsync(() -> {
                        try {
                            updateSigunguName(spot);
                        } catch (Exception e) {
                            log.error("시군구명 업데이트 실패: id={}, name={}, error={}",
                                    spot.getId(), spot.getSpotName(), e.getMessage());
                        }
                    }, executorService))
                    .collect(Collectors.toList());
            
            // 배치 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 배치 결과 집계
            successCount += batch.size();
            
            // API 호출 제한을 위한 딜레이 (배치 단위로 조정)
            if (i < totalBatches - 1) { // 마지막 배치가 아닌 경우에만 딜레이
                try {
                    Thread.sleep(200); // 배치당 0.2초 딜레이
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("시군구명 업데이트 완료: 성공={}, 실패={}", successCount, failCount);
    }

    /**
     * 시도명과 카카오 API 주소 결과의 첫 번째 단어를 비교하여 유효성 검증
     */
    private boolean isValidSidoMatch(String sidoName, String addressName) {
        if (sidoName == null || addressName == null) {
            return false;
        }

        String[] addressParts = addressName.trim().split("\\s+");
        if (addressParts.length == 0) {
            return false;
        }

        String firstAddressWord = addressParts[0];
        
        // 시도명 매핑 테이블
        switch (sidoName) {
            case "부산광역시":
                return firstAddressWord.equals("부산");
            case "대전광역시":
                return firstAddressWord.equals("대전");
            case "대구광역시":
                return firstAddressWord.equals("대구");
            case "울산광역시":
                return firstAddressWord.equals("울산");
            case "강원도":
                return firstAddressWord.equals("강원특별자치도");
            case "인천광역시":
                return firstAddressWord.equals("인천");
            case "경상남도":
                return firstAddressWord.equals("경남");
            case "경상북도":
                return firstAddressWord.equals("경북");
            case "충청남도":
                return firstAddressWord.equals("충남");
            case "충청북도":
                return firstAddressWord.equals("충북");
            case "전라남도":
                return firstAddressWord.equals("전남");
            case "전북특별자치도":
                return firstAddressWord.equals("전북특별자치도");
            case "제주특별자치도":
                return firstAddressWord.equals("제주특별자치도");
            case "세종특별자치시":
                return firstAddressWord.equals("세종특별자치시");
            case "서울특별시":
                return firstAddressWord.equals("서울");
            case "광주광역시":
                return firstAddressWord.equals("광주");
            case "경기도":
                return firstAddressWord.equals("경기");
            default:
                log.warn("알 수 없는 시도명: {}", sidoName);
                return false;
        }
    }

    @Transactional
    public void updateSigunguName(PopularTouristSpots spot) {
        if (spot.getSigunguName() != null) {
            log.debug("이미 시군구명이 설정되어 있음: {}", spot.getSpotName());
            return;
        }

        String searchQuery = buildSearchQuery(spot);
        log.info("시군구명 검색 시도: {} -> 검색어: {}", spot.getSpotName(), searchQuery);

        // 카카오 API로 장소 검색
        kakaoMapApiClient.searchLocation(searchQuery)
                .ifPresentOrElse(
                        document -> {
                            log.info("카카오 API 검색 성공: {} -> 장소명: {}, 주소: {}", 
                                    searchQuery, document.placeName(), document.getAddressName());
                            
                            // 시도명 일치 여부 확인
                            if (!isValidSidoMatch(spot.getSidoName(), document.getAddressName())) {
                                log.warn("시도명 불일치: 기대={}, 실제={} -> 대체 검색 시도", 
                                        spot.getSidoName(), document.getAddressName());
                                tryAlternativeSearch(spot);
                                return;
                            }
                            
                            String extractedSigungu = extractSigunguFromAddress(document.getAddressName());
                            if (extractedSigungu != null) {
                                spot.setSigunguName(extractedSigungu);
                                repository.save(spot);
                                
                                log.info("시군구명 업데이트 성공: {} -> {} (주소: {})", 
                                        spot.getSpotName(), extractedSigungu, document.getAddressName());
                            } else {
                                log.warn("주소에서 시군구명 추출 실패: {} -> 주소: {}", 
                                        searchQuery, document.getAddressName());
                            }
                        },
                        () -> {
                            log.warn("카카오 API 검색 실패: {} -> 대체 검색 시도", searchQuery);
                            tryAlternativeSearch(spot);
                        }
                );
    }

    private void tryAlternativeSearch(PopularTouristSpots spot) {
        String alternativeQuery = spot.getSpotName();
        log.info("대체 검색 시도: {} -> 검색어: {}", spot.getSpotName(), alternativeQuery);

        kakaoMapApiClient.searchLocation(alternativeQuery)
                .ifPresentOrElse(
                        document -> {
                            log.info("대체 검색 성공: {} -> 장소명: {}, 주소: {}", 
                                    alternativeQuery, document.placeName(), document.getAddressName());
                            
                            // 대체 검색에서도 시도명 일치 여부 확인
                            if (!isValidSidoMatch(spot.getSidoName(), document.getAddressName())) {
                                log.warn("대체 검색에서도 시도명 불일치: 기대={}, 실제={} -> 시군구명 업데이트 건너뜀", 
                                        spot.getSidoName(), document.getAddressName());
                                return; // 시도명이 일치하지 않으면 업데이트하지 않고 넘어감
                            }
                            
                            String extractedSigungu = extractSigunguFromAddress(document.getAddressName());
                            if (extractedSigungu != null) {
                                spot.setSigunguName(extractedSigungu);
                                repository.save(spot);
                                
                                log.info("대체 검색으로 시군구명 업데이트 성공: {} -> {} (주소: {})", 
                                        spot.getSpotName(), extractedSigungu, document.getAddressName());
                            } else {
                                log.warn("대체 검색에서도 시군구명 추출 실패: {} -> 주소: {}", 
                                        alternativeQuery, document.getAddressName());
                            }
                        },
                        () -> log.warn("대체 검색도 실패: {} -> 시군구명 찾기 실패", alternativeQuery)
                );
    }

    private String buildSearchQuery(PopularTouristSpots spot) {
        StringBuilder query = new StringBuilder();

        if (spot.getSidoName() != null) {
            query.append(spot.getSidoName()).append(" ");
        }
        if (spot.getSpotName() != null) {
            query.append(spot.getSpotName());
        }

        return query.toString().trim();
    }

    private String extractSigunguFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            // 주소를 공백으로 분리
            String[] parts = address.trim().split("\\s+");
            
            // 최소 2개 이상의 부분이 있어야 함 (시도 시군구)
            if (parts.length >= 2) {
                // 두 번째 부분이 시군구 (예: "강남구")
                String sigungu = parts[1];
                
                // "구", "시", "군"으로 끝나는지 확인
                if (sigungu.endsWith("구") || sigungu.endsWith("시") || sigungu.endsWith("군")) {
                    return sigungu;
                }

            }
            
            return null;
            
        } catch (Exception e) {
            log.warn("주소에서 시군구명 추출 실패: address={}, error={}", address, e.getMessage());
            return null;
        }
    }

    /**
     * ID로 관광지를 조회하여 시군구명 업데이트
     */
    @Transactional
    public void updateSigunguNameById(Long id) {
        PopularTouristSpots spot = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 관광지를 찾을 수 없습니다."));
        
        log.info("ID {} 관광지 시군구명 업데이트 시작: {}", id, spot.getSpotName());
        updateSigunguName(spot);
    }

    /**
     * ID 목록으로 여러 관광지의 시군구명 일괄 업데이트
     */
    @Transactional
    public void updateSigunguNamesByIds(List<Long> ids) {
        log.info("{}개 관광지의 시군구명 일괄 업데이트 시작", ids.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long id : ids) {
            try {
                updateSigunguNameById(id);
                successCount++;
                
                // API 호출 제한을 위한 딜레이
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("ID {} 관광지 시군구명 업데이트 실패: {}", id, e.getMessage());
                failCount++;
            }
        }
        
        log.info("ID 기반 시군구명 업데이트 완료: 성공={}, 실패={}", successCount, failCount);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
