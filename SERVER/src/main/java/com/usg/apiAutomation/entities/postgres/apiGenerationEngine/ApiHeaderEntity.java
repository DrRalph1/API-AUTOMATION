package com.usg.apiAutomation.entities.postgres.apiGenerationEngine;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_eng_headers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiHeaderEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GeneratedApiEntity generatedApi;

    @Column(name = "header_key")
    private String key;

    @Column(name = "header_value")
    private String value;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "description")
    private String description;

    @Column(name = "is_request_header")
    private Boolean isRequestHeader;

    @Column(name = "is_response_header")
    private Boolean isResponseHeader;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiHeaderEntity that = (ApiHeaderEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) &&
                Objects.equals(required, that.required) &&
                Objects.equals(description, that.description) &&
                Objects.equals(isRequestHeader, that.isRequestHeader) &&
                Objects.equals(isResponseHeader, that.isResponseHeader) &&
                Objects.equals(generatedApi != null ? generatedApi.getId() : null,
                        that.generatedApi != null ? that.generatedApi.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, value, required, description,
                isRequestHeader, isResponseHeader,
                generatedApi != null ? generatedApi.getId() : null);
    }

    @Override
    public String toString() {
        return "ApiHeaderEntity{" +
                "id='" + id + '\'' +
                ", apiId='" + (generatedApi != null ? generatedApi.getId() : null) + '\'' +
                ", key='" + key + '\'' +
                ", value='" + maskSensitiveHeader(key, value) + '\'' +
                ", required=" + required +
                ", description='" + description + '\'' +
                ", isRequestHeader=" + isRequestHeader +
                ", isResponseHeader=" + isResponseHeader +
                '}';
    }

    /**
     * Helper method to mask sensitive header values in toString()
     * Common sensitive headers: Authorization, Cookie, Api-Key, X-API-Key, etc.
     */
    private String maskSensitiveHeader(String headerKey, String headerValue) {
        if (headerValue == null) return null;

        String lowerKey = headerKey != null ? headerKey.toLowerCase() : "";
        if (lowerKey.contains("authorization") ||
                lowerKey.contains("cookie") ||
                lowerKey.contains("api-key") ||
                lowerKey.contains("apikey") ||
                lowerKey.contains("token") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("password") ||
                lowerKey.contains("auth")) {

            if (headerValue.length() <= 10) return "********";
            return headerValue.substring(0, 5) + "..." + headerValue.substring(headerValue.length() - 5);
        }

        // For non-sensitive headers, return as is but truncate if too long
        if (headerValue.length() > 50) {
            return headerValue.substring(0, 50) + "...";
        }
        return headerValue;
    }
}