package yys.safewalk.domain.model;

import java.math.BigDecimal;

public record Coordinate(BigDecimal latitude, BigDecimal longitude) {
    public Coordinate {
//        if (latitude == null || longitude == null) {
//            throw new IllegalArgumentException("Latitude and longitude cannot be null");
//        }
//        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
//            throw new IllegalArgumentException("Invalid latitude range");
//        }
//        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
//            throw new IllegalArgumentException("Invalid longitude range");
//        }
    }
}