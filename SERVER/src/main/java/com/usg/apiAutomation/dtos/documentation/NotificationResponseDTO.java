package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private List<NotificationDTO> notifications;
    private int unreadCount;
    private String timestamp;

    public NotificationResponseDTO(List<NotificationDTO> notifications) {
        this.notifications = notifications;
        this.unreadCount = notifications != null ?
                (int) notifications.stream().filter(n -> !n.isRead()).count() : 0;
        this.timestamp = LocalDateTime.now().toString();
    }
}