package com.usg.apiAutomation.dtos.documentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private String title;
    private String message;
    private String type; // info, warning, success, error
    private boolean read;
    private String time;
    private String icon;
    private String actionUrl;
    private String collectionId;
    private String endpointId;
}