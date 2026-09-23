package com.armiet.sevatrack.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armiet.sevatrack.model.Activity;
import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.model.Student;
import com.armiet.sevatrack.repository.ActivityRepository;
import com.armiet.sevatrack.repository.ParticipationRepository;
import com.armiet.sevatrack.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final StudentRepository studentRepository;
    private final ParticipationRepository participationRepository;

    public ActivityController(
            ActivityRepository activityRepository,
            StudentRepository studentRepository,
            ParticipationRepository participationRepository) {

        this.activityRepository = activityRepository;
        this.studentRepository = studentRepository;
        this.participationRepository = participationRepository;
    }

    @GetMapping("/activities")
    public String showVolunteerActivities(
            @RequestParam Long studentId,
            HttpSession session,
            Model model) {

        // Check if volunteer is logged in
        Long loggedInStudentId =
                (Long) session.getAttribute("studentId");

        if (loggedInStudentId == null ||
                !loggedInStudentId.equals(studentId)) {

            return "redirect:/login";
        }

        // Get all NSS activities
        List<Activity> activities =
                activityRepository.findAllByOrderByDateDesc();

        model.addAttribute("activities", activities);

        // Get logged-in student
        Student student = studentRepository
                .findById(studentId)
                .orElseThrow();

        model.addAttribute("student", student);

        // Find activities already joined by this student
        List<Participation> participations =
                participationRepository
                        .findByStudentId(studentId);

        Set<Long> joinedActivityIds = new HashSet<>();

        for (Participation participation : participations) {

            if (participation.getActivity() != null &&
                    participation.getActivity().getId() != null) {

                joinedActivityIds.add(
                        participation.getActivity().getId()
                );
            }
        }

        // Send joined activity IDs to the HTML page
        model.addAttribute(
                "joinedActivityIds",
                joinedActivityIds
        );

        return "activities";
    }
}