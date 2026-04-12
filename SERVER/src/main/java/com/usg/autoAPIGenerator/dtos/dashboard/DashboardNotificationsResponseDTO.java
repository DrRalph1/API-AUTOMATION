package com.usg.autoAPIGenerator.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardNotificationsResponseDTO {
    private List<DashboardNotificationDTO> notifications;
    private int unreadCount;
}