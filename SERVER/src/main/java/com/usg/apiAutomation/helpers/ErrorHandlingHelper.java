package com.usg.apiAutomation.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ErrorHandlingHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoggerUtil loggerUtil;

    @Autowired
    public ErrorHandlingHelper(LoggerUtil loggerUtil) {
        this.loggerUtil = loggerUtil;
    }

    public int determineErrorCodeBasedOnMessage(String responseMessage) {
        if (responseMessage == null || responseMessage.isEmpty()) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        String lowerErrorMessage = responseMessage.toLowerCase();

        if (lowerErrorMessage.contains("connection timed out") || lowerErrorMessage.contains("read timed out"))
            return HttpStatus.GATEWAY_TIMEOUT.value();
        if (lowerErrorMessage.contains("i/o error") || lowerErrorMessage.contains("connection refused"))
            return HttpStatus.BAD_GATEWAY.value();
        if (lowerErrorMessage.contains("no route to host") || lowerErrorMessage.contains("service unavailable"))
            return HttpStatus.SERVICE_UNAVAILABLE.value();
        if (lowerErrorMessage.contains("unauthorized") || lowerErrorMessage.contains("access denied"))
            return HttpStatus.UNAUTHORIZED.value();
        if (lowerErrorMessage.contains("forbidden") || lowerErrorMessage.contains("access forbidden"))
            return HttpStatus.FORBIDDEN.value();
        if (lowerErrorMessage.contains("not found") || lowerErrorMessage.contains("resource not available"))
            return HttpStatus.NOT_FOUND.value();
        if (lowerErrorMessage.contains("payload too large") || lowerErrorMessage.contains("request entity too large"))
            return HttpStatus.PAYLOAD_TOO_LARGE.value();
        if (lowerErrorMessage.contains("unsupported media type"))
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        if (lowerErrorMessage.contains("too many requests"))
            return HttpStatus.TOO_MANY_REQUESTS.value();
        if (lowerErrorMessage.contains("bad request") || lowerErrorMessage.contains("invalid input"))
            return HttpStatus.BAD_REQUEST.value();
        if (lowerErrorMessage.contains("method not allowed"))
            return HttpStatus.METHOD_NOT_ALLOWED.value();
        if (lowerErrorMessage.contains("conflict"))
            return HttpStatus.CONFLICT.value();
        if (lowerErrorMessage.contains("precondition failed"))
            return HttpStatus.PRECONDITION_FAILED.value();
        if (lowerErrorMessage.contains("internal server error") || lowerErrorMessage.contains("unexpected error"))
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (lowerErrorMessage.contains("not implemented"))
            return HttpStatus.NOT_IMPLEMENTED.value();

        return 520; // Unknown error
    }

    public String determineErrorMessageBasedOnCode(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return (status != null) ? status.getReasonPhrase() : "Unknown Error";
    }

    public ResponseEntity<Object> buildErrorResponse(
            String requestId,
            UUID endpointId,
            int statusCode,
            Object externalResponse,
            String requestUrl,
            String microService,
            String sourceName,
            String methodName,
            HttpMethod httpMethod) {

        try {
            JsonNode responseBody;
            String responseMessage;

            if (externalResponse == null) {
                responseBody = createDefaultErrorResponse(requestId, endpointId, statusCode);
                responseMessage = determineErrorMessageBasedOnCode(statusCode);
            } else if (externalResponse instanceof String s) {
                try {
                    responseBody = objectMapper.readTree(s);
                    responseMessage = extractresponseMessage(responseBody);
                } catch (JsonProcessingException e) {
                    responseBody = createErrorResponseFromString(requestId, endpointId, statusCode, s);
                    responseMessage = s;
                }
            } else if (externalResponse instanceof byte[] bytes) {
                try {
                    responseBody = objectMapper.readTree(bytes);
                    responseMessage = extractresponseMessage(responseBody);
                } catch (Exception e) {
                    responseBody = createDefaultErrorResponse(requestId, endpointId, statusCode);
                    responseMessage = "Error processing binary response";
                }
            } else if (externalResponse instanceof Map<?, ?>) {
                responseBody = objectMapper.valueToTree(externalResponse);
                responseMessage = extractresponseMessage(responseBody);
            } else if (externalResponse instanceof Throwable throwable) {
                responseBody = createErrorResponseFromThrowable(requestId, endpointId, statusCode, throwable);
                responseMessage = throwable.getMessage();
            } else {
                try {
                    responseBody = objectMapper.valueToTree(externalResponse);
                    responseMessage = extractresponseMessage(responseBody);
                } catch (IllegalArgumentException e) {
                    responseBody = createDefaultErrorResponse(requestId, endpointId, statusCode);
                    responseMessage = "Error processing response";
                }
            }

            if (responseMessage == null || responseMessage.isEmpty()) {
                responseMessage = determineErrorMessageBasedOnCode(statusCode);
            }

            loggerUtil.log(microService,
                    String.format("Exception: %d %s on %s request for \"%s\": \"%s\"",
                            statusCode, HttpStatus.valueOf(statusCode).getReasonPhrase(), httpMethod, requestUrl, responseMessage));

            return ResponseEntity.status(statusCode).body(responseBody);
        } catch (Exception e) {
            loggerUtil.log("api-automation", e);
            JsonNode fallbackResponse = createDefaultErrorResponse(requestId, endpointId, HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.internalServerError().body(fallbackResponse);
        }
    }

    private JsonNode createDefaultErrorResponse(String requestId, UUID endpointId, int statusCode) {
        ObjectNode dataNode = objectMapper.createObjectNode()
                .put("requestId", requestId)
                .put("endpointId", String.valueOf(endpointId));

        return objectMapper.createObjectNode()
                .put("responseCode", statusCode)
                .put("message", determineErrorMessageBasedOnCode(statusCode))
                .set("data", dataNode);
    }

    private JsonNode createErrorResponseFromString(String requestId, UUID endpointId, int statusCode, String message) {
        ObjectNode dataNode = objectMapper.createObjectNode()
                .put("requestId", requestId)
                .put("endpointId", String.valueOf(endpointId));

        return objectMapper.createObjectNode()
                .put("responseCode", statusCode)
                .put("message", message)
                .set("data", dataNode);
    }

    private JsonNode createErrorResponseFromThrowable(String requestId, UUID endpointId, int statusCode, Throwable throwable) {
        ObjectNode dataNode = objectMapper.createObjectNode()
                .put("requestId", requestId)
                .put("endpointId", String.valueOf(endpointId));

        String message = throwable.getMessage() != null ?
                throwable.getMessage() : determineErrorMessageBasedOnCode(statusCode);

        return objectMapper.createObjectNode()
                .put("responseCode", statusCode)
                .put("message", message)
                .set("data", dataNode);
    }

    private String extractresponseMessage(JsonNode responseBody) {
        if (responseBody == null) return null;

        for (String key : new String[]{"message", "error", "responseMessage"}) {
            JsonNode node = responseBody.path(key);
            if (!node.isMissingNode()) return node.asText();
        }

        return responseBody.asText();
    }
}