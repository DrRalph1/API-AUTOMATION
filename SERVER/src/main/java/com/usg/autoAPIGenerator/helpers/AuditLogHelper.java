package com.usg.autoAPIGenerator.helpers;

import com.usg.autoAPIGenerator.dtos.systemActivities.audit.AuditLogDTO;
import com.usg.autoAPIGenerator.dtos.systemActivities.audit.AuditLogSearchRequest;
import com.usg.autoAPIGenerator.entities.postgres.AuditLogEntity;
import com.usg.autoAPIGenerator.repositories.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Component
public class AuditLogHelper {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Helper method to log audit actions
    public void logAuditAction(String action, String performedBy, String details, String requestId) {
        try {
            // Create new entity WITHOUT setting ID manually
            AuditLogEntity auditEntity = AuditLogEntity.builder()
                    .userId(performedBy != null ? performedBy : "SYSTEM")
                    .action("AUDIT_SERVICE_ACTION")
                    .operation(action)
                    .details(details + " [RequestID: " + requestId + "]")
                    .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                    .lastModifiedDate(LocalDateTime.now(ZoneId.of("UTC")))
                    .isSuccess(true)
                    .build();

            // Let JPA generate the ID automatically
            auditLogRepository.save(auditEntity);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic lock failure for audit action: {} - {}", action, e.getMessage());
            // Optionally retry or just log the error since audit logs are not critical
        } catch (Exception e) {
            log.error("Failed to log audit action: {}", e.getMessage(), e);
        }
    }

    // Alternative: Return the saved entity if needed
    public AuditLogEntity logAuditActionWithReturn(String action, String performedBy, String details, String requestId) {
        try {
            AuditLogEntity auditEntity = AuditLogEntity.builder()
                    .userId(performedBy != null ? performedBy : "SYSTEM")
                    .action("AUDIT_SERVICE_ACTION")
                    .operation(action)
                    .details(details + " [RequestID: " + requestId + "]")
                    .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                    .lastModifiedDate(LocalDateTime.now(ZoneId.of("UTC")))
                    .isSuccess(true)
                    .build();

            return auditLogRepository.save(auditEntity);
        } catch (Exception e) {
            log.error("Failed to log audit action: {}", e.getMessage(), e);
            return null;
        }
    }

    // Add a method for direct entity logging
    public void logAuditEntity(AuditLogEntity auditLog) {
        try {
            if (auditLog.getAuditId() == null) {
                // New entity - let JPA generate ID
                auditLogRepository.save(auditLog);
            } else {
                // Existing entity - use merge
                auditLogRepository.save(auditLog);
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic lock failure for audit entity: {}", e.getMessage());
            // Create a fresh entity to avoid lock issues
            AuditLogEntity freshEntity = AuditLogEntity.builder()
                    .userId(auditLog.getUserId())
                    .action(auditLog.getAction())
                    .operation(auditLog.getOperation())
                    .details(auditLog.getDetails())
                    .isSuccess(auditLog.getIsSuccess())
                    .build();
            auditLogRepository.save(freshEntity);
        } catch (Exception e) {
            log.error("Failed to log audit entity: {}", e.getMessage(), e);
        }
    }

    // Existing specification methods...
    public static Specification<AuditLogEntity> withUserId(String userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<AuditLogEntity> withAction(String action) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("action"), action);
    }

    public static Specification<AuditLogEntity> withOperation(String operation) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("operation"), operation);
    }

    public static Specification<AuditLogEntity> withDetailsContaining(String details) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("details")),
                        "%" + details.toLowerCase() + "%"
                );
    }

    public static Specification<AuditLogEntity> createdAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<AuditLogEntity> createdBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }

    public static Specification<AuditLogEntity> withAuditId(UUID auditId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("auditId"), auditId);
    }

    public static AuditLogDTO convertToDTO(AuditLogEntity entity) {
        return new AuditLogDTO(
                entity.getAuditId(),
                entity.getUserId(),
                entity.getAction(),
                entity.getOperation(),
                entity.getDetails(),
                entity.getCreatedAt()
        );
    }

    public Specification<AuditLogEntity> buildSearchSpecification(AuditLogSearchRequest searchRequest) {
        Specification<AuditLogEntity> spec = Specification.where(null);

        if (searchRequest.getUserId() != null && !searchRequest.getUserId().trim().isEmpty()) {
            spec = spec.and(AuditLogHelper.withUserId(searchRequest.getUserId().trim()));
        }

        if (searchRequest.getAction() != null && !searchRequest.getAction().trim().isEmpty()) {
            spec = spec.and(AuditLogHelper.withAction(searchRequest.getAction().trim()));
        }

        if (searchRequest.getOperation() != null && !searchRequest.getOperation().trim().isEmpty()) {
            spec = spec.and(AuditLogHelper.withOperation(searchRequest.getOperation().trim()));
        }

        if (searchRequest.getDetails() != null && !searchRequest.getDetails().trim().isEmpty()) {
            spec = spec.and(AuditLogHelper.withDetailsContaining(searchRequest.getDetails().trim()));
        }

        if (searchRequest.getStartDate() != null) {
            spec = spec.and(AuditLogHelper.createdAfter(searchRequest.getStartDate()));
        }

        if (searchRequest.getEndDate() != null) {
            spec = spec.and(AuditLogHelper.createdBefore(searchRequest.getEndDate()));
        }

        if (searchRequest.getAuditId() != null) {
            spec = spec.and(AuditLogHelper.withAuditId(searchRequest.getAuditId()));
        }

        return spec;
    }

    public String buildSearchCriteriaString(AuditLogSearchRequest searchRequest) {
        StringBuilder criteria = new StringBuilder();

        if (searchRequest.getUserId() != null && !searchRequest.getUserId().trim().isEmpty()) {
            criteria.append("userId=").append(searchRequest.getUserId()).append("; ");
        }

        if (searchRequest.getAction() != null && !searchRequest.getAction().trim().isEmpty()) {
            criteria.append("action=").append(searchRequest.getAction()).append("; ");
        }

        if (searchRequest.getOperation() != null && !searchRequest.getOperation().trim().isEmpty()) {
            criteria.append("operation=").append(searchRequest.getOperation()).append("; ");
        }

        if (searchRequest.getDetails() != null && !searchRequest.getDetails().trim().isEmpty()) {
            criteria.append("details contains '").append(searchRequest.getDetails()).append("'; ");
        }

        if (searchRequest.getStartDate() != null) {
            criteria.append("from=").append(searchRequest.getStartDate()).append("; ");
        }

        if (searchRequest.getEndDate() != null) {
            criteria.append("to=").append(searchRequest.getEndDate()).append("; ");
        }

        if (searchRequest.getAuditId() != null) {
            criteria.append("auditId=").append(searchRequest.getAuditId()).append("; ");
        }

        return criteria.toString().isEmpty() ? "No filters applied" : criteria.toString();
    }
}