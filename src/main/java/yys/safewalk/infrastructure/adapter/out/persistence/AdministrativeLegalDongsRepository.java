package yys.safewalk.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yys.safewalk.entity.AdministrativeLegalDongs;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministrativeLegalDongsRepository extends JpaRepository<AdministrativeLegalDongs, Long> {

    // 실시간 검색 - 읍면동명으로 시작하는 항목들 (codeType이 H가 아닌 것만)
    @Query("SELECT a FROM AdministrativeLegalDongs a WHERE a.eupMyeonDong LIKE :query% AND a.codeType != 'H' ORDER BY a.eupMyeonDong")
    List<AdministrativeLegalDongs> findByEupMyeonDongStartingWith(@Param("query") String query, Pageable pageable);

    // 일반 검색 - 정확한 읍면동명 (codeType이 H가 아닌 것만)
    List<AdministrativeLegalDongs> findByEupMyeonDongAndCodeTypeNot(String eupMyeonDong, String codeType);

    // 일반 검색 - 읍면동명 + 시도 필터 (codeType이 H가 아닌 것만)
    List<AdministrativeLegalDongs> findByEupMyeonDongAndSidoAndCodeTypeNot(String eupMyeonDong, String sido, String codeType);

    // 일반 검색 - 읍면동명 + 시도 + 시군구 필터 (codeType이 H가 아닌 것만)
    List<AdministrativeLegalDongs> findByEupMyeonDongAndSidoAndSigunguAndCodeTypeNot(String eupMyeonDong, String sido, String sigungu, String codeType);

    // 코드로 검색 (기존 유지)
    Optional<AdministrativeLegalDongs> findByCode(String code);

    // 읍면동명으로 검색 (codeType이 H가 아닌 것만)
    @Query("SELECT a FROM AdministrativeLegalDongs a WHERE a.eupMyeonDong = :eupMyeonDong AND a.codeType != 'H' ORDER BY a.sido, a.sigungu")
    List<AdministrativeLegalDongs> findByEupMyeonDongOrderBySidoAndSigungu(@Param("eupMyeonDong") String eupMyeonDong);

    Optional<AdministrativeLegalDongs> findByCodeAndCodeTypeNot(String code, String codeType);

}