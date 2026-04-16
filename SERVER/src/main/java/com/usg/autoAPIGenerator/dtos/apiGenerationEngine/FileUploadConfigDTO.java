// FileUploadConfigDTO.java
package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadConfigDTO {
    private Long maxFileSize;
    private List<String> allowedFileTypes;
    private Boolean multipleFiles;
    private String fileParameterName;
}