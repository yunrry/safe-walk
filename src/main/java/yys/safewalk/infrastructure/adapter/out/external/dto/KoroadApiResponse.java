// infrastructure/adapter/out/external/dto/KoroadApiResponse.java
package yys.safewalk.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoroadApiResponse extends KoroadBaseResponse {

    /**
     * 응답 데이터 항목들
     * 보행자/보행노인/지자체별/연휴기간별 사고다발지역 API 공통 사용
     */
    @JsonProperty("items")
    private List<KoroadAccidentItem> items;

    /**
     * 총 검색 건수
     */
    @JsonProperty("totalCount")
    private Integer totalCount;

    /**
     * 검색된 건수
     */
    @JsonProperty("numOfRows")
    private Integer numOfRows;

    /**
     * 페이지 번호
     */
    @JsonProperty("pageNo")
    private Integer pageNo;

    // ===========================================
    // 헬퍼 메서드들
    // ===========================================

    /**
     * 응답 데이터가 비어있는지 확인
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * 응답 데이터 개수 반환
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * 다음 페이지가 있는지 확인
     */
    public boolean hasNextPage() {
        if (totalCount == null || numOfRows == null || pageNo == null) {
            return false;
        }
        return (pageNo * numOfRows) < totalCount;
    }

    /**
     * 현재 페이지의 시작 인덱스 (1부터 시작)
     */
    public int getStartIndex() {
        if (pageNo == null || numOfRows == null) {
            return 1;
        }
        return ((pageNo - 1) * numOfRows) + 1;
    }

    /**
     * 현재 페이지의 끝 인덱스
     */
    public int getEndIndex() {
        if (totalCount == null || numOfRows == null || pageNo == null) {
            return getItemCount();
        }
        int calculatedEnd = pageNo * numOfRows;
        return Math.min(calculatedEnd, totalCount);
    }

    /**
     * 전체 페이지 수 계산
     */
    public int getTotalPages() {
        if (totalCount == null || numOfRows == null || numOfRows == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / numOfRows);
    }

    /**
     * null-safe items 반환
     */
    public List<KoroadAccidentItem> getSafeItems() {
        return items != null ? items : Collections.emptyList();
    }

    /**
     * 유효한 아이템들만 필터링해서 반환
     */
    public List<KoroadAccidentItem> getValidItems() {
        return getSafeItems().stream()
                .filter(item -> item != null && item.isValid())
                .toList();
    }

    /**
     * 페이징 정보 요약
     */
    public String getPagingInfo() {
        return String.format("페이지 %d/%d (총 %d건 중 %d-%d)",
                pageNo != null ? pageNo : 1,
                getTotalPages(),
                totalCount != null ? totalCount : 0,
                getStartIndex(),
                getEndIndex());
    }

    /**
     * 응답 상태 요약
     */
    public String getResponseSummary() {
        return String.format("API 응답 - 결과코드:%s, 메시지:%s, 데이터:%d건",
                getResultCode(),
                getResultMsg(),
                getItemCount());
    }

    /**
     * 데이터 품질 체크
     */
    public ResponseQuality checkDataQuality() {
        if (isEmpty()) {
            return ResponseQuality.NO_DATA;
        }

        List<KoroadAccidentItem> validItems = getValidItems();
        double validRatio = (double) validItems.size() / getItemCount();

        if (validRatio >= 0.9) {
            return ResponseQuality.HIGH;
        } else if (validRatio >= 0.7) {
            return ResponseQuality.MEDIUM;
        } else if (validRatio >= 0.5) {
            return ResponseQuality.LOW;
        } else {
            return ResponseQuality.POOR;
        }
    }

    /**
     * 지역별 데이터 분포 확인
     */
    public boolean hasRegionalData() {
        return getSafeItems().stream()
                .anyMatch(item -> item.getSidoSggNm() != null &&
                        !item.getSidoSggNm().trim().isEmpty());
    }

    /**
     * 좌표 정보가 있는 아이템 개수
     */
    public long getItemsWithCoordinates() {
        return getSafeItems().stream()
                .filter(item -> item.getLoCrd() != null && item.getLaCrd() != null)
                .count();
    }

    /**
     * 사고건수가 있는 아이템 개수
     */
    public long getItemsWithAccidentCount() {
        return getSafeItems().stream()
                .filter(item -> item.getOccrrncCnt() != null && item.getOccrrncCnt() > 0)
                .count();
    }

    @Override
    public String toString() {
        return String.format("KoroadApiResponse{resultCode='%s', totalCount=%d, itemCount=%d, page=%d/%d}",
                getResultCode(),
                totalCount != null ? totalCount : 0,
                getItemCount(),
                pageNo != null ? pageNo : 1,
                getTotalPages());
    }

    // ===========================================
    // 내부 열거형: 데이터 품질
    // ===========================================

    public enum ResponseQuality {
        HIGH("높음", "90% 이상의 유효한 데이터"),
        MEDIUM("중간", "70-90%의 유효한 데이터"),
        LOW("낮음", "50-70%의 유효한 데이터"),
        POOR("매우낮음", "50% 미만의 유효한 데이터"),
        NO_DATA("데이터없음", "응답 데이터가 없음");

        private final String description;
        private final String detail;

        ResponseQuality(String description, String detail) {
            this.description = description;
            this.detail = detail;
        }

        public String getDescription() {
            return description;
        }

        public String getDetail() {
            return detail;
        }
    }
}