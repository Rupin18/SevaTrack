package com.armiet.sevatrack.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armiet.sevatrack.model.CompletionStatus;
import com.armiet.sevatrack.model.CompletionSubmission;
import com.armiet.sevatrack.model.ParticipationStatus;
import com.armiet.sevatrack.repository.CompletionSubmissionRepository;
import com.armiet.sevatrack.repository.ParticipationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminCompletionController {

    private final CompletionSubmissionRepository completionSubmissionRepository;
    private final ParticipationRepository participationRepository;

    public AdminCompletionController(
            CompletionSubmissionRepository completionSubmissionRepository,
            ParticipationRepository participationRepository) {

        this.completionSubmissionRepository =
                completionSubmissionRepository;

        this.participationRepository =
                participationRepository;
    }

    private boolean isAdminLoggedIn(HttpSession session) {

        Boolean adminLoggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        return adminLoggedIn != null && adminLoggedIn;
    }

    // SHOW COMPLETION SUBMISSIONS
    @GetMapping("/admin/completions")
    public String adminCompletions(
            HttpSession session,
            Model model) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        List<CompletionSubmission> submissions =
                completionSubmissionRepository.findAll();

        model.addAttribute(
                "submissions",
                submissions
        );

        return "admin-completions";
    }

    // APPROVE COMPLETION
    @PostMapping("/admin/completions/approve")
    public String approveCompletion(
            @RequestParam Long submissionId,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        CompletionSubmission submission =
                completionSubmissionRepository
                        .findById(submissionId)
                        .orElseThrow();

        // Mark the completion submission as approved.
        submission.setStatus(
                CompletionStatus.APPROVED
        );

        completionSubmissionRepository.save(
                submission
        );

        /*
         * Once completion is approved,
         * the volunteer's participation becomes approved.
         *
         * The existing dashboard uses APPROVED
         * participation to calculate service hours.
         */
        if (submission.getParticipation() != null) {

            submission.getParticipation().setStatus(
                    ParticipationStatus.APPROVED
            );

            participationRepository.save(
                    submission.getParticipation()
            );
        }

        return "redirect:/admin/completions";
    }

    // REJECT COMPLETION
    @PostMapping("/admin/completions/reject")
    public String rejectCompletion(
            @RequestParam Long submissionId,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        CompletionSubmission submission =
                completionSubmissionRepository
                        .findById(submissionId)
                        .orElseThrow();

        // Mark the completion submission as rejected.
        submission.setStatus(
                CompletionStatus.REJECTED
        );

        completionSubmissionRepository.save(
                submission
        );

        /*
         * Rejection means the activity completion
         * has not been approved.
         *
         * Therefore the participation must remain
         * PENDING so its service hours are not counted.
         */
        if (submission.getParticipation() != null) {

            submission.getParticipation().setStatus(
                    ParticipationStatus.PENDING
            );

            participationRepository.save(
                    submission.getParticipation()
            );
        }

        return "redirect:/admin/completions";
    }
}