package yys.safewalk.application.port.in.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TouristSpotAreaRequest(
        @NotNull(message = "남서쪽 좌표는 필수입니다")
        @Valid
        Coordinate swLatLng,

        @NotNull(message = "북동쪽 좌표는 필수입니다")
        @Valid
        Coordinate neLatLng
) {
    public record Coordinate(
            @NotNull(message = "위도는 필수입니다")
            Double latitude,

            @NotNull(message = "경도는 필수입니다")
            Double longitude
    ) {}
}