package com.devshowcase.api.repository;

import com.devshowcase.api.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.project.id = :projectId")
    Double findAverageRatingByProjectId(Long projectId);
}
