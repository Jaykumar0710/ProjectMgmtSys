package com.Jk.ProjectMgmtSys.repository;

import com.Jk.ProjectMgmtSys.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByProject_Id(Long ProjectId);

}
