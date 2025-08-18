package yys.safewalk.domain.model;

public class Emd {
    private final String emdCd;
    private final String name;
    private final Coordinate centerPoint;
//    private final Polygon polygon;
    private final String polygon;
    private final Integer totalAccident;

    public Emd(String emdCd, String name, Coordinate centerPoint, String polygon, Integer totalAccident) {
        this.emdCd = emdCd;
        this.name = name;
        this.centerPoint = centerPoint;
        this.polygon = polygon;
        this.totalAccident = totalAccident;
    }

    // Getters
    public String getEmdCd() { return emdCd; }
    public String getName() { return name; }
    public Coordinate getCenterPoint() { return centerPoint; }
    public String getPolygon() { return polygon; }
    public Integer getTotalAccident() { return totalAccident; }
}