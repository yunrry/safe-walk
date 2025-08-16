package yys.safewalk.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executor;

/**
 * 도로교통공단 API 설정
 * API 호출 관련 빈들과 스케줄링 설정
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class KoroadApiConfig {

    private final KoroadApiProperties koroadApiProperties;
    private final WebClientConfig webClientConfig;

    /**
     * 도로교통공단 API 전용 WebClient
     */
    @Bean(name = "koroadApiWebClient")
    public WebClient koroadApiWebClient() {
        return webClientConfig.koroadWebClient();
    }

    /**
     * Rate Limiting이 적용된 도로교통공단 API WebClient
     */
    @Bean(name = "rateLimitedKoroadApiWebClient")
    public WebClient rateLimitedKoroadApiWebClient() {
        return webClientConfig.rateLimitedKoroadWebClient();
    }

    /**
     * API 데이터 수집용 스레드 풀
     */
    @Bean(name = "koroadApiTaskExecutor")
    public Executor koroadApiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 기본 스레드 수
        executor.setMaxPoolSize(20);  // 최대 스레드 수
        executor.setQueueCapacity(100); // 큐 용량
        executor.setThreadNamePrefix("KoroadAPI-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("도로교통공단 API 스레드 풀 초기화 완료 - Core: {}, Max: {}, Queue: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * API 배치 처리용 스레드 풀 (대량 데이터 수집용)
     */
    @Bean(name = "koroadBatchTaskExecutor")
    public Executor koroadBatchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);   // 배치는 적은 수의 스레드로
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("KoroadBatch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("도로교통공단 배치 스레드 풀 초기화 완료 - Core: {}, Max: {}, Queue: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * 보행자 사고다발지역정보 API 클라이언트
     */
    @Bean
    public KoroadApiClient pedestrianAccidentApiClient() {
        return KoroadApiClient.builder()
                .webClient(koroadApiWebClient())
                .baseUrl(koroadApiProperties.getBaseUrl())
                .endpoint(koroadApiProperties.getEndpoints().getPedestrianAccident())
                .apiKey(koroadApiProperties.getApiKey())
                .defaultPageSize(koroadApiProperties.getDefaultPageSize())
                .maxPageSize(koroadApiProperties.getMaxPageSize())
                .apiType("PEDESTRIAN_ACCIDENT")
                .build();
    }

    /**
     * 보행노인 사고다발지역정보 API 클라이언트
     */
    @Bean
    public KoroadApiClient elderlyPedestrianAccidentApiClient() {
        return KoroadApiClient.builder()
                .webClient(koroadApiWebClient())
                .baseUrl(koroadApiProperties.getBaseUrl())
                .endpoint(koroadApiProperties.getEndpoints().getElderlyPedestrianAccident())
                .apiKey(koroadApiProperties.getApiKey())
                .defaultPageSize(koroadApiProperties.getDefaultPageSize())
                .maxPageSize(koroadApiProperties.getMaxPageSize())
                .apiType("ELDERLY_PEDESTRIAN_ACCIDENT")
                .build();
    }

    /**
     * 지자체별 사고다발지역정보 API 클라이언트
     */
    @Bean
    public KoroadApiClient localGovernmentAccidentApiClient() {
        return KoroadApiClient.builder()
                .webClient(koroadApiWebClient())
                .baseUrl(koroadApiProperties.getBaseUrl())
                .endpoint(koroadApiProperties.getEndpoints().getLocalGovernmentAccident())
                .apiKey(koroadApiProperties.getApiKey())
                .defaultPageSize(koroadApiProperties.getDefaultPageSize())
                .maxPageSize(koroadApiProperties.getMaxPageSize())
                .apiType("LOCAL_GOVERNMENT_ACCIDENT")
                .build();
    }

    /**
     * 연휴기간별 사고다발지역정보 API 클라이언트
     */
    @Bean
    public KoroadApiClient holidayAccidentApiClient() {
        return KoroadApiClient.builder()
                .webClient(koroadApiWebClient())
                .baseUrl(koroadApiProperties.getBaseUrl())
                .endpoint(koroadApiProperties.getEndpoints().getHolidayAccident())
                .apiKey(koroadApiProperties.getApiKey())
                .defaultPageSize(koroadApiProperties.getDefaultPageSize())
                .maxPageSize(koroadApiProperties.getMaxPageSize())
                .apiType("HOLIDAY_ACCIDENT")
                .build();
    }

    /**
     * 지자체별 대상사고통계 API 클라이언트
     */
    @Bean
    public KoroadApiClient accidentStatisticsApiClient() {
        return KoroadApiClient.builder()
                .webClient(koroadApiWebClient())
                .baseUrl(koroadApiProperties.getBaseUrl())
                .endpoint(koroadApiProperties.getEndpoints().getAccidentStatistics())
                .apiKey(koroadApiProperties.getApiKey())
                .defaultPageSize(koroadApiProperties.getDefaultPageSize())
                .maxPageSize(koroadApiProperties.getMaxPageSize())
                .apiType("ACCIDENT_STATISTICS")
                .build();
    }

    /**
     * 링크기반 사고위험지역정보 API 클라이언트
     */
    @Bean
    public KoroadApiClient linkBasedRiskAreaApiClient() {
        return KoroadApiClient.builder()
                .webClient(koroadApiWebClient())
                .baseUrl(koroadApiProperties.getBaseUrl())
                .endpoint(koroadApiProperties.getEndpoints().getLinkBasedRiskArea())
                .apiKey(koroadApiProperties.getApiKey())
                .defaultPageSize(koroadApiProperties.getDefaultPageSize())
                .maxPageSize(koroadApiProperties.getMaxPageSize())
                .apiType("LINK_BASED_RISK_AREA")
                .build();
    }

    /**
     * API 클라이언트 헬퍼 클래스
     */
    @lombok.Builder
    @lombok.Getter
    @lombok.ToString
    public static class KoroadApiClient {
        private final WebClient webClient;
        private final String baseUrl;
        private final String endpoint;
        private final String apiKey;
        private final int defaultPageSize;
        private final int maxPageSize;
        private final String apiType;

        /**
         * 완전한 API URL 생성
         */
        public String getFullUrl() {
            return baseUrl + endpoint;
        }

        /**
         * API 유형별 설명 반환
         */
        public String getDescription() {
            switch (apiType) {
                case "PEDESTRIAN_ACCIDENT":
                    return "보행자 사고다발지역정보 API";
                case "ELDERLY_PEDESTRIAN_ACCIDENT":
                    return "보행노인 사고다발지역정보 API";
                case "LOCAL_GOVERNMENT_ACCIDENT":
                    return "지자체별 사고다발지역정보 API";
                case "HOLIDAY_ACCIDENT":
                    return "연휴기간별 사고다발지역정보 API";
                case "ACCIDENT_STATISTICS":
                    return "지자체별 대상사고통계 API";
                case "LINK_BASED_RISK_AREA":
                    return "링크기반 사고위험지역정보 API";
                default:
                    return "도로교통공단 API";
            }
        }

        /**
         * API 호출 가능 여부 확인
         */
        public boolean isAvailable() {
            return webClient != null &&
                    baseUrl != null && !baseUrl.isEmpty() &&
                    endpoint != null && !endpoint.isEmpty() &&
                    apiKey != null && !apiKey.isEmpty();
        }

        /**
         * 페이지 크기 검증 및 조정
         */
        public int validatePageSize(int requestedPageSize) {
            if (requestedPageSize <= 0) {
                return defaultPageSize;
            }
            return Math.min(requestedPageSize, maxPageSize);
        }
    }

    /**
     * API 상태 모니터링 빈
     */
    @Bean
    public KoroadApiHealthIndicator koroadApiHealthIndicator() {
        return new KoroadApiHealthIndicator(
                koroadApiWebClient(),
                koroadApiProperties,
                webClientConfig
        );
    }

    /**
     * API 헬스 체크 구현
     */
    public static class KoroadApiHealthIndicator {
        private final WebClient webClient;
        private final KoroadApiProperties properties;
        private final WebClientConfig webClientConfig;

        public KoroadApiHealthIndicator(WebClient webClient,
                                        KoroadApiProperties properties,
                                        WebClientConfig webClientConfig) {
            this.webClient = webClient;
            this.properties = properties;
            this.webClientConfig = webClientConfig;
        }

        /**
         * API 연결 상태 확인
         */
        public boolean isHealthy() {
            try {
                return webClientConfig.healthCheck().block();
            } catch (Exception e) {
                log.warn("도로교통공단 API 헬스체크 실패: {}", e.getMessage());
                return false;
            }
        }

        /**
         * API 설정 검증
         */
        public boolean isConfigValid() {
            return properties.getApiKey() != null && !properties.getApiKey().isEmpty() &&
                    properties.getBaseUrl() != null && !properties.getBaseUrl().isEmpty() &&
                    properties.getConnectTimeout() != null &&
                    properties.getReadTimeout() != null;
        }
    }

    /**
     * API 설정 정보 로깅
     */
    @jakarta.annotation.PostConstruct
    public void logConfiguration() {
        log.info("=== 도로교통공단 API 설정 정보 ===");
        log.info("Base URL: {}", koroadApiProperties.getBaseUrl());
        log.info("Connect Timeout: {}ms", koroadApiProperties.getConnectTimeout().toMillis());
        log.info("Read Timeout: {}ms", koroadApiProperties.getReadTimeout().toMillis());
        log.info("Max Retry Attempts: {}", koroadApiProperties.getMaxRetryAttempts());
        log.info("Default Page Size: {}", koroadApiProperties.getDefaultPageSize());
        log.info("Max Page Size: {}", koroadApiProperties.getMaxPageSize());
        log.info("Max Requests Per Second: {}", koroadApiProperties.getMaxRequestsPerSecond());

        log.info("=== API 엔드포인트 목록 ===");
        log.info("보행자 사고다발지역: {}", koroadApiProperties.getPedestrianAccidentUrl());
        log.info("보행노인 사고다발지역: {}", koroadApiProperties.getElderlyPedestrianAccidentUrl());
        log.info("지자체별 사고다발지역: {}", koroadApiProperties.getLocalGovernmentAccidentUrl());
        log.info("연휴기간별 사고다발지역: {}", koroadApiProperties.getHolidayAccidentUrl());
        log.info("지자체별 대상사고통계: {}", koroadApiProperties.getAccidentStatisticsUrl());
        log.info("링크기반 사고위험지역: {}", koroadApiProperties.getLinkBasedRiskAreaUrl());

        // API 키는 보안상 마스킹 처리
        String maskedApiKey = koroadApiProperties.getApiKey() != null ?
                koroadApiProperties.getApiKey().substring(0, Math.min(8, koroadApiProperties.getApiKey().length())) + "****"
                : "NULL";
        log.info("API Key: {}", maskedApiKey);

        log.info("=== 도로교통공단 API 설정 완료 ===");
    }
}