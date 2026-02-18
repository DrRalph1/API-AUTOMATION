package com.usg.apiAutomation.controllers.systemActivities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usg.apiAutomation.dtos.systemActivities.user.*;
import com.usg.apiAutomation.helpers.ApiKeyNSecretHelper;
import com.usg.apiAutomation.helpers.ClientIpHelper;
import com.usg.apiAutomation.helpers.JwtHelper;
import com.usg.apiAutomation.helpers.AuditLogHelper;
import com.usg.apiAutomation.services.systemActivities.UserService;
import com.usg.apiAutomation.services.systemActivities.SMSService;
import com.usg.apiAutomation.utils.JwtUtil;
import com.usg.apiAutomation.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/plx/api/users")
@RequiredArgsConstructor
@Tag(name = "USER MANAGEMENT", description = "Endpoints for managing application users")
public class UserController {

    private final UserService userService;
    private final LoggerUtil loggerUtil;
    private final ApiKeyNSecretHelper apiKeyNSecretHelper;
    private final ClientIpHelper clientIpHelper;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final JwtHelper jwtHelper;
    private final SMSService smsService;
    private final AuditLogHelper auditLogHelper;

    @Operation(
            summary = "User Login",
            description = "Authenticates a user and returns user details with tokens if successful"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation errors", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access forbidden or account deactivated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @Valid @RequestBody UserLoginRequestDTO userLoginRequestDTO,
            @RequestHeader(value = "x-api-key", defaultValue = "", required = true)
            @Parameter(name = "x-api-key", description = "API Key", required = true, in = ParameterIn.HEADER)
            String apiKey,
            @RequestHeader(value = "x-api-secret", defaultValue = "", required = true)
            @Parameter(name = "x-api-secret", description = "API Secret", required = true, in = ParameterIn.HEADER)
            String apiSecret,
            BindingResult bindingResult,
            HttpServletRequest req) {

        // Validate API Credentials
        boolean validAPICredentials = apiKeyNSecretHelper.validateApiCredentials(apiKey, apiSecret);
        boolean validClientIP = clientIpHelper.validateAPIClientIp(apiKey, apiSecret);

        if (!validAPICredentials) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Invalid API Key or Secret.");
            errorResponse.put("requestId", "");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        if (!validClientIP) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Unknown Client IP.");
            errorResponse.put("requestId", "");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        String requestId = UUID.randomUUID().toString();

        try {
            // Log the incoming requestEntity
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Login attempt for username: " +
                            userLoginRequestDTO.getUserId());
            auditLogHelper.logAuditAction("USER_LOGIN_REQUEST", null,
                    String.format("Login attempt for username: %s", userLoginRequestDTO.getUserId()), requestId);

            // Validate requestEntity body
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                loggerUtil.log("api-automation",
                        "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
                auditLogHelper.logAuditAction("USER_LOGIN_VALIDATION_FAILED", null,
                        String.format("Validation errors for login of username %s: %s", userLoginRequestDTO.getUserId(), validationErrors),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors");
                errorResponse.put("errors", validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Call the service to authenticate the user with all required parameters
            ResponseEntity<?> responseEntity = userService.userLogin(
                    userLoginRequestDTO,
                    requestId,
                    req,
                    null // performedBy is null for user self-login
            );

            // Ensure responseEntity and response body are not null
            if (responseEntity != null && responseEntity.getBody() != null) {
                String jsonResponse = objectMapper.writeValueAsString(responseEntity.getBody());
                loggerUtil.log("api-automation",
                        "RequestEntity ID: " + requestId + ", Login response: " + jsonResponse);
                auditLogHelper.logAuditAction("USER_LOGIN_RESPONSE", null,
                        String.format("Login response for username %s: %s", userLoginRequestDTO.getUserId(), jsonResponse),
                        requestId);
                return responseEntity;
            } else {
                loggerUtil.log("api-automation",
                        "RequestEntity ID: " + requestId + ", Empty response from user service for login requestEntity");
                auditLogHelper.logAuditAction("USER_LOGIN_EMPTY_RESPONSE", null,
                        String.format("Empty response from authentication service for username %s", userLoginRequestDTO.getUserId()),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 500);
                errorResponse.put("message", "Empty response from authentication service.");
                errorResponse.put("requestId", requestId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

        } catch (JsonProcessingException e) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", JSON processing error: " + e.getMessage());
            auditLogHelper.logAuditAction("USER_LOGIN_JSON_ERROR", null,
                    String.format("JSON processing error during login for username %s: %s", userLoginRequestDTO.getUserId(), e.getMessage()),
                    requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "Error processing response data.");
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Login error: " + e.getMessage());
            auditLogHelper.logAuditAction("USER_LOGIN_ERROR", null,
                    String.format("Error during login for username %s: %s", userLoginRequestDTO.getUserId(), e.getMessage()),
                    requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred during login: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping
    @Operation(summary = "Create a new user", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO dto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "creating a user");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for creating user: " + dto.getUsername());
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        // Validate requestEntity body
        if (bindingResult.hasErrors()) {
            String validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
            auditLogHelper.logAuditAction("CREATE_USER_VALIDATION_FAILED", performedBy,
                    String.format("Validation errors creating user %s: %s", dto.getUsername(), validationErrors), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 400);
            errorResponse.put("message", "Validation errors");
            errorResponse.put("errors", validationErrors);
            errorResponse.put("requestId", requestId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Creating user: " + dto.getUsername() +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("CREATE_USER_REQUEST", performedBy,
                String.format("Creating user: %s", dto.getUsername()), requestId);

        // Call service method and return the response directly
        ResponseEntity<?> response = userService.createUser(dto, requestId, req, performedBy);

        // Log the result
        if (response.getBody() instanceof Map) {
            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", User creation completed. Response code: " +
                            responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
            auditLogHelper.logAuditAction("CREATE_USER_COMPLETED", performedBy,
                    String.format("User creation completed for %s. Response code: %s, Message: %s",
                            dto.getUsername(), responseBody.get("responseCode"), responseBody.get("message")), requestId);
        }

        return response;
    }



    @PostMapping("/reset-default-password")
    @Operation(
            summary = "Reset Default Password",
            description = "Allows users to reset their default password. Requires current password verification. Requires JWT authentication.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - account issues"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Password requirements not met"),
            @ApiResponse(responseCode = "423", description = "Account locked"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> resetDefaultPassword(
            @Valid @RequestBody ResetPasswordRequestDTO resetRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "resetting default password");
        if (authValidation != null) {
            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Authorization failed for resetting default password");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        // Validate requestEntity body
        if (bindingResult.hasErrors()) {
            String validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
            auditLogHelper.logAuditAction("RESET_DEFAULT_PASSWORD_VALIDATION_FAILED", performedBy,
                    String.format("Validation errors resetting default password for user %s: %s", resetRequest.getUserId(), validationErrors),
                    requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 400);
            errorResponse.put("message", "Validation errors");
            errorResponse.put("errors", validationErrors);
            errorResponse.put("requestId", requestId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        loggerUtil.log("web-application-firewall",
                "RequestEntity ID: " + requestId +
                        ", Resetting default password for user: " + resetRequest.getUserId() +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("RESET_DEFAULT_PASSWORD_REQUEST", performedBy,
                String.format("Resetting default password for user: %s", resetRequest.getUserId()), requestId);

        // Call service method and return the response directly
        ResponseEntity<?> response = userService.resetDefaultPassword(resetRequest, requestId, req, performedBy);

        // Log the result
        if (response.getBody() instanceof Map) {
            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Default password reset completed. Response code: " +
                            responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
            auditLogHelper.logAuditAction("RESET_DEFAULT_PASSWORD_COMPLETED", performedBy,
                    String.format("Default password reset completed for user %s. Response code: %s, Message: %s",
                            resetRequest.getUserId(), responseBody.get("responseCode"), responseBody.get("message")),
                    requestId);
        }

        return response;
    }



    @PutMapping("/forgot-password")
    @Operation(
            summary = "Forgot Password",
            description = "Generates a new password for the user and sends it via SMS. Email notification is optional."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation errors", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access forbidden. Invalid API Key, Secret, or IP.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody UserForgotPasswordRequestDTO forgotPasswordRequestDTO,
            BindingResult bindingResult,
            @RequestHeader(value = "x-api-key", defaultValue = "", required = true)
            @Parameter(name = "x-api-key", description = "API Key", required = true, in = ParameterIn.HEADER)
            String apiKey,
            @RequestHeader(value = "x-api-secret", defaultValue = "", required = true)
            @Parameter(name = "x-api-secret", description = "API Secret", required = true, in = ParameterIn.HEADER)
            String apiSecret,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate API credentials
        boolean validAPICredentials = apiKeyNSecretHelper.validateApiCredentials(apiKey, apiSecret);
        boolean validClientIP = clientIpHelper.validateAPIClientIp(apiKey, apiSecret);

        if (!validAPICredentials) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Invalid API Key or Secret.");
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        if (!validClientIP) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Unknown Client IP.");
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        try {
            // Log incoming requestEntity
            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Forgot password attempt for user: " +
                            forgotPasswordRequestDTO.getUserId());
            auditLogHelper.logAuditAction("FORGOT_PASSWORD_REQUEST", apiKey,
                    String.format("Forgot password attempt for user: %s by API client: %s", forgotPasswordRequestDTO.getUserId(), apiKey),
                    requestId);

            // Validate requestEntity body
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                loggerUtil.log("web-application-firewall",
                        "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
                auditLogHelper.logAuditAction("FORGOT_PASSWORD_VALIDATION_FAILED", apiKey,
                        String.format("Validation errors for forgot password of user %s: %s", forgotPasswordRequestDTO.getUserId(), validationErrors),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors");
                errorResponse.put("errors", validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Call service method
            ResponseEntity<?> responseEntity = userService.forgotPassword(
                    forgotPasswordRequestDTO,
                    requestId,
                    req
            );

            // Handle empty or null response
            if (responseEntity == null || responseEntity.getBody() == null) {
                loggerUtil.log("web-application-firewall",
                        "RequestEntity ID: " + requestId + ", Empty response from forgot password service");
                auditLogHelper.logAuditAction("FORGOT_PASSWORD_EMPTY_RESPONSE", apiKey,
                        String.format("Empty response from forgot password service for user %s", forgotPasswordRequestDTO.getUserId()),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 500);
                errorResponse.put("message", "Empty response from forgot password service.");
                errorResponse.put("requestId", requestId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Log response
            if (responseEntity.getBody() instanceof Map) {
                Map<?, ?> responseBody = (Map<?, ?>) responseEntity.getBody();
                loggerUtil.log("web-application-firewall",
                        "RequestEntity ID: " + requestId + ", Forgot password completed. Response code: " +
                                responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
                auditLogHelper.logAuditAction("FORGOT_PASSWORD_COMPLETED", apiKey,
                        String.format("Forgot password completed for user %s. Response code: %s, Message: %s",
                                forgotPasswordRequestDTO.getUserId(), responseBody.get("responseCode"), responseBody.get("message")),
                        requestId);
            }

            return responseEntity;

        } catch (Exception e) {
            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Error during forgot password: " + e.getMessage());
            auditLogHelper.logAuditAction("FORGOT_PASSWORD_ERROR", apiKey,
                    String.format("Error during forgot password for user %s: %s", forgotPasswordRequestDTO.getUserId(), e.getMessage()),
                    requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while processing forgot password: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    @PutMapping("/password-reset")
    @Operation(
            summary = "User Password Reset",
            description = "Resets the user password and sends a confirmation email if the reset is successful"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation errors", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access forbidden. Invalid API Key, Secret, or IP.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Password requirements not met", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody UserPasswordResetRequestDTO userPasswordResetRequestDTO,
            BindingResult bindingResult,
            @RequestHeader(value = "x-api-key", defaultValue = "", required = true)
            @Parameter(name = "x-api-key", description = "API Key", required = true, in = ParameterIn.HEADER)
            String apiKey,
            @RequestHeader(value = "x-api-secret", defaultValue = "", required = true)
            @Parameter(name = "x-api-secret", description = "API Secret", required = true, in = ParameterIn.HEADER)
            String apiSecret,
            HttpServletRequest req) {

        // Validate API credentials
        boolean validAPICredentials = apiKeyNSecretHelper.validateApiCredentials(apiKey, apiSecret);
        boolean validClientIP = clientIpHelper.validateAPIClientIp(apiKey, apiSecret);

        if (!validAPICredentials) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Invalid API Key or Secret.");
            errorResponse.put("requestId", "");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        if (!validClientIP) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Unknown Client IP.");
            errorResponse.put("requestId", "");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        String requestId = UUID.randomUUID().toString();

        try {
            // Log incoming requestEntity
            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Password reset attempt for user: " +
                            userPasswordResetRequestDTO.getUserId());
            auditLogHelper.logAuditAction("PASSWORD_RESET_REQUEST", apiKey,
                    String.format("Password reset attempt for user: %s by API client: %s", userPasswordResetRequestDTO.getUserId(), apiKey),
                    requestId);

            // Validate requestEntity body
            if (bindingResult.hasErrors()) {
                String validationErrors = bindingResult.getAllErrors().stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                loggerUtil.log("web-application-firewall",
                        "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
                auditLogHelper.logAuditAction("PASSWORD_RESET_VALIDATION_FAILED", apiKey,
                        String.format("Validation errors resetting password for user %s: %s", userPasswordResetRequestDTO.getUserId(), validationErrors),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Validation errors");
                errorResponse.put("errors", validationErrors);
                errorResponse.put("requestId", requestId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Call service method
            ResponseEntity<?> responseEntity = userService.resetPassword(
                    userPasswordResetRequestDTO,
                    requestId,
                    req,
                    null // performedBy can be null for API-key driven requestEntity
            );

            // Handle empty or null response
            if (responseEntity == null || responseEntity.getBody() == null) {
                loggerUtil.log("web-application-firewall",
                        "RequestEntity ID: " + requestId + ", Empty response from password reset service");
                auditLogHelper.logAuditAction("PASSWORD_RESET_EMPTY_RESPONSE", apiKey,
                        String.format("Empty response from password reset service for user %s", userPasswordResetRequestDTO.getUserId()),
                        requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 500);
                errorResponse.put("message", "Empty response from password reset service.");
                errorResponse.put("requestId", requestId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Log response
            if (responseEntity.getBody() instanceof Map) {
                Map<?, ?> responseBody = (Map<?, ?>) responseEntity.getBody();
                loggerUtil.log("web-application-firewall",
                        "RequestEntity ID: " + requestId + ", Password reset completed. Response code: " +
                                responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
                auditLogHelper.logAuditAction("PASSWORD_RESET_COMPLETED", apiKey,
                        String.format("Password reset completed for user %s. Response code: %s, Message: %s",
                                userPasswordResetRequestDTO.getUserId(), responseBody.get("responseCode"), responseBody.get("message")),
                        requestId);
            }

            return responseEntity;

        } catch (Exception e) {
            loggerUtil.log("web-application-firewall",
                    "RequestEntity ID: " + requestId + ", Error resetting password: " + e.getMessage());
            auditLogHelper.logAuditAction("PASSWORD_RESET_ERROR", apiKey,
                    String.format("Error resetting password for user %s: %s", userPasswordResetRequestDTO.getUserId(), e.getMessage()),
                    requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while resetting password: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    @PostMapping("/bulk")
    @Operation(summary = "Create multiple users in bulk", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users created successfully"),
            @ApiResponse(responseCode = "207", description = "Partial success (some users created, some duplicates)"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "All users already exist"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<?> createUsersBulk(
            @Valid @RequestBody List<UserDTO> dtos,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "bulk creating users");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for bulk create");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        // Validate requestEntity body
        if (bindingResult.hasErrors()) {
            String validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
            auditLogHelper.logAuditAction("BULK_CREATE_USERS_VALIDATION_FAILED", performedBy,
                    String.format("Validation errors for bulk create users: %s", validationErrors), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 400);
            errorResponse.put("message", "Validation errors");
            errorResponse.put("errors", validationErrors);
            errorResponse.put("requestId", requestId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Bulk creating " + dtos.size() + " users" +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("BULK_CREATE_USERS_REQUEST", performedBy,
                String.format("Bulk creating %d users", dtos.size()), requestId);

        // Call service method and return the response directly
        ResponseEntity<?> response = userService.createUsersBulk(dtos, requestId, req, performedBy);

        // Log the result
        if (response.getBody() instanceof Map) {
            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Bulk create completed. Response code: " +
                            responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
            auditLogHelper.logAuditAction("BULK_CREATE_USERS_COMPLETED", performedBy,
                    String.format("Bulk create completed. Response code: %s, Message: %s",
                            responseBody.get("responseCode"), responseBody.get("message")), requestId);
        }

        return response;
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get a single user by ID",
            description = "Retrieves a single user by their ID using API Key and Secret authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access forbidden. Invalid API Key, Secret, or IP.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> getUser(
            @PathVariable String userId,
            @RequestHeader(value = "x-api-key", defaultValue = "", required = true)
            @Parameter(name = "x-api-key", description = "API Key", required = true, in = ParameterIn.HEADER)
            String apiKey,
            @RequestHeader(value = "x-api-secret", defaultValue = "", required = true)
            @Parameter(name = "x-api-secret", description = "API Secret", required = true, in = ParameterIn.HEADER)
            String apiSecret,
            HttpServletRequest req) {

        // Validate API credentials
        boolean validAPICredentials = apiKeyNSecretHelper.validateApiCredentials(apiKey, apiSecret);
        boolean validClientIP = clientIpHelper.validateAPIClientIp(apiKey, apiSecret);

        if (!validAPICredentials) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Invalid API Key or Secret.");
            errorResponse.put("requestId", "");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        if (!validClientIP) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 403);
            errorResponse.put("message", "Access Forbidden. Unknown Client IP.");
            errorResponse.put("requestId", "");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        String requestId = UUID.randomUUID().toString();

        try {
            // Log incoming requestEntity
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Retrieving user with ID: " + userId +
                            ", Requested by API client with key: " + apiKey);
            auditLogHelper.logAuditAction("RETRIEVE_USER_REQUEST", apiKey,
                    String.format("Retrieving user with ID: %s by API client: %s", userId, apiKey),
                    requestId);

            // Call service method
            ResponseEntity<?> responseEntity = userService.getUser(userId, requestId, req, apiKey);

            // Handle empty or null response
            if (responseEntity == null || responseEntity.getBody() == null) {
                loggerUtil.log("api-automation",
                        "RequestEntity ID: " + requestId + ", Empty response from getUser service");
                auditLogHelper.logAuditAction("RETRIEVE_USER_EMPTY_RESPONSE", apiKey,
                        String.format("Empty response from getUser service for user %s", userId), requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 500);
                errorResponse.put("message", "Empty response from user retrieval service.");
                errorResponse.put("requestId", requestId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Log response
            if (responseEntity.getBody() instanceof Map) {
                Map<?, ?> responseBody = (Map<?, ?>) responseEntity.getBody();
                loggerUtil.log("api-automation",
                        "RequestEntity ID: " + requestId + ", User retrieval completed. Response code: " +
                                responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
                auditLogHelper.logAuditAction("RETRIEVE_USER_COMPLETED", apiKey,
                        String.format("User retrieval completed for %s. Response code: %s, Message: %s",
                                userId, responseBody.get("responseCode"), responseBody.get("message")), requestId);
            }

            return responseEntity;

        } catch (Exception e) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Error retrieving user: " + e.getMessage());
            auditLogHelper.logAuditAction("RETRIEVE_USER_ERROR", apiKey,
                    String.format("Error retrieving user %s: %s", userId, e.getMessage()), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while retrieving user: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    @Operation(
            summary = "Send SMS",
            description = "Sends an SMS using configured gateway. Requires JWT authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SMS sent successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid requestEntity data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authorization required", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to send SMS", content = @Content)
    })
    @PostMapping("/send-sms")
    public ResponseEntity<?> sendSms(
            @Valid @RequestBody SMSRequestDTO smsRequest,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "sending SMS");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for sending SMS");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        // Validate requestEntity body
        if (bindingResult.hasErrors()) {
            String validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Validation errors for SMS requestEntity: " + validationErrors);
            auditLogHelper.logAuditAction("SEND_SMS_VALIDATION_FAILED", performedBy,
                    String.format("Validation errors for SMS requestEntity to %s: %s", smsRequest.getTo(), validationErrors),
                    requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 400);
            errorResponse.put("message", "Validation errors");
            errorResponse.put("errors", validationErrors);
            errorResponse.put("requestId", requestId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Sending SMS to: " + smsRequest.getTo() +
                        ", Message length: " + (smsRequest.getMessage() != null ? smsRequest.getMessage().length() : 0) +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("SEND_SMS_REQUEST", performedBy,
                String.format("Sending SMS to: %s, Message length: %d", smsRequest.getTo(), (smsRequest.getMessage() != null ? smsRequest.getMessage().length() : 0)),
                requestId);

        // Call service method and return the response directly
        smsService.sendSms(smsRequest.getTo(), smsRequest.getMessage());

        // Log success
        loggerUtil.log("web-application-firewall", "SMS sent successfully to: " + smsRequest.getTo());
        auditLogHelper.logAuditAction("SEND_SMS_SUCCESS", performedBy,
                String.format("SMS sent successfully to: %s", smsRequest.getTo()), requestId);

        // Return success response
        return ResponseEntity.ok(Map.of(
                "responseCode", 200,
                "message", "SMS sent successfully",
                "requestId", requestId
        ));
    }



    @GetMapping("/")
    @Operation(summary = "Get all users (paginated + sortable + unique filters)", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No users found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "getting all users");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for getting all users");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Getting all users with pagination - Page: " +
                        pageable.getPageNumber() + ", Size: " + pageable.getPageSize() + ", Sort: " + pageable.getSort() +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("GET_ALL_USERS", performedBy,
                String.format("Getting all users - Page: %d, Size: %d, Sort: %s",
                        pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()),
                requestId);

        try {
            // Call service method - now returns Map with unique filter values
            Map<String, Object> result = userService.getAllUsers(pageable, requestId, req, performedBy);

            // Extract data from result map
            @SuppressWarnings("unchecked")
            List<UserDTO> users = (List<UserDTO>) result.get("users");
            @SuppressWarnings("unchecked")
            Map<String, Object> pagination = (Map<String, Object>) result.get("pagination");
            @SuppressWarnings("unchecked")
            List<String> uniqueRoles = (List<String>) result.get("uniqueRoles");
            @SuppressWarnings("unchecked")
            List<String> uniqueStatuses = (List<String>) result.get("uniqueStatuses");
            @SuppressWarnings("unchecked")
            List<String> uniqueUsernames = (List<String>) result.get("uniqueUsernames");

            // Handle no content
            if (users == null || users.isEmpty()) {
                loggerUtil.log("api-automation",
                        "RequestEntity ID: " + requestId + ", No users found in database");

                Map<String, Object> noContentResponse = new HashMap<>();
                noContentResponse.put("responseCode", 204);
                noContentResponse.put("message", "No users found");
                noContentResponse.put("uniqueRoles", uniqueRoles);
                noContentResponse.put("uniqueStatuses", uniqueStatuses);
                noContentResponse.put("uniqueUsernames", uniqueUsernames);
                noContentResponse.put("requestId", requestId);
                return ResponseEntity.ok(noContentResponse);
            }

            // Success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("responseCode", 200);
            successResponse.put("message", "Users retrieved successfully");
            successResponse.put("data", users);
            successResponse.put("pagination", pagination);
            successResponse.put("uniqueRoles", uniqueRoles);
            successResponse.put("uniqueUsernames", uniqueUsernames);
            successResponse.put("requestId", requestId);

            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Get all users completed. Total elements: " +
                            pagination.get("total_elements") + ", Total pages: " + pagination.get("total_pages") +
                            ", Unique roles: " + (uniqueRoles != null ? uniqueRoles.size() : 0) +
                            ", Unique usernames: " + (uniqueUsernames != null ? uniqueUsernames.size() : 0));

            auditLogHelper.logAuditAction("GET_ALL_USERS_COMPLETED", performedBy,
                    String.format("Get all users completed. Response code: %s, Total elements: %s",
                            200, pagination.get("total_elements")), requestId);

            return ResponseEntity.ok(successResponse);

        } catch (IllegalArgumentException e) {
            Map<String, Object> badResponse = new HashMap<>();
            badResponse.put("responseCode", 400);
            badResponse.put("message", e.getMessage());

            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Invalid pagination parameters: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(badResponse);

        } catch (Exception e) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Error getting all users: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 500);
            errorResponse.put("message", "An error occurred while getting users: " + e.getMessage());
            errorResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search users with filters (username, fullName, role)", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No users match the filters"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "401", description = "Authorization required")
    })
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String roleId,
            @PageableDefault(size = 10, sort = "username", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "searching users");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for searching users");
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Searching users with filters - username: " +
                        username + ", fullName: " + fullName + ", roleId: " + roleId +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("SEARCH_USERS", performedBy,
                String.format("Searching users with filters - username: %s, fullName: %s, roleId: %s, page: %d, size: %d",
                        username, fullName, roleId, pageable.getPageNumber(), pageable.getPageSize()),
                requestId);

        // Parse roleId to UUID if provided
        java.util.UUID parsedRoleId = null;
        if (roleId != null && !roleId.isEmpty()) {
            try {
                parsedRoleId = java.util.UUID.fromString(roleId);
            } catch (IllegalArgumentException e) {
                auditLogHelper.logAuditAction("SEARCH_USERS_INVALID_ROLE_ID", performedBy,
                        String.format("Invalid roleId format: %s", roleId), requestId);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("responseCode", 400);
                errorResponse.put("message", "Invalid roleId format. Must be a valid UUID.");
                errorResponse.put("requestId", requestId);
                errorResponse.put("data", Map.of("invalidRoleId", roleId));
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        // Call service method and return the response directly
        ResponseEntity<?> response = userService.searchUsers(
                username, fullName, parsedRoleId, pageable, requestId, req, performedBy);

        // Log the result
        if (response.getBody() instanceof Map) {
            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Search completed. Response code: " +
                            responseBody.get("responseCode") + ", Total elements: " +
                            responseBody.get("total_elements"));
            auditLogHelper.logAuditAction("SEARCH_USERS_COMPLETED", performedBy,
                    String.format("Search users completed. Response code: %s, Total elements: %s",
                            responseBody.get("responseCode"), responseBody.get("total_elements")), requestId);
        }

        return response;
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update a user by ID", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "User not found or Role not found"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserDTO dto,
            BindingResult bindingResult,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "updating a user");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for updating user: " + userId);
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        // Validate requestEntity body
        if (bindingResult.hasErrors()) {
            String validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Validation errors: " + validationErrors);
            auditLogHelper.logAuditAction("UPDATE_USER_VALIDATION_FAILED", performedBy,
                    String.format("Validation errors updating user %s: %s", userId, validationErrors), requestId);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", 400);
            errorResponse.put("message", "Validation errors");
            errorResponse.put("errors", validationErrors);
            errorResponse.put("requestId", requestId);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Updating user with ID: " + userId +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("UPDATE_USER_REQUEST", performedBy,
                String.format("Updating user with ID: %s", userId), requestId);

        // Call service method and return the response directly
        ResponseEntity<?> response = userService.updateUser(userId, dto, requestId, req, performedBy);

        // Log the result
        if (response.getBody() instanceof Map) {
            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", User update completed. Response code: " +
                            responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
            auditLogHelper.logAuditAction("UPDATE_USER_COMPLETED", performedBy,
                    String.format("User update completed for %s. Response code: %s, Message: %s",
                            userId, responseBody.get("responseCode"), responseBody.get("message")), requestId);
        }

        return response;
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user by ID", parameters = {
            @Parameter(name = "Authorization", description = "JWT Token in format: Bearer {token}", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @ApiResponse(responseCode = "401", description = "Authorization required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User is in use and cannot be deleted")
    })
    public ResponseEntity<?> deleteUser(
            @PathVariable String userId,
            HttpServletRequest req) {

        String requestId = UUID.randomUUID().toString();

        // Validate Authorization header
        ResponseEntity<?> authValidation = jwtHelper.validateAuthorizationHeader(req, "deleting a user");
        if (authValidation != null) {
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", Authorization failed for deleting user: " + userId);
            return authValidation;
        }

        // Extract performedBy from token
        String token = jwtHelper.extractTokenFromHeader(req);
        String performedBy = jwtUtil.extractUserId(token);

        loggerUtil.log("api-automation",
                "RequestEntity ID: " + requestId +
                        ", Deleting user with ID: " + userId +
                        ", Requested by: " + performedBy);
        auditLogHelper.logAuditAction("DELETE_USER_REQUEST", performedBy,
                String.format("Deleting user with ID: %s", userId), requestId);

        // Call service method and return the response directly
        ResponseEntity<?> response = userService.deleteUser(userId, requestId, req, performedBy);

        // Log the result
        if (response.getBody() instanceof Map) {
            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
            loggerUtil.log("api-automation",
                    "RequestEntity ID: " + requestId + ", User deletion completed. Response code: " +
                            responseBody.get("responseCode") + ", Message: " + responseBody.get("message"));
            auditLogHelper.logAuditAction("DELETE_USER_COMPLETED", performedBy,
                    String.format("User deletion completed for %s. Response code: %s, Message: %s",
                            userId, responseBody.get("responseCode"), responseBody.get("message")), requestId);
        }

        return response;
    }
}