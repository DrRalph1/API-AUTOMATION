package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseInfoDto {
    private String databaseName;
    private String databaseVersion;
    private String host;
    private String port;
    private String serviceName;
    private String instanceName;
    private String characterSet;
    private String nationalCharacterSet;
    private String timezone;
    private String platform;
    private String edition;
    private String status;
    private String created;
    private String uptime;
    private Long sessions;
    private Long processes;
    private String lastUpdated;
}