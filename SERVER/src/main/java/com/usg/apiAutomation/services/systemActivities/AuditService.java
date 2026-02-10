package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.dtos.systemActivities.audit.AuditLogDTO;
import com.usg.apiAutomation.dtos.systemActivities.audit.AuditLogSearchRequest;
import com.usg.apiAutomation.entities.AuditLogEntity;
import com.usg.apiAutomation.repositories.AuditLogRepository;
import com.usg.apiAutomation.helpers.AuditLogHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogHelper auditLogHelper;

    // Log a single action
    public AuditLogDTO logAction(AuditLogDTO dto, String requestId, HttpServletRequest req, String performedBy) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setUserId(dto.getUserId());
        entity.setAction(dto.getAction());
        entity.setOperation(dto.getOperation());
        entity.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        entity.setDetails(dto.getDetails());

        entity = auditLogRepository.save(entity);
        dto.setAuditId(entity.getAuditId());

        // Log the audit action itself
//        auditLogHelper.logAuditAction("CREATE_AUDIT_LOG", performedBy,
//                String.format("Created audit log for user %s: %s - %s",
//                        dto.getUserId(), dto.getAction(), dto.getOperation()),
//                requestId);

        return dto;
    }


    // Retrieve all logs with pagination
    public Map<String, Object> getAllLogs(Pageable pageable, String requestId, HttpServletRequest req, String performedBy) {
        // Log the retrieval action
//        auditLogHelper.logAuditAction("RETRIEVE_ALL_AUDIT_LOGS", performedBy,
//                String.format("Retrieved all audit logs with pagination - Page: %d, Size: %d",
//                        pageable.getPageNumber(), pageable.getPageSize()),
//                requestId);

        // Get paginated logs
        Page<AuditLogDTO> logsPage = auditLogRepository.findAll(pageable)
                .map(AuditLogHelper::convertToDTO);

        // Get unique actions, users, and operations for filter dropdowns
        List<String> uniqueActions = auditLogRepository.findDistinctActions();
        List<String> uniqueUsers = auditLogRepository.findDistinctUsers();
        List<String> uniqueOperations = auditLogRepository.findDistinctOperations();

        // Create response map with both paginated logs and unique values
        Map<String, Object> response = new HashMap<>();
        response.put("logs", logsPage.getContent());
        response.put("pagination", new HashMap<String, Object>() {{
            put("page_number", logsPage.getNumber());
            put("page_size", logsPage.getSize());
            put("total_elements", logsPage.getTotalElements());
            put("total_pages", logsPage.getTotalPages());
            put("is_first", logsPage.isFirst());
            put("is_last", logsPage.isLast());
        }});
        response.put("uniqueActions", uniqueActions);
        response.put("uniqueUsers", uniqueUsers);
        response.put("uniqueOperations", uniqueOperations);

        return response;
    }


    // Search logs with filters
    public Page<AuditLogDTO> searchLogs(AuditLogSearchRequest searchRequest, Pageable pageable,
                                        String requestId, HttpServletRequest req, String performedBy) {

        // Log the search action
        String searchCriteria = auditLogHelper.buildSearchCriteriaString(searchRequest);
//        auditLogHelper.logAuditAction("SEARCH_AUDIT_LOGS", performedBy,
//                String.format("Searched audit logs with criteria: %s. Page: %d, Size: %d",
//                        searchCriteria, pageable.getPageNumber(), pageable.getPageSize()),
//                requestId);

        Specification<AuditLogEntity> spec = auditLogHelper.buildSearchSpecification(searchRequest);
        return auditLogRepository.findAll(spec, pageable)
                .map(AuditLogHelper::convertToDTO);
    }


}