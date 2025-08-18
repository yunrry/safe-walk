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
@Table(name = "visitor_boom_neighborhoods",
    indexes = {
        @Index(name = "idx_ranking", columnList = "ranking"),
        @Index(name = "idx_sido", columnList = "sidoName"),
        @Index(name = "idx_sigungu", columnList = "sigunguName"),
        @Index(name = "idx_administrative_dong", columnList = "administrativeDong"),
        @Index(name = "idx_location", columnList = "sidoName, sigunguName"),
        @Index(name = "idx_growth_rate", columnList = "growthRate"),
        @Index(name = "idx_base_year_month", columnList = "baseYearMonth")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorBoomNeighborhoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "sido_name", length = 50)
    private String sidoName;

    @Column(name = "sigungu_name", length = 50)
    private String sigunguName;

    @Column(name = "administrative_dong", length = 50)
    private String administrativeDong;

    @Column(name = "visitor_count", precision = 15, scale = 2)
    private BigDecimal visitorCount;

    @Column(name = "last_year_visitor_count", precision = 15, scale = 2)
    private BigDecimal lastYearVisitorCount;

    @Column(name = "growth_rate", precision = 5, scale = 2)
    private BigDecimal growthRate;

    @Column(name = "base_year_month", length = 10)
    private String baseYearMonth;

    @Column(name = "search_date", length = 20)
    private String searchDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}