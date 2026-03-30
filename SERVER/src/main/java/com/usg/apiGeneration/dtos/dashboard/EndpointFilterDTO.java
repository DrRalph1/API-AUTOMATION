package com.usg.apiGeneration.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointFilterDTO {
    private String collectionId;
    private String method;
    private String search;
}