package com.armiet.sevatrack.controller;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armiet.sevatrack.model.Participation;
import com.armiet.sevatrack.model.ParticipationStatus;
import com.armiet.sevatrack.model.Student;
import com.armiet.sevatrack.repository.ParticipationRepository;
import com.armiet.sevatrack.repository.StudentRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    private final StudentRepository studentRepository;
    private final ParticipationRepository participationRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public StudentController(
            StudentRepository studentRepository,
            ParticipationRepository participationRepository) {

        this.studentRepository = studentRepository;
        this.participationRepository = participationRepository;
    }


    /* =========================================================
       STUDENT REGISTRATION
       ========================================================= */

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute(
                "student",
                new Student()
        );

        return "register";
    }


    @PostMapping("/register")
    public String registerStudent(
            @ModelAttribute("student") Student student,
            Model model) {

        /*
         * Check whether the ARMIET PIN is already registered.
         */
        if (studentRepository.existsByArmietPin(
                student.getArmietPin())) {

            model.addAttribute(
                    "error",
                    "A student with this ARMIET PIN is already registered."
            );

            return "register";
        }


        /*
         * Encode the password before saving it.
         *
         * The actual password will NOT be stored
         * directly in the database.
         */
        student.setPassword(
                passwordEncoder.encode(
                        student.getPassword()
                )
        );


        studentRepository.save(student);


        model.addAttribute(
                "success",
                "Registration successful! Welcome to ARMIET NSS."
        );


        return "register";
    }


    /* =========================================================
       VOLUNTEER LOGIN
       ========================================================= */

    @GetMapping("/login")
    public String showLoginPage() {

        return "login";
    }


    @PostMapping("/login")
    public String volunteerLogin(
            @RequestParam("armietPin") String armietPin,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {


        Student student = studentRepository
                .findByArmietPin(armietPin)
                .orElse(null);


        /*
         * Check:
         *
         * 1. Student exists
         * 2. Password matches the encrypted password
         */
        if (student != null &&
                student.getPassword() != null &&
                passwordEncoder.matches(
                        password,
                        student.getPassword())) {


            session.setAttribute(
                    "studentId",
                    student.getId()
            );


            return "redirect:/dashboard?studentId="
                    + student.getId();
        }


        model.addAttribute(
                "error",
                "Invalid ARMIET PIN or password."
        );


        return "login";
    }


    /* =========================================================
       VOLUNTEER DASHBOARD
       ========================================================= */

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam Long studentId,
            HttpSession session,
            HttpServletResponse response,
            Model model) {

        /*
         * Prevent the browser and intermediate proxies
         * from storing the dashboard.
         */
        disableCaching(response);


        Long loggedInStudentId =
                (Long) session.getAttribute("studentId");


        if (loggedInStudentId == null ||
                !loggedInStudentId.equals(studentId)) {

            return "redirect:/login";
        }


        Student student = studentRepository
                .findById(studentId)
                .orElseThrow();


        model.addAttribute(
                "student",
                student
        );


        List<Participation> participations =
                participationRepository
                        .findByStudentId(student.getId());


        int totalServiceHours = 0;


        for (Participation participation :
                participations) {


            if (participation.getStatus() ==
                    ParticipationStatus.APPROVED) {


                if (participation.getActivity() != null &&
                        participation.getActivity()
                                .getServiceHours() != null) {


                    totalServiceHours +=
                            participation.getActivity()
                                    .getServiceHours();
                }
            }
        }


        model.addAttribute(
                "totalServiceHours",
                totalServiceHours
        );


        return "dashboard";
    }


    /* =========================================================
       VOLUNTEER LOGOUT
       ========================================================= */

    @GetMapping("/logout")
    public String volunteerLogout(
            HttpSession session,
            HttpServletResponse response) {

        /*
         * Completely destroy the current volunteer session.
         */
        session.invalidate();


        /*
         * Prevent the browser from keeping
         * the logout response cached.
         */
        disableCaching(response);


        return "redirect:/login";
    }


    /**
     * Prevent the browser and intermediate proxies
     * from storing protected pages in cache.
     */
    private void disableCaching(HttpServletResponse response) {

        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate, max-age=0"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );
    }
}