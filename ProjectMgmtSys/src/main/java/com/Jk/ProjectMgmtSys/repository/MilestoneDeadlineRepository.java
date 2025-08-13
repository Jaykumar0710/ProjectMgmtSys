package com.Jk.ProjectMgmtSys.repository;

import com.Jk.ProjectMgmtSys.entity.Milestone;
import com.Jk.ProjectMgmtSys.entity.MilestoneDeadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MilestoneDeadlineRepository extends JpaRepository<MilestoneDeadline, Long> {

    List<MilestoneDeadline> findByDueDate(LocalDate dueDate);

    List<MilestoneDeadline> findByDueDateBefore(LocalDate date);

    Optional<MilestoneDeadline> findByMilestone(Milestone milestone);

    // ✅ Fetch milestone, project, and team members (due today)
    @Query("SELECT DISTINCT md FROM MilestoneDeadline md " +
            "JOIN FETCH md.milestone m " +
            "JOIN FETCH m.project p " +
            "JOIN FETCH p.teamMembers tm " +
            "WHERE md.dueDate = :dueDate")
    List<MilestoneDeadline> findByDueDateWithStudents(@Param("dueDate") LocalDate dueDate);

    // ✅ Fetch milestone, project, and team members (overdue)
    @Query("SELECT DISTINCT md FROM MilestoneDeadline md " +
            "JOIN FETCH md.milestone m " +
            "JOIN FETCH m.project p " +
            "JOIN FETCH p.teamMembers tm " +
            "WHERE md.dueDate < :date")
    List<MilestoneDeadline> findOverdueWithStudents(@Param("date") LocalDate date);

    // ✅ Optional: Native query fallback
    @Query(value = "SELECT * FROM milestone_deadline WHERE due_date < :date", nativeQuery = true)
    List<MilestoneDeadline> findOverdueNative(@Param("date") LocalDate date);

    @Query("SELECT md FROM MilestoneDeadline md WHERE md.milestone.id = :milestoneId")
    Optional<MilestoneDeadline> findByMilestoneId(@Param("milestoneId") Long milestoneId);

}
