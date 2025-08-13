package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.entity.WeeklyProgressReport;

import java.util.List;

public interface WeeklyProgressReportService {

    void saveReport(WeeklyProgressReport report);
    List<WeeklyProgressReport> getReportByStudent(User Student);
    List<WeeklyProgressReport> getReportByGuide(User Guide);
    WeeklyProgressReport getReportByIdAndStudent(Long id, User student);

    // ✅ Delete a report by ID + student (ownership check)
    void deleteReportByIdAndStudent(Long id, User student);
    WeeklyProgressReport getById(Long id);
}
