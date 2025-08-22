package yys.safewalk.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yys.safewalk.application.port.in.GetEmdBySidoCodeQuery;
import yys.safewalk.application.port.in.GetEmdDetailQuery;
import yys.safewalk.application.port.in.GetEmdInBoundsQuery;
import yys.safewalk.application.port.in.dto.EmdDetailResponse;
import yys.safewalk.application.port.in.dto.EmdInBoundsResponse;
import yys.safewalk.application.port.in.dto.EmdResponse;
import yys.safewalk.application.port.in.dto.EmdSearchRequest;
import yys.safewalk.application.service.AdministrativeLegalDongService;
import yys.safewalk.application.usecase.GetEmdDetailUseCase;
import yys.safewalk.application.usecase.GetEmdUseCase;
import yys.safewalk.domain.model.Coordinate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "EMD", description = "법정동 관련 API")
public class EmdController {

    private final GetEmdUseCase getEmdUseCase;
    private final GetEmdDetailUseCase getEmdDetailUseCase;
    private final AdministrativeLegalDongService administrativeLegalDongService;


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

        List<EmdInBoundsResponse> response = getEmdUseCase.getEmdInBounds(query);
        return ResponseEntity.ok(response);
    }




    @GetMapping("/emd/{emdCode}")
    @Operation(
            summary = "법정동 상세 조회",
            description = "법정동 코드를 기반으로 해당 법정동의 상세 사고이력 및 지리정보를 조회합니다",
            parameters = {
                    @Parameter(name = "emdCode", description = "법정동 코드", example = "11140118")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "법정동 상세 조회 성공 (데이터가 없을 수 있음)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<EmdDetailResponse> getEmdDetail(
            @PathVariable String emdCode
    ) {
        GetEmdDetailQuery query = new GetEmdDetailQuery(emdCode);
        EmdDetailResponse response = getEmdDetailUseCase.getEmdDetail(query);
        
        // null이어도 그대로 반환 (JSON에서 null로 표시)
        return ResponseEntity.ok(response);
    }


    @GetMapping("/emd/details")
    @Operation(
            summary = "지도 영역 내 법정동 상세 조회",
            description = "지도 영역(바운딩 박스) 내의 법정동들을 조회하고 각 법정동의 상세 사고이력 및 지리정보를 함께 반환합니다",
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
                    description = "지도 영역 내 법정동 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (좌표 값 오류)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EmdDetailResponse>> getEmdDetailsInBounds(
            @RequestParam BigDecimal swLat,
            @RequestParam BigDecimal swLng,
            @RequestParam BigDecimal neLat,
            @RequestParam BigDecimal neLng
    ) {
        // 1. 지도 영역 내 법정동 목록 조회
        GetEmdInBoundsQuery boundsQuery = new GetEmdInBoundsQuery(
                new Coordinate(swLat, swLng),
                new Coordinate(neLat, neLng)
        );

        List<EmdInBoundsResponse> emdsInBounds = getEmdUseCase.getEmdInBounds(boundsQuery);

        // 2. 각 법정동 코드로 상세 정보 조회 (null 값도 포함)
        List<EmdDetailResponse> detailResponses = emdsInBounds.stream()
                .map(emd -> {
                    GetEmdDetailQuery detailQuery = new GetEmdDetailQuery(emd.EMD_CD());
                    return getEmdDetailUseCase.getEmdDetail(detailQuery);
                })
                .collect(Collectors.toList());  // null 값 필터링 제거

        return ResponseEntity.ok(detailResponses);
    }




    @GetMapping("/emd/search/realtime")
    @Operation(
            summary = "읍면동명 실시간 검색",
            description = "자동완성을 위한 실시간 읍면동 검색을 제공합니다.",
            parameters = {
                    @Parameter(name = "query", description = "검색할 읍면동명", example = "청운"),
                    @Parameter(name = "limit", description = "반환할 최대 결과 수", example = "10")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "실시간 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (검색어 유효성 검증 실패)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EmdResponse>> searchRealtime(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<EmdResponse> response = administrativeLegalDongService.searchRealtime(query, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/emd/search")
    @Operation(
            summary = "읍면동명 상세 검색",
            description = "읍면동명을 기반으로 상세 검색을 제공합니다.",
            parameters = {
                    @Parameter(name = "eupMyeonDong", description = "읍면동명", example = "청운효자동"),
                    @Parameter(name = "sido", description = "시도명", example = "서울특별시"),
                    @Parameter(name = "sigungu", description = "시군구명", example = "종로구")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상세 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (검색 조건 유효성 검증 실패)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EmdResponse>> search(@Valid @ModelAttribute EmdSearchRequest request) {
        List<EmdResponse> response = administrativeLegalDongService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/emd/search/{code}")
    @Operation(
            summary = "코드 기반 행정법정동 조회",
            description = "행정구역 코드를 기반으로 특정 행정법정동 정보를 조회합니다. 입력된 코드에 자동으로 '00' 접미사가 추가됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "코드 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 코드의 행정구역을 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<EmdResponse> findByCode(
            @Parameter(description = "행정구역 코드", example = "11110540")
            @PathVariable String code
    ) {
        EmdResponse response = administrativeLegalDongService.findByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/emd/search/name")
    @Operation(
            summary = "읍면동명 단일 검색",
            description = "읍면동명만으로 검색하여 전국의 동일한 이름을 가진 모든 행정구역을 조회합니다.",
            parameters = {
                    @Parameter(name = "name", description = "읍면동명", example = "청운효자동", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "읍면동명 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (읍면동명 누락)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EmdResponse>> searchByName(
            @Parameter(description = "읍면동명", example = "청운동")
            @RequestParam String name
    ) {
        List<EmdResponse> response = administrativeLegalDongService.searchByEupMyeonDongOnly(name);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/emd/sido/{sidoCode}")
    @Operation(
            summary = "시도 코드별 법정동 조회",
            description = "시도 코드(앞 4자리)를 기반으로 해당 지역의 모든 법정동을 조회합니다. 일반 사고와 고령자 사고를 합친 총 사고 수가 포함됩니다.",
            parameters = {
                    @Parameter(name = "sidoCode", description = "시도 코드 (4자리)", example = "4713", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시도별 법정동 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmdInBoundsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (시도 코드가 4자리가 아님)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<EmdInBoundsResponse>> getEmdBySidoCode(
            @PathVariable String sidoCode) {

        GetEmdBySidoCodeQuery query = new GetEmdBySidoCodeQuery(sidoCode);
        List<EmdInBoundsResponse> response = getEmdUseCase.getEmdBySidoCode(query);

        return ResponseEntity.ok(response);
    }
}