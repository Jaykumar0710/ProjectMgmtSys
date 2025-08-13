package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.Milestone;
import com.Jk.ProjectMgmtSys.entity.MilestoneDeadline;
import com.Jk.ProjectMgmtSys.repository.MilestoneDeadlineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MilestoneDeadlineService {

    @Autowired
    private MilestoneDeadlineRepository repository;

    public void saveOrUpdateDeadline(Milestone milestone, LocalDate dueDate) {
        MilestoneDeadline deadline = repository.findByMilestone(milestone)
                .orElse(new MilestoneDeadline());
        deadline.setMilestone(milestone);
        deadline.setDueDate(dueDate);
        repository.save(deadline);
    }

    public List<MilestoneDeadline> getDueTodayWithStudents() {
        LocalDate today = LocalDate.now();
        List<MilestoneDeadline> list = repository.findByDueDateWithStudents(today);
        System.out.println("getDueTodayWithStudents - found " + list.size());
        return list;
    }

    public List<MilestoneDeadline> getOverdueWithStudents() {
        LocalDate today = LocalDate.now();
        List<MilestoneDeadline> list = repository.findOverdueWithStudents(today);
        System.out.println("getOverdueWithStudents - found " + list.size());
        return list;
    }

    public MilestoneDeadline getDeadlineByMilestoneId(Long milestoneId) {
        return repository.findByMilestoneId(milestoneId).orElse(null);
    }

}
