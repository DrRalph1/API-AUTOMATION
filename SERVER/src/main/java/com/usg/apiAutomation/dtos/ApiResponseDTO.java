package com.usg.apiAutomation.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"responseCode", "message", "timestamp", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
    private int responseCode;
    private String message;
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
    private T data;

    // OLD CONSTRUCTOR STYLE (for backward compatibility)
    public ApiResponseDTO(int responseCode, String message, T data) {
        this.responseCode = responseCode;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().toString();
    }

    // NEW STATIC FACTORY METHODS (for new controllers)
    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(200, "Success", data);
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(200, message, data);
    }

    public static <T> ApiResponseDTO<T> created(String message, T data) {
        return new ApiResponseDTO<>(201, message, data);
    }

    public static <T> ApiResponseDTO<T> noContent(String message) {
        return new ApiResponseDTO<>(204, message, null);
    }

    public static <T> ApiResponseDTO<T> error(int responseCode, String message) {
        return new ApiResponseDTO<>(responseCode, message, null);
    }

    public static <T> ApiResponseDTO<T> error(int responseCode, String message, T data) {
        return new ApiResponseDTO<>(responseCode, message, data);
    }

    // Convenience methods for common HTTP statuses
    public static <T> ApiResponseDTO<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> ApiResponseDTO<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> ApiResponseDTO<T> internalError(String message) {
        return error(500, message);
    }
}