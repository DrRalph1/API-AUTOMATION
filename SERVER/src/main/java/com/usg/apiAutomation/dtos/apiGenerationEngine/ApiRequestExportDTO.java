package com.usg.apiAutomation.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestExportDTO {

    private String exportId;
    private LocalDateTime exportTimestamp;
    private String exportedBy;
    private String format; // JSON, CSV, XML, EXCEL

    // =====================================================
    // Export Configuration
    // =====================================================

    private ExportConfig config;
    private List<String> selectedFields;
    private Map<String, Object> filters;

    // =====================================================
    // Export Data
    // =====================================================

    private List<Map<String, Object>> data;
    private ExportSummary summary;
    private String downloadUrl;
    private Long fileSize;
    private Integer recordCount;
    private String fileContent; // Base64 encoded content for direct download

    // =====================================================
    // Nested Classes
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportConfig {
        private Boolean includeRequestHeaders;
        private Boolean includeResponseHeaders;
        private Boolean includeRequestBody;
        private Boolean includeResponseBody;
        private Boolean includeMetadata;
        private Boolean maskSensitiveData;
        private Boolean compressOutput;
        private String dateFormat;
        private String delimiter; // For CSV
        private Boolean includeTimestamps;
        private Boolean aggregateData;
        private String aggregationLevel; // HOURLY, DAILY, MONTHLY
        private Boolean includeSummary;
        private String timeZone;
        private String encoding;
        private Boolean prettyPrint; // For JSON/XML
        private List<String> excludeFields;
        private Map<String, String> fieldMappings;
        private Boolean flattenNestedObjects; // For CSV
        private String nullValueRepresentation;
        private Boolean includeHeaders; // Include column headers in CSV
        private Integer maxRows;
        private Boolean paginate;
        private Integer pageSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportSummary {
        private Long totalRecords;
        private Long successfulExports;
        private Long failedExports;
        private LocalDateTime oldestRecord;
        private LocalDateTime newestRecord;
        private List<String> includedApis;
        private Map<String, Long> recordCountByApi;
        private Map<String, Long> recordCountByStatus;
        private Map<String, Long> recordCountByMethod;
        private Map<Integer, Long> recordCountByStatusCode;
        private Double averageResponseTime;
        private Long totalExecutionTime;
        private String dateRange;
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    public boolean isJsonFormat() {
        return "JSON".equalsIgnoreCase(format);
    }

    public boolean isCsvFormat() {
        return "CSV".equalsIgnoreCase(format);
    }

    public boolean isXmlFormat() {
        return "XML".equalsIgnoreCase(format);
    }

    public boolean isExcelFormat() {
        return "EXCEL".equalsIgnoreCase(format) || "XLSX".equalsIgnoreCase(format);
    }

    public String getFileExtension() {
        switch (format != null ? format.toUpperCase() : "JSON") {
            case "CSV":
                return ".csv";
            case "XML":
                return ".xml";
            case "EXCEL":
            case "XLSX":
                return ".xlsx";
            case "JSON":
            default:
                return ".json";
        }
    }

    public String getContentType() {
        switch (format != null ? format.toUpperCase() : "JSON") {
            case "CSV":
                return "text/csv";
            case "XML":
                return "application/xml";
            case "EXCEL":
            case "XLSX":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "JSON":
            default:
                return "application/json";
        }
    }
}