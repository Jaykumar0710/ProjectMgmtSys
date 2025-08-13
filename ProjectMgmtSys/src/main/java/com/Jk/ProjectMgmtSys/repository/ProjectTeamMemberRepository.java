package com.Jk.ProjectMgmtSys.repository;

import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.ProjectTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTeamMemberRepository extends JpaRepository<ProjectTeamMember, Long> {
    List<ProjectTeamMember> findByProject(Project project);

}
