package yys.safewalk.application.port.in;

import yys.safewalk.domain.model.Coordinate;

public record GetEmdInBoundsQuery(
        Coordinate swLatLng,
        Coordinate neLatLng
) {
    public GetEmdInBoundsQuery {
        if (swLatLng == null || neLatLng == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        if (swLatLng.latitude().compareTo(neLatLng.latitude()) >= 0 ||
                swLatLng.longitude().compareTo(neLatLng.longitude()) >= 0) {
            throw new IllegalArgumentException("Invalid coordinate bounds");
        }
    }
}