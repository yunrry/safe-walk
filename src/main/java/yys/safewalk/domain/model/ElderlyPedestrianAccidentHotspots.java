package yys.safewalk.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import yys.safewalk.entity.ElderlyPedestrianAccidentHotspotsEntity;

public record ElderlyPedestrianAccidentHotspots(
    Long id,
    String location,
    Integer accidentCount,
    Integer dead,
    Integer severe,
    Integer minor,
    BigDecimal latitude,
    BigDecimal longitude,
    String sidoCode,
    String sidoName,
    String sigunguCode,
    String sigunguName,
    String emdCode,
    String emdName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public ElderlyPedestrianAccidentHotspots {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("위치는 필수입니다");
        }
        if (accidentCount == null || accidentCount < 0) {
            throw new IllegalArgumentException("사고 건수는 0 이상이어야 합니다");
        }
        if (dead == null || dead < 0) {
            throw new IllegalArgumentException("사망자 수는 0 이상이어야 합니다");
        }
        if (severe == null || severe < 0) {
            throw new IllegalArgumentException("중상자 수는 0 이상이어야 합니다");
        }
        if (minor == null || minor < 0) {
            throw new IllegalArgumentException("경상자 수는 0 이상이어야 합니다");
        }
    }
    
    /**
     * 총 사상자 수 계산
     */
    public Integer getTotalCasualties() {
        return dead + severe + minor;
    }
    
    /**
     * 좌표가 설정되어 있는지 확인
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
    
    /**
     * 시군구명이 설정되어 있는지 확인
     */
    public boolean hasSigunguName() {
        return sigunguName != null && !sigunguName.isBlank();
    }

}