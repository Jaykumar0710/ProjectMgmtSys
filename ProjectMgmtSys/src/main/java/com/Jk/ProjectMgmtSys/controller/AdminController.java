package com.Jk.ProjectMgmtSys.controller;


import com.Jk.ProjectMgmtSys.entity.Grievance;
import com.Jk.ProjectMgmtSys.entity.NotificationRequest;
import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.ProjectRepository;
import com.Jk.ProjectMgmtSys.repository.UserRepository;
import com.Jk.ProjectMgmtSys.service.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        String username = principal.getName();
        String email = principal.getName();

        User user = userService.findByEmail(username);
        model.addAttribute("adminName", username);
        return "admin_dashboard";
    }

    @Autowired private PasswordEncoder passwordEncoder;
    @PostMapping("/assign-guide")
    public String assignGuideToProject(@RequestParam Long projectId,
                                       @RequestParam Long guideId,
                                       Model model) {

        Project project = projectRepository.findById(projectId).orElse(null);
        User guide = userRepository.findById(guideId).orElse(null);

        if (project == null || guide == null) {
            model.addAttribute("error", "Project or Guide not found.");
            return "redirect:/admin/assign-guide-page?error";
        }

        project.setGuide(guide);
        project.setStatus("Assigned");
        projectRepository.save(project);

        // Email context for Thymeleaf
        Context context = new Context();
        context.setVariable("subject", "New Project Assigned");
        context.setVariable("heading", "👨‍🏫 New Assignment");
        context.setVariable("bodyText", "A new project has been assigned to you.");
        context.setVariable("projectTitle", project.getTittle());

        // Use HTML template
        String htmlContent = templateEngine.process("email-template", context);
        emailService.sendHtmlEmail(guide.getEmail(), "New Project Assigned", htmlContent);

        return "redirect:/admin/assign-guide-page?success";
    }

    @GetMapping("/assign-guide-page")
    public String showAssignGuidePage(Model model) {
        List<Project> projects = projectRepository.findByStatus("Pending");
        List<User> guides = userRepository.findByRole("GUIDE");
        model.addAttribute("projects", projects);
        model.addAttribute("guides", guides);
        return "admin/assign_guide";
    }



    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            Model model
    ){
        Page<User> userPage = userService.searchAndFilter(keyword,role,page,size);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalUsers", userPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        return "admin/admin-users";
    }

    @GetMapping("/projects")
    public String listProject(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            Model model
    ){
        Page<Project> projectPage = projectService.searchAndFilter(keyword, role, page, size);

        model.addAttribute("project", projectPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("totalUsers", projectPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        return "admin/projects";
    }


    //Add User Form
    @GetMapping("users/new")
    public String showAddUserForm(Model model){
        model.addAttribute("user", new User());
        return "admin/admin-add-user";
    }

//Save user
    @PostMapping("/users")
    public String saveUser(@ModelAttribute("user") User user, Model model) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    //Edit Form User
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "admin/admin-edit-user";
        } else {
            model.addAttribute("message", "User not found");
            return "error";
        }
    }
    //Update User
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute("user") User user) {
        userService.updateUser(user); // make sure this method updates by id
        return "redirect:/admin/users";
    }
    //Delete Users
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        // Get current user
        User currentUser = userService.findByEmail(principal.getName());

        try {
            userService.deleteUser(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    //grievance


    @GetMapping("/grievances")
    public String showAllGrievances(Model model) {
        List<Grievance> grievances = grievanceService.findAll();
        long pendingCount = grievanceService.countByStatus("Pending");
        long resolvedCount = grievanceService.countByStatus("Resolved");

        model.addAttribute("grievances", grievances);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("resolvedCount", resolvedCount);
        return "admin-grievance-list";
    }


    @GetMapping("/grievance/resolve")
    public String resolveGrievanceForm(@RequestParam Long id, Model model) {
        model.addAttribute("grievance", grievanceService.getGrievanceById(id));
        return "grievance-resolve-form";
    }
    @GetMapping("/grievance/resolve/{id}")
    public String showResolutionForm(@PathVariable Long id, Model model) {
        Grievance grievance = grievanceService.getGrievanceById(id);
        model.addAttribute("grievance", grievance);
        return "grievance-resolve-form";// Make this Thymeleaf HTML page
    }

    @PostMapping("/grievance/resolve")
    public String submitResolution(@ModelAttribute Grievance grievance) {
        // Retain original grievance to avoid losing data like user
        Grievance existing = grievanceService.getGrievanceById(grievance.getId());
        existing.setResolution(grievance.getResolution());
        existing.setStatus("Resolved");
        grievanceService.saveGrievance(existing);

        // Send email notification to student
//        sendResolutionEmail(existing);

        return "redirect:/admin/grievances";
    }

//    private void sendResolutionEmail(Grievance grievance) {
//        User student = grievance.getStudent();
//        if (student == null || student.getEmail() == null || student.getEmail().isEmpty()) {
//            System.out.println("Student or student email is missing, skipping email notification.");
//            return;
//        }
//
//        MimeMessage message = mailSender.createMimeMessage();
//
//        try {
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setTo(student.getEmail());
//            helper.setSubject("Your Grievance has been Resolved");
//
//            String htmlMsg = "<html>" +
//                    "<body>" +
//                    "<h3>Hello,</h3>" +
//                    "<p>Your grievance with ID <strong>" + grievance.getId() + "</strong> has been resolved.</p>" +
//                    "<h4>Resolution Details:</h4>" +
//                    "<p>" + grievance.getResolution() + "</p>" +
//                    "<br>" +
//                    "<p>Thank you.</p>" +
//                    "</body>" +
//                    "</html>";
//
//            helper.setText(htmlMsg, true);
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/send-notification")
    public String showSendNotificationForm(Model model) {
        model.addAttribute("notificationRequest", new NotificationRequest());
        model.addAttribute("students", userRepository.findByRole("STUDENT")); // populate student list
        return "admin/send-notification"; // Thymeleaf template
    }

    @PostMapping("/send-notification")
    public String sendNotification(@ModelAttribute NotificationRequest request,
                                   @RequestParam("file") MultipartFile file,  // get uploaded file
                                   RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                // Save file to your server
                String uploadsDir = "/uploads/";
                // This is just an example path; adjust as needed:
                String realPathToUploads = new File("uploads").getAbsolutePath();
                File uploadDir = new File(realPathToUploads);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String originalFilename = file.getOriginalFilename();
                String filePath = realPathToUploads + File.separator + originalFilename;
                File dest = new File(filePath);
                file.transferTo(dest);  // save file

                // Set saved file path to your NotificationRequest
                request.setFilePath(filePath);
            }

            notificationService.sendNotification(request);
            redirectAttributes.addFlashAttribute("successMessage", "Notification sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error sending notification.");
            e.printStackTrace();
        }
        return "redirect:/admin/send-notification";
    }



}

