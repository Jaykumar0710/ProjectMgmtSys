package com.Jk.ProjectMgmtSys;

import com.Jk.ProjectMgmtSys.entity.MilestoneDeadline;
import com.Jk.ProjectMgmtSys.entity.ProjectTeamMember;
import com.Jk.ProjectMgmtSys.service.EmailService;
import com.Jk.ProjectMgmtSys.service.MilestoneDeadlineService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Component
public class DeadlineNotifier {

    @Autowired
    private MilestoneDeadlineService deadlineService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateEngine templateEngine;

    // 🔄 Runs every minute for testing. Use "0 0 8 * * *" for 8AM daily.
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDueDateNotifications() {
        List<MilestoneDeadline> dueToday = deadlineService.getDueTodayWithStudents();
        List<MilestoneDeadline> overdue = deadlineService.getOverdueWithStudents();

        System.out.println("🔁 Running Scheduled Email Notification...");
        System.out.println("✅ Milestones due today: " + dueToday.size());
        System.out.println("⚠️ Milestones overdue: " + overdue.size());

        dueToday.forEach(d -> sendHtmlReminder(d, "due-today-notification", "⏰ Reminder: Your milestone is due today."));
        overdue.forEach(d -> sendHtmlReminder(d, "overdue-notification", "⚠️ Overdue: Your milestone is past due."));
    }

    private void sendHtmlReminder(MilestoneDeadline deadline, String templateName, String heading) {
        List<ProjectTeamMember> members = deadline.getMilestone().getProject().getTeamMembers();
        String subject = "📢 Milestone Due Date Alert";

        for (ProjectTeamMember member : members) {
            String email = member.getEmail();
            if (email == null || email.isEmpty()) {
                System.out.println("⚠️ Skipping team member with empty email.");
                continue;
            }

            // Prepare template context
            Context context = new Context();
            context.setVariable("heading", heading);
            context.setVariable("studentName", member.getName());
            context.setVariable("milestoneTitle", deadline.getMilestone().getTitle());
            context.setVariable("projectTitle", deadline.getMilestone().getProject().getTittle());
            context.setVariable("dueDate", deadline.getDueDate().toString());

            String htmlContent = templateEngine.process(templateName, context);
            emailService.sendHtmlEmail(email, subject, htmlContent);

            System.out.println("📧 Email sent to: " + email);
        }
    }
}
