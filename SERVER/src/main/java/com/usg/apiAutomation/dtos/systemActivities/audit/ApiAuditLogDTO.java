package com.usg.apiAutomation.dtos.systemActivities.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiAuditLogDTO {
    private UUID logId;
    private String endpoint;
    private String method;
    private String requestHeaders;
    private Object requestBody;
    private Object responseBody;
    private Instant createdAt;
}
