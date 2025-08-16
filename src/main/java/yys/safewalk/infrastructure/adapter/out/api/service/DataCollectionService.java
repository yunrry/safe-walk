package yys.safewalk.infrastructure.adapter.out.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yys.safewalk.application.port.out.external.KoroadApiPort;
import yys.safewalk.application.port.out.external.dto.*;
import yys.safewalk.domain.riskarea.model.RegionCode;
import yys.safewalk.infrastructure.adapter.out.persistence.entity.*;
import yys.safewalk.infrastructure.adapter.out.persistence.repository.*;
import yys.safewalk.infrastructure.config.KoroadApiProperties;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 도로교통공단 API 데이터 수집 서비스 (기존 구조 활용)
 * KoroadApiPort를 통해 데이터를 수집하고 Entity로 변환하여 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectionService {

    private final KoroadApiPort koroadApiPort;
    private final KoroadApiProperties koroadApiProperties;

    private final PedestrianAccidentRepository pedestrianAccidentRepository;
    private final ElderlyPedestrianAccidentRepository elderlyPedestrianAccidentRepository;
    private final LocalGovernmentAccidentRepository localGovernmentAccidentRepository;
    private final HolidayAccidentRepository holidayAccidentRepository;
    private final AccidentStatisticsRepository accidentStatisticsRepository;
    private final RiskAreaRepository riskAreaRepository;

    private final EntityMapper entityMapper;

    /**
     * 전체 데이터 수집 실행
     */
    @Async("koroadBatchTaskExecutor")
    public CompletableFuture<DataCollectionResult> collectAllData() {
        log.info("=== 도로교통공단 API 전체 데이터 수집 시작 ===");

        DataCollectionResult result = new DataCollectionResult();
        result.setStartTime(LocalDateTime.now());

        try {
            // 1. 보행자 사고다발지역정보 수집
            log.info("보행자 사고다발지역정보 데이터 수집 시작");
            CollectionStats pedestrianStats = collectPedestrianAccidentData().get();
            result.setPedestrianAccidentStats(pedestrianStats);
            log.info("보행자 사고다발지역정보 수집 완료: {}", pedestrianStats);

            // 2. 보행노인 사고다발지역정보 수집
            log.info("보행노인 사고다발지역정보 데이터 수집 시작");
            CollectionStats elderlyStats = collectElderlyPedestrianAccidentData().get();
            result.setElderlyPedestrianAccidentStats(elderlyStats);
            log.info("보행노인 사고다발지역정보 수집 완료: {}", elderlyStats);

            // 3. 지자체별 사고다발지역정보 수집
            log.info("지자체별 사고다발지역정보 데이터 수집 시작");
            CollectionStats lgStats = collectLocalGovernmentAccidentData().get();
            result.setLocalGovernmentAccidentStats(lgStats);
            log.info("지자체별 사고다발지역정보 수집 완료: {}", lgStats);

            // 4. 연휴기간별 사고다발지역정보 수집
            log.info("연휴기간별 사고다발지역정보 데이터 수집 시작");
            CollectionStats holidayStats = collectHolidayAccidentData().get();
            result.setHolidayAccidentStats(holidayStats);
            log.info("연휴기간별 사고다발지역정보 수집 완료: {}", holidayStats);

            // 5. 지자체별 대상사고통계 수집
            log.info("지자체별 대상사고통계 데이터 수집 시작");
            CollectionStats statisticsStats = collectAccidentStatisticsData().get();
            result.setAccidentStatisticsStats(statisticsStats);
            log.info("지자체별 대상사고통계 수집 완료: {}", statisticsStats);

            // 6. 링크기반 사고위험지역정보 수집
            log.info("링크기반 사고위험지역정보 데이터 수집 시작");
            CollectionStats riskAreaStats = collectRiskAreaData().get();
            result.setRiskAreaStats(riskAreaStats);
            log.info("링크기반 사고위험지역정보 수집 완료: {}", riskAreaStats);

            result.setEndTime(LocalDateTime.now());
            result.setSuccess(true);
            result.calculateTotalStats();

            log.info("=== 도로교통공단 API 전체 데이터 수집 완료 ===");
            log.info("총 소요시간: {}분", Duration.between(result.getStartTime(), result.getEndTime()).toMinutes());
            log.info("총 수집 건수: {}", result.getTotalCollectedCount());
            log.info("총 중복 건수: {}", result.getTotalDuplicateCount());
            log.info("총 오류 건수: {}", result.getTotalErrorCount());

        } catch (Exception e) {
            result.setEndTime(LocalDateTime.now());
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("데이터 수집 중 오류 발생", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 보행자 사고다발지역정보 데이터 수집
     */
    @Async("koroadApiTaskExecutor")
    public CompletableFuture<CollectionStats> collectPedestrianAccidentData() {
        return CompletableFuture.supplyAsync(() -> {
            CollectionStats stats = new CollectionStats("보행자 사고다발지역정보");
            stats.setStartTime(LocalDateTime.now());

            try {
                List<RegionCode> regionCodes = getRegionCodes();
                List<String> years = List.of("2017", "2018", "2019", "2020", "2021", "2022", "2023");

                AtomicInteger processedCount = new AtomicInteger(0);
                int totalRegions = regionCodes.size() * years.size();

                log.info("보행자 사고다발지역정보 수집 - 총 {}개 지역, {}개 연도 처리 예정",
                        regionCodes.size(), years.size());

                for (RegionCode region : regionCodes) {
                    for (String year : years) {
                        try {
                            // Rate limiting
                            Thread.sleep(1000 / koroadApiProperties.getMaxRequestsPerSecond());

                            // 중복 체크
                            if (pedestrianAccidentRepository.isDataCollectedForRegion(
                                    region.getSiDo(), region.getGuGun(), year)) {
                                stats.incrementDuplicateCount();
                                continue;
                            }

                            // API 호출
                            SearchCriteria criteria = createSearchCriteria(region, year);
                            List<AccidentData> accidentDataList = koroadApiPort.getPedestrianAccidentData(criteria);

                            // 데이터 저장
                            SaveResult saveResult = savePedestrianAccidentData(accidentDataList, region, year);

                            if (saveResult.isSuccess()) {
                                stats.addCollectedCount(saveResult.getCollectedCount());
                                stats.addDuplicateCount(saveResult.getDuplicateCount());
                            } else {
                                stats.incrementErrorCount();
                                log.warn("보행자 사고 데이터 저장 실패 - 지역: {}, 연도: {}, 이유: {}",
                                        region.getSiDoName(), year, saveResult.getErrorMessage());
                            }

                            int processed = processedCount.incrementAndGet();
                            if (processed % 100 == 0) {
                                log.info("보행자 사고다발지역 진행률: {}/{} ({}%)",
                                        processed, totalRegions, (processed * 100 / totalRegions));
                            }

                        } catch (Exception e) {
                            stats.incrementErrorCount();
                            log.error("보행자 사고 데이터 수집 실패 - 지역: {}, 연도: {}",
                                    region.getSiDoName(), year, e);
                        }
                    }
                }

                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(true);

            } catch (Exception e) {
                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(false);
                stats.setErrorMessage(e.getMessage());
                log.error("보행자 사고다발지역정보 수집 실패", e);
            }

            return stats;
        });
    }

    /**
     * 보행노인 사고다발지역정보 데이터 수집
     */
    @Async("koroadApiTaskExecutor")
    public CompletableFuture<CollectionStats> collectElderlyPedestrianAccidentData() {
        return CompletableFuture.supplyAsync(() -> {
            CollectionStats stats = new CollectionStats("보행노인 사고다발지역정보");
            stats.setStartTime(LocalDateTime.now());

            try {
                List<RegionCode> regionCodes = getRegionCodes();
                List<String> years = List.of("2017", "2018", "2019", "2020", "2021", "2022", "2023");

                for (RegionCode region : regionCodes) {
                    for (String year : years) {
                        try {
                            Thread.sleep(1000 / koroadApiProperties.getMaxRequestsPerSecond());

                            if (elderlyPedestrianAccidentRepository.isElderlyDataCollectedForRegion(
                                    region.getSiDo(), region.getGuGun(), year)) {
                                stats.incrementDuplicateCount();
                                continue;
                            }

                            SearchCriteria criteria = createSearchCriteria(region, year);
                            List<AccidentData> accidentDataList = koroadApiPort.getElderlyPedestrianAccidentData(criteria);

                            SaveResult saveResult = saveElderlyPedestrianAccidentData(accidentDataList, region, year);

                            if (saveResult.isSuccess()) {
                                stats.addCollectedCount(saveResult.getCollectedCount());
                                stats.addDuplicateCount(saveResult.getDuplicateCount());
                            } else {
                                stats.incrementErrorCount();
                            }

                        } catch (Exception e) {
                            stats.incrementErrorCount();
                            log.error("보행노인 사고 데이터 수집 실패 - 지역: {}, 연도: {}",
                                    region.getSiDoName(), year, e);
                        }
                    }
                }

                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(true);

            } catch (Exception e) {
                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(false);
                stats.setErrorMessage(e.getMessage());
                log.error("보행노인 사고다발지역정보 수집 실패", e);
            }

            return stats;
        });
    }

    /**
     * 지자체별 사고다발지역정보 데이터 수집
     */
    @Async("koroadApiTaskExecutor")
    public CompletableFuture<CollectionStats> collectLocalGovernmentAccidentData() {
        return CompletableFuture.supplyAsync(() -> {
            CollectionStats stats = new CollectionStats("지자체별 사고다발지역정보");
            stats.setStartTime(LocalDateTime.now());

            try {
                List<RegionCode> regionCodes = getRegionCodes();
                List<String> years = List.of("2017", "2018", "2019", "2020", "2021", "2022", "2023");

                for (RegionCode region : regionCodes) {
                    for (String year : years) {
                        try {
                            Thread.sleep(1000 / koroadApiProperties.getMaxRequestsPerSecond());

                            if (localGovernmentAccidentRepository.isDataCollectedForRegion(
                                    region.getSiDo(), region.getGuGun(), year)) {
                                stats.incrementDuplicateCount();
                                continue;
                            }

                            SearchCriteria criteria = createSearchCriteria(region, year);
                            List<AccidentData> accidentDataList = koroadApiPort.getLocalGovernmentAccidentData(criteria);

                            SaveResult saveResult = saveLocalGovernmentAccidentData(accidentDataList, region, year);

                            if (saveResult.isSuccess()) {
                                stats.addCollectedCount(saveResult.getCollectedCount());
                                stats.addDuplicateCount(saveResult.getDuplicateCount());
                            } else {
                                stats.incrementErrorCount();
                            }

                        } catch (Exception e) {
                            stats.incrementErrorCount();
                            log.error("지자체별 사고 데이터 수집 실패 - 지역: {}, 연도: {}",
                                    region.getSiDoName(), year, e);
                        }
                    }
                }

                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(true);

            } catch (Exception e) {
                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(false);
                stats.setErrorMessage(e.getMessage());
                log.error("지자체별 사고다발지역정보 수집 실패", e);
            }

            return stats;
        });
    }

    /**
     * 연휴기간별 사고다발지역정보 데이터 수집
     */
    @Async("koroadApiTaskExecutor")
    public CompletableFuture<CollectionStats> collectHolidayAccidentData() {
        return CompletableFuture.supplyAsync(() -> {
            CollectionStats stats = new CollectionStats("연휴기간별 사고다발지역정보");
            stats.setStartTime(LocalDateTime.now());

            try {
                List<RegionCode> regionCodes = getRegionCodes();
                List<String> years = List.of("2017", "2018", "2019", "2020", "2021", "2022", "2023");

                for (RegionCode region : regionCodes) {
                    for (String year : years) {
                        try {
                            Thread.sleep(1000 / koroadApiProperties.getMaxRequestsPerSecond());

                            if (holidayAccidentRepository.isHolidayDataCollectedForRegion(
                                    region.getSiDo(), region.getGuGun(), year)) {
                                stats.incrementDuplicateCount();
                                continue;
                            }

                            SearchCriteria criteria = createSearchCriteria(region, year);
                            List<AccidentData> accidentDataList = koroadApiPort.getHolidayAccidentData(criteria);

                            SaveResult saveResult = saveHolidayAccidentData(accidentDataList, region, year);

                            if (saveResult.isSuccess()) {
                                stats.addCollectedCount(saveResult.getCollectedCount());
                                stats.addDuplicateCount(saveResult.getDuplicateCount());
                            } else {
                                stats.incrementErrorCount();
                            }

                        } catch (Exception e) {
                            stats.incrementErrorCount();
                            log.error("연휴기간별 사고 데이터 수집 실패 - 지역: {}, 연도: {}",
                                    region.getSiDoName(), year, e);
                        }
                    }
                }

                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(true);

            } catch (Exception e) {
                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(false);
                stats.setErrorMessage(e.getMessage());
                log.error("연휴기간별 사고다발지역정보 수집 실패", e);
            }

            return stats;
        });
    }

    /**
     * 지자체별 대상사고통계 데이터 수집
     */
    @Async("koroadApiTaskExecutor")
    public CompletableFuture<CollectionStats> collectAccidentStatisticsData() {
        return CompletableFuture.supplyAsync(() -> {
            CollectionStats stats = new CollectionStats("지자체별 대상사고통계");
            stats.setStartTime(LocalDateTime.now());

            try {
                List<RegionCode> regionCodes = getRegionCodes();
                List<String> years = List.of("2017", "2018", "2019", "2020", "2021", "2022", "2023");

                for (RegionCode region : regionCodes) {
                    for (String year : years) {
                        try {
                            Thread.sleep(1000 / koroadApiProperties.getMaxRequestsPerSecond());

                            if (accidentStatisticsRepository.existsBySidoCdAndGugunCdAndSearchYearCd(
                                    region.getSiDo(), region.getGuGun(), year)) {
                                stats.incrementDuplicateCount();
                                continue;
                            }

                            SearchCriteria criteria = createSearchCriteria(region, year);
                            List<AccidentStatisticsData> statisticsDataList = koroadApiPort.getAccidentStatistics(criteria);

                            SaveResult saveResult = saveAccidentStatisticsData(statisticsDataList, region, year);

                            if (saveResult.isSuccess()) {
                                stats.addCollectedCount(saveResult.getCollectedCount());
                                stats.addDuplicateCount(saveResult.getDuplicateCount());
                            } else {
                                stats.incrementErrorCount();
                            }

                        } catch (Exception e) {
                            stats.incrementErrorCount();
                            log.error("지자체별 대상사고통계 수집 실패 - 지역: {}, 연도: {}",
                                    region.getSiDoName(), year, e);
                        }
                    }
                }

                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(true);

            } catch (Exception e) {
                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(false);
                stats.setErrorMessage(e.getMessage());
                log.error("지자체별 대상사고통계 수집 실패", e);
            }

            return stats;
        });
    }

    /**
     * 링크기반 사고위험지역정보 데이터 수집
     */
    @Async("koroadApiTaskExecutor")
    public CompletableFuture<CollectionStats> collectRiskAreaData() {
        return CompletableFuture.supplyAsync(() -> {
            CollectionStats stats = new CollectionStats("링크기반 사고위험지역정보");
            stats.setStartTime(LocalDateTime.now());

            try {
                List<RegionCode> regionCodes = getRegionCodes();
                List<String> years = List.of("2017", "2018", "2019", "2020", "2021", "2022", "2023");

                for (RegionCode region : regionCodes) {
                    for (String year : years) {
                        try {
                            Thread.sleep(1000 / koroadApiProperties.getMaxRequestsPerSecond());

                            if (riskAreaRepository.isLinkDataCollectedForRegion(
                                    region.getSiDo(), region.getGuGun(), year)) {
                                stats.incrementDuplicateCount();
                                continue;
                            }

                            SearchCriteria criteria = createSearchCriteria(region, year);
                            List<RiskAreaData> riskAreaDataList = koroadApiPort.getLinkBasedRiskAreaData(criteria);

                            SaveResult saveResult = saveRiskAreaData(riskAreaDataList, region, year);

                            if (saveResult.isSuccess()) {
                                stats.addCollectedCount(saveResult.getCollectedCount());
                                stats.addDuplicateCount(saveResult.getDuplicateCount());
                            } else {
                                stats.incrementErrorCount();
                            }

                        } catch (Exception e) {
                            stats.incrementErrorCount();
                            log.error("링크기반 사고위험지역 데이터 수집 실패 - 지역: {}, 연도: {}",
                                    region.getSiDoName(), year, e);
                        }
                    }
                }

                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(true);

            } catch (Exception e) {
                stats.setEndTime(LocalDateTime.now());
                stats.setSuccess(false);
                stats.setErrorMessage(e.getMessage());
                log.error("링크기반 사고위험지역정보 수집 실패", e);
            }

            return stats;
        });
    }

    // ===== 데이터 저장 메서드들 =====

    /**
     * 보행자 사고 데이터 저장
     */
    @Transactional
    private SaveResult savePedestrianAccidentData(List<AccidentData> accidentDataList, RegionCode region, String year) {
        SaveResult result = new SaveResult();

        try {
            if (accidentDataList == null || accidentDataList.isEmpty()) {
                result.setSuccess(true);
                return result;
            }

            int collectedCount = 0;
            int duplicateCount = 0;

            for (AccidentData accidentData : accidentDataList) {
                // 중복 체크
                if (pedestrianAccidentRepository.existsByAfosIdAndSearchYearCd(accidentData.getAfosId(), year)) {
                    duplicateCount++;
                    continue;
                }

                // Entity 변환 및 저장
                PedestrianAccidentEntity entity = entityMapper.toPedestrianAccidentEntity(accidentData, region, year);
                pedestrianAccidentRepository.save(entity);
                collectedCount++;
            }

            result.setSuccess(true);
            result.setCollectedCount(collectedCount);
            result.setDuplicateCount(duplicateCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("보행자 사고 데이터 저장 실패", e);
        }

        return result;
    }

    /**
     * 보행노인 사고 데이터 저장
     */
    @Transactional
    private SaveResult saveElderlyPedestrianAccidentData(List<AccidentData> accidentDataList, RegionCode region, String year) {
        SaveResult result = new SaveResult();

        try {
            if (accidentDataList == null || accidentDataList.isEmpty()) {
                result.setSuccess(true);
                return result;
            }

            int collectedCount = 0;
            int duplicateCount = 0;

            for (AccidentData accidentData : accidentDataList) {
                if (elderlyPedestrianAccidentRepository.existsByAfosIdAndSearchYearCd(accidentData.getAfosId(), year)) {
                    duplicateCount++;
                    continue;
                }

                ElderlyPedestrianAccidentEntity entity = entityMapper.toElderlyPedestrianAccidentEntity(accidentData, region, year);
                elderlyPedestrianAccidentRepository.save(entity);
                collectedCount++;
            }

            result.setSuccess(true);
            result.setCollectedCount(collectedCount);
            result.setDuplicateCount(duplicateCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("보행노인 사고 데이터 저장 실패", e);
        }

        return result;
    }

    /**
     * 지자체별 사고 데이터 저장
     */
    @Transactional
    private SaveResult saveLocalGovernmentAccidentData(List<AccidentData> accidentDataList, RegionCode region, String year) {
        SaveResult result = new SaveResult();

        try {
            if (accidentDataList == null || accidentDataList.isEmpty()) {
                result.setSuccess(true);
                return result;
            }

            int collectedCount = 0;
            int duplicateCount = 0;

            for (AccidentData accidentData : accidentDataList) {
                if (localGovernmentAccidentRepository.existsByAfosIdAndSearchYearCd(accidentData.getAfosId(), year)) {
                    duplicateCount++;
                    continue;
                }

                LocalGovernmentAccidentEntity entity = entityMapper.toLocalGovernmentAccidentEntity(accidentData, region, year);
                localGovernmentAccidentRepository.save(entity);
                collectedCount++;
            }

            result.setSuccess(true);
            result.setCollectedCount(collectedCount);
            result.setDuplicateCount(duplicateCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("지자체별 사고 데이터 저장 실패", e);
        }

        return result;
    }

    /**
     * 연휴기간별 사고 데이터 저장
     */
    @Transactional
    private SaveResult saveHolidayAccidentData(List<AccidentData> accidentDataList, RegionCode region, String year) {
        SaveResult result = new SaveResult();

        try {
            if (accidentDataList == null || accidentDataList.isEmpty()) {
                result.setSuccess(true);
                return result;
            }

            int collectedCount = 0;
            int duplicateCount = 0;

            for (AccidentData accidentData : accidentDataList) {
                if (holidayAccidentRepository.existsByAfosIdAndSearchYearCd(accidentData.getAfosId(), year)) {
                    duplicateCount++;
                    continue;
                }

                HolidayAccidentEntity entity = entityMapper.toHolidayAccidentEntity(accidentData, region, year);
                holidayAccidentRepository.save(entity);
                collectedCount++;
            }

            result.setSuccess(true);
            result.setCollectedCount(collectedCount);
            result.setDuplicateCount(duplicateCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("연휴기간별 사고 데이터 저장 실패", e);
        }

        return result;
    }

    /**
     * 지자체별 대상사고통계 데이터 저장
     */
    @Transactional
    private SaveResult saveAccidentStatisticsData(List<AccidentStatisticsData> statisticsDataList, RegionCode region, String year) {
        SaveResult result = new SaveResult();

        try {
            if (statisticsDataList == null || statisticsDataList.isEmpty()) {
                result.setSuccess(true);
                return result;
            }

            int collectedCount = 0;
            int duplicateCount = 0;

            for (AccidentStatisticsData statisticsData : statisticsDataList) {
                if (accidentStatisticsRepository.existsBySidoCdAndGugunCdAndSearchYearCd(
                        region.getSiDo(), region.getGuGun(), year)) {
                    duplicateCount++;
                    continue;
                }

                AccidentStatisticsEntity entity = entityMapper.toAccidentStatisticsEntity(statisticsData, region, year);
                accidentStatisticsRepository.save(entity);
                collectedCount++;
            }

            result.setSuccess(true);
            result.setCollectedCount(collectedCount);
            result.setDuplicateCount(duplicateCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("지자체별 대상사고통계 데이터 저장 실패", e);
        }

        return result;
    }
}