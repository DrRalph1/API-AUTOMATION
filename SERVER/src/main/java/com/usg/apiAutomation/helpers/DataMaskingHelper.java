package com.usg.apiAutomation.helpers;

public class DataMaskingHelper {

    /**
     * Masks sensitive data such as API keys, secrets, and passwords.
     *
     * @param sensitiveData The data to be masked
     * @return Masked data or "[MASKED]" if input is null/empty
     */
    public static String maskSensitiveData(String sensitiveData) {
        // Handle null or empty input
        if (sensitiveData == null || sensitiveData.trim().isEmpty()) {
            return "[MASKED]";
        }

        // Remove leading/trailing whitespace
        String trimmedData = sensitiveData.trim();

        // Determine masking strategy based on length
        if (trimmedData.length() > 8) {
            // For long strings: show first 4, mask middle, show last 4
            return trimmedData.substring(0, 4) +
                    "****" +
                    trimmedData.substring(trimmedData.length() - 4);
        } else if (trimmedData.length() > 2) {
            // For medium strings: show first 2, mask the rest
            return trimmedData.substring(0, 2) + "****";
        } else {
            // For very short strings: return fixed masked value
            return "[MASKED]";
        }
    }

    /**
     * Alternative version that allows custom masking placeholder
     */
    public static String maskSensitiveData(String sensitiveData, String maskPlaceholder) {
        if (sensitiveData == null || sensitiveData.trim().isEmpty()) {
            return maskPlaceholder != null ? maskPlaceholder : "[MASKED]";
        }

        String trimmedData = sensitiveData.trim();
        String placeholder = maskPlaceholder != null ? maskPlaceholder : "****";

        if (trimmedData.length() > 8) {
            return trimmedData.substring(0, 4) +
                    placeholder +
                    trimmedData.substring(trimmedData.length() - 4);
        } else if (trimmedData.length() > 2) {
            return trimmedData.substring(0, 2) + placeholder;
        } else {
            return maskPlaceholder != null ? maskPlaceholder : "[MASKED]";
        }
    }
}