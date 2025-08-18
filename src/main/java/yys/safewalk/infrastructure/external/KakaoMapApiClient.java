package yys.safewalk.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class KakaoMapApiClient {

    private final WebClient webClient;

    @Value("${kakao.api.key}")
    private String apiKey;

    public KakaoMapApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("KA", "sdk/1.0 os/web origin/localhost")
                .build();
    }

    public Optional<KakaoLocationResponse.Document> searchLocation(String query) {
        try {
            KakaoLocationResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .bodyToMono(KakaoLocationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null &&
                    response.documents() != null &&
                    !response.documents().isEmpty()) {

                log.debug("검색 성공: {} -> 결과 {}개", query, response.documents().size());
                return Optional.of(response.documents().get(0));
            }

            log.warn("검색 결과 없음: {}", query);
            return Optional.empty();

        } catch (Exception e) {
            log.error("카카오 API 호출 실패: query={}, error={}", query, e.getMessage());
            return Optional.empty();
        }
    }

    public record KakaoLocationResponse(
            List<Document> documents
    ) {
        public record Document(
                String x, // longitude
                String y, // latitude
                String placeName,
                String addressName
        ) {
            public BigDecimal getLongitude() {
                return new BigDecimal(x);
            }

            public BigDecimal getLatitude() {
                return new BigDecimal(y);
            }
        }
    }
}