package com.usg.apiAutomation.dtos.userManagement;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDTO {
    private String type;
    private String lastUsed;
    private String os;
    private String browser;
    private String ipAddress;
    private String location;
    private boolean trusted;
}