package com.armiet.sevatrack.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armiet.sevatrack.model.CompletionSubmission;
import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.model.Student;
import com.armiet.sevatrack.repository.CompletionSubmissionRepository;
import com.armiet.sevatrack.repository.ParticipationRepository;
import com.armiet.sevatrack.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MyActivitiesController {

    private final ParticipationRepository participationRepository;
    private final StudentRepository studentRepository;
    private final CompletionSubmissionRepository completionSubmissionRepository;

    public MyActivitiesController(
            ParticipationRepository participationRepository,
            StudentRepository studentRepository,
            CompletionSubmissionRepository completionSubmissionRepository) {

        this.participationRepository = participationRepository;
        this.studentRepository = studentRepository;
        this.completionSubmissionRepository =
                completionSubmissionRepository;
    }

    @GetMapping("/my-activities")
    public String showMyActivities(
            @RequestParam Long studentId,
            HttpSession session,
            Model model) {

        Long loggedInStudentId =
                (Long) session.getAttribute("studentId");

        // Make sure the logged-in volunteer is viewing
        // their own activities.
        if (loggedInStudentId == null ||
                !loggedInStudentId.equals(studentId)) {

            return "redirect:/login";
        }

        Student student = studentRepository
                .findById(studentId)
                .orElseThrow();

        List<Participation> participations =
                participationRepository
                        .findByStudentId(studentId);

        /*
         * Store completion submissions using participation ID
         * as the key.
         *
         * Example:
         *
         * participation ID 5
         *        ↓
         * completion submission
         *
         * This allows Thymeleaf to easily find the completion
         * status for each activity.
         */
        Map<Long, CompletionSubmission>
                completionByParticipationId =
                        new HashMap<>();

        for (Participation participation : participations) {

            completionSubmissionRepository
                    .findByParticipationId(
                            participation.getId()
                    )
                    .ifPresent(submission ->
                            completionByParticipationId.put(
                                    participation.getId(),
                                    submission
                            )
                    );
        }

        model.addAttribute(
                "student",
                student
        );

        model.addAttribute(
                "participations",
                participations
        );

        model.addAttribute(
                "completionByParticipationId",
                completionByParticipationId
        );

        return "my-activities";
    }
}