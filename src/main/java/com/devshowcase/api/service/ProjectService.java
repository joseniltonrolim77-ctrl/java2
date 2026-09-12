package com.devshowcase.api.service;

import com.devshowcase.api.dto.FeedbackRequest;
import com.devshowcase.api.dto.ProjectRequest;
import com.devshowcase.api.entity.Feedback;
import com.devshowcase.api.entity.Profile;
import com.devshowcase.api.entity.Project;
import com.devshowcase.api.entity.Technology;
import com.devshowcase.api.exception.BadRequestException;
import com.devshowcase.api.exception.NotFoundException;
import com.devshowcase.api.repository.FeedbackRepository;
import com.devshowcase.api.repository.ProfileRepository;
import com.devshowcase.api.repository.ProjectRepository;
import com.devshowcase.api.repository.TechnologyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;
    private final TechnologyRepository technologyRepository;
    private final FeedbackRepository feedbackRepository;

    public ProjectService(ProjectRepository projectRepository,
                           ProfileRepository profileRepository,
                           TechnologyRepository technologyRepository,
                           FeedbackRepository feedbackRepository) {
        this.projectRepository = projectRepository;
        this.profileRepository = profileRepository;
        this.technologyRepository = technologyRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public Project create(ProjectRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new BadRequestException(
                        String.format("Profile com id %d não encontrado.", request.getProfileId())));

        List<Long> technologyIds = request.getTechnologyIds();
        Set<Technology> technologies = new HashSet<>();
        if (technologyIds != null && !technologyIds.isEmpty()) {
            List<Technology> found = technologyRepository.findAllByIdIn(technologyIds);
            if (found.size() != new HashSet<>(technologyIds).size()) {
                throw new BadRequestException("Uma ou mais tecnologias informadas não existem.");
            }
            technologies.addAll(found);
        }

        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(blankToNull(request.getDescription()));
        project.setRepositoryUrl(request.getRepositoryUrl());
        project.setProfile(profile);
        project.setTechnologies(technologies);
        project.setAverageRating(0.0);
        project.setUpvotes(0);

        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Projeto com id %d não encontrado.", id)));
    }

    @Transactional(readOnly = true)
    public Page<Project> list(Long profileId, String technology, int page, int limit) {
        boolean hasTechnology = technology != null && !technology.isBlank();
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), limit);
        return projectRepository.search(profileId, hasTechnology, hasTechnology ? technology : "", pageRequest);
    }

    /**
     * Regra de negócio: cada feedback registrado recalcula a nota média (1 a 5) do projeto.
     */
    @Transactional
    public FeedbackResult addFeedback(Long projectId, FeedbackRequest request) {
        Project project = getById(projectId);

        Feedback feedback = new Feedback();
        feedback.setProject(project);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback = feedbackRepository.save(feedback);

        Double average = feedbackRepository.findAverageRatingByProjectId(projectId);
        double roundedAverage = average != null ? Math.round(average * 100) / 100.0 : 0.0;

        project.setAverageRating(roundedAverage);
        projectRepository.save(project);

        return new FeedbackResult(feedback, roundedAverage);
    }

    /**
     * Regra de negócio: upvote apenas incrementa o contador de curtidas do projeto.
     */
    @Transactional
    public Project upvote(Long projectId) {
        Project project = getById(projectId);
        project.setUpvotes(project.getUpvotes() + 1);
        return projectRepository.save(project);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    public record FeedbackResult(Feedback feedback, double averageRating) {}
}
