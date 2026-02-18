package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchUsersRequestDTO {
    private String query;
    private Map<String, Object> filters;
    private String sortField;
    private String sortDirection;
    private int page;
    private int pageSize;
    private List<String> fields; // Fields to return
}