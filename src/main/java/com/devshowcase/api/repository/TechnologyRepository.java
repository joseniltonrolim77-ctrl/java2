package com.devshowcase.api.repository;

import com.devshowcase.api.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {
    Optional<Technology> findByName(String name);

    List<Technology> findAllByOrderByNameAsc();

    List<Technology> findAllByIdIn(List<Long> ids);
}
