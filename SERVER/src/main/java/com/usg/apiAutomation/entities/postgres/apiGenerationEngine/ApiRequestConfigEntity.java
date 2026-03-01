package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_request_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "schema_type")
    private String schemaType;

    @Column(name = "sample", columnDefinition = "text")
    private String sample;

    @Column(name = "max_size")
    private Long maxSize;

    @Column(name = "validate_schema")
    private Boolean validateSchema;

    @Column(name = "allowed_media_types", columnDefinition = "text")
    private String allowedMediaTypes;

    @ElementCollection
    @CollectionTable(name = "tb_eng_required_fields", joinColumns = @JoinColumn(name = "request_config_id"))
    @Column(name = "field_name")
    private List<String> requiredFields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiRequestConfigEntity that = (ApiRequestConfigEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(schemaType, that.schemaType) &&
                Objects.equals(sample, that.sample) &&
                Objects.equals(maxSize, that.maxSize) &&
                Objects.equals(validateSchema, that.validateSchema) &&
                Objects.equals(allowedMediaTypes, that.allowedMediaTypes) &&
                Objects.equals(requiredFields, that.requiredFields) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schemaType, sample, maxSize, validateSchema,
                allowedMediaTypes, requiredFields,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiRequestConfigEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", schemaType='" + schemaType + '\'' +
                ", sample='" + truncate(sample, 100) + '\'' +
                ", maxSize=" + maxSize +
                ", validateSchema=" + validateSchema +
                ", allowedMediaTypes='" + allowedMediaTypes + '\'' +
                ", requiredFields=" + requiredFields +
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
     * Checks if the request size is within limits
     */
    public boolean isWithinSizeLimit(long contentLength) {
        if (maxSize == null || maxSize <= 0) {
            return true; // No limit configured
        }
        return contentLength <= maxSize;
    }

    /**
     * Validates if the content type is allowed
     */
    public boolean isContentTypeAllowed(String contentType) {
        if (allowedMediaTypes == null || allowedMediaTypes.trim().isEmpty()) {
            return true; // No restriction
        }

        if (contentType == null) {
            return false;
        }

        String[] allowedTypes = allowedMediaTypes.split(",");
        for (String allowedType : allowedTypes) {
            if (contentType.trim().startsWith(allowedType.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the parsed allowed media types as an array
     */
    public String[] getAllowedMediaTypesArray() {
        if (allowedMediaTypes == null || allowedMediaTypes.trim().isEmpty()) {
            return new String[0];
        }
        return allowedMediaTypes.split(",");
    }

    /**
     * Checks if schema validation is enabled
     */
    public boolean isSchemaValidationEnabled() {
        return validateSchema != null && validateSchema;
    }

    /**
     * Builder with defaults
     */
    public static class ApiRequestConfigEntityBuilder {
        private Long maxSize = 10485760L; // Default 10MB
        private Boolean validateSchema = true; // Default to validate schema
        private String allowedMediaTypes = "application/json"; // Default to JSON
        private String schemaType = "JSON"; // Default schema type
    }
}