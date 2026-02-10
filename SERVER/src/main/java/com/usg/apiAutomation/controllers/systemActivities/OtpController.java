package com.usg.apiAutomation.controllers.systemActivities;

import com.usg.apiAutomation.dtos.userManagement.OtpVerificationRequestDTO;
import com.usg.apiAutomation.dtos.userManagement.UserDTO;
import com.usg.apiAutomation.entities.UserEntity;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.helpers.AuditLogHelper;
import com.usg.apiAutomation.repositories.AppUserRepository;
import com.usg.apiAutomation.services.systemActivities.OtpService;
import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api")
@RequiredArgsConstructor
//@Tag(name = "APP USERS", description = "Endpoints for managing application users")
@Tag(name = "SYSTEM ACTIVITIES", description = "System-level endpoints")
public class OtpController {

    private final OtpService otpService;
    private final LoggerUtil loggerUtil;
    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final JwtHelper jwtHelper;
    private final AuditLogHelper auditLogHelper;

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Verify OTP",
            description = "Verifies the OTP code for userManagement authentication. Requires JWT authentication.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Account deactivated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody OtpVerificationRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "verifying OTP");
        if (authValidation != null) {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", Authorization failed for OTP verification - User: " + request.getUserId());
            auditLogHelper.logAuditAction("VERIFY_OTP_AUTH_FAILED", null,
                    String.format("Authorization failed for OTP verification for target userManagement %s", request.getUserId()), requestId);
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        try {
            // Log the incoming request
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", OTP verification requested - User: " + performedBy + ", Target User: " + request.getUserId());
            auditLogHelper.logAuditAction("VERIFY_OTP_REQUEST", performedBy,
                    String.format("OTP verification requested for target userManagement %s by %s", request.getUserId(), performedBy), requestId);

