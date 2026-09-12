package com.devshowcase.api.service;

import com.devshowcase.api.dto.TechnologyRequest;
import com.devshowcase.api.entity.Technology;
import com.devshowcase.api.exception.ConflictException;
import com.devshowcase.api.repository.TechnologyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechnologyService {

    private final TechnologyRepository technologyRepository;

    public TechnologyService(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Transactional
    public Technology create(TechnologyRequest request) {
        technologyRepository.findByName(request.getName()).ifPresent(t -> {
            throw new ConflictException("Já existe uma tecnologia cadastrada com este nome.");
        });

        Technology technology = new Technology();
        technology.setName(request.getName());
        return technologyRepository.save(technology);
    }

    @Transactional(readOnly = true)
    public List<Technology> findAll() {
        return technologyRepository.findAllByOrderByNameAsc();
    }
}
