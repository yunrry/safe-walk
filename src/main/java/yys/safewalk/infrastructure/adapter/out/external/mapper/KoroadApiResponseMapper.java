package yys.safewalk.infrastructure.adapter.out.external.mapper;

import yys.safewalk.application.port.out.external.dto.RiskIndexData;
import yys.safewalk.application.port.out.external.dto.*;
import yys.safewalk.domain.riskarea.model.RiskLevel;
import yys.safewalk.infrastructure.adapter.out.external.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KoroadApiResponseMapper {

    /**
     * 일반 사고다발지역 API 응답을 AccidentData 리스트로 변환
     */
    public List<AccidentData> toAccidentDataList(KoroadApiResponse response) {
        if (response == null || !isSuccessResponse(response)) {
            log.warn("Invalid or failed API response: {}", response);
            return Collections.emptyList();
        }

        if (response.getItems() == null || response.getItems().isEmpty()) {
            log.debug("No accident data found in response");
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .filter(Objects::nonNull)
                .map(this::toAccidentData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 개별 사고다발지역 아이템을 AccidentData로 변환
     */
    public AccidentData toAccidentData(KoroadAccidentItem item) {
        if (item == null) {
            return null;
        }

        try {
            return AccidentData.builder()
                    .afosId(item.getAfosId())
                    .afosAreaId(item.getAfosAreaId())
                    .legalDongCode(item.getBjdCd())
                    .spotCode(item.getSpotCd())
                    .regionName(item.getSidoSggNm())
                    .spotName(item.getSpotNm())
                    .accidentCount(item.getOccrrncCnt())
                    .casualtyCount(item.getCasltCnt())
                    .deathCount(item.getDthDnvCnt())
                    .seriousInjuryCount(item.getSeDnvCnt())
                    .minorInjuryCount(item.getSlDnvCnt())
                    .injuryReportCount(item.getWndDnvCnt())
                    .longitude(item.getLoCrd())
                    .latitude(item.getLaCrd())
                    .geometryJson(item.getGeomJson())
                    .apiType(determineApiType(item))
                    .build();
        } catch (Exception e) {
            log.error("Error converting accident item to AccidentData: {}", item, e);
            return null;
        }
    }

    /**
     * 통계 API 응답을 AccidentStatisticsData 리스트로 변환
     */
    public List<AccidentStatisticsData> toAccidentStatisticsDataList(KoroadStatisticsResponse response) {
        if (response == null || !isSuccessResponse(response)) {
            log.warn("Invalid or failed statistics API response: {}", response);
            return Collections.emptyList();
        }

        if (response.getItems() == null || response.getItems().isEmpty()) {
            log.debug("No statistics data found in response");
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .filter(Objects::nonNull)
                .map(this::toAccidentStatisticsData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 개별 통계 아이템을 AccidentStatisticsData로 변환
     */
    public AccidentStatisticsData toAccidentStatisticsData(KoroadStatisticsItem item) {
        if (item == null) {
            return null;
        }

        try {
            return AccidentStatisticsData.builder()
                    .standardYear(item.getStdYear())
                    .accidentClassificationName(item.getAccClNm())
                    .regionName(item.getSidoSggNm())
                    .accidentCount(item.getAccCnt())
                    .accidentCountRatio(item.getAccCntCmrt())
                    .deathCount(item.getDthDnvCnt())
                    .deathCountRatio(item.getDthDnvCntCmrt())
                    .fatalityRate(item.getFtltRate())
                    .injuredPersonCount(item.getInjpsnCnt())
                    .injuredPersonCountRatio(item.getInjpsnCntCmrt())
                    .totalAccidentCount(item.getTotAccCnt())
                    .totalDeathCount(item.getTotDthDnvCnt())
                    .totalInjuredPersonCount(item.getTotInjpsnCnt())
                    .accidentsPer100kPopulation(item.getPop100k())
                    .accidentsPer10kVehicles(item.getCar10k())
                    // 법규위반 통계
                    .speedingCount(item.getCnt02701())
                    .centerLineViolationCount(item.getCnt02702())
                    .signalViolationCount(item.getCnt02703())
                    .safeDistanceViolationCount(item.getCnt02704())
                    .safeDrivingViolationCount(item.getCnt02705())
                    .intersectionViolationCount(item.getCnt02706())
                    .pedestrianProtectionViolationCount(item.getCnt02707())
                    .otherViolationCount(item.getCnt02799())
                    // 사고유형별 통계
                    .vehicleVsPedestrianCount(item.getCnt01401())
                    .vehicleVsVehicleCount(item.getCnt01402())
                    .singleVehicleCount(item.getCnt01403())
                    .railwayCrossingCount(item.getCnt01404())
                    .build();
        } catch (Exception e) {
            log.error("Error converting statistics item to AccidentStatisticsData: {}", item, e);
            return null;
        }
    }

    /**
     * 위험지역 API 응답을 RiskAreaData 리스트로 변환
     */
    public List<RiskAreaData> toRiskAreaDataList(KoroadRiskAreaResponse response) {
        if (response == null || !isSuccessResponse(response)) {
            log.warn("Invalid or failed risk area API response: {}", response);
            return Collections.emptyList();
        }

        if (response.getItems() == null || response.getItems().isEmpty()) {
            log.debug("No risk area data found in response");
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .filter(Objects::nonNull)
                .map(this::toRiskAreaData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 개별 위험지역 아이템을 RiskAreaData로 변환
     */
    public RiskAreaData toRiskAreaData(KoroadRiskAreaItem item) {
        if (item == null) {
            return null;
        }

        try {
            return RiskAreaData.builder()
                    .riskAreaName(item.getAccRiskAreaNm())
                    .totalAccidentCount(item.getTotAccCnt())
                    .totalDeathCount(item.getTotDthDnvCnt())
                    .totalSeriousInjuryCount(item.getTotSeDnvCnt())
                    .totalMinorInjuryCount(item.getTotSlDnvCnt())
                    .totalInjuryReportCount(item.getTotWndDnvCnt())
                    .accidentAnalysisTypes(parseAccidentAnalysisTypes(item.getCauseAnalsTyNm()))
                    .centerPointUtmkX(item.getCntpntUtmkXCrd())
                    .centerPointUtmkY(item.getCntpntUtmkYCrd())
                    .geometryWkt(item.getGeomWkt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting risk area item to RiskAreaData: {}", item, e);
            return null;
        }
    }

    /**
     * 위험지수 API 응답을 RiskIndexData로 변환
     */
    public RiskIndexData toRiskIndexData(KoroadRiskIndexResponse response) {
        if (response == null || !isSuccessResponse(response)) {
            log.warn("Invalid or failed risk index API response: {}", response);
            return null;
        }

        if (response.getItems() == null || response.getItems().isEmpty()) {
            log.debug("No risk index data found in response");
            return null;
        }

        // 첫 번째 아이템을 사용 (일반적으로 단일 결과)
        KoroadRiskIndexItem item = response.getItems().get(0);
        return toRiskIndexData(item);
    }

    /**
     * 개별 위험지수 아이템을 RiskIndexData로 변환
     */
    public RiskIndexData toRiskIndexData(KoroadRiskIndexItem item) {
        if (item == null) {
            return null;
        }

        try {
            return RiskIndexData.builder()
                    .index(item.getIndex())
                    .lineString(item.getLineString())
                    .analysisValue(item.getAnalsValue())
                    .analysisGrade(item.getAnalsGrd())
                    .riskLevel(determineRiskLevel(item.getAnalsGrd()))
                    .build();
        } catch (Exception e) {
            log.error("Error converting risk index item to RiskIndexData: {}", item, e);
            return null;
        }
    }

    // ===========================================
    // 헬퍼 메서드들
    // ===========================================

    /**
     * API 응답이 성공인지 확인
     */
    private boolean isSuccessResponse(KoroadBaseResponse response) {
        return response != null && "00".equals(response.getResultCode());
    }

    /**
     * API 타입 결정 (URL 경로 기반으로 추정)
     */
    private String determineApiType(KoroadAccidentItem item) {
        // 실제로는 호출 컨텍스트에서 전달받아야 하지만,
        // 데이터 특성으로 추정 가능한 경우도 있음
        return "UNKNOWN"; // 호출하는 곳에서 설정하도록 변경 필요
    }

    /**
     * 사고 분석 유형 문자열을 리스트로 파싱
     */
    private List<String> parseAccidentAnalysisTypes(String causeAnalysisTypes) {
        if (causeAnalysisTypes == null || causeAnalysisTypes.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // JSON 배열 형태: ["기타","안전거리 미확보", "U턴중"]
            String cleaned = causeAnalysisTypes
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "");

            return List.of(cleaned.split("\\s*,\\s*"));
        } catch (Exception e) {
            log.warn("Failed to parse accident analysis types: {}", causeAnalysisTypes, e);
            return Collections.singletonList(causeAnalysisTypes);
        }
    }

    /**
     * 위험지수 등급을 위험도 레벨로 변환
     */
    private RiskLevel determineRiskLevel(Integer analysisGrade) {
        if (analysisGrade == null) {
            return RiskLevel.UNKNOWN;
        }

        return switch (analysisGrade) {
            case 1 -> RiskLevel.SAFE;
            case 2 -> RiskLevel.CAUTION;
            case 3 -> RiskLevel.DANGER;
            case 4 -> RiskLevel.VERY_DANGER;
            default -> RiskLevel.UNKNOWN;
        };
    }

    /**
     * 매핑 성공률 로깅을 위한 헬퍼
     */
    public void logMappingStatistics(String apiType, int totalItems, int successfulMappings) {
        double successRate = totalItems > 0 ? (double) successfulMappings / totalItems * 100 : 0;
        log.info("API {} mapping statistics: {}/{} items mapped successfully ({}%)",
                apiType, successfulMappings, totalItems, String.format("%.1f", successRate));
    }
}