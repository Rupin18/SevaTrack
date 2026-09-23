package com.armiet.sevatrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armiet.sevatrack.model.Activity;
import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.model.ParticipationStatus;
import com.armiet.sevatrack.model.Student;
import com.armiet.sevatrack.repository.ActivityRepository;
import com.armiet.sevatrack.repository.ParticipationRepository;
import com.armiet.sevatrack.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ParticipationController {

    private final ParticipationRepository participationRepository;
    private final StudentRepository studentRepository;
    private final ActivityRepository activityRepository;

    public ParticipationController(
            ParticipationRepository participationRepository,
            StudentRepository studentRepository,
            ActivityRepository activityRepository) {

        this.participationRepository = participationRepository;
        this.studentRepository = studentRepository;
        this.activityRepository = activityRepository;
    }


    // ================= JOIN ACTIVITY =================

    @PostMapping("/activities/join")
    public String joinActivity(
            @RequestParam Long studentId,
            @RequestParam Long activityId,
            HttpSession session) {

        // CHECK VOLUNTEER LOGIN SESSION

        Long loggedInStudentId =
                (Long) session.getAttribute("studentId");

        if (loggedInStudentId == null ||
                !loggedInStudentId.equals(studentId)) {

            return "redirect:/login";
        }


        // CHECK IF STUDENT ALREADY JOINED

        if (participationRepository
                .existsByStudentIdAndActivityId(studentId, activityId)) {

            return "redirect:/activities?studentId="
                    + studentId
                    + "&error=alreadyJoined";
        }


        // FIND STUDENT

        Student student = studentRepository
                .findById(studentId)
                .orElseThrow();


        // FIND ACTIVITY

        Activity activity = activityRepository
                .findById(activityId)
                .orElseThrow();


        // CREATE PARTICIPATION

        Participation participation = new Participation();

        participation.setStudent(student);

        participation.setActivity(activity);


        // NEW PARTICIPATION STARTS AS PENDING

        participation.setStatus(
                ParticipationStatus.PENDING
        );


        // SAVE PARTICIPATION

        participationRepository.save(participation);


        // REDIRECT BACK TO ACTIVITIES

        return "redirect:/activities?studentId="
                + studentId
                + "&success=joined";
    }

}