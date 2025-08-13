package com.Jk.ProjectMgmtSys.repository;

import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.entity.WeeklyProgressReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeeklyProgressReportRepository extends JpaRepository<WeeklyProgressReport, Long> {
    List<WeeklyProgressReport> findByStudent(User student);
    List<WeeklyProgressReport> findByGuide(User guide);

    Optional<WeeklyProgressReport> findByIdAndStudent(Long id, User student);
}
