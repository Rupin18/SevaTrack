package com.armiet.sevatrack.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.armiet.sevatrack.model.Activity;
import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.repository.ActivityRepository;
import com.armiet.sevatrack.repository.CompletionSubmissionRepository;
import com.armiet.sevatrack.repository.ParticipationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminActivityController {

    private final ActivityRepository activityRepository;
    private final ParticipationRepository participationRepository;
    private final CompletionSubmissionRepository completionSubmissionRepository;

    public AdminActivityController(
            ActivityRepository activityRepository,
            ParticipationRepository participationRepository,
            CompletionSubmissionRepository completionSubmissionRepository) {

        this.activityRepository = activityRepository;
        this.participationRepository = participationRepository;
        this.completionSubmissionRepository =
                completionSubmissionRepository;
    }

    private boolean isAdminLoggedIn(HttpSession session) {

        Boolean adminLoggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        return adminLoggedIn != null && adminLoggedIn;
    }

    // SHOW ACTIVITIES
    @GetMapping("/admin/activities")
    public String adminActivities(
            HttpSession session,
            Model model) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "activity",
                new Activity()
        );

        model.addAttribute(
                "activities",
                activityRepository.findAllByOrderByDateDesc()
        );

        return "admin-activities";
    }

    // ADD ACTIVITY
    @PostMapping("/admin/activities/add")
    public String addActivity(
            HttpSession session,
            @ModelAttribute("activity") Activity activity) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        activityRepository.save(activity);

        return "redirect:/admin/activities";
    }

    // SHOW EDIT ACTIVITY PAGE
    @GetMapping("/admin/activities/edit/{id}")
    public String showEditActivity(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        Activity activity = activityRepository
                .findById(id)
                .orElseThrow();

        model.addAttribute(
                "activity",
                activity
        );

        return "admin-edit-activity";
    }

    // UPDATE ACTIVITY
    @PostMapping("/admin/activities/edit/{id}")
    public String updateActivity(
            @PathVariable Long id,
            HttpSession session,
            @ModelAttribute("activity") Activity activity) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        Activity existingActivity =
                activityRepository
                        .findById(id)
                        .orElseThrow();

        existingActivity.setTitle(
                activity.getTitle()
        );

        existingActivity.setDate(
                activity.getDate()
        );

        existingActivity.setLocation(
                activity.getLocation()
        );

        existingActivity.setDescription(
                activity.getDescription()
        );

        existingActivity.setServiceHours(
                activity.getServiceHours()
        );

        activityRepository.save(
                existingActivity
        );

        return "redirect:/admin/activities";
    }

    // DELETE ACTIVITY
    @Transactional
    @PostMapping("/admin/activities/delete/{id}")
    public String deleteActivity(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        /*
         * First find all volunteer participations
         * belonging to this activity.
         */
        List<Participation> participations =
                participationRepository
                        .findByActivityId(id);

        /*
         * Delete completion submissions first.
         *
         * CompletionSubmission references Participation,
         * so it must be removed before the participation.
         */
        for (Participation participation : participations) {

            completionSubmissionRepository
                    .deleteByParticipationId(
                            participation.getId()
                    );
        }

        /*
         * Now it is safe to delete the participations.
         */
        participationRepository.deleteByActivityId(id);

        /*
         * Finally delete the activity itself.
         */
        activityRepository.deleteById(id);

        return "redirect:/admin/activities";
    }
}