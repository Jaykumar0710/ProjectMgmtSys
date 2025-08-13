package com.Jk.ProjectMgmtSys.controller;

import com.Jk.ProjectMgmtSys.entity.*;
import com.Jk.ProjectMgmtSys.repository.MilestoneDeadlineRepository;
import com.Jk.ProjectMgmtSys.repository.ProjectDocumentRepository;
import com.Jk.ProjectMgmtSys.repository.ProjectRepository;
import com.Jk.ProjectMgmtSys.repository.UserRepository;
import com.Jk.ProjectMgmtSys.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/guide")
public class GuideController {

    @Autowired
    private ProjectRepository projectRepository;


    @Autowired
    private ProjectDocumentRepository projectDocumentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private MilestoneDeadlineRepository milestoneDeadlineRepository;

    @Autowired
    private MilestoneDeadlineService milestoneDeadlineService;

    @Autowired private WeeklyProgressReportServiceImpl reportService;

    @GetMapping("/dashboard")
    public String guideDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model){
        User guide = userService.findByEmail(userDetails.getUsername());
        List<Project> assignedProjects = projectService.getProjectByGuide(guide);
        model.addAttribute("guide", guide);
        model.addAttribute("projects", assignedProjects);

        return "guide_dashboard";
    }



    @GetMapping("/assigned")
    public String viewAssignedProjects(Model model, Principal principal) {
        User guide = userRepository.findByEmail(principal.getName());
        List<Project> projects = projectRepository.findByGuide(guide);
        model.addAttribute("projects", projects);
        return "project_assigned";  // Thymeleaf template
    }

    @PostMapping("/approve/{id}")
    public String approvedProject(@PathVariable Long id, @RequestParam String feedback) {
        Project project = projectRepository.findById(id).orElseThrow();
        project.setStatus("Approved");
        project.setFeedback(feedback);
        projectRepository.save(project);

        // Prepare HTML email content
        Context context = new Context();
        context.setVariable("subject", "Project Approved");
        context.setVariable("heading", "🎉 Congratulations!");
        context.setVariable("bodyText", "Your project has been successfully approved.");
        context.setVariable("projectTitle", project.getTittle());
        context.setVariable("feedback", feedback);

        String htmlContent = templateEngine.process("email-template", context);

        // Send Email
        emailService.sendHtmlEmail(
                project.getStudent().getEmail(),
                "Project Approved - " + project.getTittle(),
                htmlContent
        );

        return "redirect:/guide/assigned";
    }

    @PostMapping("/reject/{id}")
    public String rejectProject(@PathVariable Long id, @RequestParam String feedback) {
        Project project = projectRepository.findById(id).orElseThrow();
        project.setStatus("Rejected");
        project.setFeedback(feedback);
        projectRepository.save(project);

        // Prepare HTML email content
        Context context = new Context();
        context.setVariable("subject", "Project Rejected");
        context.setVariable("heading", "⚠️ Project Rejected");
        context.setVariable("bodyText", "Unfortunately, your project has been rejected.");
        context.setVariable("projectTitle", project.getTittle());
        context.setVariable("feedback", feedback);

        String htmlContent = templateEngine.process("email-template", context);

        // Send Email
        emailService.sendHtmlEmail(
                project.getStudent().getEmail(),
                "Project Rejected - " + project.getTittle(),
                htmlContent
        );

        return "redirect:/guide/assigned";
    }


    @GetMapping("/documents")
    public String  viewStudentDocuments(@AuthenticationPrincipal UserDetails userDetails, Model model){
        User guide = userService.findByEmail(userDetails.getUsername());
        List<Project> assignedProjects = projectService.getProjectByGuide(guide);

        model.addAttribute("projects", assignedProjects);
        return "guide/view_documents";
    }

    @GetMapping("/project/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        ProjectDocument doc = projectDocumentRepository.findById(id)
                .orElseThrow(() -> new IOException("Document not found."));
        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }
