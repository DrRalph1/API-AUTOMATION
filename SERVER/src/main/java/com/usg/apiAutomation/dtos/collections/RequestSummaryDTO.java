package com.usg.apiAutomation.dtos.collections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSummaryDTO {
    private String id;
    private String name;
    private String method;
    private String url;
    private String description;
    private String authType;
    private String body;
    private String tests;
    private String preRequestScript;
    private boolean saved;
}