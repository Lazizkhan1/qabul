package uz.umft.qabul.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.umft.qabul.entity.Application;
import uz.umft.qabul.enums.ApplicationStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByUser_IdAndStatusIn(UUID userId, Collection<ApplicationStatus> statuses);

    Optional<Application> findTopByUser_IdOrderByCreatedAtDesc(UUID userId);

    @Query(value = """
            select a.*
            from applications a
            join tuition t on t.id = a.tuition_id
            where (:status is null or a.status = :status)
              and (:schoolYear is null or t.school_year = :schoolYear)
              and (:majorId is null or t.major_id = :majorId)
              and (:majorTypeId is null or t.major_type_id = :majorTypeId)
              and (:majorLangId is null or t.major_lang_id = :majorLangId)
              and (:degree is null or t.degree = cast(:degree as varchar))
            order by a.created_at desc
            """,
            countQuery = """
                    select count(*)
                    from applications a
                    join tuition t on t.id = a.tuition_id
                    where (:status is null or a.status = :status)
                      and (:schoolYear is null or t.school_year = :schoolYear)
                      and (:majorId is null or t.major_id = :majorId)
                      and (:majorTypeId is null or t.major_type_id = :majorTypeId)
                      and (:majorLangId is null or t.major_lang_id = :majorLangId)
                      and (:degree is null or t.degree = cast(:degree as varchar))
                    """,
            nativeQuery = true)
    Page<Application> findAllForModeration(
            @Param("status") String status,
            @Param("schoolYear") Integer schoolYear,
            @Param("majorId") Integer majorId,
            @Param("majorTypeId") Integer majorTypeId,
            @Param("majorLangId") Integer majorLangId,
            @Param("degree") String degree,
            Pageable pageable
    );
}