            // Validate request body
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                loggerUtil.log("otp-controller",
                        "Request ID: " + requestId + ", Validation errors: " + validationErrors);
                auditLogHelper.logAuditAction("VERIFY_OTP_VALIDATION_FAILED", performedBy,
                        String.format("Validation errors for OTP verification of userManagement %s: %s", request.getUserId(), validationErrors),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors");
                errorResponse.put("errors", validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId +
                            ", Verifying OTP for userManagement: " + request.getUserId() +
                            ", Requested by: " + performedBy);
            auditLogHelper.logAuditAction("VERIFY_OTP", performedBy,
                    String.format("Verifying OTP for target userManagement %s", request.getUserId()), requestId);

            // Verify OTP first
            boolean isValid = otpService.verifyOtp(request.getUserId(), request.getOtp());
            if (!isValid) {
                loggerUtil.log("otp-controller",
                        "Request ID: " + requestId + ", Invalid OTP - User: " + performedBy + ", Target User: " + request.getUserId());
                auditLogHelper.logAuditAction("VERIFY_OTP_FAILED", performedBy,
                        String.format("Invalid or expired OTP for target userManagement %s", request.getUserId()), requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 401);
                errorResponse.put("message", "Invalid or expired OTP");
                errorResponse.put("requestId", requestId);
                errorResponse.put("data", Map.of("userId", request.getUserId()));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Get userManagement details if OTP is valid
            Optional<UserDTO> userOptional = appUserRepository.getUserByUserId(request.getUserId());
            if (userOptional.isEmpty()) {
                loggerUtil.log("otp-controller",
                        "Request ID: " + requestId + ", User not found - User: " + performedBy + ", Target User: " + request.getUserId());
                auditLogHelper.logAuditAction("VERIFY_OTP_USER_NOT_FOUND", performedBy,
                        String.format("User not found for OTP verification target %s", request.getUserId()), requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 404);
                errorResponse.put("message", "User not found");
                errorResponse.put("requestId", requestId);
                errorResponse.put("data", Map.of("userId", request.getUserId()));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            UserDTO user = userOptional.get();

            // Check if account is active
            if (Boolean.FALSE.equals(user.getIsActive())) {
                loggerUtil.log("otp-controller",
                        "Request ID: " + requestId + ", Account deactivated - User: " + performedBy + ", Target User: " + request.getUserId());
                auditLogHelper.logAuditAction("VERIFY_OTP_ACCOUNT_DEACTIVATED", performedBy,
                        String.format("Account deactivated for target userManagement %s", request.getUserId()), requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 403);
                errorResponse.put("message", "Your account has been deactivated. Please contact support");
                errorResponse.put("requestId", requestId);
                errorResponse.put("data", Map.of("userId", request.getUserId()));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Prepare userManagement data response
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", user.getUserId());
            userData.put("staff_id", user.getStaffId());
            userData.put("username", user.getFullName());
            userData.put("email", user.getEmailAddress());
            userData.put("role_id", user.getRoleId());
            userData.put("role", user.getRoleName());
            userData.put("phone_number", user.getPhoneNumber());
            userData.put("last_login", user.getLastLogin());
            userData.put("is_active", user.getIsActive());
            userData.put("is_default_password", user.getIsDefaultPassword());

            // Update last login time
            try {
                appUserRepository.updateLastLogin(user.getUserId(), LocalDateTime.now(), LocalDateTime.now());
                loggerUtil.log("otp-controller",
                        "Request ID: " + requestId + ", Last login updated - User: " + performedBy + ", Target User: " + request.getUserId());
                auditLogHelper.logAuditAction("VERIFY_OTP_LAST_LOGIN_UPDATED", performedBy,
                        String.format("Last login updated for target userManagement %s", request.getUserId()), requestId);
            } catch (Exception e) {
                loggerUtil.log("otp-controller",
                        "Request ID: " + requestId + ", Failed to update last login - User: " + performedBy + ", Target User: " + request.getUserId() + ", Error: " + e.getMessage());
                auditLogHelper.logAuditAction("VERIFY_OTP_LAST_LOGIN_UPDATE_FAILED", performedBy,
                        String.format("Failed to update last login for target userManagement %s: %s", request.getUserId(), e.getMessage()), requestId);
            }

            // Create success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("responseCode", 200);
            successResponse.put("message", "OTP verified successfully");
            successResponse.put("data", userData);
            successResponse.put("requestId", requestId);

            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", OTP verified successfully - User: " + performedBy + ", Target User: " + request.getUserId());
            auditLogHelper.logAuditAction("VERIFY_OTP_SUCCESS", performedBy,
                    String.format("OTP verified successfully for target userManagement %s", request.getUserId()), requestId);

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", Error verifying OTP - User: " + performedBy + ", Target User: " + request.getUserId() + ", Error: " + e.getMessage());
            auditLogHelper.logAuditAction("VERIFY_OTP_ERROR", performedBy,
                    String.format("Error verifying OTP for target userManagement %s: %s", request.getUserId(), e.getMessage()), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while verifying OTP: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/send-otp")
    @Operation(
            summary = "Send OTP",
            description = "Sends OTP code to userManagement's registered phone number. Requires JWT authentication.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> sendOtp(
            @RequestParam(required = true) String userId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "sending OTP");
        if (authValidation != null) {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", Authorization failed for sending OTP - Target User: " + userId);
            auditLogHelper.logAuditAction("SEND_OTP_AUTH_FAILED", null,
                    String.format("Authorization failed for sending OTP to target userManagement %s", userId), requestId);
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        try {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", OTP send requested - User: " + performedBy + ", Target User: " + userId);
            auditLogHelper.logAuditAction("SEND_OTP_REQUEST", performedBy,
                    String.format("Send OTP requested for target userManagement %s by %s", userId, performedBy), requestId);

            // Validate userId parameter
            if (userId == null || userId.trim().isEmpty()) {
                auditLogHelper.logAuditAction("SEND_OTP_INVALID_PARAM", performedBy,
                        "User ID is required for send OTP", requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "User ID is required");
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId +
                            ", Sending OTP to userManagement: " + userId +
                            ", Requested by: " + performedBy);
            auditLogHelper.logAuditAction("SEND_OTP", performedBy,
                    String.format("Sending OTP to target userManagement %s", userId), requestId);

            // Fetch userManagement details from DB
            UserEntity user = appUserRepository.findByUserIdIgnoreCase(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate and send OTP
            otpService.generateAndSendOtp(userId, user.getFullName(), user.getEmailAddress(), user.getPhoneNumber());

            // Create success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("responseCode", 200);
            successResponse.put("message", "OTP sent to " + user.getPhoneNumber());
            successResponse.put("data", Map.of(
                    "userId", userId,
                    "phoneNumber", user.getPhoneNumber()
            ));
            successResponse.put("requestId", requestId);

            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", OTP sent successfully - User: " + performedBy + ", Target User: " + userId + ", Phone: " + user.getPhoneNumber());
            auditLogHelper.logAuditAction("SEND_OTP_SUCCESS", performedBy,
                    String.format("OTP sent successfully to target userManagement %s at phone %s", userId, user.getPhoneNumber()), requestId);

            return ResponseEntity.ok(successResponse);

        } catch (MessagingException e) {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", Failed to send OTP email - User: " + performedBy + ", Target User: " + userId + ", Error: " + e.getMessage());
            auditLogHelper.logAuditAction("SEND_OTP_EMAIL_FAILED", performedBy,
                    String.format("Failed to send OTP email for target userManagement %s: %s", userId, e.getMessage()), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "Failed to send OTP email");
            errorResponse.put("requestId", requestId);
            errorResponse.put("data", Map.of("userId", userId));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (RuntimeException e) {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", User not found - User: " + performedBy + ", Target User: " + userId);
            auditLogHelper.logAuditAction("SEND_OTP_USER_NOT_FOUND", performedBy,
                    String.format("User not found for send OTP target %s", userId), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 404);
            errorResponse.put("message", "User not found");
            errorResponse.put("requestId", requestId);
            errorResponse.put("data", Map.of("userId", userId));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            loggerUtil.log("otp-controller",
                    "Request ID: " + requestId + ", Error sending OTP - User: " + performedBy + ", Target User: " + userId + ", Error: " + e.getMessage());
            auditLogHelper.logAuditAction("SEND_OTP_ERROR", performedBy,
                    String.format("Error sending OTP to target userManagement %s: %s", userId, e.getMessage()), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while sending OTP: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}