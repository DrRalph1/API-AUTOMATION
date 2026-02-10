package com.usg.apiAutomation.utils;

import lombok.experimental.UtilityClass;
import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
public class FileUtil {

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static String getFileDescription(String fileName) {
        String lowerName = fileName.toLowerCase();

        if (lowerName.contains("web-application-firewall") || lowerName.contains("gateway")) {
            return "API Gateway access logs and request routing information";
        } else if (lowerName.contains("audit") || lowerName.contains("security")) {
            return "Security audit, authentication, and compliance event logs";
        } else if (lowerName.contains("behavioral") || lowerName.contains("analysis")) {
            return "Behavioral analysis and anomaly detection logs";
        } else if (lowerName.contains("geolocation")) {
            return "Geolocation tracking, IP mapping, and regional activity logs";
        } else if (lowerName.contains("otp") || lowerName.contains("controller")) {
            return "OTP generation, validation, and verification controller logs";
        } else if (lowerName.contains("rule-group") || lowerName.contains("rule-groups")) {
            return "Rule group configuration and evaluation logs";
        } else if (lowerName.contains("waf-engine") || lowerName.contains("engine")) {
            return "WAF engine operations, processing, and systemActivities activity logs";
        } else if (lowerName.contains("waf-policy") || lowerName.contains("waf-policies") ||
                lowerName.contains("policy")) {
            return "WAF policy management and enforcement logs";
        } else if (lowerName.contains("waf-rule") || lowerName.contains("waf-rules") ||
                lowerName.contains("rule")) {
            return "WAF rule execution and evaluation logs";
        } else if (lowerName.contains("web-application-firewall") || lowerName.contains("waf")) {
            return "Web Application Firewall (WAF) core systemActivities and traffic inspection logs";
        } else if (lowerName.contains("web-socket") || lowerName.contains("socket")) {
            return "WebSocket configuration, connections, and communication logs";
        } else if (lowerName.contains("access") || lowerName.contains("request")) {
            return "HTTP access and request logs";
        } else if (lowerName.contains("error") || lowerName.contains("exception")) {
            return "Error and exception logs";
        } else if (lowerName.contains("debug") || lowerName.contains("trace")) {
            return "Debugging and detailed trace logs";
        } else {
            return "General application log file";
        }
    }


    public static String getFileFormat(String fileName) {
        String lowerName = fileName.toLowerCase();

        if (lowerName.contains("json")) {
            return "JSON format";
        } else if (lowerName.contains("csv")) {
            return "CSV format";
        } else if (lowerName.contains("xml")) {
            return "XML format";
        } else {
            return "Structured text format";
        }
    }

    public static LocalDateTime toLocalDateTime(java.nio.file.attribute.FileTime fileTime) {
        return fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}