package yys.safewalk.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "행정법정동 조회 응답")
public record EmdResponse(
        @Schema(description = "ID", example = "1")
        Long id,

        @Schema(description = "행정구역 코드", example = "11110540")
        String code,

        @Schema(description = "시도명", example = "서울특별시")
        String sido,

        @Schema(description = "시군구명", example = "종로구")
        String sigungu,

        @Schema(description = "읍면동명", example = "청운효자동")
        String eupMyeonDong,

        @Schema(description = "하위 레벨", example = "법정동")
        String subLevel,

        @Schema(description = "위도", example = "37.586")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.973")
        BigDecimal longitude,

        @Schema(description = "코드 타입", example = "법정동")
        String codeType
) {
}