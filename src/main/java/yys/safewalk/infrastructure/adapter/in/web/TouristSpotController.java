package yys.safewalk.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yys.safewalk.application.port.in.dto.TouristSpotResponse;
import yys.safewalk.application.service.TouristSpotAreaService;
import yys.safewalk.domain.model.Coordinate;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "관광지 API", description = "관광지 조회 관련 API")
public class TouristSpotController {

    private final TouristSpotAreaService touristSpotAreaService;

    @GetMapping("/tourist-spots")
    @Operation(
            summary = "지도 영역 내 관광지 조회",
            description = "지정된 지도 영역(남서쪽 좌표와 북동쪽 좌표로 정의)에 포함되는 관광지 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "swLat", description = "남서쪽 위도", example = "35.820"),
                    @Parameter(name = "swLng", description = "남서쪽 경도", example = "129.200"),
                    @Parameter(name = "neLat", description = "북동쪽 위도", example = "35.850"),
                    @Parameter(name = "neLng", description = "북동쪽 경도", example = "129.230")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "관광지 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TouristSpotResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (좌표 유효성 검증 실패)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<TouristSpotResponse>> getTouristSpotsByArea(
            @RequestParam BigDecimal swLat,
            @RequestParam BigDecimal swLng,
            @RequestParam BigDecimal neLat,
            @RequestParam BigDecimal neLng
    ) {
        Coordinate swCoordinate = new Coordinate(swLat, swLng);
        Coordinate neCoordinate = new Coordinate(neLat, neLng);

        List<TouristSpotResponse> response = touristSpotAreaService.getTouristSpotsByArea(swCoordinate, neCoordinate);
        return ResponseEntity.ok(response);
    }
}