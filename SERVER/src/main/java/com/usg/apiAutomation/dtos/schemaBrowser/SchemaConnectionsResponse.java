package com.usg.apiAutomation.dtos.schemaBrowser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaConnectionsResponse {
    private List<ConnectionDto> connections;
    private Integer totalCount;
    private Integer activeCount;
    private Integer inactiveCount;
    private String lastUpdated;

    public SchemaConnectionsResponse(List<ConnectionDto> connections) {
        this.connections = connections;
        this.totalCount = connections.size();
        this.activeCount = (int) connections.stream().filter(c -> "connected".equals(c.getStatus())).count();
        this.inactiveCount = totalCount - activeCount;
        this.lastUpdated = java.time.LocalDateTime.now().toString();
    }
}