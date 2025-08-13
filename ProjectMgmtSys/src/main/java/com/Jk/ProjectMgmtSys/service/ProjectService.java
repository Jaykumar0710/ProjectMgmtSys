package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public Project saveProject(Project project){
        return  projectRepository.save(project);
    }

    public Optional<Project> getProjectById(Long id){
        return  projectRepository.findById(id);
    }

    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    public List<Project> getProjectByStudent(User student){
        return projectRepository.findByStudent(student);
    }

    public List<Project> getProjectByGuide(User guide){
        return projectRepository.findByGuide(guide);
    }
    public void assignGuide(Project project, User guide){
        project.getGuide();
        projectRepository.save(project);
    }


    public Project getProjectByStudentId(Long studentId) {
        return projectRepository.findByStudent_Id(studentId);
    }
    public Page<Project> searchAndFilter(String keyword, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return projectRepository.searchByKeywordAndRole(keyword, role, pageable);
        } else {
            return projectRepository.findAll(pageable);
        }
    }
}


