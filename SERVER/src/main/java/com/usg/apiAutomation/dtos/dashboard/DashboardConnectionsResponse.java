package com.usg.apiAutomation.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardConnectionsResponse {
    private List<ConnectionDto> connections;
    private int total;
    private int active;
    private int idle;
    private int disconnected;
    private String timestamp;

    // Constructor for easy instantiation
    public DashboardConnectionsResponse(List<ConnectionDto> connections) {
        this.connections = connections;
        this.total = connections.size();
        this.active = (int) connections.stream().filter(c -> "connected".equals(c.getStatus())).count();
        this.idle = (int) connections.stream().filter(c -> "idle".equals(c.getStatus())).count();
        this.disconnected = (int) connections.stream().filter(c -> "disconnected".equals(c.getStatus())).count();
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // Constructor with all parameters
    public DashboardConnectionsResponse(List<ConnectionDto> connections, int total, int active, int idle, int disconnected) {
        this.connections = connections;
        this.total = total;
        this.active = active;
        this.idle = idle;
        this.disconnected = disconnected;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}