package com.usg.apiAutomation.utils;

import com.usg.apiAutomation.dtos.systemActivities.systemLogs.LogEntry;
import com.usg.apiAutomation.dtos.systemActivities.systemLogs.LogEntryDetails;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class LogParserUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final Pattern LOG_PATTERN = Pattern.compile("\\[(.*?)\\](.*?)\\{(.*?)\\}");
    private static final Pattern PARAMS_PATTERN = Pattern.compile("(\\w+)=([^,}]+)");

    public static LogEntry parseLogLine(String line, String logFile, String logFilePath, long id) {
        try {
            Matcher matcher = LOG_PATTERN.matcher(line);
            if (matcher.find()) {
                String timestampStr = matcher.group(1).trim();
                String methodName = matcher.group(2).trim();
                String paramsString = matcher.group(3).trim();

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
                Date timestampDate = Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant());

                // Extract parameters dynamically
                String performedBy = extractParam(paramsString, "performedBy");
                String requestId = extractParam(paramsString, "requestId");
                String operation = extractParam(paramsString, "operation");
                String policyId = extractParam(paramsString, "policyId");
                String responseTime = extractParam(paramsString, "responseTime");
                String error = extractParam(paramsString, "error");
                String uri = extractParam(paramsString, "uri");

                String severity = determineSeverity(methodName, error, operation);
                String sourceIp = "Internal";
                String ruleId = generateRuleId(methodName, operation);
                String action = determineAction(operation, error);
                String requestUrl = determineRequestUrl(uri, operation);

                LogEntryDetails details = new LogEntryDetails(
                        buildPayload(methodName, paramsString),
                        operation,
                        "HTTP",
                        determineHttpMethod(operation),
                        determineResponseCode(error),
                        requestId,
                        performedBy,
                        policyId,
                        responseTime,
                        error,
                        uri
                );

                return new LogEntry(
                        id,
                        timestampDate,
                        sourceIp,
                        ruleId,
                        buildMessage(methodName, operation, error),
                        severity,
                        action,
                        requestUrl,
                        performedBy != null ? performedBy : "System",
                        logFilePath,
                        details
                );
            }
        } catch (Exception e) {
            return createBasicLogEntry(line, logFilePath, id);
        }

        return createBasicLogEntry(line, logFilePath, id);
    }

    private static String extractParam(String paramsString, String paramName) {
        Pattern pattern = Pattern.compile(paramName + "=([^,}]+)");
        Matcher matcher = pattern.matcher(paramsString);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            return "null".equals(value) ? null : value;
        }
        return null;
    }

    private static String determineSeverity(String methodName, String error, String operation) {
        if (error != null && !error.isEmpty()) {
            return "high";
        }
        if (methodName.contains("HealthCheck") || "detailedHealthCheck".equals(operation)) {
            return "low";
        }
        if (methodName.contains("testPolicy") || "testPolicy".equals(operation) ||
                methodName.contains("scanRequest") || "scanRequest".equals(operation)) {
            return "medium";
        }
        return "low";
    }

    private static String generateRuleId(String methodName, String operation) {
        String controller = "APP";
        if (methodName.contains(".")) {
            String[] parts = methodName.split("\\.");
            if (parts.length > 0) {
                controller = parts[0].toUpperCase().replace("CONTROLLER", "");
            }
        }

        if (operation != null) {
            return controller + "-" + operation.toUpperCase();
        }
        return controller + "-" + methodName.replace(".", "-").toUpperCase();
    }

    private static String determineAction(String operation, String error) {
        if (error != null && !error.isEmpty()) {
            return "failed";
        }
        if (operation != null) {
            switch (operation) {
                case "scanRequest": return "scanned";
                case "testPolicy": return "tested";
                case "detailedHealthCheck": return "checked";
                case "getPolicies": return "retrieved";
                case "getRules": return "retrieved";
                default: return "executed";
            }
        }
        return "executed";
    }

    private static String determineRequestUrl(String uri, String operation) {
        if (uri != null) {
            return uri;
        }
        if (operation != null) {
            switch (operation) {
                case "detailedHealthCheck": return "/api/health";
                case "getPolicies": return "/api/policies";
                case "getRules": return "/api/rules";
                case "testPolicy": return "/api/policies/test";
                case "scanRequest": return "/api/scan";
                default: return "/api/" + operation.toLowerCase();
            }
        }
        return "/api/unknown";
    }

    private static String determineHttpMethod(String operation) {
        if (operation == null) return "GET";

        switch (operation) {
            case "getPolicies":
            case "getRules":
            case "detailedHealthCheck":
                return "GET";
            case "testPolicy":
            case "scanRequest":
                return "POST";
            default:
                return "GET";
        }
    }

    private static Integer determineResponseCode(String error) {
        if (error != null && !error.isEmpty()) {
            return 500;
        }
        return 200;
    }

    private static String buildMessage(String methodName, String operation, String error) {
        StringBuilder message = new StringBuilder();
        message.append(methodName);

        if (operation != null) {
            message.append(" - ").append(operation);
        }

        if (error != null && !error.isEmpty()) {
            message.append(" - Error: ").append(error);
        }

        return message.toString();
    }

    private static String buildPayload(String methodName, String paramsString) {
        return methodName + " {" + paramsString + "}";
    }

    private static LogEntry createBasicLogEntry(String line, String logFilePath, long id) {
        Date now = new Date();
        return new LogEntry(
                id,
                now,
                "Unknown",
                "UNKNOWN",
                line,
                "low",
                "logged",
                "N/A",
                "System",
                logFilePath,
                new LogEntryDetails(line, "N/A", "Unknown", "Unknown", 200, null, null, null, null, null, null)
        );
    }
}