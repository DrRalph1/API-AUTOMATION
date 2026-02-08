package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDto {
    private String id;
    private String name;
    private String description;
    private String host;
    private String port;
    private String service;
    private String username;
    private String password; // Note: In production, handle passwords securely
    private String status;
    private String type;
    private String latency;
    private String uptime;
    private String lastConnected;
    private String driver;
    private String version;
    private int maxConnections;
    private int currentConnections;
    private String databaseSize;
    private String tablespaceUsed;

    // Additional fields
    private boolean sslEnabled;
    private String sslVersion;
    private String connectionPool;
    private int connectionTimeout;
    private int queryTimeout;
    private String schema;
    private String charset;
    private String timezone;
    private String createdBy;
    private String createdAt;
    private String lastTested;
    private String testResult;
}