package com.armiet.sevatrack.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.armiet.sevatrack.model.CompletionStatus;
import com.armiet.sevatrack.model.CompletionSubmission;
import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.repository.CompletionSubmissionRepository;
import com.armiet.sevatrack.repository.ParticipationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CompletionSubmissionController {

    private final CompletionSubmissionRepository completionSubmissionRepository;
    private final ParticipationRepository participationRepository;

    public CompletionSubmissionController(
            CompletionSubmissionRepository completionSubmissionRepository,
            ParticipationRepository participationRepository) {

        this.completionSubmissionRepository =
                completionSubmissionRepository;

        this.participationRepository =
                participationRepository;
    }

    // SUBMIT OR RESUBMIT ACTIVITY COMPLETION
    @PostMapping("/activities/submit-completion")
    public String submitCompletion(
            @RequestParam Long participationId,
            @RequestParam String description,
            @RequestParam("photo") MultipartFile photo,
            HttpSession session) {

        Long loggedInStudentId =
                (Long) session.getAttribute("studentId");

        if (loggedInStudentId == null) {
            return "redirect:/login";
        }

        Participation participation =
                participationRepository
                        .findById(participationId)
                        .orElseThrow();

        // Make sure this participation belongs
        // to the currently logged-in volunteer.
        if (!participation.getStudent()
                .getId()
                .equals(loggedInStudentId)) {

            return "redirect:/login";
        }

        /*
         * Check whether a completion submission already exists.
         *
         * A volunteer may submit only once while the submission
         * is SUBMITTED or APPROVED.
         *
         * If the admin rejected it, the same submission can be
         * updated and submitted again.
         */
        CompletionSubmission submission =
                completionSubmissionRepository
                        .findByParticipationId(participationId)
                        .orElse(null);

        if (submission != null &&
                submission.getStatus() != CompletionStatus.REJECTED) {

            return "redirect:/my-activities?studentId="
                    + loggedInStudentId;
        }

        String photoPath = null;

        // SAVE NEW PHOTO
        if (photo != null && !photo.isEmpty()) {

            try {

                String originalFileName =
                        photo.getOriginalFilename();

                String extension = "";

                if (originalFileName != null &&
                        originalFileName.contains(".")) {

                    extension =
                            originalFileName.substring(
                                    originalFileName.lastIndexOf(".")
                            );
                }

                String fileName =
                        UUID.randomUUID().toString()
                                + extension;

                Path uploadDirectory =
                        Paths.get(
                                "src/main/resources/static/uploads"
                        );

                Files.createDirectories(
                        uploadDirectory
                );

                Path filePath =
                        uploadDirectory.resolve(fileName);

                Files.copy(
                        photo.getInputStream(),
                        filePath
                );

                photoPath =
                        "/uploads/" + fileName;

            } catch (IOException e) {

                e.printStackTrace();

                return "redirect:/my-activities?studentId="
                        + loggedInStudentId
                        + "&error=upload";
            }
        }

        // CREATE FIRST SUBMISSION
        if (submission == null) {

            submission =
                    new CompletionSubmission();

            submission.setParticipation(
                    participation
            );
        }

        // UPDATE / RESET SUBMISSION
        submission.setDescription(
                description
        );

        submission.setSubmittedAt(
                LocalDateTime.now()
        );

        submission.setStatus(
                CompletionStatus.SUBMITTED
        );

        if (photoPath != null) {
            submission.setPhotoPath(photoPath);
        }

        completionSubmissionRepository.save(
                submission
        );

        return "redirect:/my-activities?studentId="
                + loggedInStudentId
                + "&success=submitted";
    }
}
