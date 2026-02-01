package com.usg.apiAutomation.dtos.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"responseCode", "message", "timestamp", "totalProcessed", "successful", "failed", "data"})
public class BulkRoleResponseDTO {
    private int responseCode;
    private String message;
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
    private Integer totalProcessed;
    private Integer successful;
    private Integer failed;
    private List<RoleResultDTO> data;
}