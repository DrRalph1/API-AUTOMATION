package com.usg.apiAutomation.dtos.userManagement;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportUsersRequestDTO {
    @NotNull(message = "File is required")
    private MultipartFile file;

    private String fileType; // csv, json, excel
    private boolean updateExisting;
    private boolean sendWelcomeEmails;
    private String defaultRole;
    private String defaultStatus;
    private Map<String, String> fieldMapping;
    private List<String> requiredFields;

    // Helper method to get filename
    public String getFileName() {
        return file != null ? file.getOriginalFilename() : null;
    }
}