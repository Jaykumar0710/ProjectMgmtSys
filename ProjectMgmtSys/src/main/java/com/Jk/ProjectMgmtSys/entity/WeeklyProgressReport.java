package com.Jk.ProjectMgmtSys.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class WeeklyProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String updateText;
    private String guideFeedback;
    private int weekNumber;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private  User guide;

    public WeeklyProgressReport() {
    }

    public WeeklyProgressReport(Long id, String updateText, String guideFeedback, int weekNumber, LocalDateTime createdAt, User student, User guide) {
        this.id = id;
        this.updateText = updateText;
        this.guideFeedback = guideFeedback;
        this.weekNumber = weekNumber;
        this.createdAt = createdAt;
        this.student = student;
        this.guide = guide;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUpdateText() {
        return updateText;
    }

    public void setUpdateText(String updateText) {
        this.updateText = updateText;
    }

    public String getGuideFeedback() {
        return guideFeedback;
    }

    public void setGuideFeedback(String guideFeedback) {
        this.guideFeedback = guideFeedback;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public User getGuide() {
        return guide;
    }

    public void setGuide(User guide) {
        this.guide = guide;
    }
}
