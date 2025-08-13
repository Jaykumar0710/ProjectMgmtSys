package com.Jk.ProjectMgmtSys.controller;

import com.Jk.ProjectMgmtSys.entity.*;
import com.Jk.ProjectMgmtSys.repository.*;
import com.Jk.ProjectMgmtSys.service.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    private static final String DEFAULT_FEEDBACK = "Waiting for guide approval";
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectTeamMemberRepository projectTeamMemberRepository;
    @Autowired private EmailService emailService;
    @Autowired private ProjectDocumentRepository projectDocumentRepository;
    @Autowired private ProjectDocumentService documentService;
    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;
    @Autowired private MilestoneService milestoneService;
    @Autowired private WeeklyProgressReportRepository weeklyProgressReportRepository;
    @Autowired private WeeklyProgressReportServiceImpl weeklyProgressReportService;
    @Autowired private  GrievanceService grievanceService;
    @Autowired private MilestoneDeadlineService milestoneDeadlineService;

    // ---------- Utility Methods ----------
    private User getCurrentStudent(Principal principal) {
        return userRepository.findByEmail(principal.getName());
    }

    private Project getFirstProject(User student) {
        List<Project> projects = projectRepository.findByStudent(student);
        return projects.isEmpty() ? null : projects.get(0);
    }

    // ---------- Dashboard ----------
    @GetMapping("/dashboard")
    public String studentDashboard(Model model, Principal principal) {
        User student = getCurrentStudent(principal);
        List<Project> projects = projectRepository.findByStudent(student);
        projects.forEach(p -> p.setTeamMembers(projectTeamMemberRepository.findByProject(p)));
        model.addAttribute("projects", projects);

        // Get first project
        Project firstProject = getFirstProject(student);

        // Prepare milestone data
        List<Milestone> milestones = (firstProject != null)
                ? milestoneService.getMilestonesByProject(firstProject.getId())
                : List.of();

        // Prepare milestone cards with due date and overdue info
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<Map<String, Object>> milestoneCards = milestones.stream().map(m -> {
            Map<String, Object> card = new HashMap<>();
            card.put("title", m.getTitle());

            MilestoneDeadline deadline = milestoneDeadlineService.getDeadlineByMilestoneId(m.getId());

            if (deadline != null && deadline.getDueDate() != null) {
                LocalDate dueDate = deadline.getDueDate();
                card.put("dueDate", dueDate);
                card.put("dueDateFormatted", dueDate.format(formatter));

                // Overdue if dueDate is before today AND milestone status is NOT completed
                boolean isOverdue = dueDate.isBefore(LocalDate.now())
                        && !"Completed".equalsIgnoreCase(m.getStatus());
                card.put("isOverdue", isOverdue);
            } else {
                card.put("dueDate", null);
                card.put("dueDateFormatted", "No due date");
                card.put("isOverdue", false);
            }
            return card;
        }).toList();

        model.addAttribute("milestoneCards", milestoneCards);

        long notStarted = milestones.stream().filter(m -> "Not Started".equalsIgnoreCase(m.getStatus())).count();
        long inProgress = milestones.stream().filter(m -> "In Progress".equalsIgnoreCase(m.getStatus())).count();
        long completed = milestones.stream().filter(m -> "Completed".equalsIgnoreCase(m.getStatus())).count();

        // Calculate average progress
        int milestoneProgress = 0;
        if (!milestones.isEmpty()) {
            int totalProgress = milestones.stream().mapToInt(Milestone::getProgress).sum();
            milestoneProgress = totalProgress / milestones.size(); // average progress
        }

        model.addAttribute("milestoneStats", Map.of(
                "total", milestones.size(),
                "notStarted", notStarted,
                "inProgress", inProgress,
                "completed", completed
        ));
        model.addAttribute("milestoneProgress", milestoneProgress);

        return "student/student_dashboard";
    }



    // ---------- Project Form ----------
    @GetMapping("/form")
    public String showProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "project_form";
    }

    @GetMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(id).orElse(null);
        User student = getCurrentStudent(principal);

        if (project == null || !project.getStudent().equals(student)) {
            redirectAttributes.addFlashAttribute("error", "Project not found or unauthorized.");
            return "redirect:/student/view";
        }

        if ("Approved".equalsIgnoreCase(project.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Approved project cannot be deleted.");
            return "redirect:/student/view";
        }

        projectRepository.delete(project);
        redirectAttributes.addFlashAttribute("success", "Project deleted successfully.");
        return "redirect:/student/view";
    }



    @GetMapping("/view")
    public String viewProject(Model model, Principal principal) {
        User student = getCurrentStudent(principal);
        List<Project> projects = projectRepository.findByStudent(student);
        projects.forEach(p -> p.setTeamMembers(projectTeamMemberRepository.findByProject(p)));
        model.addAttribute("projects", projects);
        return "student/project_view";
    }


    @PostMapping("/submit")
    public String submitProject(@ModelAttribute Project project,
                                @RequestParam Map<String, String> formData,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        User student = getCurrentStudent(principal);

        // Prevent multiple active submissions
        for (Project p : projectRepository.findByStudent(student)) {
            if ("Pending".equalsIgnoreCase(p.getStatus()) || "Approved".equalsIgnoreCase(p.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "You already have a submitted project.");
                return "redirect:/student/view";
            }
        }

        // Initialize and save project
        project.setStudent(student);
        project.setStatus("Pending");
        project.setFeedback(DEFAULT_FEEDBACK);
        project.setGuide(null);
        Project savedProject = projectRepository.save(project);

        // Team Members
        List<ProjectTeamMember> teamMembers = new ArrayList<>();
        int index = 0;
        while (formData.containsKey("teamMembers[" + index + "].name")) {
            ProjectTeamMember member = new ProjectTeamMember();
            member.setName(formData.get("teamMembers[" + index + "].name"));
            member.setRollNumber(formData.get("teamMembers[" + index + "].rollNumber"));
            member.setEmail(formData.get("teamMembers[" + index + "].email"));
            member.setRoleInProject(formData.get("teamMembers[" + index + "].roleInProject"));
            member.setProject(savedProject);
            teamMembers.add(member);
            index++;
        }
        projectTeamMemberRepository.saveAll(teamMembers);

        // Notify Student
        emailService.sendEmail(
                student.getEmail(),
                "Project Submitted Successfully",
                "Your project \"" + savedProject.getTittle() + "\" has been submitted and is pending guide approval."
        );

        logger.info("Project submitted by {}", student.getEmail());
        redirectAttributes.addFlashAttribute("success", "Project submitted successfully!");
        return "redirect:/student/view";
    }
    // Show Edit Form
    @GetMapping("/edit/member/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProjectTeamMember member = projectTeamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid team member ID: " + id));
        model.addAttribute("member", member);
        return "edit_member";
    }

    // Handle Edit Submission
    @PostMapping("/update/member/{id}")
    public String updateMember(@PathVariable Long id,
                               @ModelAttribute("member") ProjectTeamMember updatedMember,
                               RedirectAttributes redirectAttributes) {
        ProjectTeamMember existing = projectTeamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid team member ID: " + id));

        existing.setName(updatedMember.getName());
        existing.setEmail(updatedMember.getEmail());
        existing.setRollNumber(updatedMember.getRollNumber());
        existing.setRoleInProject(updatedMember.getRoleInProject());

        projectTeamMemberRepository.save(existing);

        redirectAttributes.addFlashAttribute("success", "Team member updated successfully!");
        return "redirect:/student/view";  // Adjust if you redirect elsewhere
    }

    // Delete Member
    @GetMapping("/delete/member/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ProjectTeamMember member = projectTeamMemberRepository.findById(id)
                .orElse(null);
        if (member != null) {
            projectTeamMemberRepository.delete(member);
            redirectAttributes.addFlashAttribute("success", "Team member deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Team member not found!");
        }
        return "redirect:/student/view";
    }
    @GetMapping("/add")
    public String showAddForm(@RequestParam("projectId") Long projectId, Model model) {
        ProjectTeamMember member = new ProjectTeamMember();
        model.addAttribute("member", member);
        model.addAttribute("projectId", projectId);
        return "student/add_member";
    }

    @PostMapping("/add")
    public String addMember(@ModelAttribute("member") ProjectTeamMember member,
                            @RequestParam("projectId") Long projectId,
                            RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        member.setProject(project);
        projectTeamMemberRepository.save(member);

        redirectAttributes.addFlashAttribute("success", "Team member added successfully!");
        return "redirect:/student/view";
    }


    // ---------- Document Upload ----------
    @PostMapping("/project/upload")
    public String uploadDocument(@RequestParam("documentType") String type,
                                 @RequestParam("file") MultipartFile file,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) throws IOException {

        User student = getCurrentStudent(principal);
        Project project = getFirstProject(student);

        if (project == null) {
            redirectAttributes.addFlashAttribute("msg", "No associated project found.");
            return "redirect:/student/documents";
        }

        if (file.isEmpty() || file.getOriginalFilename() == null) {
            redirectAttributes.addFlashAttribute("msg", "Invalid file.");
            return "redirect:/student/documents";
        }

        int version = documentService.getNextVersion(project, type);
        String path = documentService.saveDocument(file, project, type, version);
        documentService.saveToDatabase(project, type, file.getOriginalFilename(), path, version);

        logger.info("Document uploaded: {} by {}", file.getOriginalFilename(), student.getEmail());
        redirectAttributes.addFlashAttribute("msg", "Uploaded successfully!");
        return "redirect:/student/documents";
    }

    @GetMapping("/documents")
    public String getDocuments(Model model, Principal principal) {
        User student = getCurrentStudent(principal);
        Project project = getFirstProject(student);
        List<ProjectDocument> docs = (project != null)
                ? documentService.getDocuments(project)
                : new ArrayList<>();
        model.addAttribute("documents", docs);
        return "student/file_upload";
    }



    // ---------- File Download ----------
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

    // ---------- File Delete ----------
    @GetMapping("/project/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        ProjectDocument doc = projectDocumentRepository.findById(id).orElse(null);
        if (doc != null) {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
            projectDocumentRepository.delete(doc);
            redirectAttributes.addFlashAttribute("msg", "File deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("msg", "File not found.");
        }
        return "redirect:/student/documents";
    }

//    @GetMapping("/document/preview/{id}")
//    public ResponseEntity<Resource> previewPdf(@PathVariable Long id) throws IOException {
//        ProjectDocument doc = projectDocumentRepository.findById(id).orElseThrow();
//        Path path = Paths.get(doc.getFilePath());
//
//        Resource resource = new UrlResource(path.toUri());
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(resource);
//    }

    @GetMapping("/view_milestones")
    public String viewStudentMilestones(Principal principal, Model model) {
        String studentEmail = principal.getName();
        User student = userService.findByEmail(studentEmail);

        // Print student debug info
        System.out.println("Logged in student: " + student.getId() + " - " + student.getName());

        Project project = projectService.getProjectByStudentId(student.getId());

        if (project == null) {
            model.addAttribute("message", "No project assigned yet.");
            model.addAttribute("milestones", List.of()); // Ensure empty list
            return "student/view_milestones";
        }

        System.out.println("Found project: " + project.getId() + " for student");

        List<Milestone> milestones = milestoneService.getMilestonesByProject(project.getId());
        System.out.println("Milestone count: " + milestones.size());

       // model.addAttribute("milestoneCards", milestoneCards);
        Project firstProject = getFirstProject(student);

        // Prepare milestone data


        // Prepare milestone cards with due date and overdue info
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<Map<String, Object>> milestoneCards = milestones.stream().map(m -> {
            Map<String, Object> card = new HashMap<>();
            card.put("title", m.getTitle());

            MilestoneDeadline deadline = milestoneDeadlineService.getDeadlineByMilestoneId(m.getId());

            if (deadline != null && deadline.getDueDate() != null) {
                LocalDate dueDate = deadline.getDueDate();
                card.put("dueDate", dueDate);
                card.put("dueDateFormatted", dueDate.format(formatter));

                // Overdue if dueDate is before today AND milestone status is NOT completed
                boolean isOverdue = dueDate.isBefore(LocalDate.now())
                        && !"Completed".equalsIgnoreCase(m.getStatus());
                card.put("isOverdue", isOverdue);
            } else {
                card.put("dueDate", null);
                card.put("dueDateFormatted", "No due date");
                card.put("isOverdue", false);
            }
            return card;
        }).toList();

        model.addAttribute("milestoneCards", milestoneCards);

        long notStarted = milestones.stream().filter(m -> "Not Started".equalsIgnoreCase(m.getStatus())).count();
        long inProgress = milestones.stream().filter(m -> "In Progress".equalsIgnoreCase(m.getStatus())).count();
        long completed = milestones.stream().filter(m -> "Completed".equalsIgnoreCase(m.getStatus())).count();

        // Calculate average progress
        int milestoneProgress = 0;
        if (!milestones.isEmpty()) {
            int totalProgress = milestones.stream().mapToInt(Milestone::getProgress).sum();
            milestoneProgress = totalProgress / milestones.size(); // average progress
        }

        model.addAttribute("milestoneStats", Map.of(
                "total", milestones.size(),
                "notStarted", notStarted,
                "inProgress", inProgress,
                "completed", completed
        ));
        model.addAttribute("milestoneProgress", milestoneProgress);

        model.addAttribute("milestones", milestones);
        model.addAttribute("projectTitle", project.getTittle()); // optional
        return "student/view_milestones";
    }

    @GetMapping("/weekly_report")
    public String viewWeeklyReportPage(Model model, Principal principal) {
        User student = userService.findByEmail(principal.getName());
        List<WeeklyProgressReport> reports = weeklyProgressReportService.getReportByStudent(student);
        model.addAttribute("reports", reports);
        model.addAttribute("report", new WeeklyProgressReport());
        return "student/weekly_report";
    }


    @PostMapping("/submit_weekly_report")
    public String submitWeeklyReport(@ModelAttribute WeeklyProgressReport report, Principal principal){
        User student = userService.findByEmail(principal.getName());
        Project project =projectService.getProjectByStudentId(student.getId());
        report.setStudent(student);
        report.setGuide(project.getGuide());
        report.setCreatedAt(LocalDateTime.now());
        weeklyProgressReportService.saveReport(report);
        return "redirect:/student/weekly_report";

    }

        // Handle deleting a report
    @GetMapping("/delete_report/{id}")
    public String deleteReport(@PathVariable("id") Long reportId, Principal principal) {
        User student = userService.findByEmail(principal.getName());
        weeklyProgressReportService.deleteReportByIdAndStudent(reportId,student);
        return "redirect:/student/weekly_report";  // Redirect back to the report list after deletion
    }

    @GetMapping("/grievance")
    public String viewStudentGrievance(Model model, Principal principal){
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("grievances", grievanceService.getGrievancesByUser(user));
        return "student-grievance-list";
    }

    @GetMapping("/grievance/new")
    public String newGrievanceForm(Model model){
        model.addAttribute("grievance", new Grievance());
        return "grievance-form";
    }

    @PostMapping("/save")
    public String saveGrievance(@ModelAttribute Grievance grievance, Principal principal){
        User user = userService.findByEmail(principal.getName());
        grievance.setUser(user);
        grievanceService.saveGrievance(grievance);
        return "redirect:/student/grievance";
    }

    @Autowired private NotificationRepository notificationRepository;

    @GetMapping("/notifications")
    public String viewNotifications(@RequestParam(required = false) String readStatus,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) Long senderId,
                                    Model model,
                                    Principal principal) {

        User student = userRepository.findByEmail(principal.getName()); // ✅ Get student from logged-in user
        if (student == null) return "redirect:/login";

        Long studentId = student.getId();

        List<Notification> allNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getReceivers().contains(student))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());

        if (readStatus != null && !readStatus.isBlank()) {
            allNotifications = allNotifications.stream()
                    .filter(n -> {
                        Boolean read = n.getReadStatus().getOrDefault(studentId, false);
                        return "read".equalsIgnoreCase(readStatus) ? read : !read;
                    })
                    .collect(Collectors.toList());
        }

        if (category != null && !category.isBlank()) {
            allNotifications = allNotifications.stream()
                    .filter(n -> category.equalsIgnoreCase(n.getCategory()))
                    .collect(Collectors.toList());
        }

        if (senderId != null) {
            allNotifications = allNotifications.stream()
                    .filter(n -> n.getSender() != null && senderId.equals(n.getSender().getId()))
                    .collect(Collectors.toList());
        }

        Map<Long, Boolean> readStatusMap = allNotifications.stream()
                .collect(Collectors.toMap(Notification::getId, n ->
                        n.getReadStatus().getOrDefault(studentId, false)));

        Set<String> categories = notificationRepository.findAll().stream()
                .map(Notification::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<User> senders = notificationRepository.findAll().stream()
                .map(Notification::getSender)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        model.addAttribute("notifications", allNotifications);
        model.addAttribute("readStatusMap", readStatusMap);
        model.addAttribute("studentName", student.getName());
        model.addAttribute("categories", categories);
        model.addAttribute("senders", senders);

        return "student/notifications";
    }

    @PostMapping("/notifications/mark-read/{id}")
    public String markAsRead(@PathVariable Long id, Principal principal) {
        User student = userRepository.findByEmail(principal.getName());
        if (student == null) return "redirect:/login";

        Long studentId = student.getId();

        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            Notification notification = opt.get();
            Map<Long, Boolean> status = notification.getReadStatus();
            status.put(studentId, true); // ✅ Mark as read
            notification.setReadStatus(status);
            notificationRepository.save(notification);
        }

        return "redirect:/student/notifications";
    }

    @GetMapping("/notifications/download/{id}")
    public ResponseEntity<Resource> downloadNotificationFile(@PathVariable Long id) throws IOException {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IOException("Notification not found."));

        String filePath = notification.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new IOException("No file attached.");
        }

        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found or not readable.");
        }

        String fileName = path.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }










}
