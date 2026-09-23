package com.armiet.sevatrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.armiet.sevatrack.model.CompletionSubmission;

public interface CompletionSubmissionRepository
        extends JpaRepository<CompletionSubmission, Long> {

    Optional<CompletionSubmission> findByParticipationId(
            Long participationId
    );

    boolean existsByParticipationId(
            Long participationId
    );

    void deleteByParticipationId(
            Long participationId
    );
}