package com.usg.apiAutomation.utils.apiEngine;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiParameterDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.GenerateApiRequestDTO;
import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GenUrlBuilderUtil {

    @Data
    @Builder
    public static class GenUrlInfo {
        private String endpointPath;
        private String fullUrl;
        private String urlPattern;
        private String exampleUrl;
        private String curlExample;
    }

    /**
     * Build complete Gen URL information - CENTRALIZED METHOD
     */
    public GenUrlInfo buildGenUrlInfo(GeneratedApiEntity api) {
        String baseUrl = getBaseUrl();
        String apiId = api.getId();

        // Build endpoint path with gen prefix and API ID
        StringBuilder endpointPath = new StringBuilder();
        endpointPath.append("/plx/api/gen/").append(apiId);

        // Add base path if present
        if (api.getBasePath() != null && !api.getBasePath().isEmpty()) {
            if (!api.getBasePath().startsWith("/")) {
                endpointPath.append("/");
            }
            endpointPath.append(api.getBasePath());
        }

        // Add endpoint path
        if (api.getEndpointPath() != null && !api.getEndpointPath().isEmpty()) {
            if (!api.getEndpointPath().startsWith("/")) {
                endpointPath.append("/");
            }
            endpointPath.append(api.getEndpointPath());
        }

        // Add path parameter placeholders
        if (api.getParameters() != null) {
            List<ApiParameterEntity> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .toList();

            for (ApiParameterEntity param : pathParams) {
                endpointPath.append("/{").append(param.getKey()).append("}");
            }
        }

        String endpointPathStr = endpointPath.toString();

        // Build full URL with base URL placeholder
        String fullUrl = "{{baseUrl}}" + endpointPathStr;

        // Build URL pattern with query parameter placeholders
        StringBuilder urlPattern = new StringBuilder(fullUrl);

        if (api.getParameters() != null) {
            List<ApiParameterEntity> queryParams = api.getParameters().stream()
                    .filter(p -> "query".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            if (!queryParams.isEmpty()) {
                urlPattern.append("?");
                for (int i = 0; i < queryParams.size(); i++) {
                    ApiParameterEntity param = queryParams.get(i);
                    if (i > 0) {
                        urlPattern.append("&");
                    }
                    urlPattern.append(param.getKey()).append("={").append(param.getKey()).append("}");
                }
            }
        }

        String urlPatternStr = urlPattern.toString();

        // Build example URL with sample values
        StringBuilder exampleUrl = new StringBuilder("https://api.example.com").append(endpointPathStr);

        // Replace path parameter placeholders with examples
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("path".equals(param.getParameterType())) {
                    String placeholder = "{" + param.getKey() + "}";
                    String example = param.getExample() != null && !param.getExample().isEmpty() ?
                            param.getExample() : param.getKey().toLowerCase() + "-value";
                    int startIndex = exampleUrl.indexOf(placeholder);
                    if (startIndex != -1) {
                        exampleUrl.replace(startIndex, startIndex + placeholder.length(), example);
                    }
                }
            }
        }

        // Add query parameters with examples
        if (api.getParameters() != null) {
            List<String> queryExamples = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    String example = param.getExample() != null && !param.getExample().isEmpty() ?
                            param.getExample() : param.getKey().toLowerCase() + "-value";
                    queryExamples.add(param.getKey() + "=" + example);
                }
            }

            if (!queryExamples.isEmpty()) {
                exampleUrl.append(exampleUrl.toString().contains("?") ? "&" : "?").append(String.join("&", queryExamples));
            }
        }

        String exampleUrlStr = exampleUrl.toString();

        // Build curl example
        String curlExample = buildCurlExample(api, exampleUrlStr);

        return GenUrlInfo.builder()
                .endpointPath(endpointPathStr)
                .fullUrl(fullUrl)
                .urlPattern(urlPatternStr)
                .exampleUrl(exampleUrlStr)
                .curlExample(curlExample)
                .build();
    }

    /**
     * Build curl example with the gen URL
     */
    private String buildCurlExample(GeneratedApiEntity api, String exampleUrl) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(api.getHttpMethod() != null ? api.getHttpMethod() : "GET").append(" \\\n");
        curl.append("  '").append(exampleUrl).append("'");

        // Add headers
        curl.append(" \\\n  -H 'Content-Type: application/json'");
        curl.append(" \\\n  -H 'Accept: application/json'");

        // Add header parameters
        if (api.getParameters() != null) {
            List<ApiParameterEntity> headerParams = api.getParameters().stream()
                    .filter(p -> "header".equals(p.getParameterType()))
                    .collect(Collectors.toList());

            for (ApiParameterEntity param : headerParams) {
                String example = param.getExample() != null && !param.getExample().isEmpty() ?
                        param.getExample() : param.getKey().toLowerCase() + "-value";
                curl.append(" \\\n  -H '").append(param.getKey()).append(": ").append(example).append("'");
            }
        }

        // Add auth headers if configured
        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            curl.append(" \\\n");
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    String header = api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key";
                    curl.append("  -H '").append(header).append(": your-api-key'");

                    if (api.getAuthConfig().getApiSecretHeader() != null) {
                        curl.append(" \\\n  -H '").append(api.getAuthConfig().getApiSecretHeader()).append(": your-api-secret'");
                    }
                    break;
                case "BEARER":
                case "JWT":
                    curl.append("  -H 'Authorization: Bearer your-jwt-token'");
                    break;
                case "BASIC":
                    curl.append("  -u 'username:password'");
                    break;
                case "ORACLE_ROLES":
                    curl.append("  -H 'X-Oracle-Session: your-session-id'");
                    break;
            }
        }

        // Add custom headers from headers array
        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null &&
                        !header.getKey().isEmpty()) {
                    curl.append(" \\\n");
                    curl.append("  -H '").append(header.getKey()).append(": ");
                    curl.append(header.getValue() != null ? header.getValue() : "value");
                    curl.append("'");
                }
            }
        }

        // Add request body for non-GET requests and if there are body parameters
        if (!"GET".equals(api.getHttpMethod())) {
            // Build sample body from body parameters
            if (api.getParameters() != null) {
                List<ApiParameterEntity> bodyParams = api.getParameters().stream()
                        .filter(p -> "body".equals(p.getParameterType()))
                        .collect(Collectors.toList());

                if (!bodyParams.isEmpty()) {
                    Map<String, Object> sampleBody = new HashMap<>();
                    for (ApiParameterEntity param : bodyParams) {
                        String example = param.getExample() != null && !param.getExample().isEmpty() ?
                                param.getExample() : param.getKey().toLowerCase() + "-value";

                        if ("integer".equals(param.getApiType()) || "number".equals(param.getApiType())) {
                            try {
                                sampleBody.put(param.getKey(), Long.parseLong(example));
                            } catch (NumberFormatException e) {
                                sampleBody.put(param.getKey(), 123);
                            }
                        } else if ("boolean".equals(param.getApiType())) {
                            sampleBody.put(param.getKey(), true);
                        } else {
                            sampleBody.put(param.getKey(), example);
                        }
                    }

                    try {
                        String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper()
                                .writerWithDefaultPrettyPrinter().writeValueAsString(sampleBody);
                        curl.append(" \\\n");
                        curl.append("  -d '").append(jsonBody.replace("'", "\\'")).append("'");
                    } catch (Exception e) {
                        log.warn("Failed to generate sample JSON body: {}", e.getMessage());
                    }
                } else if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                    curl.append(" \\\n");
                    curl.append("  -d '").append(api.getRequestConfig().getSample().replace("'", "\\'")).append("'");
                }
            }
        }

        return curl.toString();
    }

    /**
     * Get the base URL from configuration
     */
    public String getBaseUrl() {
        // Try system property first
        String baseUrl = System.getProperty("api.base.url");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return removeTrailingSlash(baseUrl);
        }

        // Try environment variable
        baseUrl = System.getenv("API_BASE_URL");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return removeTrailingSlash(baseUrl);
        }

        // Default fallback
        return "{{baseUrl}}";
    }

    /**
     * Remove trailing slash from URL if present
     */
    private String removeTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Build URL template with placeholders
     */
    public String buildUrlTemplate(GeneratedApiEntity api) {
        StringBuilder template = new StringBuilder();
        template.append("{{baseUrl}}/plx/api/gen/").append(api.getId());

        if (api.getBasePath() != null && !api.getBasePath().isEmpty()) {
            template.append(api.getBasePath().startsWith("/") ? "" : "/").append(api.getBasePath());
        }

        if (api.getEndpointPath() != null && !api.getEndpointPath().isEmpty()) {
            template.append(api.getEndpointPath().startsWith("/") ? "" : "/").append(api.getEndpointPath());
        }

        // Add path parameter placeholders
        if (api.getParameters() != null) {
            List<ApiParameterEntity> pathParams = api.getParameters().stream()
                    .filter(p -> "path".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            for (ApiParameterEntity param : pathParams) {
                template.append("/{").append(param.getKey()).append("}");
            }
        }

        // Add query parameter placeholders
        if (api.getParameters() != null) {
            List<ApiParameterEntity> queryParams = api.getParameters().stream()
                    .filter(p -> "query".equals(p.getParameterType()))
                    .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                    .collect(Collectors.toList());

            if (!queryParams.isEmpty()) {
                template.append("?");
                for (int i = 0; i < queryParams.size(); i++) {
                    ApiParameterEntity param = queryParams.get(i);
                    if (i > 0) {
                        template.append("&");
                    }
                    template.append(param.getKey()).append("={").append(param.getKey()).append("}");
                }
            }
        }

        return template.toString();
    }


    /**
     * Build path placeholders string
     */
    public String buildPathPlaceholders(GeneratedApiEntity api) {
        if (api.getParameters() == null) return "";

        return api.getParameters().stream()
                .filter(p -> "path".equals(p.getParameterType()))
                .map(p -> "{" + p.getKey() + "}")
                .collect(Collectors.joining("/", "/", ""));
    }

    /**
     * Build query placeholders string
     */
    public String buildQueryPlaceholders(GeneratedApiEntity api) {
        if (api.getParameters() == null) return "";

        List<ApiParameterEntity> queryParams = api.getParameters().stream()
                .filter(p -> "query".equals(p.getParameterType()))
                .sorted(Comparator.comparing(ApiParameterEntity::getPosition))
                .collect(Collectors.toList());

        if (queryParams.isEmpty()) return "";

        return queryParams.stream()
                .map(p -> p.getKey() + "={" + p.getKey() + "}")
                .collect(Collectors.joining("&", "?", ""));
    }



    /**
     * Build endpoint path with path parameter placeholders
     */
    public String buildEndpointPathWithParameters(GenerateApiRequestDTO request,
                                                  ApiSourceObjectDTO sourceObjectDTO,
                                                  ParameterGeneratorUtil parameterGeneratorUtil) {
        // Start with the base endpoint from request or generate from API code
        String baseEndpoint = request.getEndpointPath();
        if (baseEndpoint == null || baseEndpoint.isEmpty()) {
            baseEndpoint = "/api/v1/" + request.getApiCode().toLowerCase();
        }

        log.info("Building endpoint path with parameters. Base: {}", baseEndpoint);

        // Generate parameters from source to identify path parameters
        List<ApiParameterDTO> parameters = parameterGeneratorUtil.generateParameterDTOsFromSource(sourceObjectDTO);

        // Filter and sort path parameters by position
        List<ApiParameterDTO> pathParams = parameters.stream()
                .filter(p -> "path".equalsIgnoreCase(p.getParameterType()))
                .sorted(Comparator.comparing(ApiParameterDTO::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        log.info("Found {} path parameters: {}", pathParams.size(),
                pathParams.stream().map(ApiParameterDTO::getKey).collect(Collectors.joining(", ")));

        // Build the full endpoint with path parameter placeholders
        StringBuilder endpointBuilder = new StringBuilder(baseEndpoint);

        // Remove trailing slash if present
        if (endpointBuilder.length() > 0 && endpointBuilder.charAt(endpointBuilder.length() - 1) == '/') {
            endpointBuilder.setLength(endpointBuilder.length() - 1);
        }

        // Add each path parameter as a placeholder
        for (ApiParameterDTO param : pathParams) {
            endpointBuilder.append("/{").append(param.getKey()).append("}");
        }

        String fullEndpoint = endpointBuilder.toString();
        log.info("Final endpoint path with placeholders: {}", fullEndpoint);

        return fullEndpoint;
    }
}