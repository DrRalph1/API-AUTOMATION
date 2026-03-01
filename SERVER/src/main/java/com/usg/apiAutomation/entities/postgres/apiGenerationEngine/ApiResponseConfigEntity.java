package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_response_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "success_schema", columnDefinition = "text")
    private String successSchema;

    @Column(name = "error_schema", columnDefinition = "text")
    private String errorSchema;

    @Column(name = "include_metadata")
    private Boolean includeMetadata;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "compression")
    private String compression;

    @ElementCollection
    @CollectionTable(name = "tb_eng_metadata_fields", joinColumns = @JoinColumn(name = "response_config_id"))
    @Column(name = "field_name")
    private List<String> metadataFields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponseConfigEntity that = (ApiResponseConfigEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(successSchema, that.successSchema) &&
                Objects.equals(errorSchema, that.errorSchema) &&
                Objects.equals(includeMetadata, that.includeMetadata) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(compression, that.compression) &&
                Objects.equals(metadataFields, that.metadataFields) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, successSchema, errorSchema, includeMetadata,
                contentType, compression, metadataFields,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiResponseConfigEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", successSchema='" + truncate(successSchema, 100) + '\'' +
                ", errorSchema='" + truncate(errorSchema, 100) + '\'' +
                ", includeMetadata=" + includeMetadata +
                ", contentType='" + contentType + '\'' +
                ", compression='" + compression + '\'' +
                ", metadataFields=" + metadataFields +
                '}';
    }

    /**
     * Helper method to truncate long strings in toString()
     */
    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...";
    }

    /**
     * Checks if metadata should be included in responses
     */
    public boolean shouldIncludeMetadata() {
        return includeMetadata != null && includeMetadata;
    }

    /**
     * Gets the response content type with proper formatting
     */
    public String getFormattedContentType() {
        if (contentType == null || contentType.trim().isEmpty()) {
            return "application/json"; // Default
        }
        return contentType;
    }

    /**
     * Checks if response compression is enabled
     */
    public boolean isCompressionEnabled() {
        return compression != null &&
                !compression.trim().isEmpty() &&
                !"none".equalsIgnoreCase(compression);
    }

    /**
     * Gets the compression algorithm to use
     */
    public String getCompressionAlgorithm() {
        if (!isCompressionEnabled()) {
            return null;
        }
        return switch (compression.toLowerCase()) {
            case "gzip" -> "gzip";
            case "deflate" -> "deflate";
            case "br" -> "brotli";
            case "compress" -> "compress";
            default -> compression;
        };
    }

    /**
     * Validates if the metadata field is configured to be included
     */
    public boolean isMetadataFieldIncluded(String fieldName) {
        if (metadataFields == null || metadataFields.isEmpty()) {
            return false;
        }
        return metadataFields.contains(fieldName);
    }

    /**
     * Gets the success response schema, with optional pretty printing
     */
    public String getSuccessSchema(boolean prettyPrint) {
        if (!prettyPrint || successSchema == null) {
            return successSchema;
        }
        // Try to pretty print JSON if it's a valid JSON string
        return prettyPrintJson(successSchema);
    }

    /**
     * Gets the error response schema, with optional pretty printing
     */
    public String getErrorSchema(boolean prettyPrint) {
        if (!prettyPrint || errorSchema == null) {
            return errorSchema;
        }
        // Try to pretty print JSON if it's a valid JSON string
        return prettyPrintJson(errorSchema);
    }

    /**
     * Simple JSON pretty printing (can be enhanced with Jackson)
     */
    private String prettyPrintJson(String json) {
        if (json == null) return null;

        // Basic indentation - consider using Jackson's ObjectMapper for full implementation
        int indentLevel = 0;
        StringBuilder pretty = new StringBuilder();
        boolean inString = false;

        for (char c : json.toCharArray()) {
            switch (c) {
                case '{':
                case '[':
                    pretty.append(c).append('\n');
                    indentLevel++;
                    pretty.append("  ".repeat(indentLevel));
                    break;
                case '}':
                case ']':
                    pretty.append('\n');
                    indentLevel--;
                    pretty.append("  ".repeat(indentLevel)).append(c);
                    break;
                case ',':
                    pretty.append(c).append('\n');
                    pretty.append("  ".repeat(indentLevel));
                    break;
                case ':':
                    pretty.append(c).append(' ');
                    break;
                case '"':
                    inString = !inString;
                    pretty.append(c);
                    break;
                default:
                    pretty.append(c);
                    break;
            }
        }
        return pretty.toString();
    }

    /**
     * Builder with defaults
     */
    public static class ApiResponseConfigEntityBuilder {
        private Boolean includeMetadata = false; // Default to no metadata
        private String contentType = "application/json"; // Default to JSON
        private String compression = "none"; // Default to no compression
    }
}