package com.armiet.sevatrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.armiet.sevatrack.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAllByOrderByDateDesc();

}