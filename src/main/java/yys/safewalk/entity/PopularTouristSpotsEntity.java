package yys.safewalk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import yys.safewalk.domain.model.PopularTouristSpots;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "popular_tourist_spots",
        indexes = {
                @Index(name = "idx_sido", columnList = "sidoName"),
                @Index(name = "idx_sigungu", columnList = "sigunguName"),
                @Index(name = "idx_spot_name", columnList = "spotName"),
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_tourist_spot_id", columnList = "touristSpotId"),
                @Index(name = "idx_location", columnList = "sidoName, sigunguName")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularTouristSpotsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sido_name", length = 50)
    private String sidoName;

    @Column(name = "sigungu_name", length = 50)
    private String sigunguName;

    @Column(name = "spot_name", length = 200)
    private String spotName;

    @Column(name = "tourist_spot_id", length = 100)
    private String touristSpotId;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "age_group", length = 20)
    private String ageGroup;

    @Column(name = "ratio", precision = 5, scale = 2)
    private BigDecimal ratio;

    @Column(name = "base_year_month", length = 10)
    private String baseYearMonth;

    @Column(name = "growth_rate", precision = 5, scale = 2)
    private BigDecimal growthRate;

    @Column(name = "source_file", length = 100)
    private String sourceFile;

    @Column(name = "longitude", precision = 12, scale = 9)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 12, scale = 9)
    private BigDecimal latitude;

    @Column(name = "sido_code", length = 10)
    private String sidoCode;

    @Column(name = "mode", length = 50)
    private String mode;

    @Column(name = "rank")
    private Integer rank;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public PopularTouristSpots toDomain() {
        return PopularTouristSpots.builder()
                .id(this.id)
                .sidoName(this.sidoName)
                .sigunguName(this.sigunguName)
                .spotName(this.spotName)
                .touristSpotId(this.touristSpotId)
                .category(this.category)
                .ageGroup(this.ageGroup)
                .ratio(this.ratio)
                .baseYearMonth(this.baseYearMonth)
                .growthRate(this.growthRate)
                .sourceFile(this.sourceFile)
                .longitude(this.longitude)
                .latitude(this.latitude)
                .sidoCode(this.sidoCode)
                .mode(this.mode)
                .rank(this.rank)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}