//    @GetMapping("/download/{id}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
//        ProjectDocument document = projectDocumentRepository.findById(id).orElseThrow();
//        Path filePath = Paths.get(document.getFilePath());
//        Resource resource = new UrlResource(filePath.toUri());
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
//                .body(resource);
//    }
    @GetMapping("/view_milestones/{projectId}")
    public String viewMilestones(@PathVariable Long projectId, Principal principal, Model model) {
        User guide = userService.findByEmail(principal.getName());
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        List<Milestone> milestones = milestoneService.getMilestonesByProject(projectId);
        model.addAttribute("milestones", milestones);
        model.addAttribute("projectId", projectId);
        return "guide/view_milestones";
    }

    @GetMapping("/add_milestones/{projectId}")
    public String addMilestonesForm(@PathVariable Long projectId, Principal principal, Model model) {
        User guide = userService.findByEmail(principal.getName());
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        Milestone milestone = new Milestone();
        milestone.setProject(project);
        model.addAttribute("milestone", milestone);
        return "guide/add_milestone";
    }

    @PostMapping("/save_milestone")
    public String saveMilestone(@ModelAttribute Milestone milestone, Principal principal) {
        User guide = userService.findByEmail(principal.getName());
        Project project = projectService.getProjectById(milestone.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        milestone.setProject(project);
        milestoneService.saveMilestone(milestone);
        return "redirect:/guide/view_milestones/" + project.getId();
    }

    // GET: Show Edit Milestone Form
    @GetMapping("/edit_milestone/{milestoneId}")
    public String editMilestoneForm(@PathVariable Long milestoneId, Principal principal, Model model) {
        User guide = userService.findByEmail(principal.getName());

        Milestone milestone = milestoneService.getMilestoneById(milestoneId);
        if (milestone == null) {
            throw new RuntimeException("Milestone not found");
        }

        Project project = milestone.getProject();
        if (!project.getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        model.addAttribute("milestone", milestone);
        return "guide/edit_milestone";
    }

    // POST: Save Edited Milestone
    @PostMapping("/update_milestone")
    public String updateMilestone(@ModelAttribute Milestone milestone, Principal principal) {
        User guide = userService.findByEmail(principal.getName());

        Project project = projectService.getProjectById(milestone.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        milestone.setProject(project);
        milestoneService.saveMilestone(milestone); // acts as update
        return "redirect:/guide/view_milestones/" + project.getId();
    }
    @GetMapping("/delete_milestone/{milestoneId}")
    public String deleteMilestone(@PathVariable Long milestoneId, Principal principal) {
        User guide = userService.findByEmail(principal.getName());

        Milestone milestone = milestoneService.getMilestoneById(milestoneId);
        if (milestone == null) {
            throw new RuntimeException("Milestone not found");
        }

        Project project = milestone.getProject();
        if (!project.getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        milestoneService.deleteMilestoneById(milestoneId);
        return "redirect:/guide/view_milestones/" + project.getId();
    }

    // Show the form to set or update deadline
    @GetMapping("/set_deadline/{milestoneId}")
    public String showDeadlineForm(@PathVariable Long milestoneId, Principal principal, Model model) {
        User guide = userService.findByEmail(principal.getName());
        Milestone milestone = milestoneService.getMilestoneById(milestoneId);

        if (milestone == null || !milestone.getProject().getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        MilestoneDeadline deadline = milestoneDeadlineService.getDeadlineByMilestoneId(milestoneId);
        model.addAttribute("milestone", milestone);
        model.addAttribute("deadline", deadline); // could be null
        return "guide/add_deadline";

    }

    // Save or update the deadline
    @PostMapping("/save_deadline")
    public String saveDeadline(@RequestParam Long milestoneId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                               Principal principal) {

        User guide = userService.findByEmail(principal.getName());
        Milestone milestone = milestoneService.getMilestoneById(milestoneId);

        if (milestone == null || !milestone.getProject().getGuide().getId().equals(guide.getId())) {
            return "access-denied";
        }

        milestoneDeadlineService.saveOrUpdateDeadline(milestone, dueDate);

        Project project = milestone.getProject();
        List<ProjectTeamMember> teamMembers = project.getTeamMembers();

        // Prepare Thymeleaf context for email template
        Context context = new Context();
        context.setVariable("heading", "📅 New Milestone Due Date Set");
        context.setVariable("milestoneTitle", milestone.getTitle());
        context.setVariable("projectTitle", project.getTittle());
        context.setVariable("dueDate", dueDate.toString());

        String subject = "New Milestone Due Date Set - " + milestone.getTitle();

        // Generate the HTML content using Thymeleaf template engine
        String htmlContent = templateEngine.process("due-today-notification", context);

        // ✅ Loop through team members now, not students
        for (ProjectTeamMember member : teamMembers) {
            if (member.getEmail() != null && !member.getEmail().isEmpty()) {
                System.out.println("Sending email to: " + member.getEmail());
                emailService.sendHtmlEmail(member.getEmail(), subject, htmlContent);
            } else {
                System.out.println("Skipping team member with empty email");
            }
        }

        return "redirect:/guide/view_milestones/" + project.getId();
    }

    @GetMapping("/review_reports")
    public String reviewStudentReports(Model model, Principal principal){
        User guide = userService.findByEmail(principal.getName());
        List<WeeklyProgressReport> reports = reportService.getReportByGuide(guide);
        model.addAttribute("reports", reports);
        return "guide/review_reports";
    }

    @PostMapping("/submit_feedback/{id}")
    public String submitFeedback(@PathVariable Long id, @RequestParam String feedback){
        WeeklyProgressReport report = reportService.getById(id);
        if (report != null ){
            report.setGuideFeedback( feedback);
            reportService.saveReport(report);

            Context context = new Context();
            context.setVariable("studentName", report.getStudent().getName());
            context.setVariable("weekNumber", report.getWeekNumber());
            context.setVariable("feedback", feedback);
            context.setVariable("year", LocalDate.now().getYear());

            String body = templateEngine.process("feedback-email-template", context);

            emailService.sendHtmlEmail(
                    report.getStudent().getEmail(),
                    "Feedback on Weekly Progress Report",
                    body
            );


        }
        return "redirect:/guide/review_reports";

    }













}


