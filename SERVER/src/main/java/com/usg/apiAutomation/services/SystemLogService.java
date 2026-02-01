package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.systemLogs.LogEntriesResponse;
import com.usg.apiAutomation.dtos.systemLogs.LogEntry;
import com.usg.apiAutomation.dtos.systemLogs.LogFileContentResponse;
import com.usg.apiAutomation.dtos.systemLogs.LogFileResponse;
import com.usg.apiAutomation.helpers.FilePathHelper;
import com.usg.apiAutomation.utils.FileUtil;
import com.usg.apiAutomation.utils.LogParserUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SystemLogService {

    private final LoggerUtil loggerUtil;

    private String logsDirectory;
    private final Map<String, FileMetadata> fileMetadataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> fileLineCountCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHED_FILES = 1000;

    @PostConstruct
    public void init() {
        this.logsDirectory = resolveLogsDirectory();
        log.info("System logs directory resolved to: {}", this.logsDirectory);
        preloadFileMetadata();
    }

    // ========== PUBLIC SERVICE METHODS ==========

    public List<LogFileResponse> getLogFiles(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting system log files for user: {}", requestId, performedBy);
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Getting system log files for user: " + performedBy);

            Path logsPath = Paths.get(logsDirectory);
            if (!Files.exists(logsPath) || !Files.isDirectory(logsPath)) {
                String message = "System logs directory not found at path: " + logsPath;
                log.warn("Request ID: {}, {}", requestId, message);
                return Collections.emptyList();
            }

            List<LogFileResponse> logFiles = Files.list(logsPath)
                    .filter(this::isLogFile)
                    .parallel()
                    .map(this::mapToLogFileResponse)
                    .sorted((f1, f2) -> f2.getLastModified().compareTo(f1.getLastModified()))
                    .collect(Collectors.toList());

            log.info("Request ID: {}, Retrieved {} system log files", requestId, logFiles.size());
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", Retrieved " + logFiles.size() + " system log files");

            return logFiles;

        } catch (Exception e) {
            String errorMsg = "Error retrieving system log files: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            loggerUtil.log("web-application-firewall",
                    "Request ID: " + requestId + ", " + errorMsg);
            return Collections.emptyList();
        }
    }

    public LogEntriesResponse getLogEntries(String search, String severity, String timeFilter, int page, int size,
                                            String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting system log entries - Search: {}, Severity: {}, TimeFilter: {}, Page: {}, Size: {}",
                    requestId, search, severity, timeFilter, page, size);

            List<LogEntry> filteredLogs = parseAndFilterLogsStreaming(search, severity, timeFilter, page * size + size);

            int totalItems = filteredLogs.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = Math.min((page - 1) * size, totalItems);
            int endIndex = Math.min(startIndex + size, totalItems);

            List<LogEntry> paginatedLogs = filteredLogs.subList(startIndex, endIndex);
            LogEntriesResponse response = new LogEntriesResponse(paginatedLogs, page, size, totalItems, totalPages);

            log.info("Request ID: {}, Retrieved {} system log entries out of {} total, {} pages",
                    requestId, paginatedLogs.size(), totalItems, totalPages);

            return response;

        } catch (Exception e) {
            String errorMsg = "Error retrieving system log entries: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new LogEntriesResponse(Collections.emptyList(), page, size, 0, 0);
        }
    }

    public LogFileContentResponse getLogFileContent(String filename, String search, String requestId,
                                                    HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting content of system log file: {} with search: {}",
                    requestId, filename, search);

            Path filePath = Paths.get(logsDirectory, filename).normalize();

            if (!filePath.startsWith(Paths.get(logsDirectory)) || !Files.exists(filePath)) {
                String message = "System log file not found or access denied: " + filename;
                log.warn("Request ID: {}, {}", requestId, message);
                return new LogFileContentResponse(filename, "File not found or access denied", search);
            }

            String content = readFileContentEfficiently(filePath, search);
            LogFileContentResponse response = new LogFileContentResponse(filename, content, search);

            log.info("Request ID: {}, Retrieved content from system log file: {}, Content length: {}",
                    requestId, filename, content.length());

            return response;

        } catch (Exception e) {
            String errorMsg = "Error reading system log file content: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new LogFileContentResponse(filename, "Error reading file: " + e.getMessage(), search);
        }
    }

    public String exportLogsToCsv(String search, String severity, String timeFilter, String requestId,
                                  HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Exporting system logs to CSV - Search: {}, Severity: {}, TimeFilter: {}",
                    requestId, search, severity, timeFilter);

            List<LogEntry> filteredLogs = parseAndFilterLogsStreaming(search, severity, timeFilter, 10000);
            String csvContent = generateCsvContent(filteredLogs);

            log.info("Request ID: {}, Exported {} system log entries to CSV, CSV length: {}",
                    requestId, filteredLogs.size(), csvContent.length());

            return csvContent;

        } catch (Exception e) {
            String errorMsg = "Error exporting system logs to CSV: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return "Timestamp,Source IP,Rule ID,Message,Severity,Action,Request URL,Log File\n";
        }
    }

    public Map<String, Object> getLogStatistics(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting system log statistics", requestId);

            Map<String, Object> statistics = getLogStatisticsFromCache();

            log.info("Request ID: {}, Retrieved system log statistics", requestId);

            return statistics;

        } catch (Exception e) {
            String errorMsg = "Error getting system log statistics: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return Map.of("error", "Failed to fetch system log statistics: " + e.getMessage());
        }
    }

    public Page<LogEntry> searchLogs(String query, Pageable pageable, String requestId,
                                     HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Searching system logs - Query: {}, Page: {}, Size: {}",
                    requestId, query, pageable.getPageNumber(), pageable.getPageSize());

            List<LogEntry> filteredLogs = searchLogsStreaming(query, pageable.getPageSize() * (pageable.getPageNumber() + 1));

            int totalItems = filteredLogs.size();
            int startIndex = (int) pageable.getOffset();
            int endIndex = Math.min(startIndex + pageable.getPageSize(), totalItems);

            List<LogEntry> paginatedLogs = filteredLogs.subList(startIndex, endIndex);
            Page<LogEntry> resultPage = new PageImpl<>(paginatedLogs, pageable, totalItems);

            log.info("Request ID: {}, Search completed - Found {} system logs, Total pages: {}",
                    requestId, totalItems, resultPage.getTotalPages());

            return resultPage;

        } catch (Exception e) {
            String errorMsg = "Error searching system logs: " + e.getMessage();
            log.error("Request ID: {}, {}", requestId, errorMsg);
            return new PageImpl<>(Collections.emptyList());
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private String resolveLogsDirectory() {
        try {
            String logsDirectory = FilePathHelper.getLogFilePath("logs");
            Path logsDir = Paths.get(logsDirectory);
            Files.createDirectories(logsDir);
            log.info("System logs directory resolved or created: {}", logsDir.toAbsolutePath());
            return logsDir.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("Failed to resolve or create system logs directory", e);
            Path fallback = Paths.get(System.getProperty("user.home"), "webApplicationFirewall", "logs");
            try {
                Files.createDirectories(fallback);
                log.warn("Using fallback system logs directory: {}", fallback.toAbsolutePath());
            } catch (IOException ioException) {
                log.error("Failed to create fallback system logs directory", ioException);
            }
            return fallback.toAbsolutePath().toString();
        }
    }

    private void preloadFileMetadata() {
        try {
            Path logsPath = Paths.get(logsDirectory);
            if (Files.exists(logsPath) && Files.isDirectory(logsPath)) {
                Files.list(logsPath)
                        .filter(this::isLogFile)
                        .parallel()
                        .forEach(this::cacheFileMetadata);
            }
        } catch (Exception e) {
            log.warn("Failed to preload system log file metadata: {}", e.getMessage());
        }
    }

    private boolean isLogFile(Path path) {
        return Files.isRegularFile(path) &&
                (path.toString().toLowerCase().endsWith(".log") ||
                        path.toString().toLowerCase().endsWith(".txt"));
    }

    private List<LogEntry> parseAndFilterLogsStreaming(String search, String severity, String timeFilter, int limit) throws IOException {
        List<LogEntry> results = Collections.synchronizedList(new ArrayList<>());
        Path logsPath = Paths.get(logsDirectory);

        if (!Files.exists(logsPath)) {
            return results;
        }

        try (Stream<Path> paths = Files.list(logsPath)) {
            paths.filter(this::isLogFile)
                    .parallel()
                    .forEach(file -> {
                        try {
                            parseLogFileStreaming(file, search, severity, timeFilter, results, limit);
                        } catch (IOException e) {
                            log.error("Error parsing system log file: {}", file.getFileName(), e);
                        }
                    });
        }

        return results.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void parseLogFileStreaming(Path filePath, String search, String severity,
                                       String timeFilter, List<LogEntry> results, int limit) throws IOException {
        String fileName = filePath.getFileName().toString();
        String filePathStr = filePath.toString();

        try (Stream<String> lines = Files.lines(filePath)) {
            AtomicLong entryId = new AtomicLong(1);

            lines.sequential()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(line -> LogParserUtil.parseLogLine(line, fileName, filePathStr, entryId.getAndIncrement()))
                    .filter(Objects::nonNull)
                    .filter(log -> severity.equals("all") || log.getSeverity().equals(severity))
                    .filter(log -> search.isEmpty() || containsSearchTerm(log, search))
                    .filter(log -> filterByTime(log, timeFilter))
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .limit(limit)
                    .forEach(results::add);
        }
    }

    private String readFileContentEfficiently(Path filePath, String search) throws IOException {
        if (Files.size(filePath) > 10 * 1024 * 1024) {
            return readLargeFileReverse(filePath, search, 1000);
        } else {
            String content = Files.readString(filePath);
            if (search != null && !search.trim().isEmpty()) {
                return filterContentBySearch(content, search);
            }
            return content;
        }
    }

    private String readLargeFileReverse(Path filePath, String search, int maxLines) throws IOException {
        List<String> lines = new ArrayList<>();
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
             FileChannel channel = file.getChannel()) {

            StringBuilder currentLine = new StringBuilder();
            long fileSize = channel.size();
            long position = fileSize - 1;
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            int linesRead = 0;

            while (position >= 0 && linesRead < maxLines) {
                long start = Math.max(0, position - buffer.capacity() + 1);
                buffer.clear();
                channel.read(buffer, start);
                buffer.flip();

                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String chunk = new String(data, StandardCharsets.UTF_8);

                for (int i = chunk.length() - 1; i >= 0; i--) {
                    char c = chunk.charAt(i);
                    if (c == '\n') {
                        if (currentLine.length() > 0) {
                            String line = currentLine.reverse().toString();
                            if (search == null || search.trim().isEmpty() ||
                                    line.toLowerCase().contains(search.toLowerCase())) {
                                lines.add(line);
                                linesRead++;
                                if (linesRead >= maxLines) break;
                            }
                            currentLine.setLength(0);
                        }
                    } else {
                        currentLine.append(c);
                    }
                }
                position = start - 1;
            }

            if (currentLine.length() > 0 && linesRead < maxLines) {
                String line = currentLine.reverse().toString();
                if (search == null || search.trim().isEmpty() ||
                        line.toLowerCase().contains(search.toLowerCase())) {
                    lines.add(line);
                }
            }
        }

        Collections.reverse(lines);
        return String.join("\n", lines);
    }

    private List<LogEntry> searchLogsStreaming(String query, int limit) throws IOException {
        return parseAndFilterLogsStreaming(query, "all", "all", limit);
    }

    private Map<String, Object> getLogStatisticsFromCache() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // Total files in cache
            statistics.put("totalFiles", fileMetadataCache.size());

            // Total entries across all files
            long totalEntries = fileLineCountCache.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            statistics.put("totalEntries", totalEntries);

            // Get sample logs for distribution analysis
            List<LogEntry> sampleLogs = parseAndFilterLogsStreaming("", "all", "24h", 1000);

            // Severity distribution
            Map<String, Long> severityCounts = sampleLogs.stream()
                    .filter(log -> log.getSeverity() != null && !log.getSeverity().isEmpty())
                    .collect(Collectors.groupingBy(
                            LogEntry::getSeverity,
                            Collectors.counting()
                    ));
            statistics.put("severityDistribution", severityCounts);

            // Action distribution (with null safety)
            Map<String, Long> actionCounts = sampleLogs.stream()
                    .filter(log -> log.getAction() != null && !log.getAction().isEmpty())
                    .collect(Collectors.groupingBy(
                            LogEntry::getAction,
                            Collectors.counting()
                    ));
            statistics.put("actionDistribution", actionCounts);

            // Recent entries estimation
            statistics.put("lastHourEntries", estimateRecentEntries(1));
            statistics.put("last24HoursEntries", estimateRecentEntries(24));

            // DEBUG: Log the actual file names to understand what we have
            log.debug("Files in cache: {}",
                    fileMetadataCache.values().stream()
                            .map(meta -> meta.fileName)
                            .collect(Collectors.toList()));

            // File type distribution - based on LoggerUtil naming patterns
            Map<String, Long> fileTypeCounts = fileMetadataCache.values().stream()
                    .filter(meta -> meta.fileName != null && !meta.fileName.isEmpty())
                    .collect(Collectors.groupingBy(
                            meta -> {
                                String fileName = meta.fileName.toLowerCase();

                                // First check for combined logs (they have different format)
                                if (fileName.contains("-combined-logs-")) {
                                    return "COMBINED_TXT";
                                }

                                // Extract log level from filename pattern: microservice-LEVEL-date.extension
                                String[] parts = fileName.split("-");
                                if (parts.length >= 3) {
                                    // The second part should be the log level (info, severe, warning, config)
                                    String levelPart = parts[parts.length - 2]; // Get second last part before date

                                    // Check the file extension to determine actual format
                                    String extension = "";
                                    if (fileName.endsWith(".json")) extension = "JSON";
                                    else if (fileName.endsWith(".csv")) extension = "CSV";
                                    else if (fileName.endsWith(".xml")) extension = "XML";
                                    else if (fileName.endsWith(".txt")) extension = "TXT";
                                    else if (fileName.endsWith(".log")) extension = "LOG";
                                    else if (fileName.endsWith(".md")) extension = "MD";
                                    else extension = "UNKNOWN";

                                    // Map based on LoggerUtil logic
                                    switch (levelPart) {
                                        case "severe":
                                        case "warning":
                                            return "ERROR_LOGS"; // Should be JSON format
                                        case "info":
                                            return "INFO_LOGS"; // Should be CSV format
                                        case "config":
                                            return "CONFIG_LOGS"; // Should be XML format
                                        default:
                                            return levelPart.toUpperCase() + "_" + extension;
                                    }
                                }

                                // Fallback to file extension
                                if (fileName.endsWith(".json")) return "JSON";
                                if (fileName.endsWith(".csv")) return "CSV";
                                if (fileName.endsWith(".xml")) return "XML";
                                if (fileName.endsWith(".log")) return "LOG";
                                if (fileName.endsWith(".txt")) return "TXT";
                                if (fileName.endsWith(".md")) return "MD";
                                return "OTHER";
                            },
                            Collectors.counting()
                    ));
            statistics.put("fileTypeDistribution", fileTypeCounts);

            // Also add raw file extension distribution for clarity
            Map<String, Long> extensionCounts = fileMetadataCache.values().stream()
                    .filter(meta -> meta.fileName != null && !meta.fileName.isEmpty())
                    .collect(Collectors.groupingBy(
                            meta -> {
                                String fileName = meta.fileName;
                                int dotIndex = fileName.lastIndexOf('.');
                                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                                    return fileName.substring(dotIndex + 1).toLowerCase();
                                }
                                return "no_extension";
                            },
                            Collectors.counting()
                    ));
            statistics.put("fileExtensionDistribution", extensionCounts);

            // Add cache sizes for monitoring
            statistics.put("fileMetadataCacheSize", fileMetadataCache.size());
            statistics.put("fileLineCountCacheSize", fileLineCountCache.size());

            // Add timestamp for when statistics were generated
            statistics.put("generatedAt", Instant.now().toString());

            // Add average entries per file if we have files
            if (fileMetadataCache.size() > 0) {
                double avgEntriesPerFile = (double) totalEntries / fileMetadataCache.size();
                statistics.put("averageEntriesPerFile", String.format("%.2f", avgEntriesPerFile));
            }

        } catch (Exception e) {
            log.error("Error generating cached system log statistics: {}", e.getMessage(), e);
            statistics.put("error", "Failed to generate complete system log statistics: " + e.getMessage());
            statistics.put("errorTimestamp", Instant.now().toString());
        }

        return statistics;
    }


    private long estimateRecentEntries(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return fileMetadataCache.values().stream()
                .filter(meta -> meta.lastModified.isAfter(cutoff))
                .mapToLong(meta -> fileLineCountCache.getOrDefault(meta.fileName, 0L))
                .sum();
    }

    private void cacheFileMetadata(Path filePath) {
        try {
            if (fileMetadataCache.size() >= MAX_CACHED_FILES) {
                evictOldestCacheEntries();
            }

            String fileName = filePath.getFileName().toString();
            FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
            LocalDateTime lastModified = FileUtil.toLocalDateTime(lastModifiedTime);
            long sizeBytes = Files.size(filePath);

            long lineCount = estimateLineCount(filePath);

            fileMetadataCache.put(fileName, new FileMetadata(fileName, lastModified, sizeBytes));
            fileLineCountCache.put(fileName, lineCount);

        } catch (IOException e) {
            log.debug("Failed to cache metadata for system log file: {}", filePath.getFileName());
        }
    }

    private long estimateLineCount(Path filePath) throws IOException {
        if (Files.size(filePath) > 100 * 1024 * 1024) {
            return estimateLineCountBySampling(filePath);
        } else {
            try (Stream<String> lines = Files.lines(filePath)) {
                return lines.count();
            }
        }
    }

    private long estimateLineCountBySampling(Path filePath) throws IOException {
        long fileSize = Files.size(filePath);
        if (fileSize == 0) return 0;

        long[] samples = {0, fileSize / 2, fileSize - Math.min(100000, fileSize / 10)};
        long totalLines = 0;
        int sampleCount = 0;

        for (long position : samples) {
            try (Stream<String> lines = Files.lines(filePath)) {
                long linesInSample = lines.limit(1000).count();
                totalLines += linesInSample;
                sampleCount++;
            }
        }

        return sampleCount > 0 ? (totalLines / sampleCount) * (fileSize / 1000) : 0;
    }

    private void evictOldestCacheEntries() {
        int entriesToRemove = Math.max(1, fileMetadataCache.size() / 10);

        fileMetadataCache.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(entriesToRemove)
                .forEach(entry -> {
                    fileMetadataCache.remove(entry.getKey());
                    fileLineCountCache.remove(entry.getKey());
                });
    }

    private LogFileResponse mapToLogFileResponse(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            FileMetadata metadata = fileMetadataCache.get(fileName);

            if (metadata == null) {
                cacheFileMetadata(filePath);
                metadata = fileMetadataCache.get(fileName);
            }

            if (metadata != null) {
                return new LogFileResponse(
                        fileName,
                        filePath.toString(),
                        FileUtil.formatFileSize(metadata.sizeBytes),
                        metadata.lastModified,
                        fileLineCountCache.getOrDefault(fileName, 0L),
                        "System Log File",
                        FileUtil.getFileFormat(fileName),
                        ""
                );
            }
        } catch (Exception e) {
            log.error("Error mapping system log file to response: {}", filePath, e);
        }

        String fileName = filePath.getFileName().toString();
        LocalDateTime fallbackDate = getFallbackLastModified(filePath);
        return new LogFileResponse(
                fileName,
                filePath.toString(),
                "Unknown",
                fallbackDate,
                0L,
                "System Log File",
                "Unknown format",
                ""
        );
    }

    private LocalDateTime getFallbackLastModified(Path filePath) {
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
            return FileUtil.toLocalDateTime(lastModifiedTime);
        } catch (IOException e) {
            return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
    }

    private String filterContentBySearch(String content, String search) {
        if (search == null || search.trim().isEmpty()) return content;

        return Arrays.stream(content.split("\n"))
                .filter(line -> line.toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.joining("\n"));
    }

    private boolean containsSearchTerm(LogEntry log, String search) {
        if (search == null || search.trim().isEmpty()) return true;

        String searchLower = search.toLowerCase();
        return log.getSourceIp().toLowerCase().contains(searchLower) ||
                log.getRuleId().toLowerCase().contains(searchLower) ||
                log.getMessage().toLowerCase().contains(searchLower) ||
                log.getRequestUrl().toLowerCase().contains(searchLower) ||
                log.getLogFile().toLowerCase().contains(searchLower) ||
                (log.getDetails().getPerformedBy() != null &&
                        log.getDetails().getPerformedBy().toLowerCase().contains(searchLower)) ||
                (log.getDetails().getRequestId() != null &&
                        log.getDetails().getRequestId().toLowerCase().contains(searchLower)) ||
                (log.getDetails().getError() != null &&
                        log.getDetails().getError().toLowerCase().contains(searchLower)) ||
                (log.getDetails().getUri() != null &&
                        log.getDetails().getUri().toLowerCase().contains(searchLower));
    }

    private boolean filterByTime(LogEntry log, String timeFilter) {
        if (timeFilter.equals("all")) return true;

        Date now = new Date();
        Date logTime = log.getTimestamp();
        long diffInHours = (now.getTime() - logTime.getTime()) / (1000 * 60 * 60);

        switch (timeFilter) {
            case "1h": return diffInHours <= 1;
            case "6h": return diffInHours <= 6;
            case "24h": return diffInHours <= 24;
            default: return true;
        }
    }

    private String generateCsvContent(List<LogEntry> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Source IP,Rule ID,Message,Severity,Action,Request URL,Log File,Performed By,Request ID,Error,URI\n");

        for (LogEntry log : logs) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    log.getTimestamp().toString(),
                    log.getSourceIp(),
                    log.getRuleId(),
                    log.getMessage().replace("\"", "\"\""),
                    log.getSeverity(),
                    log.getAction(),
                    log.getRequestUrl(),
                    log.getLogFile(),
                    log.getDetails().getPerformedBy() != null ? log.getDetails().getPerformedBy() : "N/A",
                    log.getDetails().getRequestId() != null ? log.getDetails().getRequestId() : "N/A",
                    log.getDetails().getError() != null ? log.getDetails().getError().replace("\"", "\"\"") : "N/A",
                    log.getDetails().getUri() != null ? log.getDetails().getUri() : "N/A"
            ));
        }

        return csv.toString();
    }

    private static class FileMetadata implements Comparable<FileMetadata> {
        final String fileName;
        final LocalDateTime lastModified;
        final long sizeBytes;
        final long lastAccessed;

        FileMetadata(String fileName, LocalDateTime lastModified, long sizeBytes) {
            this.fileName = fileName;
            this.lastModified = lastModified;
            this.sizeBytes = sizeBytes;
            this.lastAccessed = System.currentTimeMillis();
        }

        @Override
        public int compareTo(FileMetadata other) {
            return Long.compare(this.lastAccessed, other.lastAccessed);
        }
    }
}