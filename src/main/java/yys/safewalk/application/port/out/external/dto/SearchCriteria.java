// application/port/out/external/dto/SearchCriteria.java
package yys.safewalk.application.port.out.external.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {

    /**
     * 검색 연도 (필수)
     * 예: "2023", "2022"
     */
    @NotBlank(message = "검색 연도는 필수입니다")
    @Pattern(regexp = "\\d{4}", message = "연도는 4자리 숫자여야 합니다")
    private String year;

    /**
     * 시도 코드 (필수)
     * 예: "11" (서울), "26" (부산), "27" (대구) 등
     */
    @NotBlank(message = "시도 코드는 필수입니다")
    @Pattern(regexp = "\\d{2}", message = "시도 코드는 2자리 숫자여야 합니다")
    private String siDo;

    /**
     * 시군구 코드 (필수)
     * 예: "680" (강남구), "305" (강북구) 등
     */
    @NotBlank(message = "시군구 코드는 필수입니다")
    @Pattern(regexp = "\\d{3}", message = "시군구 코드는 3자리 숫자여야 합니다")
    private String guGun;

    /**
     * 결과 형식 (옵션)
     * "json" 또는 "xml"
     */
    @Builder.Default
    private String type = "json";

    /**
     * 한 번에 가져올 검색 건수 (옵션)
     * 기본값: 100, 최대: 1000
     */
    @Min(value = 1, message = "검색 건수는 최소 1개 이상이어야 합니다")
    @Max(value = 1000, message = "검색 건수는 최대 1000개까지 가능합니다")
    @Builder.Default
    private Integer numOfRows = 100;

    /**
     * 페이지 번호 (옵션)
     * 기본값: 1
     */
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    @Builder.Default
    private Integer pageNo = 1;

    /**
     * 전체 데이터를 가져오기 위한 대용량 검색 조건 생성
     */
    public static SearchCriteria forBulkCollection(String year, String siDo, String guGun) {
        return SearchCriteria.builder()
                .year(year)
                .siDo(siDo)
                .guGun(guGun)
                .type("json")
                .numOfRows(1000)
                .pageNo(1)
                .build();
    }

    /**
     * 테스트용 소량 데이터 검색 조건 생성
     */
    public static SearchCriteria forTesting(String year, String siDo, String guGun) {
        return SearchCriteria.builder()
                .year(year)
                .siDo(siDo)
                .guGun(guGun)
                .type("json")
                .numOfRows(10)
                .pageNo(1)
                .build();
    }

    /**
     * 서울 강남구 2023년 기본 검색 조건 (예시/테스트용)
     */
    public static SearchCriteria defaultSeoulGangnam() {
        return SearchCriteria.builder()
                .year("2023")
                .siDo("11")
                .guGun("680")
                .type("json")
                .numOfRows(100)
                .pageNo(1)
                .build();
    }

    /**
     * 다음 페이지 검색 조건 생성
     */
    public SearchCriteria nextPage() {
        return SearchCriteria.builder()
                .year(this.year)
                .siDo(this.siDo)
                .guGun(this.guGun)
                .type(this.type)
                .numOfRows(this.numOfRows)
                .pageNo(this.pageNo + 1)
                .build();
    }

    /**
     * 페이지 크기 변경
     */
    public SearchCriteria withPageSize(int pageSize) {
        return SearchCriteria.builder()
                .year(this.year)
                .siDo(this.siDo)
                .guGun(this.guGun)
                .type(this.type)
                .numOfRows(pageSize)
                .pageNo(this.pageNo)
                .build();
    }

    /**
     * 지역 정보를 문자열로 반환
     */
    public String getRegionKey() {
        return siDo + "-" + guGun;
    }

    /**
     * 검색 조건의 유효성 검증
     */
    public boolean isValid() {
        return year != null && year.matches("\\d{4}") &&
                siDo != null && siDo.matches("\\d{2}") &&
                guGun != null && guGun.matches("\\d{3}") &&
                numOfRows != null && numOfRows >= 1 && numOfRows <= 1000 &&
                pageNo != null && pageNo >= 1;
    }

    /**
     * 캐시 키 생성용
     */
    public String toCacheKey() {
        return String.format("koroad:%s:%s:%s:%s:%d:%d",
                year, siDo, guGun, type, numOfRows, pageNo);
    }

    @Override
    public String toString() {
        return String.format("SearchCriteria{year='%s', region='%s-%s', numOfRows=%d, pageNo=%d}",
                year, siDo, guGun, numOfRows, pageNo);
    }
}