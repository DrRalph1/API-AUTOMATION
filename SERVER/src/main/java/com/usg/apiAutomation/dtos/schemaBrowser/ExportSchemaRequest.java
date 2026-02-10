package com.usg.apiAutomation.dtos.schemaBrowser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportSchemaRequest {
    @NotBlank(message = "Connection ID is required")
    private String connectionId;

    @NotBlank(message = "Export format is required")
    private String format; // JSON, XML, SQL, CSV

    private String schema;
    private String objectType;
    private String objectName;
    private Boolean includeData;
    private Boolean includeDDL;
    private Boolean includeGrants;
    private String fileName;

    @NotNull(message = "Include metadata is required")
    private Boolean includeMetadata;

    private String compression; // NONE, ZIP, GZIP
    private String encoding; // UTF-8, UTF-16, ISO-8859-1
}