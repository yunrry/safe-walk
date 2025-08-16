package yys.safewalk.application.port.out.external;

import yys.safewalk.application.port.out.external.dto.*;
import yys.safewalk.infrastructure.adapter.out.external.dto.ApiHealthStatus;

import java.util.List;

public interface KoroadApiPort {
    List<AccidentData> getPedestrianAccidentData(SearchCriteria criteria);
    List<AccidentData> getElderlyPedestrianAccidentData(SearchCriteria criteria);
    List<AccidentData> getLocalGovernmentAccidentData(SearchCriteria criteria);
    List<AccidentData> getHolidayAccidentData(SearchCriteria criteria);
    List<AccidentStatisticsData> getAccidentStatistics(SearchCriteria criteria);
    List<RiskAreaData> getLinkBasedRiskAreaData(SearchCriteria criteria);

    RiskIndexData getRealTimeRiskIndex(RouteInfo route);
//
//    // API 상태 확인
    boolean isApiAvailable();
    ApiHealthStatus getApiHealthStatus();
}