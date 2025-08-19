package yys.safewalk.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import yys.safewalk.application.port.in.dto.EmdResponse;
import yys.safewalk.application.port.in.dto.EmdSearchRequest;
import yys.safewalk.entity.AdministrativeLegalDongs;
import yys.safewalk.infrastructure.adapter.out.persistence.AdministrativeLegalDongsRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativeLegalDongServiceTest {

    @Mock
    private AdministrativeLegalDongsRepository repository;

    @InjectMocks
    private AdministrativeLegalDongService service;

    @Test
    @DisplayName("코드로 검색 성공 - 00 접미사 자동 추가, 응답에서는 8자리")
    void findByCode_Success() {
        // Given
        String inputCode = "11110";
        String searchCode = "1111000"; // DB 검색용 (8자리 + 00)
        String responseCode = "11110000"; // 응답용 (8자리)

        AdministrativeLegalDongs dong = AdministrativeLegalDongs.builder()
                .id(1L)
                .code("1111000000") // DB에는 10자리로 저장
                .sido("서울특별시")
                .sigungu("종로구")
                .eupMyeonDong("청운효자동")
                .codeType("B") // H가 아닌 타입
                .build();

        when(repository.findByCode("1111000")).thenReturn(Optional.of(dong));

        // When
        EmdResponse result = service.findByCode(inputCode);

        // Then
        assertThat(result.code()).isEqualTo("11110000"); // 8자리 응답
        assertThat(result.eupMyeonDong()).isEqualTo("청운효자동");
    }

    @Test
    @DisplayName("실시간 검색 성공 - 응답 코드는 8자리, codeType H 제외")
    void searchRealtime_Success() {
        // Given
        String query = "청운";
        int limit = 10;

        AdministrativeLegalDongs dong = AdministrativeLegalDongs.builder()
                .id(1L)
                .code("1111000000") // DB에는 10자리
                .sido("서울특별시")
                .sigungu("종로구")
                .eupMyeonDong("청운효자동")
                .latitude(BigDecimal.valueOf(37.586))
                .longitude(BigDecimal.valueOf(126.973))
                .codeType("B") // H가 아닌 타입
                .build();

        when(repository.findByEupMyeonDongStartingWith(query, PageRequest.of(0, limit)))
                .thenReturn(List.of(dong));

        // When
        List<EmdResponse> result = service.searchRealtime(query, limit);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("11110000"); // 8자리 응답
        assertThat(result.get(0).eupMyeonDong()).isEqualTo("청운효자동");
    }

    @Test
    @DisplayName("상세 검색 성공 - 응답 코드는 8자리, codeType H 제외")
    void search_Success() {
        // Given
        EmdSearchRequest request = new EmdSearchRequest("청운효자동", "서울특별시", "종로구");

        AdministrativeLegalDongs dong = AdministrativeLegalDongs.builder()
                .id(1L)
                .code("1111000000") // DB에는 10자리
                .eupMyeonDong("청운효자동")
                .sido("서울특별시")
                .sigungu("종로구")
                .codeType("B") // H가 아닌 타입
                .build();

        when(repository.findByEupMyeonDongAndSidoAndSigunguAndCodeTypeNot(
                request.eupMyeonDong(), request.sido(), request.sigungu(), "H"))
                .thenReturn(List.of(dong));

        // When
        List<EmdResponse> result = service.search(request);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("11110000"); // 8자리 응답
        assertThat(result.get(0).eupMyeonDong()).isEqualTo("청운효자동");
    }

    @Test
    @DisplayName("읍면동명만으로 검색 성공 - codeType H 제외")
    void searchByEupMyeonDongOnly_Success() {
        // Given
        String eupMyeonDong = "청운효자동";

        AdministrativeLegalDongs dong1 = AdministrativeLegalDongs.builder()
                .id(1L)
                .code("1111000000")
                .sido("서울특별시")
                .sigungu("종로구")
                .eupMyeonDong("청운효자동")
                .codeType("B") // H가 아닌 타입
                .build();

        AdministrativeLegalDongs dong2 = AdministrativeLegalDongs.builder()
                .id(2L)
                .code("2222000000")
                .sido("부산광역시")
                .sigungu("해운대구")
                .eupMyeonDong("청운효자동")
                .codeType("B") // H가 아닌 타입
                .build();

        when(repository.findByEupMyeonDongOrderBySidoAndSigungu(eupMyeonDong))
                .thenReturn(List.of(dong1, dong2));

        // When
        List<EmdResponse> result = service.searchByEupMyeonDongOnly(eupMyeonDong);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("11110000"); // 8자리 응답
        assertThat(result.get(0).sido()).isEqualTo("서울특별시");
        assertThat(result.get(1).code()).isEqualTo("22220000"); // 8자리 응답
        assertThat(result.get(1).sido()).isEqualTo("부산광역시");
    }

    @Test
    @DisplayName("codeType이 H인 데이터는 검색에서 제외")
    void search_ExcludeCodeTypeH() {
        // Given
        EmdSearchRequest request = new EmdSearchRequest("판교동", "경기도", "성남시 분당구");

        AdministrativeLegalDongs dongB = AdministrativeLegalDongs.builder()
                .id(1L)
                .code("4113510800")
                .sido("경기도")
                .sigungu("성남시 분당구")
                .eupMyeonDong("판교동")
                .codeType("B")
                .build();

        // codeType이 H인 데이터는 repository에서 반환되지 않음
        when(repository.findByEupMyeonDongAndSidoAndSigunguAndCodeTypeNot(
                request.eupMyeonDong(), request.sido(), request.sigungu(), "H"))
                .thenReturn(List.of(dongB)); // B 타입만 반환

        // When
        List<EmdResponse> result = service.search(request);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).codeType()).isEqualTo("B");
    }
}