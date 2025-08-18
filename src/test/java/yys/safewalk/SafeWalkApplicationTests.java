package yys.safewalk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import yys.safewalk.application.service.TouristSpotCoordinateService;
import yys.safewalk.infrastructure.external.KakaoMapApiClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.main.lazy-initialization=true"
})
class SafeWalkApplicationTests {

	@Test
	void contextLoads() {
		// 기본적인 애플리케이션 컨텍스트 로딩 테스트
	}
}
