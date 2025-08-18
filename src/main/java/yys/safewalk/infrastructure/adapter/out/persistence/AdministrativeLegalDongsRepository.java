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

    // 실시간 검색 - 읍면동명으로 시작하는 항목들
    @Query("SELECT a FROM AdministrativeLegalDongs a WHERE a.eupMyeonDong LIKE :query% ORDER BY a.eupMyeonDong")
    List<AdministrativeLegalDongs> findByEupMyeonDongStartingWith(@Param("query") String query, Pageable pageable);

    // 일반 검색 - 정확한 읍면동명
    List<AdministrativeLegalDongs> findByEupMyeonDong(String eupMyeonDong);

    // 일반 검색 - 읍면동명 + 시도 필터
    List<AdministrativeLegalDongs> findByEupMyeonDongAndSido(String eupMyeonDong, String sido);

    // 일반 검색 - 읍면동명 + 시도 + 시군구 필터
    List<AdministrativeLegalDongs> findByEupMyeonDongAndSidoAndSigungu(String eupMyeonDong, String sido, String sigungu);

    // 코드로 검색
    Optional<AdministrativeLegalDongs> findByCode(String code);

    // 읍면동명으로 검색
    @Query("SELECT a FROM AdministrativeLegalDongs a WHERE a.eupMyeonDong = :eupMyeonDong ORDER BY a.sido, a.sigungu")
    List<AdministrativeLegalDongs> findByEupMyeonDongOrderBySidoAndSigungu(@Param("eupMyeonDong") String eupMyeonDong);
}