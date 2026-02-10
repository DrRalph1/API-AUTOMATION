package com.usg.apiAutomation.dtos.systemActivities.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActionAuditDTO {
    private UUID auditId;
    private String userId;
    private String action; // ENQUIRY, PAYMENT, REVERSAL, REPRINT
    private Map<String, Object> requestData;
    private Map<String, Object> responseData;
    private Instant createdAt;
}
