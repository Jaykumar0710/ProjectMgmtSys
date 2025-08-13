package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.entity.WeeklyProgressReport;
import com.Jk.ProjectMgmtSys.repository.WeeklyProgressReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class WeeklyProgressReportServiceImpl implements  WeeklyProgressReportService{

    @Autowired private WeeklyProgressReportRepository reportRepository;
    @Override
    public void saveReport(WeeklyProgressReport report) {
        reportRepository.save(report);
    }

    @Override
    public List<WeeklyProgressReport> getReportByStudent(User Student) {
        return reportRepository.findByStudent(Student);
    }

    @Override
    public List<WeeklyProgressReport> getReportByGuide(User Guide) {
        return reportRepository.findByGuide(Guide);
    }

    @Override
    public WeeklyProgressReport getReportByIdAndStudent(Long id, User student) {
        return reportRepository.findByIdAndStudent(id, student)
                .orElseThrow(() -> new RuntimeException("Report not found or access denied"));
    }

    @Override
    public void deleteReportByIdAndStudent(Long id, User student) {
        WeeklyProgressReport report = getReportByIdAndStudent(id, student);
        reportRepository.delete(report);

    }

    @Override
    public WeeklyProgressReport getById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }
}
