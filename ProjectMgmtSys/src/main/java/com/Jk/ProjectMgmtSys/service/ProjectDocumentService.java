package com.Jk.ProjectMgmtSys.service;

import com.Jk.ProjectMgmtSys.entity.Project;
import com.Jk.ProjectMgmtSys.entity.ProjectDocument;
import com.Jk.ProjectMgmtSys.repository.ProjectDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectDocumentService {

    @Autowired
    private ProjectDocumentRepository projectDocumentRepository;

    // Get next version based on project and document type
    public int getNextVersion(Project project, String type) {
        return projectDocumentRepository.countByProjectAndDocumentType(project, type) + 1;
    }

    // Save file to local storage
    public String saveDocument(MultipartFile file, Project project, String type, int version) throws IOException {
        // Use a consistent absolute path for saving files (e.g., inside project root)
        String baseDir = System.getProperty("user.dir") + "/uploads/" + project.getId() + "/" + type + "/v" + version;

        File directory = new File(baseDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + baseDir);
            }
        }

        Path filePath = Paths.get(baseDir, file.getOriginalFilename());
        file.transferTo(filePath.toFile());

        return filePath.toString(); // Save this path in DB
    }


    // Save document metadata to database
    public ProjectDocument saveToDatabase(Project project, String type, String fileName, String filePath, int version) {
        ProjectDocument doc = new ProjectDocument();
        doc.setDocumentType(type);
        doc.setFileName(fileName);
        doc.setFilePath(filePath);
        doc.setVersion(version);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setProject(project);

        return projectDocumentRepository.save(doc);
    }

    // Get all documents for a project
    public List<ProjectDocument> getDocuments(Project project) {
        return projectDocumentRepository.findByProject(project);
    }
}
