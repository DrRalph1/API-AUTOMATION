package com.usg.apiAutomation.dtos.systemActivities.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.usg.apiAutomation.entities.postgres.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for application users")
@Builder(builderClassName = "UserDTOBuilder", toBuilder = true)
public class UserDTO {

    @Schema(description = "Unique identifier of the user", example = "USER001")
    private String userId;

    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Username of the user", example = "johndoe")
    private String username;

    @Schema(description = "Password of the user (only for create/update operations)", example = "securePassword123!")
    private String password;

    @NotBlank(message = "Email address cannot be blank")
    @Schema(description = "Email address of the user", example = "johndoe@gmail.com")
    private String emailAddress;

    // ✅ ADDED
    @Schema(description = "Phone number of the user", example = "+233241234567")
    private String phoneNumber;

    @NotBlank(message = "Full name cannot be blank")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    // ✅ ADDED
    @Schema(description = "Staff ID of the user", example = "STF001")
    private String staffId;

    @NotNull(message = "Role ID is required")
    @Schema(description = "Role ID assigned to the user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID roleId;

    @Schema(description = "Role name assigned to the user", example = "ADMIN")
    private String roleName;

    @Schema(description = "Indicates if the user account is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Indicates if the user is still using the default password", example = "true")
    @JsonProperty("isDefaultPassword")
    private Boolean isDefaultPassword;

    @Schema(description = "Number of consecutive failed login attempts", example = "0")
    private Integer failedLoginAttempts;

    @Schema(description = "Timestamp until which the account is locked", example = "2024-01-15T10:30:00Z")
    private Instant accountLockedUntil;

    // ✅ ADDED
    @Schema(description = "Last login timestamp")
    private LocalDateTime lastLogin;

    // ✅ ADDED
    @Schema(description = "JWT token for authenticated session")
    private String token;

    @Schema(description = "Date and time when the user was created", example = "2024-01-01T09:00:00")
    private LocalDateTime createdDate;

    @Schema(description = "Date and time when the user was last modified", example = "2024-01-15T09:30:00")
    private LocalDateTime lastModifiedDate;

    // Constructor for create operations (without systemActivities fields)
    public UserDTO(String userId, String username, String password, String fullName,
                   UUID roleId, String roleName, Boolean isActive, Boolean isDefaultPassword) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.isActive = isActive != null ? isActive : true;
        this.isDefaultPassword = isDefaultPassword != null ? isDefaultPassword : true;
        this.failedLoginAttempts = 0;
    }

    public UserDTO(
            String userId,
            String username,
            String password,
            String fullName,
            UUID roleId,
            String roleName,
            Boolean isActive,
            Boolean isDefaultPassword,
            Integer failedLoginAttempts,
            Instant accountLockedUntil,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.isActive = isActive;
        this.isDefaultPassword = isDefaultPassword;
        this.failedLoginAttempts = failedLoginAttempts;
        this.accountLockedUntil = accountLockedUntil;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    // Constructor for create operations (without isDefaultPassword)
    public UserDTO(String userId, String username, String password, String fullName,
                   UUID roleId, String roleName, Boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.isActive = isActive != null ? isActive : true;
        this.isDefaultPassword = true; // Default to true for new users
        this.failedLoginAttempts = 0;
    }

    // Static factory method for response (without sensitive data)
    public static UserDTO fromEntityForResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDTO.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .fullName(entity.getFullName())
                .emailAddress(entity.getEmailAddress()) // ✅ ADDED
                .phoneNumber(entity.getPhoneNumber())   // ✅ ADDED
                .staffId(entity.getStaffId())           // ✅ ADDED
                .roleId(entity.getRole() != null ? entity.getRole().getRoleId() : null)
                .roleName(entity.getRole() != null ? entity.getRole().getRoleName() : null)
                .isActive(entity.getIsActive())
                .isDefaultPassword(entity.getIsDefaultPassword())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .accountLockedUntil(entity.getAccountLockedUntil())
                .lastLogin(entity.getLastLogin())       // ✅ ADDED
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    // Static factory method for create/update (with password)
    public static UserDTO fromEntityForManagement(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDTO.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .password(entity.getPassword()) // Include password only for management operations
                .fullName(entity.getFullName())
                .emailAddress(entity.getEmailAddress()) // ✅ ADDED
                .phoneNumber(entity.getPhoneNumber())   // ✅ ADDED
                .staffId(entity.getStaffId())           // ✅ ADDED
                .roleId(entity.getRole() != null ? entity.getRole().getRoleId() : null)
                .roleName(entity.getRole() != null ? entity.getRole().getRoleName() : null)
                .isActive(entity.getIsActive())
                .isDefaultPassword(entity.getIsDefaultPassword())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .accountLockedUntil(entity.getAccountLockedUntil())
                .lastLogin(entity.getLastLogin())       // ✅ ADDED
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }


    // Add this constructor to UserDTO
    public UserDTO(
            String userId,
            String username,
            String password,
            String fullName,
            UUID roleId,
            String roleName,
            Boolean isActive,
            Boolean isDefaultPassword,
            Integer failedLoginAttempts,
            Instant accountLockedUntil,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate,
            String staffId,        // New
            String emailAddress,
            String phoneNumber,    // New
            LocalDateTime lastLogin      // New
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.isActive = isActive;
        this.isDefaultPassword = isDefaultPassword;
        this.failedLoginAttempts = failedLoginAttempts;
        this.accountLockedUntil = accountLockedUntil;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.staffId = staffId;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.lastLogin = lastLogin;
    }


    // Helper method to check if account is currently locked
    @Schema(description = "Indicates if the account is currently locked", example = "false")
    public Boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return Instant.now().isBefore(accountLockedUntil);
    }

    // ✅ Mapping helper
    public static UserDTO fromEntity(UserEntity entity) {
        if (entity == null) return null;
        return UserDTO.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .emailAddress(entity.getEmailAddress())
                .build();
    }

    // Helper method to check if password needs to be changed
    @Schema(description = "Indicates if the user needs to change their password", example = "true")
    public Boolean isPasswordChangeRequired() {
        return Boolean.TRUE.equals(isDefaultPassword);
    }
}