package yys.safewalk.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yys.safewalk.application.service.TouristSpotCoordinateService;

@RestController
@RequestMapping("/api/admin/tourist-spots")
@RequiredArgsConstructor
public class TouristSpotAdminController {

    private final TouristSpotCoordinateService coordinateService;

//    @PostMapping("/update-coordinates")
//    public String updateCoordinates() {
//        coordinateService.updateAllCoordinates();
//        return "좌표 업데이트 작업이 시작되었습니다.";
//    }
}