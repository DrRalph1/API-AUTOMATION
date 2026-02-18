package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersListResponseDTO {
    private List<UserDTO> users;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private Map<String, Object> stats;
}