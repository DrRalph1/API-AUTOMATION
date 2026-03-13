package com.usg.apiAutomation.helpers.apiEngine;

import com.usg.apiAutomation.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingHelper {

    private final LoggerUtil loggerUtil;

    /**
     * Log API generation activity
     */
    public void logApiGeneration(String requestId, String apiName, String performedBy) {
        String message = String.format("Request ID: %s, Generating API: %s by: %s",
                requestId, apiName, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.info(message);
    }

    /**
     * Log API execution activity
     */
    public void logApiExecution(String requestId, String apiId, String performedBy,
                                String contentType, String clientIp, String userAgent) {
        String message = String.format("Request ID: %s, Executing API: %s by: %s, Content-Type: %s, IP: %s, User-Agent: %s",
                requestId, apiId, performedBy, contentType, clientIp, userAgent);
        loggerUtil.log("apiGeneration", message);
        log.debug(message);
    }

    /**
     * Log API test activity
     */
    public void logApiTest(String requestId, String apiId, String testName, String performedBy) {
        String message = String.format("Request ID: %s, Testing API: %s with test: %s by: %s",
                requestId, apiId, testName, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.info(message);
    }

    /**
     * Log API details retrieval
     */
    public void logGetApiDetails(String requestId, String apiId, String performedBy) {
        String message = String.format("Request ID: %s, Getting API details for: %s by: %s",
                requestId, apiId, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.debug(message);
    }

    /**
     * Log API analytics retrieval
     */
    public void logGetApiAnalytics(String requestId, String apiId, String performedBy) {
        String message = String.format("Request ID: %s, Getting analytics for API: %s by: %s",
                requestId, apiId, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.debug(message);
    }

    /**
     * Log code generation
     */
    public void logCodeGeneration(String requestId, String apiId, String language, String performedBy) {
        String message = String.format("Request ID: %s, Generating %s code for API: %s by: %s",
                requestId, language != null ? language : "all", apiId, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.info(message);
    }

    /**
     * Log API status update
     */
    public void logStatusUpdate(String requestId, String apiId, String newStatus, String performedBy) {
        String message = String.format("Request ID: %s, Updating status for API: %s to: %s by: %s",
                requestId, apiId, newStatus, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.info(message);
    }

    /**
     * Log source object validation
     */
    public void logSourceValidation(String requestId, String objectName, String performedBy) {
        String message = String.format("Request ID: %s, Validating source object: %s by: %s",
                requestId, objectName, performedBy);
        loggerUtil.log("apiGeneration", message);
        log.debug(message);
    }

    /**
     * Log success
     */
    public void logSuccess(String requestId, String action, String details) {
        String message = String.format("Request ID: %s, %s completed successfully: %s",
                requestId, action, details);
        loggerUtil.log("apiGeneration", message);
        log.info(message);
    }

    /**
     * Log error
     */
    public void logError(String requestId, String action, String error, Exception e) {
        String message = String.format("Request ID: %s, Error %s: %s",
                requestId, action, error);
        loggerUtil.log("apiGeneration", message);
        log.error(message, e);
    }
}