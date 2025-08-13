package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.Grievance;
import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.GrievanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class GrievanceService {

    @Autowired
    private GrievanceRepository grievanceRepository;

    public List<Grievance> getAllGrievances(){
        return grievanceRepository.findAll();
    }

    public Grievance getGrievanceById(Long id){
        return grievanceRepository.findById(id).orElse(null);
    }

    public List<Grievance> getGrievancesByUser(User user){
        return grievanceRepository.findByUser(user);
    }

    public Grievance saveGrievance(Grievance grievance){
        return grievanceRepository.save(grievance);
    }

    public void  deleteGrievance(Long id){
        grievanceRepository.deleteById(id);
    }

    public Grievance resolveGrievance(Long id, String resolution){
        Grievance g = getGrievanceById(id);
        g.setResolution(resolution);
        g.setStatus("Resolved");
        return  grievanceRepository.save(g);
    }

    //    public long countByStatus(String pending) {
//        return grievanceRepository.count();
//    }
    public long countByStatus(String status) {
        return grievanceRepository.countByStatus(status);
    }



    public List<Grievance> getRecentGrievances() {
        return grievanceRepository.findTop5ByOrderByIdDesc(); // latest 5 grievances
    }

    public List<Grievance> findAll() {
        return grievanceRepository.findAll();
    }
}

