package com.Jk.ProjectMgmtSys.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProjectDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    private String documentType; //SYNOPSIS, REPORT, PPT, etc
    private String fileName;

    private String filePath;
    private int version;
    private LocalDateTime uploadedAt;

    @ManyToOne
    private  Project project;

    public Long getId() {
        return id;
    }
    public String getFileName() {
        return fileName;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
   public Project getProject() {
        return project;
    }
   public void setProject(Project project) {
        this.project = project;
    }
    public void setFileName(String fileName) {
    this.fileName=fileName;
    }
}
