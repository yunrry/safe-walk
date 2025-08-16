// infrastructure/adapter/out/external/KoroadApiAdapter.java
package yys.safewalk.infrastructure.adapter.out.external;

import lombok.*;
import yys.safewalk.application.port.out.external.dto.SearchCriteria;
import yys.safewalk.application.port.out.external.dto.RiskIndexData;
import yys.safewalk.application.port.out.external.KoroadApiPort;
import yys.safewalk.application.port.out.external.dto.*;
import yys.safewalk.common.exception.ExternalApiException;
import yys.safewalk.domain.riskarea.model.RegionCode;
import yys.safewalk.infrastructure.adapter.out.external.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import yys.safewalk.infrastructure.adapter.out.external.mapper.KoroadApiResponseMapper;
import yys.safewalk.infrastructure.config.KoroadApiProperties;


import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KoroadApiAdapter implements KoroadApiPort {

    private final WebClient koroadWebClient;
    private final KoroadApiProperties koroadApiProperties;
    private final KoroadApiResponseMapper responseMapper;

    // ===========================================
    // 1. 보행자 사고다발지역정보 API
    // ===========================================
    @Override
    public List<AccidentData> getPedestrianAccidentData(SearchCriteria criteria) {
        log.info("Fetching pedestrian accident data for criteria: {}", criteria);

        try {
            KoroadApiResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/frequentzone/pedstrians")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", criteria.getYear())
                            .queryParam("siDo", criteria.getSiDo())
                            .queryParam("guGun", criteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", criteria.getNumOfRows())
                            .queryParam("pageNo", criteria.getPageNo())
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadApiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toAccidentDataList(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch pedestrian accident data: {}", e.getMessage());
            throw new ExternalApiException("보행자 사고다발지역 API 호출 실패", e);
        }
    }

    // ===========================================
    // 2. 보행노인 사고다발지역정보 API
    // ===========================================
    @Override
    public List<AccidentData> getElderlyPedestrianAccidentData(SearchCriteria criteria) {
        log.info("Fetching elderly pedestrian accident data for criteria: {}", criteria);

        try {
            KoroadApiResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/frequentzone/oldman")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", criteria.getYear())
                            .queryParam("siDo", criteria.getSiDo())
                            .queryParam("guGun", criteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", criteria.getNumOfRows())
                            .queryParam("pageNo", criteria.getPageNo())
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadApiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toAccidentDataList(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch elderly pedestrian accident data: {}", e.getMessage());
            throw new ExternalApiException("보행노인 사고다발지역 API 호출 실패", e);
        }
    }

    // ===========================================
    // 3. 지자체별 사고다발지역정보 API
    // ===========================================
    @Override
    public List<AccidentData> getLocalGovernmentAccidentData(SearchCriteria criteria) {
        log.info("Fetching local government accident data for criteria: {}", criteria);

        try {
            KoroadApiResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/frequentzone/lg")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", criteria.getYear())
                            .queryParam("siDo", criteria.getSiDo())
                            .queryParam("guGun", criteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", criteria.getNumOfRows())
                            .queryParam("pageNo", criteria.getPageNo())
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadApiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toAccidentDataList(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch local government accident data: {}", e.getMessage());
            throw new ExternalApiException("지자체별 사고다발지역 API 호출 실패", e);
        }
    }

    // ===========================================
    // 4. 연휴기간별 사고다발지역정보 API
    // ===========================================
    @Override
    public List<AccidentData> getHolidayAccidentData(SearchCriteria criteria) {
        log.info("Fetching holiday accident data for criteria: {}", criteria);

        try {
            KoroadApiResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/frequentzone/tmzon")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", criteria.getYear())
                            .queryParam("siDo", criteria.getSiDo())
                            .queryParam("guGun", criteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", criteria.getNumOfRows())
                            .queryParam("pageNo", criteria.getPageNo())
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadApiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toAccidentDataList(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch holiday accident data: {}", e.getMessage());
            throw new ExternalApiException("연휴기간별 사고다발지역 API 호출 실패", e);
        }
    }

    // ===========================================
    // 5. 지자체별 대상사고통계 API
    // ===========================================
    @Override
    public List<AccidentStatisticsData> getAccidentStatistics(SearchCriteria criteria) {
        log.info("Fetching accident statistics for criteria: {}", criteria);

        try {
            KoroadStatisticsResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stt")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", criteria.getYear())
                            .queryParam("siDo", criteria.getSiDo())
                            .queryParam("guGun", criteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", criteria.getNumOfRows())
                            .queryParam("pageNo", criteria.getPageNo())
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadStatisticsResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toAccidentStatisticsDataList(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch accident statistics: {}", e.getMessage());
            throw new ExternalApiException("지자체별 대상사고통계 API 호출 실패", e);
        }
    }

    // ===========================================
    // 6. 링크기반 사고위험지역정보 API
    // ===========================================
    @Override
    public List<RiskAreaData> getLinkBasedRiskAreaData(SearchCriteria criteria) {
        log.info("Fetching link-based risk area data for criteria: {}", criteria);

        try {
            KoroadRiskAreaResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/accident/riskArea")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", criteria.getYear())
                            .queryParam("siDo", criteria.getSiDo())
                            .queryParam("guGun", criteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", criteria.getNumOfRows())
                            .queryParam("pageNo", criteria.getPageNo())
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadRiskAreaResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toRiskAreaDataList(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch link-based risk area data: {}", e.getMessage());
            throw new ExternalApiException("링크기반 사고위험지역 API 호출 실패", e);
        }
    }

    // ===========================================
    // 7. 세부링크 도로위험지수정보 API
    // ===========================================
    @Override
    public RiskIndexData getRealTimeRiskIndex(RouteInfo route) {
        log.info("Fetching real-time risk index for route: {}", route);

        try {
            KoroadRiskIndexResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/road/dgdgr/link")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchLineString", route.getLineString())
                            .queryParam("vhctyCd", route.getVehicleType())
                            .queryParam("type", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadRiskIndexResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return responseMapper.toRiskIndexData(response);
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch real-time risk index: {}", e.getMessage());
            throw new ExternalApiException("세부링크 도로위험지수 API 호출 실패", e);
        }
    }


    @Override
    public boolean isApiAvailable() {
        try {
            SearchCriteria testCriteria = SearchCriteria.builder()
                    .year("2023")
                    .siDo("11")
                    .guGun("680")
                    .numOfRows(1)
                    .pageNo(1)
                    .build();

            KoroadApiResponse response = koroadWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/frequentzone/pedstrians")
                            .queryParam("authKey", koroadApiProperties.getApiKey())
                            .queryParam("searchYearCd", testCriteria.getYear())
                            .queryParam("siDo", testCriteria.getSiDo())
                            .queryParam("guGun", testCriteria.getGuGun())
                            .queryParam("type", "json")
                            .queryParam("numOfRows", 1)
                            .queryParam("pageNo", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(KoroadApiResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return response != null && "00".equals(response.getResultCode());
        } catch (Exception e) {
            log.warn("API health check failed", e);
            return false;
        }
    }

    @Override
    public ApiHealthStatus getApiHealthStatus() {
        boolean isAvailable = isApiAvailable();

        return ApiHealthStatus.builder()
                .available(isAvailable)
                .lastChecked(System.currentTimeMillis())
                .responseTime(measureResponseTime())
                .build();
    }

    private long measureResponseTime() {
        long startTime = System.currentTimeMillis();
        isApiAvailable();
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 한국의 모든 시도/시군구 코드를 반환
     */
    private List<RegionCode> getKoreanRegionCodes() {
        // 실제로는 별도 파일이나 데이터베이스에서 로드
        return List.of(
                // 서울특별시
                RegionCode.of("11", "680"), // 강남구
                RegionCode.of("11", "740"), // 강동구
                RegionCode.of("11", "305"), // 강북구
                // ... 모든 지역 코드

                // 부산광역시
                RegionCode.of("26", "440"), // 강서구
                RegionCode.of("26", "410"), // 금정구
                // ... 더 많은 지역 코드들

                // 대구광역시
                RegionCode.of("27", "200"), // 중구
                RegionCode.of("27", "290"), // 달서구
                // ... 계속

                // 인천광역시
                RegionCode.of("28", "185"), // 계양구
                RegionCode.of("28", "245"), // 미추홀구
                // ... 계속

                // 광주광역시
                RegionCode.of("29", "155"), // 광산구
                RegionCode.of("29", "170"), // 남구
                // ... 계속

                // 대전광역시
                RegionCode.of("30", "230"), // 유성구
                RegionCode.of("30", "200"), // 서구
                // ... 계속

                // 울산광역시
                RegionCode.of("31", "200"), // 중구
                RegionCode.of("31", "140"), // 남구
                // ... 계속

                // 세종특별자치시
                RegionCode.of("36", "110"), // 세종시

                // 경기도
                RegionCode.of("41", "820"), // 가평군
                RegionCode.of("41", "280"), // 고양시
                // ... 계속

                // 강원도
                RegionCode.of("42", "150"), // 강릉시
                RegionCode.of("42", "820"), // 고성군
                // ... 계속

                // 충청북도
                RegionCode.of("43", "720"), // 괴산군
                RegionCode.of("43", "740"), // 단양군
                // ... 계속

                // 충청남도
                RegionCode.of("44", "250"), // 공주시
                RegionCode.of("44", "710"), // 금산군
                // ... 계속

                // 전라북도
                RegionCode.of("45", "130"), // 군산시
                RegionCode.of("45", "180"), // 김제시
                // ... 계속

                // 전라남도
                RegionCode.of("46", "910"), // 강진군
                RegionCode.of("46", "230"), // 광양시
                // ... 계속

                // 경상북도
                RegionCode.of("47", "290"), // 경주시
                RegionCode.of("47", "130"), // 구미시
                // ... 계속

                // 경상남도
                RegionCode.of("48", "170"), // 거제시
                RegionCode.of("48", "120"), // 김해시
                // ... 계속

                // 제주특별자치도
                RegionCode.of("50", "110"), // 제주시
                RegionCode.of("50", "130")  // 서귀포시
        );
    }






}