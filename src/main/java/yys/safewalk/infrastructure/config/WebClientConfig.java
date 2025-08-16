// infrastructure/config/WebClientConfig.java
package yys.safewalk.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final KoroadApiProperties koroadApiProperties;

    /**
     * 도로교통공단 API 전용 WebClient
     */
    @Bean(name = "koroadWebClient")
    public WebClient koroadWebClient() {
        return WebClient.builder()
                .baseUrl(koroadApiProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .defaultHeaders(this::setDefaultHeaders)
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleError())
                .codecs(configurer -> {
                    // 응답 크기 제한 설정 (10MB)
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                })
                .build();
    }

    /**
     * 일반 용도 WebClient (다른 외부 API용)
     */
    @Bean(name = "generalWebClient")
    public WebClient generalWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .defaultHeaders(this::setGeneralHeaders)
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleError())
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024);
                })
                .build();
    }

    /**
     * HTTP 클라이언트 설정
     */
    private HttpClient createHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) koroadApiProperties.getConnectTimeout().toMillis())
                .responseTimeout(koroadApiProperties.getReadTimeout())
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(
                            koroadApiProperties.getReadTimeout().toSeconds(), TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(
                            koroadApiProperties.getConnectTimeout().toSeconds(), TimeUnit.SECONDS));
                })
                // 커넥션 풀 설정
                .keepAlive(true)
                .compress(true);
    }

    /**
     * 도로교통공단 API 기본 헤더 설정
     */
    private void setDefaultHeaders(HttpHeaders headers) {
        headers.set(HttpHeaders.USER_AGENT, koroadApiProperties.getUserAgent());
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT_CHARSET, "UTF-8");
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        // API 키는 쿼리 파라미터로 전달되므로 헤더에는 추가하지 않음
    }

    /**
     * 일반 웹클라이언트 기본 헤더 설정
     */
    private void setGeneralHeaders(HttpHeaders headers) {
        headers.set(HttpHeaders.USER_AGENT, "SafeWalk-Service/1.0");
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 요청 로깅 필터
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) ->
                        values.forEach(value -> log.debug("Request Header: {}={}", name, value)));
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * 응답 로깅 필터
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("Response Status: {}", clientResponse.statusCode());
                clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                        values.forEach(value -> log.debug("Response Header: {}={}", name, value)));
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * 에러 처리 필터
     */
    private ExchangeFilterFunction handleError() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("Unknown error")
                        .flatMap(errorBody -> {
                            log.error("API Error - Status: {}, Body: {}",
                                    clientResponse.statusCode(), errorBody);
                            return Mono.just(clientResponse);
                        });
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Rate Limiting 적용을 위한 WebClient (필요시 사용)
     */
    @Bean(name = "rateLimitedKoroadWebClient")
    public WebClient rateLimitedKoroadWebClient() {
        return WebClient.builder()
                .baseUrl(koroadApiProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .defaultHeaders(this::setDefaultHeaders)
                .filter(rateLimitingFilter())
                .filter(retryFilter())
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleError())
                .build();
    }

    /**
     * Rate Limiting 필터
     */
    private ExchangeFilterFunction rateLimitingFilter() {
        return (request, next) -> {
            // 실제 구현시에는 Redis나 별도 Rate Limiter 라이브러리 사용
            // 여기서는 간단한 delay로 대체
            return Mono.delay(Duration.ofMillis(
                            1000 / koroadApiProperties.getMaxRequestsPerSecond()))
                    .then(next.exchange(request));
        };
    }

    /**
     * 재시도 필터
     */
    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> {
            return next.exchange(request)
                    .retryWhen(Retry.backoff(
                                    koroadApiProperties.getMaxRetryAttempts(),
                                    koroadApiProperties.getRetryInterval())
                            .filter(throwable -> {
                                // 재시도할 예외 타입 결정
                                return throwable instanceof java.net.ConnectException ||
                                        throwable instanceof java.util.concurrent.TimeoutException ||
                                        (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException &&
                                                ((org.springframework.web.reactive.function.client.WebClientResponseException) throwable)
                                                        .getStatusCode().is5xxServerError());
                            })
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                log.error("API 호출 재시도 횟수 초과: {}", retrySignal.totalRetries());
                                return retrySignal.failure();
                            }));
        };
    }

    /**
     * 캐시 가능한 요청인지 확인하는 헬퍼 메서드
     */
    public boolean isCacheableRequest(String uri) {
        // GET 요청이고 특정 패턴에 해당하는 경우만 캐시
        return uri.contains("/frequentzone/") ||
                uri.contains("/stt") ||
                uri.contains("/accident/riskArea");
    }

    /**
     * WebClient 헬스체크
     */
    public Mono<Boolean> healthCheck() {
        return koroadWebClient()
                .get()
                .uri("/")
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false)
                .timeout(Duration.ofSeconds(5));
    }
}