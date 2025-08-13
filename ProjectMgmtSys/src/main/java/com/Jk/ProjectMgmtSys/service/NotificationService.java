package com.Jk.ProjectMgmtSys.service;


import com.Jk.ProjectMgmtSys.entity.Notification;
import com.Jk.ProjectMgmtSys.entity.NotificationRequest;
import com.Jk.ProjectMgmtSys.entity.User;
import com.Jk.ProjectMgmtSys.repository.NotificationRepository;
import com.Jk.ProjectMgmtSys.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    public void sendNotification( NotificationRequest request){
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setDescription(request.getDescription());
        notification.setCategory(request.getCategory());
        notification.setFilePath(request.getFilePath());

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        notification.setSender(sender);

        List<User> receivers = userRepository.findAllById(request.getReceiverIds());
        notification.setReceivers(receivers);

        Map<Long, Boolean> statusMap = new HashMap<>();
        for (User student : receivers){
            statusMap.put(student.getId(), false);
        }

        notification.setReadStatus(statusMap);

        notificationRepository.save(notification);

        for (User student : receivers) {
            String subject = "New Notification: " + notification.getTitle();

            User assignedGuide = student.getGuide();
            String guideName = (assignedGuide != null) ? assignedGuide.getName() : "FYPMS Admin";

            // Prepare model for Thymeleaf
            Map<String, Object> model = new HashMap<>();
            model.put("studentName", student.getName());
            model.put("title", request.getTitle());
            model.put("description", request.getDescription());
            model.put("category", request.getCategory());
            model.put("guideName", guideName);

            try {
                emailService.sendNotificationEmail(student.getEmail(), subject, model);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + student.getEmail());
                e.printStackTrace();
            }
        }



    }
}
