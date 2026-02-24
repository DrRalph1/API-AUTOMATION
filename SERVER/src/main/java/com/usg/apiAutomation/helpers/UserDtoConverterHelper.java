package com.usg.apiAutomation.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.userManagement.ActivityLogDTO;
import com.usg.apiAutomation.dtos.userManagement.DeviceInfoDTO;
import com.usg.apiAutomation.dtos.userManagement.UserDTO;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import com.usg.apiAutomation.entities.postgres.UserRoleEntity;
import com.usg.apiAutomation.entities.postgres.userManagement.UserActivityEntity;
import com.usg.apiAutomation.entities.postgres.userManagement.UserDeviceEntity;
import com.usg.apiAutomation.entities.postgres.userManagement.UserTagEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDtoConverterHelper {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;

    public UserDTO convertToDto(UserEntity userEntity,
                                UserRoleEntity roleEntity,
                                List<UserDeviceEntity> devices,
                                List<UserTagEntity> tags,
                                long apiKeysCount,
                                long activeSessionsCount) {

        UserDTO dto = new UserDTO();

        dto.setId(userEntity.getUserId());
        dto.setUsername(userEntity.getUsername());
        dto.setEmail(userEntity.getEmailAddress());
        dto.setFullName(userEntity.getFullName());
        dto.setRole(roleEntity != null ? roleEntity.getRoleName() : null);
        dto.setStatus(userEntity.getIsActive() ? "active" : "inactive");
        dto.setAvatarColor(generateAvatarColor(userEntity.getUserId()));
        dto.setDepartment(userEntity.getStaffId()); // Using staffId as department identifier
        dto.setPermissions(getPermissionsForRole(roleEntity));
        dto.setMfaEnabled(false); // Add MFA field to UserEntity if needed
        dto.setEmailVerified(userEntity.getEmailAddress() != null);
        dto.setPhoneVerified(userEntity.getPhoneNumber() != null);
        dto.setApiAccessCount((int) apiKeysCount);
        dto.setLastLoginIp(getLastLoginIp(userEntity));
        dto.setLocation(getUserLocation(userEntity));
        dto.setTimezone(getUserTimezone(userEntity));
        dto.setTotalLogins(getTotalLogins(userEntity));
        dto.setFailedLogins(userEntity.getFailedLoginAttempts());
        dto.setSecurityScore(calculateSecurityScore(userEntity));
        dto.setTags(tags.stream().map(UserTagEntity::getTagName).collect(Collectors.toList()));
        dto.setApiKeys((int) apiKeysCount);
        dto.setActiveSessions((int) activeSessionsCount);

        dto.setLastActive(userEntity.getLastLogin() != null ?
                userEntity.getLastLogin().format(DateTimeFormatter.ISO_DATE_TIME) :
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        dto.setJoinedDate(userEntity.getCreatedDate() != null ?
                userEntity.getCreatedDate().format(DateTimeFormatter.ISO_DATE_TIME) :
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        // Convert devices
        List<Map<String, Object>> deviceMaps = devices.stream()
                .map(this::convertDeviceToMap)
                .collect(Collectors.toList());
        dto.setDevices(deviceMaps);

        return dto;
    }

    public List<DeviceInfoDTO> convertDevicesToDeviceInfo(List<UserDeviceEntity> devices) {
        return devices.stream()
                .map(this::convertToDeviceInfo)
                .collect(Collectors.toList());
    }

    private DeviceInfoDTO convertToDeviceInfo(UserDeviceEntity device) {
        DeviceInfoDTO info = new DeviceInfoDTO();
        info.setType(device.getDeviceType());
        info.setLastUsed(device.getLastUsed() != null ?
                device.getLastUsed().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")) :
                "N/A");
        return info;
    }

    private Map<String, Object> convertDeviceToMap(UserDeviceEntity device) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", device.getDeviceType());
        map.put("lastUsed", device.getLastUsed() != null ?
                device.getLastUsed().format(DateTimeFormatter.ISO_DATE_TIME) :
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return map;
    }

    public ActivityLogDTO convertToActivityLog(UserActivityEntity activity) {
        return ActivityLogDTO.builder()
                .id(activity.getActivityId().toString())
                .type(activity.getActivityType())
                .description(activity.getDescription())
                .timestamp(Date.from(activity.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .ipAddress(activity.getIpAddress())
                .device(activity.getDeviceInfo())
                .location(activity.getLocation())
                .success(activity.getIsSuccess())
                .build();
    }

    private List<String> getPermissionsForRole(UserRoleEntity role) {
        if (role == null) return Arrays.asList("read");

        switch (role.getRoleName().toLowerCase()) {
            case "admin":
                return Arrays.asList("read", "write", "delete", "admin", "manage_users", "manage_roles");
            case "developer":
                return Arrays.asList("read", "write", "api_access", "debug");
            case "viewer":
                return Arrays.asList("read");
            default:
                return Arrays.asList("read");
        }
    }

    private String generateAvatarColor(String userId) {
        String[] colors = {"#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#14B8A6", "#F97316"};
        int index = Math.abs(userId.hashCode()) % colors.length;
        return colors[index];
    }

    private String getLastLoginIp(UserEntity user) {
        // This would come from UserSessionEntity
        return "N/A";
    }

    private String getUserLocation(UserEntity user) {
        // This would come from UserSessionEntity
        return "N/A";
    }

    private String getUserTimezone(UserEntity user) {
        // Default timezone
        return "UTC";
    }

    private int getTotalLogins(UserEntity user) {
        // This would be calculated from UserActivityEntity
        return 0;
    }

    private int calculateSecurityScore(UserEntity user) {
        int score = 50;

        if (user.getIsDefaultPassword() != null && !user.getIsDefaultPassword()) score += 20;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) score += 10;
        if (user.getEmailAddress() != null && user.getEmailAddress().contains("@")) score += 10;
        if (user.getFailedLoginAttempts() == 0) score += 10;

        return Math.min(score, 100);
    }
}