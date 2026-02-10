package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
    private String status;
    private String type;
    private String color;
    private String lastUsed;
    private String driver;
    private String version;
    private Integer maxConnections;
    private Integer currentConnections;
    private String databaseSize;
    private String tablespaceUsed;
    private String sid;
    private String serviceName;
    private String latency;
    private String uptime;
    private String lastConnected;
    private String driverVersion;
    private String databaseVersion;
    private Boolean sslEnabled;
    private String connectionString;
}