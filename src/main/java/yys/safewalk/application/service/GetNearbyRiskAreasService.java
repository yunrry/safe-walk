package yys.safewalk.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yys.safewalk.application.port.in.GetNearbyRiskAreasUseCase;
import yys.safewalk.application.port.in.dto.RiskAreaDto;

import java.util.List;

// Application Service에서 사용
@Service
@RequiredArgsConstructor
public class GetNearbyRiskAreasService implements GetNearbyRiskAreasUseCase {

//    // Domain Port 의존
//    private final RiskAreaRepository riskAreaRepository;  // domain/riskarea/port/
//
//    // Application Port 의존
//    private final CachePort cachePort;                    // application/port/out/cache/
//    private final KoroadApiPort koroadApiPort;           // application/port/out/external/
//
//    @Override
//    public List<RiskAreaDto> getNearbyRiskAreas(Location location, int radius) {
//        // 1. 캐시 확인
//        Optional<List<RiskAreaDto>> cached = cachePort.get(
//                generateCacheKey(location, radius),
//                List.class
//        );
//
//        if (cached.isPresent()) {
//            return cached.get();
//        }
//
//        // 2. 도메인 레포지토리에서 조회
//        List<RiskArea> riskAreas = riskAreaRepository.findNearbyRiskAreas(location, radius);
//
//        // 3. 외부 API에서 최신 정보 보강
//        SearchCriteria criteria = SearchCriteria.of(location, radius);
//        List<AccidentData> recentData = koroadApiPort.getPedestrianAccidentData(criteria);
//
//        // 4. 도메인 로직 적용 및 DTO 변환
//        List<RiskAreaDto> result = riskAreas.stream()
//                .map(RiskAreaDto::from)
//                .collect(Collectors.toList());
//
//        // 5. 캐시에 저장
//        cachePort.put(generateCacheKey(location, radius), result, Duration.ofMinutes(10));
//
//        return result;
//
//    }

    @Override
    public List<RiskAreaDto> getNearbyRiskAreas(yys.safewalk.domain.riskarea.model.Location location, int radius) {
        return List.of();
    }
}