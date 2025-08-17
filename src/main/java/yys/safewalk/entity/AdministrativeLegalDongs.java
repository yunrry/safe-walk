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
@Table(name = "administrative_legal_dongs", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_code", columnNames = "code")
    },
    indexes = {
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_sido", columnList = "sido"),
        @Index(name = "idx_sigungu", columnList = "sigungu"),
        @Index(name = "idx_eup_myeon_dong", columnList = "eupMyeonDong"),
        @Index(name = "idx_location", columnList = "longitude, latitude"),
        @Index(name = "idx_code_type", columnList = "codeType")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrativeLegalDongs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "sido", length = 50)
    private String sido;

    @Column(name = "sigungu", length = 50)
    private String sigungu;

    @Column(name = "eup_myeon_dong", length = 50)
    private String eupMyeonDong;

    @Column(name = "sub_level", length = 50)
    private String subLevel;

    @Column(name = "latitude", precision = 12, scale = 9)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 12, scale = 9)
    private BigDecimal longitude;

    @Column(name = "code_type", length = 10)
    private String codeType;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}