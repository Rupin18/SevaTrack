package com.armiet.sevatrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.armiet.sevatrack.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByRollNumber(String rollNumber);

    Optional<Student> findByRollNumber(String rollNumber);

}