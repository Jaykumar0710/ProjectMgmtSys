package com.Jk.ProjectMgmtSys.controller;

import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.ProjectTeamMember;
import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.ProjectRepository;
import com.Jk.ProjectMgmtSys.repository.ProjectTeamMemberRepository;
import com.Jk.ProjectMgmtSys.repository.UserRepository;
import com.Jk.ProjectMgmtSys.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student/project")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectTeamMemberRepository projectTeamMemberRepository;

    @Autowired
    private EmailService emailService;

//    @GetMapping("/form")
//    public String showProjectForm(Model model) {
//        model.addAttribute("project", new Project());
//        return "project_form"; //
//    }

//    @PostMapping("/submit")
//    public String submitProject(@ModelAttribute Project project,
//                                @RequestParam Map<String, String> formData,
//                                Principal principal) {
//
//        User student = userRepository.findByEmail(principal.getName());
//
//        // OPTIONAL: Check if student already has a Pending or Approved project
//        List<Project> existingProjects = projectRepository.findByStudent(student);
//        for (Project p : existingProjects) {
//            if (p.getStatus().equalsIgnoreCase("Pending") || p.getStatus().equalsIgnoreCase("Approved")) {
//                // Redirect with message: You already have an active project
//                return "redirect:/student/project/view?error=already-submitted";
//            }
//        }
//
//        // Set default project fields
//        project.setStudent(student);
//        project.setStatus("Pending");
//        project.setFeedback("Waiting for guide approval");
//        project.setGuide(null); // Initially, guide is not assigned
//
//        // Save project first to get ID
//        Project savedProject = projectRepository.save(project);
//
//        // Collect team member data
//        List<ProjectTeamMember> teamMembers = new ArrayList<>();
//        int index = 0;
//
//        while (formData.containsKey("teamMembers[" + index + "].name")) {
//            ProjectTeamMember member = new ProjectTeamMember();
//            member.setName(formData.get("teamMembers[" + index + "].name"));
//            member.setRollNumber(formData.get("teamMembers[" + index + "].rollNumber"));
//            member.setEmail(formData.get("teamMembers[" + index + "].email"));
//            member.setRoleInProject(formData.get("teamMembers[" + index + "].roleInProject"));
//            member.setProject(savedProject);
//
//            teamMembers.add(member);
//            index++;
//        }
//
//        // Save all team members
//        projectTeamMemberRepository.saveAll(teamMembers);
//        // Notify student
//        emailService.sendEmail(
//                student.getEmail(),
//                "Project Submitted Successfully",
//                "Your project \"" + savedProject.getTittle() + "\" has been submitted and is pending guide approval."
//        );
//
//// OPTIONAL: notify admin or guide pool if applicable
//
//
//        return "redirect:/student/project/view?success=submitted";
//    }



//    @GetMapping("/view")
//    public String viewProject(Model model, Principal principal){
//        User student = userRepository.findByEmail(principal.getName());
//        List<Project> projects = projectRepository.findByStudent(student);
//
//        // Eager fetch team members or initialize them manually if lazy
//        for (Project project : projects) {
//            List<ProjectTeamMember> members = projectTeamMemberRepository.findByProject(project);
//            project.setTeamMembers(members); // Ensure your Project entity has setTeamMembers()
//        }
//
//        model.addAttribute("projects", projects);
//        return "student/project_view";
//    }


    @GetMapping("/projects")
    public String viewStudentProjects(Model model,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        model.addAttribute("projects", projectRepository.getProjectsByStudent(user));
        return "student/project_list"; // templates/student/project_list.html
    }
}
