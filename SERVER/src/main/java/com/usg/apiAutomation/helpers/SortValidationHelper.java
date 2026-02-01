package com.usg.apiAutomation.helpers;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class SortValidationHelper {

    /**
     * Validate that all sort fields are valid against a set of allowed fields
     *
     * @param sort The Sort object to validate
     * @param allowedFields Array of allowed field names
     * @return The invalid field name, or null if all fields are valid
     */
    public String validateSortFields(Sort sort, String[] allowedFields) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }

        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedFields));

        for (Sort.Order order : sort) {
            String property = order.getProperty();
            // Clean property name (remove brackets if present)
            if (property.contains("[")) {
                property = property.replaceAll("[\\[\\]\"]", "");
            }

            if (!allowedSet.contains(property)) {
                // Try case-insensitive match
                boolean found = false;
                for (String allowedField : allowedFields) {
                    if (allowedField.equalsIgnoreCase(property)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return property;
                }
            }
        }
        return null;
    }

    /**
     * Validate sort fields and throw an exception if invalid
     *
     * @param sort The Sort object to validate
     * @param allowedFields Array of allowed field names
     * @param entityName Name of the entity for error message
     * @throws IllegalArgumentException if invalid sort field is found
     */
    public void validateSortFieldsOrThrow(Sort sort, String[] allowedFields, String entityName) {
        String invalidField = validateSortFields(sort, allowedFields);
        if (invalidField != null) {
            throw new IllegalArgumentException(
                    String.format("Invalid sort field '%s' for %s. Allowed fields: %s",
                            invalidField, entityName, String.join(", ", allowedFields))
            );
        }
    }

    /**
     * Get error message for invalid sort field
     *
     * @param invalidField The invalid field name
     * @param allowedFields Array of allowed field names
     * @param entityName Name of the entity
     * @return Formatted error message
     */
    public String getErrorMessage(String invalidField, String[] allowedFields, String entityName) {
        return String.format("Invalid sort field '%s' for %s. Allowed fields: %s",
                invalidField, entityName, String.join(", ", allowedFields));
    }

    /**
     * Get allowed fields for common entities (optional - for standardization)
     */
    public String[] getAllowedFieldsForIntegration() {
        return new String[]{
                "integrationId", "integrationCode", "integrationName",
                "description", "active", "createdAt", "updatedAt"
        };
    }

    public String[] getAllowedFieldsForAppRole() {
        return new String[]{
                "roleId", "roleName", "description", "createdAt", "updatedAt"
        };
    }

    public String[] getAllowedFieldsForUser() {
        return new String[]{
                "userId", "username", "email", "firstName", "lastName",
                "active", "createdAt", "updatedAt"
        };
    }

    // List of valid sort fields for IntegrationEntity
    private static final String[] VALID_SORT_FIELDS = {
            "integrationId", "integrationCode", "integrationName",
            "description", "active", "createdAt", "updatedAt"
    };
}