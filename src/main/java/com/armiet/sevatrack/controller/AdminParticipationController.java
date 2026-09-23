package com.armiet.sevatrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.model.ParticipationStatus;
import com.armiet.sevatrack.repository.ParticipationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminParticipationController {

    private final ParticipationRepository participationRepository;

    public AdminParticipationController(
            ParticipationRepository participationRepository) {

        this.participationRepository = participationRepository;
    }


    // ================= ADMIN PARTICIPATION PAGE =================

    @GetMapping("/admin/participations")
    public String adminParticipations(
            HttpSession session,
            Model model) {

        Boolean adminLoggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        model.addAttribute(
                "participations",
                participationRepository.findAll()
        );

        return "admin-participations";
    }


    // ================= APPROVE PARTICIPATION =================

    @PostMapping("/admin/participations/approve")
    public String approveParticipation(
            @RequestParam Long participationId,
            HttpSession session) {

        Boolean adminLoggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        Participation participation =
                participationRepository
                        .findById(participationId)
                        .orElseThrow();

        participation.setStatus(
                ParticipationStatus.APPROVED
        );

        participationRepository.save(participation);

        return "redirect:/admin/participations";
    }

}