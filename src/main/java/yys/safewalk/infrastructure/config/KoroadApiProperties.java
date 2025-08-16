// infrastructure/config/KoroadApiProperties.java
package yys.safewalk.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "external.koroad")
public class KoroadApiProperties {

    /**
     * 도로교통공단 OpenAPI 인증키
     */
    @NotBlank(message = "Koroad API key is required")
    private String apiKey;

    /**
     * API 기본 URL
     */
    @NotBlank(message = "Koroad API base URL is required")
    private String baseUrl = "https://opendata.koroad.or.kr/data/rest";

    /**
     * 연결 타임아웃 (초)
     */
    @NotNull
    @Positive
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * 읽기 타임아웃 (초)
     */
    @NotNull
    @Positive
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * 최대 재시도 횟수
     */
    @Positive
    private int maxRetryAttempts = 3;

    /**
     * 재시도 간격 (초)
     */
    @NotNull
    @Positive
    private Duration retryInterval = Duration.ofSeconds(1);

    /**
     * 기본 페이지 크기
     */
    @Positive
    private int defaultPageSize = 100;

    /**
     * 최대 페이지 크기
     */
    @Positive
    private int maxPageSize = 1000;

    /**
     * Rate limiting: 초당 최대 요청 수
     */
    @Positive
    private int maxRequestsPerSecond = 10;

    /**
     * User-Agent 헤더
     */
    private String userAgent = "Tourism-Safety-Service/1.0";

    /**
     * API 엔드포인트 경로들
     */
    private final Endpoints endpoints = new Endpoints();

    @Data
    public static class Endpoints {
        /**
         * 보행자 사고다발지역정보 API
         */
        private String pedestrianAccident = "/frequentzone/pedstrians";

        /**
         * 보행노인 사고다발지역정보 API
         */
        private String elderlyPedestrianAccident = "/frequentzone/oldman";

        /**
         * 지자체별 사고다발지역정보 API
         */
        private String localGovernmentAccident = "/frequentzone/lg";

        /**
         * 연휴기간별 사고다발지역정보 API
         */
        private String holidayAccident = "/frequentzone/tmzon";

        /**
         * 지자체별 대상사고통계 API
         */
        private String accidentStatistics = "/stt";

        /**
         * 링크기반 사고위험지역정보 API
         */
        private String linkBasedRiskArea = "/accident/riskArea";

        /**
         * 세부링크 도로위험지수정보 API
         */
        private String roadRiskIndex = "/road/dgdgr/link";
    }

    /**
     * 완전한 API URL 생성
     */
    public String getFullUrl(String endpoint) {
        return baseUrl + endpoint;
    }

    /**
     * 보행자 사고다발지역 API URL
     */
    public String getPedestrianAccidentUrl() {
        return getFullUrl(endpoints.getPedestrianAccident());
    }

    /**
     * 보행노인 사고다발지역 API URL
     */
    public String getElderlyPedestrianAccidentUrl() {
        return getFullUrl(endpoints.getElderlyPedestrianAccident());
    }

    /**
     * 지자체별 사고다발지역 API URL
     */
    public String getLocalGovernmentAccidentUrl() {
        return getFullUrl(endpoints.getLocalGovernmentAccident());
    }

    /**
     * 연휴기간별 사고다발지역 API URL
     */
    public String getHolidayAccidentUrl() {
        return getFullUrl(endpoints.getHolidayAccident());
    }

    /**
     * 지자체별 대상사고통계 API URL
     */
    public String getAccidentStatisticsUrl() {
        return getFullUrl(endpoints.getAccidentStatistics());
    }

    /**
     * 링크기반 사고위험지역 API URL
     */
    public String getLinkBasedRiskAreaUrl() {
        return getFullUrl(endpoints.getLinkBasedRiskArea());
    }

    /**
     * 세부링크 도로위험지수 API URL
     */
    public String getRoadRiskIndexUrl() {
        return getFullUrl(endpoints.getRoadRiskIndex());
    }
}