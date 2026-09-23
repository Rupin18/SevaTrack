package com.armiet.sevatrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.armiet.sevatrack.model.Participation;

public interface ParticipationRepository
        extends JpaRepository<Participation, Long> {

    List<Participation> findByStudentId(Long studentId);

    List<Participation> findByActivityId(Long activityId);

    boolean existsByStudentIdAndActivityId(
            Long studentId,
            Long activityId
    );

    void deleteByActivityId(Long activityId);
}