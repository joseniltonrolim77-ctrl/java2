package com.devshowcase.api.service;

import com.devshowcase.api.dto.ProfileRequest;
import com.devshowcase.api.entity.Profile;
import com.devshowcase.api.exception.ConflictException;
import com.devshowcase.api.exception.NotFoundException;
import com.devshowcase.api.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public Profile create(ProfileRequest request) {
        profileRepository.findByEmail(request.getEmail()).ifPresent(p -> {
            throw new ConflictException("Já existe um perfil cadastrado com este e-mail.");
        });

        Profile profile = new Profile();
        profile.setName(request.getName());
        profile.setEmail(request.getEmail());
        profile.setBio(blankToNull(request.getBio()));
        profile.setAvatarUrl(blankToNull(request.getAvatarUrl()));
        return profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Profile getById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado."));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
