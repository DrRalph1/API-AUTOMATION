package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchUsersResponse {
    private String query;
    private List<UserDto> results;
    private int total;
    private Date searchAt;
    private Map<String, Object> metadata;
}