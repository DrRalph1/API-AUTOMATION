package com.usg.apiAutomation.utils;

import com.usg.apiAutomation.helpers.FilePathHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LoggerUtil {

    private final Logger logger = Logger.getLogger(LoggerUtil.class.getName());
    private static final int CALLER_STACK_INDEX = 3;

    // File type configurations
    private enum LogFileType {
        // Structured data formats
        JSON(".json", "application/json"),
        CSV(".csv", "text/csv"),
        XML(".xml", "application/xml"),

        // Plain text formats
        TXT(".txt", "text/plain"),
        LOG(".log", "text/plain"),
        MD(".md", "text/markdown");

        private final String extension;
        private final String contentType;

        LogFileType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public String getExtension() {
            return extension;
        }

        public String getContentType() {
            return contentType;
        }
    }

    // Configuration for which file type to use for each log level/type
    private LogFileType getFileTypeForLevel(Level level) {
        if (level == Level.SEVERE || level == Level.WARNING) {
            return LogFileType.JSON; // Structured format for easier analysis of errors
        } else if (level == Level.INFO) {
            return LogFileType.CSV; // Good for data logging/audit trails
        } else if (level == Level.CONFIG) {
            return LogFileType.XML; // Structured config logs
        }
        return LogFileType.TXT; // Default for other levels
    }

    public LoggerUtil() {
        // Default constructor
    }

    /**
     * Logs messages to a daily log file with appropriate file type.
     *
     * @param microService The microservice name.
     * @param message      The actual message or error to be logged.
     */
    public void log(String microService, Object message) {
        log(Level.INFO, microService, getCallerInfo(), message);
    }

    /**
     * Logs messages with a specified log level using appropriate file format.
     *
     * @param level        The logging level (e.g., INFO, WARNING, SEVERE).
     * @param microService The microservice name.
     * @param source       The location of the code where the error occurred.
     * @param message      The actual message or error to be logged.
     */
    public void log(Level level, String microService, String source, Object message) {
        try {
            Instant timestamp = Instant.now();
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            LocalDate now = LocalDate.now();
            String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Get appropriate file type based on log level
            LogFileType fileType = getFileTypeForLevel(level);

            // Create logs directory (single directory for all log types)
            String logsDirectory = FilePathHelper.getLogFilePath("logs");
            Path logsDir = Paths.get(logsDirectory);
            Files.createDirectories(logsDir);

            // Create log file with appropriate extension in the main logs directory
            Path logFile = logsDir.resolve(
                    String.format("%s-%s-%s%s",
                            microService,
                            level.getName().toLowerCase(),
                            formattedDate,
                            fileType.getExtension()
                    )
            );

            // Format message based on file type
            String formattedMessage = formatMessageForFileType(
                    fileType, timestamp, source, requestId, level, message
            );

            // Write to the specific format file
            Files.write(logFile, formattedMessage.getBytes(),
                    Files.exists(logFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

            // Also write to a combined daily log for quick viewing
            writeToCombinedLog(microService, formattedDate, timestamp, source, requestId, level, message);

            // Fallback to console logger if needed
            logger.log(level, "[{0}] {1} - {2}", new Object[]{microService, source, message});

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write to log file", e);
        }
    }

    /**
     * Formats log message according to file type.
     */
    private String formatMessageForFileType(LogFileType fileType, Instant timestamp,
                                            String source, String requestId,
                                            Level level, Object message) {
        switch (fileType) {
            case JSON:
                return String.format(
                        "{\"timestamp\":\"%s\",\"requestId\":\"%s\",\"level\":\"%s\",\"source\":\"%s\",\"message\":%s}\n",
                        timestamp, requestId, level.getName(), source,
                        message instanceof String ? "\"" + escapeJson(message.toString()) + "\"" : message
                );

            case CSV:
                return String.format(
                        "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        timestamp, requestId, level.getName(), source,
                        escapeCsv(message.toString())
                );

            case XML:
                return String.format(
                        "<log><timestamp>%s</timestamp><requestId>%s</requestId><level>%s</level><source>%s</source><message>%s</message></log>\n",
                        timestamp, requestId, level.getName(), source,
                        escapeXml(message.toString())
                );

            case MD:
                return String.format(
                        "## %s Log Entry\n" +
                                "- **Timestamp**: %s\n" +
                                "- **RequestEntity ID**: %s\n" +
                                "- **Level**: %s\n" +
                                "- **Source**: %s\n" +
                                "- **Message**: %s\n\n",
                        level.getName(), timestamp, requestId, level.getName(), source, message
                );

            default: // TXT, LOG
                return String.format(
                        "[%s] [%s] [%s] [%s] %s: %s\n",
                        timestamp, requestId, level.getName(), source, Thread.currentThread().getName(), message
                );
        }
    }

    /**
     * Also writes to a combined text log for easy human reading.
     */
    private void writeToCombinedLog(String microService, String formattedDate,
                                    Instant timestamp, String source,
                                    String requestId, Level level, Object message) throws IOException {
        String logsDirectory = FilePathHelper.getLogFilePath("logs");
        Path logsDir = Paths.get(logsDirectory);

        // Create combined log in the same directory
        Path combinedLogFile = logsDir.resolve(
                microService + "-combined-logs-" + formattedDate + ".txt"
        );

        String combinedMessage = String.format(
                "[%s] [%s] [%s] [%s] %s: %s\n",
                timestamp, requestId, level.getName(), source, Thread.currentThread().getName(), message
        );

        Files.write(combinedLogFile, combinedMessage.getBytes(),
                Files.exists(combinedLogFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    }

    /**
     * Specialized logging methods for different use cases.
     */

    /**
     * Logs audit trail data (suitable for CSV format).
     */
    public void logAudit(String microService, String userId, String action, String resource, String status) {
        String auditMessage = String.format("User=%s, Action=%s, Resource=%s, Status=%s",
                userId, action, resource, status);
        log(Level.INFO, microService, getCallerInfo(), auditMessage);
    }

    /**
     * Logs error with stack trace (suitable for JSON format).
     */
    public void logError(String microService, Throwable error) {
        StringBuilder errorDetails = new StringBuilder();
        errorDetails.append(error.getMessage()).append("\n");
        for (StackTraceElement element : error.getStackTrace()) {
            errorDetails.append("\tat ").append(element).append("\n");
        }
        log(Level.SEVERE, microService, getCallerInfo(), errorDetails.toString());
    }

    /**
     * Logs performance metrics (suitable for CSV format).
     */
    public void logPerformance(String microService, String operation, long durationMs) {
        String perfMessage = String.format("Operation=%s, Duration=%dms", operation, durationMs);
        log(Level.INFO, microService, getCallerInfo(), perfMessage);
    }

    /**
     * Logs configuration changes (suitable for XML format).
     */
    public void logConfigChange(String microService, String configKey, String oldValue, String newValue) {
        String configMessage = String.format("ConfigKey=%s, OldValue=%s, NewValue=%s",
                configKey, oldValue, newValue);
        log(Level.CONFIG, microService, getCallerInfo(), configMessage);
    }

    // Helper methods for escaping special characters
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeCsv(String input) {
        return input.replace("\"", "\"\""); // Double quotes for CSV
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > CALLER_STACK_INDEX) {
            StackTraceElement caller = stackTrace[CALLER_STACK_INDEX];
            String fullClassName = caller.getClassName();
            String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            return simpleClassName + "." + caller.getMethodName();
        }
        return "Unknown.Unknown";
    }
}