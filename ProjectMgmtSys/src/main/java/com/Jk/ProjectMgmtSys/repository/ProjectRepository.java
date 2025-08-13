package com.Jk.ProjectMgmtSys.repository;

import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Get projects assigned to a specific student
    List<Project> findByStudent(User student);

    // Get projects assigned to a specific guide
    List<Project> findByGuide(User guide);

    // Get projects by status
    List<Project> findByStatus(String status);

    // Get a project by student ID (if only one project per student)
    Project findByStudent_Id(Long studentId);

    // Get all projects assigned to a student ID (if multiple allowed)
    List<Project> findAllByStudent_Id(Long studentId);

    Project getProjectsByStudent(User student);

    Project findByGuideId(Long id);

    @Query("SELECT p FROM Project p WHERE " +
            "(:keyword IS NULL OR LOWER(p.tittle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.domain) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:role IS NULL OR LOWER(p.student.role) = LOWER(:role) OR LOWER(p.guide.role) = LOWER(:role))")
    Page<Project> searchByKeywordAndRole(@Param("keyword") String keyword,
                                         @Param("role") String role,
                                         Pageable pageable);}
