package com.usg.autoAPIGenerator.helpers.apiEngine;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

@Component
public class RequestValidatorHelper {

    /**
     * Extract validation errors from BindingResult
     */
    public String extractValidationErrors(BindingResult bindingResult) {
        if (bindingResult == null || !bindingResult.hasErrors()) {
            return null;
        }

        return bindingResult.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }


    /**
     * Validate status update request
     */
    public ValidationResult validateStatusUpdate(String status) {
        ValidationResult result = new ValidationResult();

        if (status == null || status.trim().isEmpty()) {
            result.addError("Status is required");
        } else {
            String upperStatus = status.toUpperCase();
            if (!"DRAFT".equals(upperStatus) && !"ACTIVE".equals(upperStatus) &&
                    !"DEPRECATED".equals(upperStatus)) {
                result.addError("Invalid status. Allowed values: DRAFT, ACTIVE, DEPRECATED");
            }
        }

        return result;
    }

    /**
     * Inner class for validation results
     */
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();

        public void addError(String error) {
            valid = false;
            if (errors.length() > 0) {
                errors.append(", ");
            }
            errors.append(error);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors.toString();
        }
    }
}