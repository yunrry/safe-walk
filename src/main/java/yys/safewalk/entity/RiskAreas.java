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
@Table(name = "risk_areas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_risk_area", columnNames = {"yearCode", "sigunguCode", "riskAreaCode"})
    },
    indexes = {
        @Index(name = "idx_year", columnList = "yearCode"),
        @Index(name = "idx_sigungu", columnList = "sigunguCode"),
        @Index(name = "idx_risk_area", columnList = "riskAreaCode"),
        @Index(name = "idx_accident_count", columnList = "totalAccidentCount")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAreas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_code", nullable = false)
    private Integer yearCode;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @Column(name = "sigungu_name", length = 50)
    private String sigunguName;

    @Column(name = "risk_area_name", nullable = false, columnDefinition = "TEXT")
    private String riskAreaName;

    @Column(name = "risk_area_code", nullable = false, length = 20)
    private String riskAreaCode;

    @Column(name = "risk_area_polygon", columnDefinition = "TEXT")
    private String riskAreaPolygon;

    @Column(name = "total_accident_count")
    private Integer totalAccidentCount;

    @Column(name = "total_death_count")
    private Integer totalDeathCount;

    @Column(name = "total_serious_injury_count")
    private Integer totalSeriousInjuryCount;

    @Column(name = "total_minor_injury_count")
    private Integer totalMinorInjuryCount;

    @Column(name = "total_injury_report_count")
    private Integer totalInjuryReportCount;

    @Column(name = "accident_analysis_type", length = 200)
    private String accidentAnalysisType;

    @Column(name = "center_utmk_x", precision = 15, scale = 4)
    private BigDecimal centerUtmkX;

    @Column(name = "center_utmk_y", precision = 15, scale = 4)
    private BigDecimal centerUtmkY;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}