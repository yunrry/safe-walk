package yys.safewalk.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import yys.safewalk.application.service.TouristSpotCoordinateService;
import yys.safewalk.application.service.TouristSpotSigunguUpdateService;
import yys.safewalk.application.service.TouristSpotSigunguUpdateNaverService;
import yys.safewalk.application.service.TouristSpotCoordinateNaverService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotAdminController {

    private final TouristSpotCoordinateService coordinateService;
    private final TouristSpotSigunguUpdateService sigunguUpdateService;
    private final TouristSpotSigunguUpdateNaverService sigunguUpdateNaverService;
    private final TouristSpotCoordinateNaverService coordinateNaverService;

//    @PostMapping("/update-coordinates")
//    public String updateCoordinates() {
//        coordinateService.updateAllCoordinates();
//        return "좌표 업데이트 작업이 시작되었습니다.";
//    }
//
//    @PostMapping("/update-coordinates-naver")
//    public String updateCoordinatesWithNaver() {
//        coordinateNaverService.updateAllCoordinates();
//        return "네이버 API로 좌표 업데이트 작업이 시작되었습니다.";
//    }
//
//
//    @PostMapping("/{id}/update-coordinate-naver")
//    public String updateCoordinateByIdWithNaver(@PathVariable Long id) {
//        coordinateNaverService.updateCoordinateById(id);
//        return "ID " + id + " 관광지의 좌표 업데이트가 완료되었습니다. (네이버 API)";
//    }
//
//
//    @PostMapping("/batch-update-coordinates-naver")
//    public String updateCoordinatesByIdsWithNaver(@RequestBody List<Long> ids) {
//        coordinateNaverService.updateCoordinatesByIds(ids);
//        return ids.size() + "개 관광지의 좌표 일괄 업데이트가 완료되었습니다. (네이버 API)";
//    }
//
//    @PostMapping("/update-sigungu-names")
//    public String updateSigunguNames() {
//        sigunguUpdateService.updateAllSigunguNames();
//        return "시군구명 업데이트 작업이 시작되었습니다.";
//    }
//
//    @PostMapping("/{id}/update-sigungu-name")
//    public String updateSigunguNameById(@PathVariable Long id) {
//        sigunguUpdateService.updateSigunguNameById(id);
//        return "ID " + id + " 관광지의 시군구명 업데이트가 완료되었습니다.";
//    }
//
//    @PostMapping("/batch-update-sigungu-names")
//    public String updateSigunguNamesByIds(@RequestBody List<Long> ids) {
//        sigunguUpdateService.updateSigunguNamesByIds(ids);
//        return ids.size() + "개 관광지의 시군구명 일괄 업데이트가 완료되었습니다.";
//    }
//
//    @PostMapping("/update-sigungu-names-naver")
//    public String updateSigunguNamesWithNaver() {
//        sigunguUpdateNaverService.updateAllSigunguNames();
//        return "네이버 API로 시군구명 업데이트 작업이 시작되었습니다.";
//    }
//
//    @PostMapping("/{id}/update-sigungu-name-naver")
//    public String updateSigunguNameByIdWithNaver(@PathVariable Long id) {
//        sigunguUpdateNaverService.updateSigunguNameById(id);
//        return "ID " + id + " 관광지의 시군구명 업데이트가 완료되었습니다. (네이버 API)";
//    }
//
//    @PostMapping("/batch-update-sigungu-names-naver")
//    public String updateSigunguNamesByIdsWithNaver(@RequestBody List<Long> ids) {
//        sigunguUpdateNaverService.updateSigunguNamesByIds(ids);
//        return ids.size() + "개 관광지의 시군구명 일괄 업데이트가 완료되었습니다. (네이버 API)";
//    }
}