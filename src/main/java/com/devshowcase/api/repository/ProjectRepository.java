package com.devshowcase.api.repository;

import com.devshowcase.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // O parâmetro :technology é sempre enviado como string (nunca null — ver ProjectService.list,
    // que troca null/blank por "" e usa :hasTechnology para ligar/desligar o filtro). Isso evita um
    // problema conhecido do Hibernate/PostgreSQL: quando o mesmo parâmetro nomeado é usado tanto em
    // "= :x" quanto dentro de uma função de texto (LOWER/CONCAT) e pode ser null, o driver JDBC não
    // consegue inferir o tipo do bind e o Postgres acaba recebendo-o como bytea, causando o erro
    // "function lower(bytea) does not exist".
    @Query(value = "SELECT p FROM Project p " +
            "WHERE (:profileId IS NULL OR p.profile.id = :profileId) " +
            "AND (:hasTechnology = false OR EXISTS (" +
            "   SELECT 1 FROM Technology t WHERE t MEMBER OF p.technologies " +
            "   AND LOWER(t.name) LIKE LOWER(CONCAT('%', :technology, '%')))) " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Project p " +
                    "WHERE (:profileId IS NULL OR p.profile.id = :profileId) " +
                    "AND (:hasTechnology = false OR EXISTS (" +
                    "   SELECT 1 FROM Technology t WHERE t MEMBER OF p.technologies " +
                    "   AND LOWER(t.name) LIKE LOWER(CONCAT('%', :technology, '%'))))")
    Page<Project> search(@Param("profileId") Long profileId,
                          @Param("hasTechnology") boolean hasTechnology,
                          @Param("technology") String technology,
                          Pageable pageable);
}
