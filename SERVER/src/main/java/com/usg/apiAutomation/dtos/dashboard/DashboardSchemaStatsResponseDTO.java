package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSchemaStatsResponseDTO {
    // Basic counts
    private int totalObjects;
    private int tables;
    private int views;
    private int procedures;
    private int packages;
    private int functions;
    private int triggers;
    private int indexes;
    private int sequences;
    private int materializedViews;
    private int partitions;

    // Database info
    private String databaseName;
    private String databaseSize;
    private String version;

    // Monthly changes
    private int monthlyGrowth;
    private int tableChange;
    private int viewChange;
    private int procedureChange;
    private int functionChange;
    private int packageChange;
    private int triggerChange;
    private int indexChange;
    private int sequenceChange;
    private int materializedViewChange;
    private int totalObjectsChange;

    // Additional metrics
    private String databaseType;
    private String host;
    private String port;
    private String schemaName;
    private String characterSet;
    private String nationalCharacterSet;
    private String blockSize;
    private String status;
    private String createdDate;
    private String lastBackup;
    private String backupSize;
    private String tablespaceName;
    private String datafileSize;
    private String usedSpace;
    private String freeSpace;
    private int usersCount;
    private int rolesCount;
    private int profilesCount;

    // Performance metrics
    private String avgQueryTime;
    private String cacheHitRatio;
    private String bufferCacheHitRatio;
    private String libraryCacheHitRatio;
    private String diskReadsPerTransaction;
    private String parseCountPerTransaction;

    // Timestamp
    private String lastUpdated;

    // Detailed breakdowns
    private Map<String, Integer> tableTypes;
    private Map<String, Integer> indexTypes;
    private Map<String, Integer> constraintTypes;
    private Map<String, Integer> partitionTypes;
}