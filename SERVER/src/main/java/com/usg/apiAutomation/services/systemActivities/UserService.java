package com.usg.apiAutomation.services.systemActivities;

import com.usg.apiAutomation.dtos.userManagement.*;
import com.usg.apiAutomation.entities.UserRoleEntity;
import com.usg.apiAutomation.entities.UserEntity;
import com.usg.apiAutomation.exceptions.GlobalExceptionHandler.BusinessRuleException;
import com.usg.apiAutomation.exceptions.GlobalExceptionHandler.ConflictException;
import com.usg.apiAutomation.exceptions.GlobalExceptionHandler.ResourceNotFoundException;
import com.usg.apiAutomation.helpers.ErrorHandlingHelper;
import com.usg.apiAutomation.helpers.SortValidationHelper;
import com.usg.apiAutomation.repositories.AppRoleRepository;
import com.usg.apiAutomation.repositories.AppUserRepository;
import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.mail.MessagingException;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository roleRepository;
    private final SortValidationHelper sortValidationHelper;
    private final LoggerUtil loggerUtil;
    private final AccountLockService accountLockService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ErrorHandlingHelper errorHandlingHelper;
    private final EmailService emailService;
    private final SMSService smsService;

    // List of valid sort fields for UserEntity
    private static final String[] VALID_SORT_FIELDS = {
            "userId", "username", "fullName", "role.roleName",
            "createdAt", "updatedAt"
    };

    // ========== CREATE USER ==========
    @Transactional
    public ResponseEntity<?> createUser(UserDTO dto, String requestId, HttpServletRequest req, String performedBy) {
        Map<String, Object> result = new HashMap<>();

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Creating userManagement: " + dto.getUsername() +
                        ", Requested by: " + safePerformedBy);

        try {
            validateUserBusinessRules(dto);

            // Check if username already exists
            Optional<UserEntity> existingUser = appUserRepository.findByUsername(dto.getUsername());
            if (existingUser.isPresent()) {
                String message = String.format("Username '%s' already exists", dto.getUsername());
                result.put("responseCode", 409);
                result.put("message", message);

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", Conflict: " + message);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }

            UserRoleEntity role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Role with ID %s not found", dto.getRoleId())
                    ));

            // ✅ CRITICAL: Generate a userManagement ID before creating the entity
            String generatedUserId = dto.getUsername();

            UserEntity entity = UserEntity.builder()
                    .userId(generatedUserId)  // ✅ MUST SET THE USER ID HERE
                    .username(dto.getUsername())
                    .fullName(dto.getFullName())
                    .role(role)
                    .isActive(true)  // ✅ Set default active status
                    .isDefaultPassword(true)  // ✅ New users have default password
                    .failedLoginAttempts(0)   // ✅ Initialize failed attempts
                    // Optional: Set email, phone, staffId if provided in DTO
                    .emailAddress(dto.getEmailAddress() != null ? dto.getEmailAddress() : null)
                    .phoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : null)
                    .staffId(dto.getStaffId() != null ? dto.getStaffId() : null)
                    .build();

            // ✅ Set a default password for new users
            if (entity.getPassword() == null) {
                String defaultPassword = "Password123!"; // Or generate a random one
                entity.setPassword(passwordEncoder.encode(defaultPassword));
            }

            entity = appUserRepository.save(entity);
            dto.setUserId(entity.getUserId());
            dto.setRoleName(role.getRoleName());
            dto.setIsActive(entity.getIsActive());
            dto.setIsDefaultPassword(entity.getIsDefaultPassword());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId +
                            ", User created successfully. ID: " + generatedUserId +
                            ", Username: " + dto.getUsername());

            result.put("responseCode", 201);
            result.put("message", "User created successfully");
            result.put("data", dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (ResourceNotFoundException e) {
            result.put("responseCode", 404);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Resource not found: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);

        } catch (ConflictException e) {
            result.put("responseCode", 409);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Conflict: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);

        } catch (BusinessRuleException e) {
            result.put("responseCode", 422);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Business rule violation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);

        } catch (DataIntegrityViolationException e) {
            result.put("responseCode", 409);
            result.put("message", "A userManagement with similar data already exists");

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Data integrity violation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);

        } catch (Exception e) {
            result.put("responseCode", 500);
            result.put("message", "Internal server error: " + e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error creating userManagement: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }




    // ========== USER LOGIN ==========
    @Transactional
    public ResponseEntity<?> userLogin(UserLoginRequestDTO userLoginRequestDTO, String requestId,
                                       HttpServletRequest req, String performedBy) {
        Map<String, Object> result = new HashMap<>();
        result.put("user_id", userLoginRequestDTO.getUserId());
        String userId = userLoginRequestDTO.getUserId();

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";
        loggerUtil.log("api-automation", "[Login] Request ID: " + requestId +
                ", Login attempt for userManagement: " + userId +
                ", Performed by: " + safePerformedBy);

        try {
            loggerUtil.log("api-automation",
                    "[Login] Request ID: " + requestId + ", Checking if userManagement exists: " + userId);

            Optional<UserEntity> userOptional = appUserRepository.findByUserIdIgnoreCase(userId);

            if (userOptional.isEmpty()) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Invalid userManagement: " + userId);
                throw new RuntimeException("Invalid User ID or Password");
            }

            UserEntity user = userOptional.get();
            loggerUtil.log("api-automation",
                    "[Login] Request ID: " + requestId +
                            ", User found: " + userId +
                            ", DefaultPwd: " + user.getIsDefaultPassword() +
                            ", Active: " + user.getIsActive());

            // Check if account is locked
            if (accountLockService.isAccountLocked(user.getUserId())) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Account is locked for userManagement: " + userId);
                String lockStatus = accountLockService.getLockStatus(user.getUserId());
                if ("PERMANENTLY_LOCKED".equals(lockStatus)) {
                    throw new RuntimeException("Account locked due to multiple failed attempts. Please contact support to unlock your account.");
                } else {
                    throw new RuntimeException("Account locked. Please contact support.");
                }
            }

            // Null-safe password check
            if (user.getPassword() == null) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Password not set for userManagement: " + userId);
                throw new RuntimeException("User password not set. Contact support.");
            }

            // Verify password
            loggerUtil.log("api-automation",
                    "[Login] Request ID: " + requestId + ", Verifying password for userManagement: " + userId);
            if (!passwordEncoder.matches(userLoginRequestDTO.getPassword(), user.getPassword())) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Invalid password attempt for userManagement: " + userId);
                try {
                    accountLockService.handleFailedLoginAttempt(user.getUserId());
                    loggerUtil.log("api-automation",
                            "[Login] Request ID: " + requestId + ", Incremented failed login attempt count for userManagement: " + userId);
                } catch (MessagingException e) {
                    loggerUtil.log("api-automation",
                            "Request ID: " + requestId + ", Failed to send account locked email: " + e.getMessage());
                }

                int maxAttempts = 3;
                int attemptsRemaining = maxAttempts - (user.getFailedLoginAttempts() + 1);
                String errorMsg = "Invalid User ID or Password";
                if (attemptsRemaining > 0) {
                    errorMsg += ". " + attemptsRemaining + " attempts remaining before account lock.";
                } else {
                    errorMsg += ". Account has been locked due to multiple failed attempts. Contact support.";
                }
                throw new RuntimeException(errorMsg);
            }

            // Reset failed attempts on successful login
            accountLockService.resetFailedAttempts(user.getUserId());
            loggerUtil.log("api-automation",
                    "[Login] Request ID: " + requestId + ", Reset failed attempts for userManagement: " + userId);

            if (Boolean.FALSE.equals(user.getIsActive())) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Inactive account: " + userId);
                throw new RuntimeException("Your account has been deactivated. Please contact support");
            }

            // Enhanced default password check with null safety
            Boolean isDefaultPassword = user.getIsDefaultPassword();
            if (isDefaultPassword == null) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId +
                                ", isDefaultPassword is null for userManagement: " + userId + ", defaulting to false");
                isDefaultPassword = false;
            }

            // GENERATE JWT
            String jwtToken = jwtUtil.generateToken(user.getUserId());

            if (Boolean.TRUE.equals(isDefaultPassword)) {
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId +
                                ", User " + userId + " has default password - requires reset");

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("requiresPasswordReset", true);
                responseData.put("token", jwtToken);
                responseData.put("userId", user.getUserId());
                responseData.put("emailAddress", user.getEmailAddress() != null ? user.getEmailAddress() : "");

                result.put("responseCode", 200);
                result.put("message", "Please reset your default password");
                result.put("data", responseData);
            } else {
                // Update last login + reset counters
                appUserRepository.updateLastLogin(user.getUserId(), LocalDateTime.now(), LocalDateTime.now());
                appUserRepository.resetFailedLoginAttempts(user.getUserId());

                String role = appUserRepository.getUserRoleByUserId(user.getUserId());

                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", user.getUserId());
                userData.put("staff_id", user.getStaffId() != null ? user.getStaffId() : "");
                userData.put("role", role != null ? role : "");
                userData.put("username", user.getUsername() != null ? user.getUsername() : "");
                userData.put("email", user.getEmailAddress() != null ? user.getEmailAddress() : "");
                userData.put("phone_number", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                userData.put("last_login", user.getLastLogin());
                userData.put("is_active", user.getIsActive() != null ? user.getIsActive() : true);
                userData.put("is_default_password", isDefaultPassword);
                userData.put("token", jwtToken);

                result.put("responseCode", 200);
                result.put("message", "Login successful");
                result.put("data", userData);

                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Successful login for userManagement: " + userId);
            }

        } catch (Exception e) {
            String responseMessage = e.getMessage();
            if (responseMessage == null) {
                responseMessage = "Unknown error occurred during login";
                loggerUtil.log("api-automation",
                        "[Login] Request ID: " + requestId + ", Null error message for userManagement: " + userId);
            }

            loggerUtil.log("api-automation",
                    "[Login] Request ID: " + requestId +
                            ", Error during login for userManagement: " + userId + " -> " + responseMessage);

            int errorCode = errorHandlingHelper.determineErrorCodeBasedOnMessage(responseMessage);
            result.put("responseCode", errorCode);
            result.put("message", responseMessage);
        }

        loggerUtil.log("api-automation",
                "[Login] Request ID: " + requestId +
                        ", Final response for userManagement: " + userId +
                        ", code: " + result.get("responseCode"));

        return ResponseEntity.status((int) result.get("responseCode")).body(result);
    }




    @Transactional
    public ResponseEntity<?> resetPassword(
            UserPasswordResetRequestDTO userPasswordResetRequestDTO,
            String requestId,
            HttpServletRequest req,
            String performedBy) {

        Map<String, Object> result = new HashMap<>();
        String userId = userPasswordResetRequestDTO.getUserId();
        result.put("user_id", userId);

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("web-application-firewall",
                "[PasswordReset] Request ID: " + requestId +
                        ", Password reset attempt for userManagement: " + userId +
                        ", Performed by: " + safePerformedBy);

        try {
            // Fetch userManagement from repository
            Optional<UserDTO> userOptional = appUserRepository.getUserByUserId(userId);
            if (userOptional.isEmpty()) {
                loggerUtil.log("web-application-firewall",
                        "[PasswordReset] Request ID: " + requestId + ", User not found: " + userId);
                result.put("responseCode", 404);
                result.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            UserDTO user = userOptional.get();
            loggerUtil.log("web-application-firewall",
                    "[PasswordReset] Request ID: " + requestId +
                            ", User found: " + userId +
                            ", Active: " + user.getIsActive());

            // Validate new password
            String newPassword = userPasswordResetRequestDTO.getNewPassword();
            String confirmPassword = userPasswordResetRequestDTO.getConfirmPassword();

            if (!newPassword.equals(confirmPassword)) {
                result.put("responseCode", 409);
                result.put("message", "New password and confirm password do not match");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }

            if (newPassword.length() < 8) {
                result.put("responseCode", 409);
                result.put("message", "New password must be at least 8 characters");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }

            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                result.put("responseCode", 409);
                result.put("message", "New password cannot be the same as current password");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }

            // Encrypt and update password
            String encryptedPassword = passwordEncoder.encode(newPassword);
            appUserRepository.updatePassword(
                    userId,
                    user.getPassword(),
                    encryptedPassword,
                    LocalDateTime.now()
            );

            // Send email notification
            String email = user.getEmailAddress();
            String phoneNumber = user.getPhoneNumber();
//            if (email != null && !email.isEmpty()) {
//                try {
//                    String emailSubject = "Password Reset Successful";
//                    String emailBody = "<html><body>" +
//                            "<h2>Password Reset Successful</h2>" +
//                            "<p>Your password has been successfully reset.</p>" +
//                            "<p>If you did not initiate this password reset, please contact support immediately.</p>" +
//                            "</body></html>";
//
//                    emailService.sendEmail(email, emailSubject, emailBody);
//                    loggerUtil.log("web-application-firewall",
//                            "[PasswordReset] Request ID: " + requestId + ", Email sent to: " + email);
//                } catch (Exception e) {
//                    loggerUtil.log("web-application-firewall",
//                            "[PasswordReset] Request ID: " + requestId +
//                                    ", Password reset successful but failed to send email: " + e.getMessage());
//                }
//            }

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                try {
                    String smsMessage = "Dear " + user.getFullName() + ", your password has been successfully reset. " +
                            "If you did not initiate this request (ID: " + requestId.substring(0, 8) + "), " +
                            "please contact support immediately.";

                    smsService.sendSms(phoneNumber, smsMessage);
                    loggerUtil.log("web-application-firewall",
                            "[PasswordReset] Request ID: " + requestId + ", SMS sent to: " + phoneNumber);
                } catch (Exception e) {
                    loggerUtil.log("web-application-firewall",
                            "[PasswordReset] Request ID: " + requestId +
                                    ", Password reset successful but failed to send SMS: " + e.getMessage());
                }
            }

            // Build response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", userId);
            responseData.put("email", email != null ? email : "");
            responseData.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
            responseData.put("passwordReset", true);

            result.put("responseCode", 200);
            result.put("message", "Password reset successful");
            result.put("data", responseData);

            loggerUtil.log("web-application-firewall",
                    "[PasswordReset] Request ID: " + requestId + ", Successful password reset for userManagement: " + userId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("web-application-firewall",
                    "[PasswordReset] Request ID: " + requestId +
                            ", Error during password reset for userManagement: " + userId + " -> " + e.getMessage());

            result.put("responseCode", 500);
            result.put("message", "An error occurred during password reset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }



    @Transactional
    public ResponseEntity<?> forgotPassword(UserForgotPasswordRequestDTO userForgotPasswordRequestDTO,
                                            String requestId,
                                            HttpServletRequest req) {

        Map<String, Object> result = new HashMap<>();
        String userId = userForgotPasswordRequestDTO.getUserId();
        result.put("user_id", userId);

        loggerUtil.log("web-application-firewall",
                "[ForgotPassword] Request ID: " + requestId +
                        ", Password reset attempt for userManagement: " + userId);

        try {
            // Fetch userManagement from repository
            Optional<UserDTO> userOptional = appUserRepository.getUserByUserId(userId);
            if (userOptional.isEmpty()) {
                loggerUtil.log("web-application-firewall",
                        "[ForgotPassword] Request ID: " + requestId + ", User not found: " + userId);
                result.put("responseCode", 404);
                result.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            UserDTO user = userOptional.get();
            loggerUtil.log("web-application-firewall",
                    "[ForgotPassword] Request ID: " + requestId +
                            ", User found: " + userId +
                            ", Active: " + user.getIsActive());

            // Generate a temporary password
            String tempPassword = RandomStringUtils.randomAlphanumeric(10); // Apache Commons Lang
            String encryptedPassword = passwordEncoder.encode(tempPassword);

            // Update password in DB
            appUserRepository.updatePassword(
                    userId,
                    user.getPassword(),
                    encryptedPassword,
                    LocalDateTime.now()
            );

            // Send SMS with temporary password
            String phoneNumber = user.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                try {
                    String smsMessage = "Dear " + user.getFullName() + ", your temporary password is: " +
                            tempPassword + ". Use it to login and change your password immediately. ";

                    smsService.sendSms(phoneNumber, smsMessage);
                    loggerUtil.log("web-application-firewall",
                            "[ForgotPassword] Request ID: " + requestId + ", SMS sent to: " + phoneNumber);
                } catch (Exception e) {
                    loggerUtil.log("web-application-firewall",
                            "[ForgotPassword] Request ID: " + requestId +
                                    ", Password reset successful but failed to send SMS: " + e.getMessage());
                }
            }

            // Optional email notification (commented out)
        /*
        String email = userManagement.getEmailAddress();
        if (email != null && !email.isEmpty()) {
            try {
                String emailSubject = "Password Reset Successful";
                String emailBody = "<html><body>" +
                        "<h2>Password Reset Successful</h2>" +
                        "<p>Your password has been successfully reset.</p>" +
                        "<p>If you did not initiate this request, please contact support immediately.</p>" +
                        "</body></html>";

                emailService.sendEmail(email, emailSubject, emailBody);
                loggerUtil.log("web-application-firewall",
                        "[ForgotPassword] Request ID: " + requestId + ", Email sent to: " + email);
            } catch (Exception e) {
                loggerUtil.log("web-application-firewall",
                        "[ForgotPassword] Request ID: " + requestId +
                                ", Password reset successful but failed to send email: " + e.getMessage());
            }
        }
        */

            // Build response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", userId);
            responseData.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
            responseData.put("passwordReset", true);

            result.put("responseCode", 200);
            result.put("message", "Temporary password sent via SMS");
            result.put("data", responseData);

            loggerUtil.log("web-application-firewall",
                    "[ForgotPassword] Request ID: " + requestId + ", Successful password reset for userManagement: " + userId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            loggerUtil.log("web-application-firewall",
                    "[ForgotPassword] Request ID: " + requestId +
                            ", Error during password reset for userManagement: " + userId + " -> " + e.getMessage());

            result.put("responseCode", 500);
            result.put("message", "An error occurred during password reset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }




    @Transactional
    public ResponseEntity<?> resetDefaultPassword(ResetPasswordRequestDTO resetRequest, String requestId,
                                                  HttpServletRequest req, String performedBy) {

        Map<String, Object> result = new HashMap<>();
        result.put("user_id", resetRequest.getUserId());
        String userId = resetRequest.getUserId();

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("web-application-firewall",
                "[DefaultPasswordReset] Request ID: " + requestId +
                        ", Default password reset for userManagement: " + userId +
                        ", Performed by: " + safePerformedBy);

        try {
            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId + ", Checking if userManagement exists: " + userId);

            // Verify userManagement exists
            Optional<UserDTO> userOptional = appUserRepository.getUserByUserId(userId);
            if (userOptional.isEmpty()) {
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", Invalid userManagement: " + userId);
                throw new RuntimeException("Invalid userManagement ID");
            }

            UserDTO user = userOptional.get();
            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId +
                            ", User found: " + userId +
                            ", DefaultPwd: " + user.getIsDefaultPassword() +
                            ", Active: " + user.getIsActive());

            // Check if account is locked
            if (accountLockService.isAccountLocked(userId)) {
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", Account is locked for userManagement: " + userId);
                String lockStatus = accountLockService.getLockStatus(userId);
                if ("PERMANENTLY_LOCKED".equals(lockStatus)) {
                    throw new RuntimeException("Account locked due to multiple failed attempts. Please contact support to unlock your account.");
                } else {
                    throw new RuntimeException("Account locked. Please contact support.");
                }
            }

            // Null-safe password check
            if (user.getPassword() == null) {
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", Password not set for userManagement: " + userId);
                throw new RuntimeException("User password not set. Contact support.");
            }

            // Verify old password
            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId + ", Verifying old password for userManagement: " + userId);
            if (!passwordEncoder.matches(resetRequest.getOldPassword(), user.getPassword())) {
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", Invalid old password attempt for userManagement: " + userId);
                try {
                    accountLockService.handleFailedLoginAttempt(userId);
                    loggerUtil.log("web-application-firewall",
                            "[DefaultPasswordReset] Request ID: " + requestId + ", Incremented failed attempt count for userManagement: " + userId);
                } catch (MessagingException e) {
                    loggerUtil.log("web-application-firewall",
                            "[DefaultPasswordReset] Request ID: " + requestId + ", Failed to send account locked email: " + e.getMessage());
                }

                int maxAttempts = 3;
                int attemptsRemaining = maxAttempts - (user.getFailedLoginAttempts() + 1);
                String errorMsg = "Invalid current password";
                if (attemptsRemaining > 0) {
                    errorMsg += ". " + attemptsRemaining + " attempts remaining before account lock.";
                } else {
                    errorMsg += ". Account has been locked due to multiple failed attempts. Contact support.";
                }
                throw new RuntimeException(errorMsg);
            }

            // Validate new password
            if (resetRequest.getNewPassword() == null || resetRequest.getNewPassword().length() < 8) {
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", New password too short for userManagement: " + userId);
                throw new RuntimeException("New password must be at least 8 characters");
            }

            // Check if new password is same as old password
            if (passwordEncoder.matches(resetRequest.getNewPassword(), user.getPassword())) {
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", New password same as old for userManagement: " + userId);
                throw new RuntimeException("New password cannot be the same as current password");
            }

            // Encode the new password
            String encodedNewPassword = passwordEncoder.encode(resetRequest.getNewPassword());

            // Update password and reset default flag
            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId + ", Before update - User: " + userId +
                            ", DefaultPwd: " + user.getIsDefaultPassword());

            appUserRepository.updatePasswordAndResetDefaultFlag(
                    userId,
                    encodedNewPassword,
                    LocalDateTime.now()
            );

            // Verify the update worked by fetching fresh data
            Optional<UserDTO> updatedUserOptional = appUserRepository.getUserByUserId(userId);
            if (updatedUserOptional.isPresent()) {
                UserDTO updatedUser = updatedUserOptional.get();
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", After update - User: " + userId +
                                ", DefaultPwd: " + updatedUser.getIsDefaultPassword() +
                                ", Update successful: " + !Boolean.TRUE.equals(updatedUser.getIsDefaultPassword()));
            }

            // Reset failed attempts on successful password reset
            accountLockService.resetFailedAttempts(userId);
            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId + ", Reset failed attempts for userManagement: " + userId);

            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", userId);
            responseData.put("requiresPasswordReset", false);
            responseData.put("passwordUpdated", true);

            result.put("responseCode", 200);
            result.put("message", "Password updated successfully");
            result.put("data", responseData);

            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId + ", Successful default password reset for userManagement: " + userId);

        } catch (Exception e) {
            String responseMessage = e.getMessage();
            if (responseMessage == null) {
                responseMessage = "Unknown error occurred during password reset";
                loggerUtil.log("web-application-firewall",
                        "[DefaultPasswordReset] Request ID: " + requestId + ", Null error message for userManagement: " + userId);
            }

            loggerUtil.log("web-application-firewall",
                    "[DefaultPasswordReset] Request ID: " + requestId +
                            ", Error during default password reset for userManagement: " + userId + " -> " + responseMessage);

            int errorCode = errorHandlingHelper.determineErrorCodeBasedOnMessage(responseMessage);
            result.put("responseCode", errorCode);
            result.put("message", responseMessage);
        }

        loggerUtil.log("web-application-firewall",
                "[DefaultPasswordReset] Request ID: " + requestId +
                        ", Final response for userManagement: " + userId +
                        ", code: " + result.get("responseCode"));

        return ResponseEntity.status((int) result.get("responseCode")).body(result);
    }




    // ========== BULK CREATE USERS ==========
    @Transactional
    public ResponseEntity<?> createUsersBulk(List<UserDTO> dtos, String requestId, HttpServletRequest req, String performedBy) {
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Bulk creating " + dtos.size() + " users" +
                        ", Requested by: " + safePerformedBy);

        if (dtos == null || dtos.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("responseCode", 400);
            result.put("message", "No users provided for bulk creation");

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", No users provided for bulk creation");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        // Validate all DTOs first
        for (UserDTO dto : dtos) {
            try {
                validateUserBusinessRules(dto);
            } catch (BusinessRuleException e) {
                Map<String, Object> result = new HashMap<>();
                result.put("responseCode", 422);
                result.put("message", "Validation failed for userManagement: " + e.getMessage());

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", Validation failed: " + e.getMessage());

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);
            }
        }

        List<UserResultDTO> results = new ArrayList<>();
        List<UserEntity> entitiesToSave = new ArrayList<>();
        int createdCount = 0;
        int duplicateCount = 0;
        AtomicInteger roleNotFoundCount = new AtomicInteger();

        // Process each DTO
        for (UserDTO dto : dtos) {
            UserResultDTO result = UserResultDTO.builder()
                    .username(dto.getUsername())
                    .fullName(dto.getFullName())
                    .roleId(String.valueOf(dto.getRoleId()))
                    .build();

            try {
                // Check if role exists
                UserRoleEntity role = roleRepository.findById(dto.getRoleId())
                        .orElseThrow(() -> {
                            result.setStatus("ROLE_NOT_FOUND");
                            result.setMessage(String.format("Role with ID %s not found", dto.getRoleId()));
                            roleNotFoundCount.getAndIncrement();
                            return new ResourceNotFoundException(result.getMessage());
                        });

                // Check for duplicate username
                Optional<UserEntity> existingUser = appUserRepository.findByUsername(dto.getUsername());

                if (existingUser.isPresent()) {
                    result.setStatus("DUPLICATE");
                    result.setMessage(String.format(
                            "Username '%s' already exists", dto.getUsername()
                    ));
                    result.setUserId(existingUser.get().getUserId().toString());
                    result.setRoleName(existingUser.get().getRole().getRoleName());
                    duplicateCount++;
                } else {
                    UserEntity entity = UserEntity.builder()
                            .username(dto.getUsername())
                            .fullName(dto.getFullName())
                            .role(role)
                            .build();
                    entitiesToSave.add(entity);
                    result.setStatus("PENDING_CREATION");
                    createdCount++;
                }

            } catch (ResourceNotFoundException ex) {
                // Already handled in the lambda
            }

            results.add(result);
        }

        // Bulk save all new users
        if (!entitiesToSave.isEmpty()) {
            try {
                List<UserEntity> savedEntities = appUserRepository.saveAll(entitiesToSave);

                // Update results with created userManagement IDs
                int savedIndex = 0;
                for (UserResultDTO result : results) {
                    if ("PENDING_CREATION".equals(result.getStatus())) {
                        UserEntity savedEntity = savedEntities.get(savedIndex);
                        result.setUserId(savedEntity.getUserId().toString());
                        result.setRoleName(savedEntity.getRole().getRoleName());
                        result.setStatus("CREATED");
                        result.setMessage("User created successfully");
                        savedIndex++;
                    }
                }

            } catch (DataIntegrityViolationException ex) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("responseCode", 409);
                errorResult.put("message", "One or more users already exist");
                errorResult.put("data", results);

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", Data integrity violation: " + ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResult);
            }
        }

        // Prepare response
        BulkUserResponseDTO response = BulkUserResponseDTO.builder()
                .data(results)
                .build();

        // Set appropriate response code and message
        if (createdCount > 0 && duplicateCount == 0 && roleNotFoundCount.get() == 0) {
            response.setResponseCode(200);
            response.setMessage("All users created successfully");
        } else if (createdCount == 0 && duplicateCount > 0 && roleNotFoundCount.get() == 0) {
            response.setResponseCode(409);
            response.setMessage("All users already exist");
        } else if (roleNotFoundCount.get() == dtos.size()) {
            response.setResponseCode(404);
            response.setMessage("All specified roles not found");
        } else if (createdCount > 0 || duplicateCount > 0 || roleNotFoundCount.get() > 0) {
            response.setResponseCode(207); // Multi-Status
            response.setMessage(String.format(
                    "Bulk creation completed: %d created, %d duplicates, %d roles not found",
                    createdCount, duplicateCount, roleNotFoundCount.get()
            ));
        } else {
            response.setResponseCode(400);
            response.setMessage("No users were processed");
        }

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Bulk userManagement creation completed: " + createdCount + " created, " +
                        duplicateCount + " duplicates, " + roleNotFoundCount.get() + " roles not found");

        // Convert to standard response format
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("responseCode", response.getResponseCode());
        finalResult.put("message", response.getMessage());
        finalResult.put("data", response.getData());
        finalResult.put("success_count", createdCount);
        finalResult.put("duplicate_count", duplicateCount);
        finalResult.put("role_not_found_count", roleNotFoundCount.get());
        finalResult.put("request_id", requestId);

        HttpStatus status = determineHttpStatus(response.getResponseCode());
        return ResponseEntity.status(status).body(finalResult);
    }

    private HttpStatus determineHttpStatus(int responseCode) {
        switch (responseCode) {
            case 200: return HttpStatus.OK;
            case 207: return HttpStatus.MULTI_STATUS;
            case 409: return HttpStatus.CONFLICT;
            case 404: return HttpStatus.NOT_FOUND;
            case 400: return HttpStatus.BAD_REQUEST;
            case 422: return HttpStatus.UNPROCESSABLE_ENTITY;
            default: return HttpStatus.BAD_REQUEST;
        }
    }

    // ========== GET USER ==========
    @Transactional
    public ResponseEntity<?> getUser(String userId, String requestId, HttpServletRequest req, String performedBy) {
        Map<String, Object> result = new HashMap<>();

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Getting userManagement with ID: " + userId +
                        ", Requested by: " + safePerformedBy);

        try {
            UserEntity entity = appUserRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("User with ID %s not found", userId)
                    ));

            UserDTO dto = UserDTO.builder()
                    .userId(entity.getUserId())
                    .username(entity.getUsername())
                    .phoneNumber(entity.getPhoneNumber())
                    .emailAddress(entity.getEmailAddress())
                    .staffId(entity.getStaffId())           // ✅ ADD THIS
                    .fullName(entity.getFullName())
                    .roleId(entity.getRole().getRoleId())
                    .roleName(entity.getRole().getRoleName())
                    .isActive(entity.getIsActive())         // ✅ ADD THIS
                    .build();

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", User retrieved successfully: " + userId);

            result.put("responseCode", 200);
            result.put("message", "User retrieved successfully");
            result.put("data", dto);

            return ResponseEntity.ok(result);

        } catch (ResourceNotFoundException e) {
            result.put("responseCode", 404);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", User not found: " + userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);

        } catch (Exception e) {
            result.put("responseCode", 500);
            result.put("message", "Internal server error: " + e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error getting userManagement: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ========== GET ALL USERS ==========
    @Transactional
    public Map<String, Object> getAllUsers(Pageable pageable, String requestId, HttpServletRequest req, String performedBy) {
        // Optionally log/audit the retrieval
        // auditLogHelper.logAuditAction("RETRIEVE_ALL_USERS", performedBy, String.format("Retrieved all users - Page: %d, Size: %d", pageable.getPageNumber(), pageable.getPageSize()), requestId);

        // Get paginated users
        Page<UserEntity> usersPage = appUserRepository.findAll(pageable);

        // Map entities to DTOs - you already do this mapping elsewhere; adapt if needed
        List<UserDTO> users = usersPage.stream().map(entity -> UserDTO.builder()
                        .userId(entity.getUserId())
                        .username(entity.getUsername())
                        .fullName(entity.getFullName())
                        .emailAddress(entity.getEmailAddress()) // ✅ Make sure this is included
                        .phoneNumber(entity.getPhoneNumber())   // ✅ Make sure this is included
                        .staffId(entity.getStaffId())           // ✅ Make sure this is included
                        .createdDate(entity.getCreatedDate())
                        .failedLoginAttempts(entity.getFailedLoginAttempts())
                        .lastLogin(entity.getLastLogin())
                        .isActive(entity.getIsActive())
                        .accountLockedUntil(entity.getAccountLockedUntil())
                        .roleId(entity.getRole() != null ? entity.getRole().getRoleId() : null)
                        .roleName(entity.getRole() != null ? entity.getRole().getRoleName() : null)
                        .build())
                .collect(Collectors.toList());

        // Get unique filter values
        List<String> uniqueRoles = appUserRepository.findDistinctRoleNames();
        List<Boolean> uniqueStatuses = appUserRepository.findDistinctStatuses();
        List<String> uniqueUsernames = appUserRepository.findDistinctUsernames();

        // Build pagination map
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page_number", usersPage.getNumber());
        pagination.put("page_size", usersPage.getSize());
        pagination.put("total_elements", usersPage.getTotalElements());
        pagination.put("total_pages", usersPage.getTotalPages());
        pagination.put("is_first", usersPage.isFirst());
        pagination.put("is_last", usersPage.isLast());

        // Compose response
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("pagination", pagination);
        response.put("uniqueRoles", uniqueRoles);
        response.put("uniqueStatuses", uniqueStatuses);
        response.put("uniqueUsernames", uniqueUsernames);

        return response;
    }

    // ========== SEARCH USERS ==========
    @Transactional
    public ResponseEntity<?> searchUsers(
            String username,
            String fullName,
            UUID roleId,
            Pageable pageable,
            String requestId,
            HttpServletRequest req,
            String performedBy) {

        Map<String, Object> result = new HashMap<>();

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Searching users with filters - username: " +
                        username + ", fullName: " + fullName + ", roleId: " + roleId +
                        ", Requested by: " + safePerformedBy);

        try {
            // Validate sort fields
            sortValidationHelper.validateSortFieldsOrThrow(pageable.getSort(), VALID_SORT_FIELDS, "User");

            // Fix sorting field mappings
            Sort sort = pageable.getSort().stream()
                    .map(order -> {
                        String property = order.getProperty();
                        // Handle common field name mappings
                        if (property.contains("[")) {
                            property = property.replaceAll("[\\[\\]\"]", "");
                        }
                        if ("username".equalsIgnoreCase(property)) {
                            property = "username";
                        } else if ("fullName".equalsIgnoreCase(property)) {
                            property = "fullName";
                        } else if ("roleName".equalsIgnoreCase(property)) {
                            property = "role.roleName";
                        }
                        return new Sort.Order(order.getDirection(), property);
                    })
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));

            Pageable fixedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sort
            );

            // Create Specification for dynamic query
            Specification<UserEntity> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Filter by username
                if (StringUtils.hasText(username)) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("username")),
                            "%" + username.toLowerCase() + "%"));
                }

                // Filter by full name
                if (StringUtils.hasText(fullName)) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fullName")),
                            "%" + fullName.toLowerCase() + "%"));
                }

                // Filter by role ID
                if (roleId != null) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("role").get("roleId"),
                            roleId));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            Page<UserEntity> pageResult = appUserRepository.findAll(spec, fixedPageable);

            Page<UserDTO> page = pageResult.map(entity -> UserDTO.builder()
                    .userId(entity.getUserId())
                    .username(entity.getUsername())
                    .fullName(entity.getFullName())
                    .roleId(entity.getRole().getRoleId())
                    .roleName(entity.getRole().getRoleName())
                    .build());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId +
                            ", Search completed - Total: " + page.getTotalElements() +
                            ", Content size: " + page.getContent().size());

            if (page.isEmpty()) {
                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", No users found with the given filters");

                result.put("responseCode", 200);
                result.put("message", "No users found with the given filters");
                result.put("data", page);
                result.put("total_elements", page.getTotalElements());
                result.put("total_pages", page.getTotalPages());
                result.put("current_page", page.getNumber());

                return ResponseEntity.ok(result);
            }

            result.put("responseCode", 200);
            result.put("message", "Users retrieved successfully");
            result.put("data", page);
            result.put("total_elements", page.getTotalElements());
            result.put("total_pages", page.getTotalPages());
            result.put("current_page", page.getNumber());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            result.put("responseCode", 400);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Invalid search parameters: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);

        } catch (Exception e) {
            result.put("responseCode", 500);
            result.put("message", "Internal server error: " + e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error searching users: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ========== UPDATE USER ==========
    // ========== UPDATE USER ==========
    @Transactional
    public ResponseEntity<?> updateUser(String userId, UserDTO dto, String requestId, HttpServletRequest req, String performedBy) {
        Map<String, Object> result = new HashMap<>();

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Updating userManagement with ID: " + userId +
                        ", Requested by: " + safePerformedBy);

        try {
            // ✅ ADDED: Validate all fields including email
            validateUserUpdateBusinessRules(dto);

            UserEntity entity = appUserRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("User with ID %s not found", userId)
                    ));

            // Check if username conflicts with existing userManagement
            if (!entity.getUsername().equals(dto.getUsername())) {
                Optional<UserEntity> existingUser = appUserRepository.findByUsername(dto.getUsername());
                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                    result.put("responseCode", 409);
                    result.put("message", String.format("Username '%s' already exists", dto.getUsername()));

                    loggerUtil.log("api-automation",
                            "Request ID: " + requestId + ", Username conflict: " + result.get("message"));

                    return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
                }
            }

            // Check if email conflicts (if email is being updated)
            if (dto.getEmailAddress() != null && !dto.getEmailAddress().equals(entity.getEmailAddress())) {
                Optional<UserEntity> existingEmail = appUserRepository.findByEmailAddress(dto.getEmailAddress());
                if (existingEmail.isPresent() && !existingEmail.get().getUserId().equals(userId)) {
                    result.put("responseCode", 409);
                    result.put("message", String.format("Email '%s' already exists", dto.getEmailAddress()));

                    loggerUtil.log("api-automation",
                            "Request ID: " + requestId + ", Email conflict: " + result.get("message"));

                    return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
                }
            }

            // Check if role has changed
            if (!entity.getRole().getRoleId().equals(dto.getRoleId())) {
                UserRoleEntity newRole = roleRepository.findById(dto.getRoleId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Role with ID %s not found", dto.getRoleId())
                        ));
                entity.setRole(newRole);
                dto.setRoleName(newRole.getRoleName());
            } else {
                dto.setRoleName(entity.getRole().getRoleName());
            }

            // ✅ UPDATED: Update ALL fields
            entity.setUsername(dto.getUsername());
            entity.setFullName(dto.getFullName());
            entity.setEmailAddress(dto.getEmailAddress()); // ✅ ADDED
            entity.setPhoneNumber(dto.getPhoneNumber());   // ✅ ADDED
            entity.setStaffId(dto.getStaffId());           // ✅ ADDED
            entity.setIsActive(dto.getIsActive());         // ✅ ADDED

            // Update last modified date
            entity.setLastModifiedDate(LocalDateTime.now());

            appUserRepository.save(entity);
            dto.setUserId(entity.getUserId());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", User updated successfully: " + userId);

            result.put("responseCode", 200);
            result.put("message", "User updated successfully");
            result.put("data", dto);

            return ResponseEntity.ok(result);

        } catch (ResourceNotFoundException e) {
            result.put("responseCode", 404);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Resource not found: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);

        } catch (BusinessRuleException e) {
            result.put("responseCode", 422);
            result.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Business rule violation: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);

        } catch (Exception e) {
            result.put("responseCode", 500);
            result.put("message", "Internal server error: " + e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error updating userManagement: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ✅ ADDED: Separate validation for updates (allows null for optional fields)
    private void validateUserUpdateBusinessRules(UserDTO dto) {
        // Validate username
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BusinessRuleException("Username cannot be empty");
        }

        dto.setUsername(dto.getUsername().trim());

        if (dto.getUsername().length() > 50) {
            throw new BusinessRuleException("Username cannot exceed 50 characters");
        }

        // Validate username format (alphanumeric with underscores)
        if (!dto.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessRuleException(
                    "Username can only contain alphanumeric characters and underscores"
            );
        }

        // Validate full name
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new BusinessRuleException("Full name cannot be empty");
        }

        dto.setFullName(dto.getFullName().trim());

        if (dto.getFullName().length() > 100) {
            throw new BusinessRuleException("Full name cannot exceed 100 characters");
        }

        // Validate full name format (letters, spaces, hyphens, apostrophes)
        if (!dto.getFullName().matches("^[a-zA-Z\\s'-]+$")) {
            throw new BusinessRuleException(
                    "Full name can only contain letters, spaces, hyphens, and apostrophes"
            );
        }

        // Validate role ID
        if (dto.getRoleId() == null) {
            throw new BusinessRuleException("Role ID cannot be null");
        }

        // ✅ ADDED: Validate email if provided
        if (dto.getEmailAddress() != null && !dto.getEmailAddress().trim().isEmpty()) {
            String email = dto.getEmailAddress().trim();
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                throw new BusinessRuleException("Invalid email address format");
            }
            if (email.length() > 100) {
                throw new BusinessRuleException("Email cannot exceed 100 characters");
            }
        }

        // ✅ ADDED: Validate phone number if provided
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty()) {
            String phone = dto.getPhoneNumber().trim();
            // Basic phone validation - adjust as needed
            if (!phone.matches("^\\+?[0-9\\s-]{10,20}$")) {
                throw new BusinessRuleException("Invalid phone number format");
            }
        }

        // ✅ ADDED: Validate staff ID if provided
        if (dto.getStaffId() != null && dto.getStaffId().trim().length() > 50) {
            throw new BusinessRuleException("Staff ID cannot exceed 50 characters");
        }
    }




    // ========== DELETE USER ==========
    @Transactional
    public ResponseEntity<?> deleteUser(String userId, String requestId, HttpServletRequest req, String performedBy) {
        Map<String, Object> result = new HashMap<>();

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String safePerformedBy = performedBy != null ? performedBy : "UNKNOWN";

        loggerUtil.log("api-automation",
                "Request ID: " + requestId +
                        ", Deleting userManagement with ID: " + userId +
                        ", Requested by: " + safePerformedBy);

        try {
            if (!appUserRepository.existsById(userId)) {
                result.put("responseCode", 404);
                result.put("message", String.format("User with ID %s not found", userId));

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", User not found: " + userId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            // Optional: Check if userManagement is in use before deletion
            boolean isInUse = checkIfUserIsInUse(userId);
            if (isInUse) {
                result.put("responseCode", 409);
                result.put("message", "User is in use and cannot be deleted");

                loggerUtil.log("api-automation",
                        "Request ID: " + requestId + ", User is in use: " + userId);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }

            appUserRepository.deleteById(userId);

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", User deleted successfully: " + userId);

            result.put("responseCode", 200);
            result.put("message", "User deleted successfully");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("responseCode", 500);
            result.put("message", "Internal server error: " + e.getMessage());

            loggerUtil.log("api-automation",
                    "Request ID: " + requestId + ", Error deleting userManagement: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    private void validateUserBusinessRules(UserDTO dto) {
        // Validate username
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BusinessRuleException("Username cannot be empty");
        }

        dto.setUsername(dto.getUsername().trim());

        if (dto.getUsername().length() > 50) {
            throw new BusinessRuleException("Username cannot exceed 50 characters");
        }

        // Validate username format (alphanumeric with underscores)
        if (!dto.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessRuleException(
                    "Username can only contain alphanumeric characters and underscores"
            );
        }

        // Validate full name
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new BusinessRuleException("Full name cannot be empty");
        }

        dto.setFullName(dto.getFullName().trim());

        if (dto.getFullName().length() > 100) {
            throw new BusinessRuleException("Full name cannot exceed 100 characters");
        }

        // Validate full name format (letters, spaces, hyphens, apostrophes)
        if (!dto.getFullName().matches("^[a-zA-Z\\s'-]+$")) {
            throw new BusinessRuleException(
                    "Full name can only contain letters, spaces, hyphens, and apostrophes"
            );
        }

        // Validate role ID
        if (dto.getRoleId() == null) {
            throw new BusinessRuleException("Role ID cannot be null");
        }
    }

    // Helper method to check if userManagement is in use (example implementation)
    private boolean checkIfUserIsInUse(String userId) {
        // Implement logic to check if userManagement is referenced elsewhere
        // For example, check if userManagement has any associated records in other tables
        // This would depend on your business requirements
        // Return true if userManagement is in use, false otherwise

        // Example: Check if userManagement is currently logged in or has active sessions
        // You might want to check if userManagement has any created records in the systemActivities

        return false; // Placeholder - implement according to your requirements
    }
}