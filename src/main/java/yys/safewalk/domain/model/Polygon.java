package yys.safewalk.domain.model;

public record Polygon(String coordinates) {
    public Polygon {
        if (coordinates == null || coordinates.trim().isEmpty()) {
            throw new IllegalArgumentException("Polygon coordinates cannot be null or empty");
        }
    }
}