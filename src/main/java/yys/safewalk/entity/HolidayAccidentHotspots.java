package yys.safewalk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holiday_accident_hotspots",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_hotspot_fid", columnNames = "accidentHotspotFid")
    },
    indexes = {
        @Index(name = "idx_hotspot_fid", columnList = "accidentHotspotFid"),
        @Index(name = "idx_hotspot_id", columnList = "accidentHotspotId"),
        @Index(name = "idx_sido_code", columnList = "sidoCode"),
        @Index(name = "idx_point_code", columnList = "pointCode"),
        @Index(name = "idx_location", columnList = "longitude, latitude"),
        @Index(name = "idx_legal_dong", columnList = "legalDong")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayAccidentHotspots {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accident_hotspot_fid", nullable = false)
    private Long accidentHotspotFid;

    @Column(name = "accident_hotspot_id", nullable = false)
    private Long accidentHotspotId;

    @Column(name = "sido_code", nullable = false, length = 10)
    private String sidoCode;

    @Column(name = "point_code", nullable = false, length = 20)
    private String pointCode;

    @Column(name = "sido_sigungu_name", nullable = false, length = 100)
    private String sidoSigunguName;

    @Column(name = "point_name", columnDefinition = "TEXT")
    private String pointName;

    @Column(name = "legal_dong", length = 50)
    private String legalDong;

    @Column(name = "accident_count")
    private Integer accidentCount;

    @Column(name = "casualty_count")
    private Integer casualtyCount;

    @Column(name = "death_count")
    private Integer deathCount;

    @Column(name = "serious_injury_count")
    private Integer seriousInjuryCount;

    @Column(name = "minor_injury_count")
    private Integer minorInjuryCount;

    @Column(name = "injury_report_count")
    private Integer injuryReportCount;

    @Column(name = "longitude", precision = 12, scale = 9)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 12, scale = 9)
    private BigDecimal latitude;

    @Column(name = "hotspot_polygon", columnDefinition = "JSON")
    private String hotspotPolygon;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}