package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.Milestone;
import com.Jk.ProjectMgmtSys.repository.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;

    public List<Milestone> getMilestonesByProject(Long projectId){
        return milestoneRepository.findByProject_Id(projectId);
    }

    public Milestone saveMilestone(Milestone milestone){
        return  milestoneRepository.save(milestone);
    }

    public Milestone getMilestoneById(Long id){
        return milestoneRepository.findById(id).orElse(null);
    }

    public void deleteMilestone(Long id){
        milestoneRepository.deleteById(id);
    }

    public void deleteMilestoneById(Long milestoneId) {
        milestoneRepository.deleteById(milestoneId);
    }
}
