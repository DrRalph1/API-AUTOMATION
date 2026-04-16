// GraphQLConfigDTO.java
package com.usg.autoAPIGenerator.dtos.apiGenerationEngine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLConfigDTO {
    private String operationType;           // "query", "mutation", "subscription"
    private String operationName;
    private String schema;
    private Boolean enableIntrospection;
    private Boolean enablePersistedQueries;
    private Integer maxQueryDepth;
    private Boolean enableBatching;
    private Boolean subscriptionsEnabled;
    private List<String> customDirectives;
}