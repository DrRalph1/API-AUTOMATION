package com.usg.apiAutomation.dtos.userManagement;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkUserResponseDTO {
    private int responseCode;
    private String message;
    private List<UserResultDTO> data;
}