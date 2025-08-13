package com.Jk.ProjectMgmtSys.controller;

import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.ProjectRepository;
import com.Jk.ProjectMgmtSys.repository.UserRepository;
import com.Jk.ProjectMgmtSys.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine; // ✅ Updated for Spring Boot 3+

import java.util.List;

@Controller
public class DashboardController {

    // ---------- STUDENT ----------
//    @Controller
//    @RequestMapping("/student")
//    public static class StudentController {
//        @GetMapping("/dashboard")
//        public String studentDashboard() {
//            return "student_dashboard";
//        }
//    }

    // ---------- GUIDE ----------
//    @Controller
//    @RequestMapping("/guide")
//    public static class GuideController {
//        @GetMapping("/dashboard")
//        public String guideDashboard() {
//            return "guide_dashboard";
//        }
//    }

    // ---------- ADMIN ----------
//    @Controller
//    @RequestMapping("/admin")
//    public static class AdminController {
//
//        @Autowired
//        private ProjectRepository projectRepository;
//
//        @Autowired
//        private UserRepository userRepository;
//
//        @Autowired
//        private EmailService emailService;
//
//        @Autowired
//        private SpringTemplateEngine templateEngine; // ✅ For Thymeleaf 3.1 / Spring Boot 3

//        @PostMapping("/assign-guide")
//        public String assignGuideToProject(@RequestParam Long projectId,
//                                           @RequestParam Long guideId,
//                                           Model model) {
//
//            Project project = projectRepository.findById(projectId).orElse(null);
//            User guide = userRepository.findById(guideId).orElse(null);
//
//            if (project == null || guide == null) {
//                model.addAttribute("error", "Project or Guide not found.");
//                return "redirect:/admin/assign-guide-page?error";
//            }
//
//            project.setGuide(guide);
//            project.setStatus("Assigned");
//            projectRepository.save(project);
//
//            // Email context for Thymeleaf
//            Context context = new Context();
//            context.setVariable("subject", "New Project Assigned");
//            context.setVariable("heading", "👨‍🏫 New Assignment");
//            context.setVariable("bodyText", "A new project has been assigned to you.");
//            context.setVariable("projectTitle", project.getTittle());
//
//            // Use HTML template
//            String htmlContent = templateEngine.process("email-template", context);
//            emailService.sendHtmlEmail(guide.getEmail(), "New Project Assigned", htmlContent);
//
//            return "redirect:/admin/assign-guide-page?success";
//        }
//
//        @GetMapping("/assign-guide-page")
//        public String showAssignGuidePage(Model model) {
//            List<Project> projects = projectRepository.findByStatus("Pending");
//            List<User> guides = userRepository.findByRole("GUIDE");
//            model.addAttribute("projects", projects);
//            model.addAttribute("guides", guides);
//            return "admin/assign_guide";
//        }
//
//        @GetMapping("/dashboard")
//        public String adminDashboard() {
//            return "admin_dashboard";
//        }
//    }
}

