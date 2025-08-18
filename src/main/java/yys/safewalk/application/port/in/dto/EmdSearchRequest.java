package yys.safewalk.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "행정법정동 검색 요청")
public record EmdSearchRequest(
        @Schema(description = "읍면동명", example = "청운효자동", required = true)
        @NotBlank(message = "읍면동명은 필수입니다")
        String eupMyeonDong,

        @Schema(description = "시도명", example = "서울특별시")
        String sido,

        @Schema(description = "시군구명", example = "종로구")
        String sigungu
) {
}