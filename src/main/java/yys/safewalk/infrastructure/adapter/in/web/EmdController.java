package yys.safewalk.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yys.safewalk.application.port.in.GetEmdDetailQuery;
import yys.safewalk.application.port.in.GetEmdInBoundsQuery;
import yys.safewalk.application.port.in.dto.EmdDetailResponse;
import yys.safewalk.application.port.in.dto.EmdInBoundsResponse;
import yys.safewalk.application.usecase.GetEmdDetailUseCase;
import yys.safewalk.application.usecase.GetEmdInBoundsUseCase;
import yys.safewalk.domain.model.Coordinate;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "EMD", description = "법정동 관련 API")
public class EmdController {

    private final GetEmdInBoundsUseCase getEmdInBoundsUseCase;
    private final GetEmdDetailUseCase getEmdDetailUseCase;

    @GetMapping("/emd")
    @Operation(
            summary = "지도 영역 법정동 조회",
            parameters = {
                    @Parameter(name = "swLat", description = "남서쪽 위도", example = "35.820"),
                    @Parameter(name = "swLng", description = "남서쪽 경도", example = "129.200"),
                    @Parameter(name = "neLat", description = "북동쪽 위도", example = "35.850"),
                    @Parameter(name = "neLng", description = "북동쪽 경도", example = "129.230")
            }
    )
    public ResponseEntity<List<EmdInBoundsResponse>> getEmdInBounds(
            @RequestParam BigDecimal swLat,
            @RequestParam BigDecimal swLng,
            @RequestParam BigDecimal neLat,
            @RequestParam BigDecimal neLng
    ) {
        GetEmdInBoundsQuery query = new GetEmdInBoundsQuery(
                new Coordinate(swLat, swLng),
                new Coordinate(neLat, neLng)
        );

        List<EmdInBoundsResponse> response = getEmdInBoundsUseCase.getEmdInBounds(query);
        return ResponseEntity.ok(response);
    }




    @GetMapping("/emd/{emdCode}")
    @Operation(
            summary = "법정동 상세 조회",
            description = "법정동 코드를 기반으로 해당 법정동의 상세 사고이력 및 지리정보를 조회합니다",
            parameters = {
                    @Parameter(name = "emdCode", description = "법정동 코드", example = "11110103")
            }
    )
    public ResponseEntity<EmdDetailResponse> getEmdDetail(
            @PathVariable String emdCode
    ) {
        GetEmdDetailQuery query = new GetEmdDetailQuery(emdCode);
        EmdDetailResponse response = getEmdDetailUseCase.getEmdDetail(query);
        return ResponseEntity.ok(response);
    }
}