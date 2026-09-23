package com.armiet.sevatrack.controller;

import java.util.List;

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

    public StudentController(
            StudentRepository studentRepository,
            ParticipationRepository participationRepository) {

        this.studentRepository = studentRepository;
        this.participationRepository = participationRepository;
    }

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

        if (studentRepository.existsByRollNumber(
                student.getRollNumber())) {

            model.addAttribute(
                    "error",
                    "A student with this roll number is already registered."
            );

            return "register";
        }

        studentRepository.save(student);

        model.addAttribute(
                "success",
                "Registration successful! Welcome to ARMIET NSS."
        );

        return "register";
    }

    @GetMapping("/login")
    public String showLoginPage() {

        return "login";
    }

    @PostMapping("/login")
    public String volunteerLogin(
            @RequestParam("rollNumber") String rollNumber,
            @RequestParam("email") String email,
            HttpSession session,
            Model model) {

        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElse(null);

        if (student != null &&
                student.getEmail() != null &&
                student.getEmail().equalsIgnoreCase(email)) {

            session.setAttribute(
                    "studentId",
                    student.getId()
            );

            return "redirect:/dashboard?studentId="
                    + student.getId();
        }

        model.addAttribute(
                "error",
                "Invalid Roll Number or Email."
        );

        return "login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam Long studentId,
            HttpSession session,
            HttpServletResponse response,
            Model model) {

        /*
         * Prevent the browser from caching the dashboard.
         * This is important after logout because otherwise
         * the browser may show an old cached dashboard
         * when the user presses Back.
         */
        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );

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

        for (Participation participation : participations) {

            if (participation.getStatus() ==
                    ParticipationStatus.APPROVED) {

                if (participation.getActivity() != null &&
                        participation.getActivity().getServiceHours() != null) {

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

    @GetMapping("/logout")
    public String volunteerLogout(
            HttpSession session,
            HttpServletResponse response) {

        /*
         * Remove the logged-in volunteer from the session.
         */
        session.removeAttribute("studentId");

        /*
         * Tell the browser not to keep the logout response cached.
         */
        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );

        return "redirect:/login";
    }
}