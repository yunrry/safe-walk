package yys.safewalk.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PopularTouristSpots(
    Long id,
    String sidoName,
    String sigunguName,
    String spotName,
    String touristSpotId,
    String category,
    String ageGroup,
    BigDecimal ratio,
    String baseYearMonth,
    BigDecimal growthRate,
    String sourceFile,
    BigDecimal longitude,
    BigDecimal latitude,
    String sidoCode,
    String mode,
    Integer rank,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public PopularTouristSpots {
        if (spotName == null || spotName.isBlank()) {
            throw new IllegalArgumentException("관광지명은 필수입니다");
        }
        if (touristSpotId == null || touristSpotId.isBlank()) {
            throw new IllegalArgumentException("관광지 ID는 필수입니다");
        }
        if (sidoName == null || sidoName.isBlank()) {
            throw new IllegalArgumentException("시도명은 필수입니다");
        }
    }
    
    /**
     * 좌표가 설정되어 있는지 확인
     */
    public boolean hasCoordinates() {
        return longitude != null && latitude != null;
    }
    
    /**
     * 시군구명이 설정되어 있는지 확인
     */
    public boolean hasSigunguName() {
        return sigunguName != null && !sigunguName.isBlank();
    }
    
    /**
     * 새로운 좌표로 업데이트된 객체 생성
     */
    public PopularTouristSpots withCoordinates(BigDecimal longitude, BigDecimal latitude) {
        return new PopularTouristSpots(
            id, sidoName, sigunguName, spotName, touristSpotId, category, ageGroup,
            ratio, baseYearMonth, growthRate, sourceFile, longitude, latitude,
            sidoCode, mode, rank, createdAt, updatedAt
        );
    }
    
    /**
     * 새로운 시군구명으로 업데이트된 객체 생성
     */
    public PopularTouristSpots withSigunguName(String sigunguName) {
        return new PopularTouristSpots(
            id, sidoName, sigunguName, spotName, touristSpotId, category, ageGroup,
            ratio, baseYearMonth, growthRate, sourceFile, longitude, latitude,
            sidoCode, mode, rank, createdAt, updatedAt
        );
    }
    
    /**
     * 빌더 패턴을 위한 정적 내부 클래스
     */
    public static class Builder {
        private Long id;
        private String sidoName;
        private String sigunguName;
        private String spotName;
        private String touristSpotId;
        private String category;
        private String ageGroup;
        private BigDecimal ratio;
        private String baseYearMonth;
        private BigDecimal growthRate;
        private String sourceFile;
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String sidoCode;
        private String mode;
        private Integer rank;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder sidoName(String sidoName) {
            this.sidoName = sidoName;
            return this;
        }
        
        public Builder sigunguName(String sigunguName) {
            this.sigunguName = sigunguName;
            return this;
        }
        
        public Builder spotName(String spotName) {
            this.spotName = spotName;
            return this;
        }
        
        public Builder touristSpotId(String touristSpotId) {
            this.touristSpotId = touristSpotId;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder ageGroup(String ageGroup) {
            this.ageGroup = ageGroup;
            return this;
        }
        
        public Builder ratio(BigDecimal ratio) {
            this.ratio = ratio;
            return this;
        }
        
        public Builder baseYearMonth(String baseYearMonth) {
            this.baseYearMonth = baseYearMonth;
            return this;
        }
        
        public Builder growthRate(BigDecimal growthRate) {
            this.growthRate = growthRate;
            return this;
        }
        
        public Builder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }
        
        public Builder longitude(BigDecimal longitude) {
            this.longitude = longitude;
            return this;
        }
        
        public Builder latitude(BigDecimal latitude) {
            this.latitude = latitude;
            return this;
        }
        
        public Builder sidoCode(String sidoCode) {
            this.sidoCode = sidoCode;
            return this;
        }
        
        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }
        
        public Builder rank(Integer rank) {
            this.rank = rank;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public PopularTouristSpots build() {
            return new PopularTouristSpots(
                id, sidoName, sigunguName, spotName, touristSpotId, category, ageGroup,
                ratio, baseYearMonth, growthRate, sourceFile, longitude, latitude,
                sidoCode, mode, rank, createdAt, updatedAt
            );
        }
    }
    
    /**
     * 빌더 인스턴스 생성
     */
    public static Builder builder() {
        return new Builder();
    }
}
