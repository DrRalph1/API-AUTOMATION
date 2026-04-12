package com.usg.autoAPIGenerator.utils.apiEngine;

import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CodeLanguageGeneratorUtil {

    public String generateCodeForLanguage(GeneratedApiEntity api, String language, GenUrlBuilderUtil.GenUrlInfo genUrlInfo) {
        switch (language) {
            case "curl":
                return genUrlInfo.getCurlExample();
            case "javascript":
                return generateFunctionalJavaScriptCode(api, genUrlInfo.getFullUrl());
            case "python":
                return generateFunctionalPythonCode(api, genUrlInfo.getFullUrl());
            case "java":
                return generateFunctionalJavaCode(api, genUrlInfo.getFullUrl());
            case "csharp":
                return generateFunctionalCSharpCode(api, genUrlInfo.getFullUrl());
            case "php":
                return generateFunctionalPhpCode(api, genUrlInfo.getFullUrl());
            case "ruby":
                return generateFunctionalRubyCode(api, genUrlInfo.getFullUrl());
            case "go":
                return generateFunctionalGoCode(api, genUrlInfo.getFullUrl());
            default:
                return "// No code example available for " + language;
        }
    }

    private String generateFunctionalJavaScriptCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder js = new StringBuilder();
        js.append("// Auto-generated functional JavaScript code for ").append(escapeJavaScriptString(api.getApiName())).append("\n\n");

        String baseUrl = fullUrl;

        js.append("// Base URL\n");
        js.append("const baseUrl = '").append(escapeJavaScriptString(baseUrl)).append("';\n\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                js.append("// Build URL with query parameters\n");
                js.append("const queryParams = new URLSearchParams();\n");

                for (ApiParameterEntity param : queryParams) {
                    if (param.getExample() != null) {
                        js.append("queryParams.append('").append(escapeJavaScriptString(param.getKey()))
                                .append("', '").append(escapeJavaScriptString(param.getExample())).append("');\n");
                    } else {
                        js.append("// Optional parameter: ").append(param.getKey()).append("\n");
                        js.append("if (params && params.").append(param.getKey()).append(") {\n");
                        js.append("  queryParams.append('").append(escapeJavaScriptString(param.getKey()))
                                .append("', params.").append(param.getKey()).append(");\n");
                        js.append("}\n");
                    }
                }

                js.append("const url = queryParams.toString() ? \n");
                js.append("  `${baseUrl}?${queryParams.toString()}` : baseUrl;\n\n");
            } else {
                js.append("const url = baseUrl;\n\n");
            }
        } else {
            js.append("const url = baseUrl;\n\n");
        }

        js.append("// Headers with actual values\n");
        js.append("const headers = {\n");
        js.append("  'Content-Type': 'application/json',\n");
        js.append("  'Accept': 'application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    js.append("  '").append(escapeJavaScriptString(
                                    api.getAuthConfig().getApiKeyHeader() != null ?
                                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key"))
                            .append("': '").append(escapeJavaScriptString(
                                    api.getAuthConfig().getApiKeyValue() != null ?
                                            api.getAuthConfig().getApiKeyValue() : "")).append("',\n");
                    break;
                case "BEARER":
                case "JWT":
                    js.append("  'Authorization': 'Bearer ").append(escapeJavaScriptString(
                            api.getAuthConfig().getJwtSecret() != null ?
                                    api.getAuthConfig().getJwtSecret() : "")).append("',\n");
                    break;
                case "BASIC":
                    js.append("  'Authorization': 'Basic ' + btoa(");
                    js.append("'").append(escapeJavaScriptString(
                            api.getAuthConfig().getBasicUsername() != null ?
                                    api.getAuthConfig().getBasicUsername() : "")).append(":' + ");
                    js.append("'").append(escapeJavaScriptString(
                            api.getAuthConfig().getBasicPassword() != null ?
                                    api.getAuthConfig().getBasicPassword() : "")).append("'),\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    js.append("  '").append(escapeJavaScriptString(header.getKey()))
                            .append("': '").append(escapeJavaScriptString(header.getValue())).append("',\n");
                }
            }
        }

        js.append("};\n\n");

        js.append("/**\n");
        js.append(" * Call the ").append(api.getApiName()).append(" API\n");
        js.append(" * @param {Object} params - Request parameters\n");
        js.append(" * @returns {Promise<Object>} API response\n");
        js.append(" */\n");
        js.append("async function callApi(params = {}) {\n");

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null &&
                api.getRequestConfig().getSample() != null) {
            js.append("  const requestBody = params.body || ").append(api.getRequestConfig().getSample()).append(";\n");
            js.append("  \n");
        }

        js.append("  const options = {\n");
        js.append("    method: '").append(api.getHttpMethod()).append("',\n");
        js.append("    headers,\n");

        if (!"GET".equals(api.getHttpMethod())) {
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                js.append("    body: JSON.stringify(requestBody)\n");
            } else {
                js.append("    body: params.body ? JSON.stringify(params.body) : undefined\n");
            }
        }

        js.append("  };\n\n");

        js.append("  console.log('Making request to:', url);\n");
        js.append("  console.log('With headers:', headers);\n\n");

        js.append("  try {\n");
        js.append("    const response = await fetch(url, options);\n");
        js.append("    console.log('Status:', response.status);\n");
        js.append("    \n");
        js.append("    const data = await response.json();\n");
        js.append("    \n");
        js.append("    if (response.ok) {\n");
        js.append("      console.log('Success:', data);\n");
        js.append("      return data;\n");
        js.append("    } else {\n");
        js.append("      console.error('Error:', data);\n");
        js.append("      throw new Error(data.message || `HTTP error ${response.status}`);\n");
        js.append("    }\n");
        js.append("  } catch (error) {\n");
        js.append("    console.error('Network error:', error);\n");
        js.append("    throw error;\n");
        js.append("  }\n");
        js.append("}\n\n");

        js.append("// Example usage with actual values:\n");
        js.append("/*\n");
        js.append("callApi({\n");

        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if (param.getExample() != null) {
                    js.append("  ").append(param.getKey()).append(": '")
                            .append(escapeJavaScriptString(param.getExample())).append("',\n");
                }
            }
        }

        if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            js.append("  body: ").append(api.getRequestConfig().getSample()).append("\n");
        }

        js.append("})\n");
        js.append("  .then(data => console.log('Success:', data))\n");
        js.append("  .catch(error => console.error('Error:', error));\n");
        js.append("*/\n");

        return js.toString();
    }

    private String generateFunctionalPythonCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder py = new StringBuilder();
        py.append("# Auto-generated functional Python code for ").append(api.getApiName()).append("\n\n");
        py.append("import requests\n");
        py.append("import json\n");
        py.append("from urllib.parse import urlencode\n\n");

        String baseUrl = fullUrl;

        py.append("# Base URL\n");
        py.append("base_url = \"").append(baseUrl).append("\"\n\n");

        // URL building with parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                py.append("# Build URL with query parameters\n");
                py.append("def build_url(params=None):\n");
                py.append("    url = base_url\n");
                py.append("    if params:\n");
                py.append("        query_params = {}\n");

                for (ApiParameterEntity param : queryParams) {
                    py.append("        if '").append(param.getKey()).append("' in params:\n");
                    py.append("            query_params['").append(param.getKey()).append("'] = params['").append(param.getKey()).append("']\n");
                }

                py.append("        if query_params:\n");
                py.append("            url += '?' + urlencode(query_params)\n");
                py.append("    return url\n\n");

                py.append("# Example with actual values\n");
                py.append("params = {\n");
                for (ApiParameterEntity param : queryParams) {
                    if (param.getExample() != null) {
                        py.append("    '").append(param.getKey()).append("': '").append(param.getExample()).append("',\n");
                    }
                }
                py.append("}\n");
                py.append("url = build_url(params)\n\n");
            } else {
                py.append("url = base_url\n\n");
            }
        } else {
            py.append("url = base_url\n\n");
        }

        py.append("# Headers with actual values\n");
        py.append("headers = {\n");
        py.append("    'Content-Type': 'application/json',\n");
        py.append("    'Accept': 'application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    py.append("    '").append(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key").append("': '");
                    py.append(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "").append("',\n");
                    break;
                case "BEARER":
                case "JWT":
                    py.append("    'Authorization': 'Bearer ");
                    py.append(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "").append("',\n");
                    break;
                case "BASIC":
                    py.append("    'Authorization': requests.auth.HTTPBasicAuth('");
                    py.append(api.getAuthConfig().getBasicUsername() != null ?
                            api.getAuthConfig().getBasicUsername() : "").append("', '");
                    py.append(api.getAuthConfig().getBasicPassword() != null ?
                            api.getAuthConfig().getBasicPassword() : "").append("'),\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    py.append("    '").append(header.getKey()).append("': '");
                    py.append(header.getValue()).append("',\n");
                }
            }
        }

        py.append("}\n\n");

        py.append("def call_api(params=None, body=None):\n");
        py.append("    \"\"\"\n");
        py.append("    Call the ").append(api.getApiName()).append(" API\n");
        py.append("    Args:\n");
        py.append("        params: Query parameters dictionary\n");
        py.append("        body: Request body for POST/PUT requests\n");
        py.append("    Returns:\n");
        py.append("        Response object\n");
        py.append("    \"\"\"\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            py.append("    url = build_url(params)\n");
        } else {
            py.append("    url = base_url\n");
        }
        py.append("    \n");
        py.append("    print(f\"Making ").append(api.getHttpMethod()).append(" request to: {url}\")\n");
        py.append("    print(\"With headers:\", headers)\n");
        py.append("    \n");

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            py.append("    request_body = body if body is not None else ").append(api.getRequestConfig().getSample()).append("\n");
            py.append("    print(\"With body:\", request_body)\n");
            py.append("    response = requests.").append(api.getHttpMethod().toLowerCase());
            py.append("(url, headers=headers, json=request_body)\n");
        } else {
            py.append("    response = requests.").append(api.getHttpMethod().toLowerCase());
            py.append("(url, headers=headers)\n");
        }
        py.append("    \n");
        py.append("    print(f\"Status Code: {response.status_code}\")\n");
        py.append("    print(\"Response Headers:\", response.headers)\n");
        py.append("    print(\"Response Body:\")\n");
        py.append("    try:\n");
        py.append("        print(json.dumps(response.json(), indent=2))\n");
        py.append("    except:\n");
        py.append("        print(response.text)\n");
        py.append("    \n");
        py.append("    return response\n\n");

        py.append("# Example usage\n");
        py.append("if __name__ == \"__main__\":\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            py.append("    # Example with parameters\n");
            py.append("    params = {\n");
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    py.append("        '").append(param.getKey()).append("': '").append(param.getExample()).append("',\n");
                }
            }
            py.append("    }\n");
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            py.append("    # Example with request body\n");
            py.append("    body = ").append(api.getRequestConfig().getSample()).append("\n");
            py.append("    response = call_api(params=params, body=body)\n");
        } else if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            py.append("    response = call_api(params=params)\n");
        } else {
            py.append("    response = call_api()\n");
        }

        return py.toString();
    }

    private String generateFunctionalJavaCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder java = new StringBuilder();
        java.append("// Auto-generated functional Java code for ").append(api.getApiName()).append("\n\n");

        java.append("import java.net.URI;\n");
        java.append("import java.net.http.HttpClient;\n");
        java.append("import java.net.http.HttpRequest;\n");
        java.append("import java.net.http.HttpResponse;\n");
        java.append("import java.util.HashMap;\n");
        java.append("import java.util.Map;\n");
        java.append("import com.fasterxml.jackson.databind.ObjectMapper;\n\n");

        String baseUrl = fullUrl;

        java.append("public class ").append(toClassName(api.getApiName())).append("ApiClient {\n\n");
        java.append("    private static final String BASE_URL = \"").append(baseUrl).append("\";\n");
        java.append("    private final HttpClient httpClient;\n");
        java.append("    private final ObjectMapper objectMapper;\n\n");

        java.append("    public ").append(toClassName(api.getApiName())).append("ApiClient() {\n");
        java.append("        this.httpClient = HttpClient.newHttpClient();\n");
        java.append("        this.objectMapper = new ObjectMapper();\n");
        java.append("    }\n\n");

        java.append("    /**\n");
        java.append("     * Build headers for the request\n");
        java.append("     */\n");
        java.append("    private HttpRequest.Builder buildRequest(String url) {\n");
        java.append("        HttpRequest.Builder builder = HttpRequest.newBuilder()\n");
        java.append("                .uri(URI.create(url))\n");
        java.append("                .header(\"Content-Type\", \"application/json\")\n");
        java.append("                .header(\"Accept\", \"application/json\");\n\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    java.append("        builder.header(\"");
                    java.append(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                    java.append("\", \"");
                    java.append(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "");
                    java.append("\");\n");
                    break;
                case "BEARER":
                case "JWT":
                    java.append("        builder.header(\"Authorization\", \"Bearer ");
                    java.append(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "");
                    java.append("\");\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    java.append("        builder.header(\"").append(header.getKey()).append("\", \"");
                    java.append(header.getValue()).append("\");\n");
                }
            }
        }

        java.append("        return builder;\n");
        java.append("    }\n\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                java.append("    /**\n");
                java.append("     * Build URL with query parameters\n");
                java.append("     */\n");
                java.append("    private String buildUrl(Map<String, String> params) {\n");
                java.append("        StringBuilder url = new StringBuilder(BASE_URL);\n");
                java.append("        if (params != null && !params.isEmpty()) {\n");
                java.append("            url.append(\"?\");\n");
                java.append("            params.forEach((key, value) -> {\n");
                java.append("                if (url.charAt(url.length() - 1) != '?') {\n");
                java.append("                    url.append(\"&\");\n");
                java.append("                }\n");
                java.append("                url.append(key).append(\"=\").append(value);\n");
                java.append("            });\n");
                java.append("        }\n");
                java.append("        return url.toString();\n");
                java.append("    }\n\n");
            }
        }

        java.append("    /**\n");
        java.append("     * Call the ").append(api.getApiName()).append(" API\n");
        java.append("     */\n");
        java.append("    public HttpResponse<String> callApi(");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            java.append("Map<String, String> params");
        }

        if (!"GET".equals(api.getHttpMethod())) {
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                java.append(", ");
            }
            java.append("Object requestBody");
        }
        java.append(") throws Exception {\n\n");

        // Build URL
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }
            if (!queryParams.isEmpty()) {
                java.append("        String url = buildUrl(params);\n");
            } else {
                java.append("        String url = BASE_URL;\n");
            }
        } else {
            java.append("        String url = BASE_URL;\n");
        }
        java.append("        // System.out.println(\"Making request to: \" + url);\n\n");

        java.append("        HttpRequest.Builder builder = buildRequest(url);\n\n");

        if (!"GET".equals(api.getHttpMethod())) {
            java.append("        // Prepare request body\n");
            java.append("        String requestBodyJson = objectMapper.writeValueAsString(\n");
            java.append("            requestBody != null ? requestBody : ");
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                java.append(api.getRequestConfig().getSample());
            } else {
                java.append("new HashMap<>()");
            }
            java.append("\n        );\n");
            java.append("        // System.out.println(\"Request body: \" + requestBodyJson);\n");
            java.append("        builder = builder.").append(api.getHttpMethod().toLowerCase()).append("(HttpRequest.BodyPublishers.ofString(requestBodyJson));\n\n");
        } else {
            java.append("        builder = builder.GET();\n\n");
        }

        java.append("        HttpRequest request = builder.build();\n\n");
        java.append("        // System.out.println(\"Sending request...\");\n");
        java.append("        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());\n\n");
        java.append("        // System.out.println(\"Status Code: \" + response.statusCode());\n");
        java.append("        // System.out.println(\"Response Headers: \" + response.headers());\n");
        java.append("        // System.out.println(\"Response Body: \" + response.body());\n\n");
        java.append("        return response;\n");
        java.append("    }\n\n");

        java.append("    public static void main(String[] args) {\n");
        java.append("        ").append(toClassName(api.getApiName())).append("ApiClient client = new ").append(toClassName(api.getApiName())).append("ApiClient();\n");
        java.append("        try {\n");

        Map<String, String> paramsMap = new HashMap<>();
        if (api.getParameters() != null) {
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    paramsMap.put(param.getKey(), param.getExample());
                }
            }
        }

        if (!paramsMap.isEmpty()) {
            java.append("            Map<String, String> params = new HashMap<>();\n");
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                java.append("            params.put(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\");\n");
            }
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            if (!paramsMap.isEmpty()) {
                java.append("            Object requestBody = ").append(api.getRequestConfig().getSample()).append(";\n");
                java.append("            client.callApi(params, requestBody);\n");
            } else {
                java.append("            Object requestBody = ").append(api.getRequestConfig().getSample()).append(";\n");
                java.append("            client.callApi(requestBody);\n");
            }
        } else if (!paramsMap.isEmpty()) {
            java.append("            client.callApi(params);\n");
        } else {
            java.append("            client.callApi();\n");
        }

        java.append("        } catch (Exception e) {\n");
        java.append("            e.printStackTrace();\n");
        java.append("        }\n");
        java.append("    }\n");
        java.append("}\n");

        return java.toString();
    }

    private String generateFunctionalCSharpCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder cs = new StringBuilder();
        cs.append("// Auto-generated functional C# code for ").append(api.getApiName()).append("\n\n");

        cs.append("using System;\n");
        cs.append("using System.Net.Http;\n");
        cs.append("using System.Net.Http.Headers;\n");
        cs.append("using System.Text;\n");
        cs.append("using System.Threading.Tasks;\n");
        cs.append("using System.Collections.Generic;\n");
        cs.append("using Newtonsoft.Json;\n\n");

        String baseUrl = fullUrl;

        cs.append("public class ").append(toClassName(api.getApiName())).append("ApiClient\n");
        cs.append("{\n");
        cs.append("    private static readonly string BaseUrl = \"").append(baseUrl).append("\";\n");
        cs.append("    private readonly HttpClient _httpClient;\n\n");

        cs.append("    public ").append(toClassName(api.getApiName())).append("ApiClient()\n");
        cs.append("    {\n");
        cs.append("        _httpClient = new HttpClient();\n");
        cs.append("        _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue(\"application/json\"));\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    cs.append("        _httpClient.DefaultRequestHeaders.Add(\"");
                    cs.append(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                    cs.append("\", \"");
                    cs.append(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "");
                    cs.append("\");\n");
                    break;
                case "BEARER":
                case "JWT":
                    cs.append("        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue(\"Bearer\", \"");
                    cs.append(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "");
                    cs.append("\");\n");
                    break;
                case "BASIC":
                    cs.append("        var authToken = Convert.ToBase64String(Encoding.ASCII.GetBytes($\"");
                    cs.append(api.getAuthConfig().getBasicUsername() != null ?
                            api.getAuthConfig().getBasicUsername() : "");
                    cs.append(":");
                    cs.append(api.getAuthConfig().getBasicPassword() != null ?
                            api.getAuthConfig().getBasicPassword() : "");
                    cs.append("\"));\n");
                    cs.append("        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue(\"Basic\", authToken);\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    cs.append("        _httpClient.DefaultRequestHeaders.Add(\"");
                    cs.append(header.getKey()).append("\", \"");
                    cs.append(header.getValue()).append("\");\n");
                }
            }
        }

        cs.append("    }\n\n");

        // URL building method
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                cs.append("    private string BuildUrl(Dictionary<string, string> queryParams)\n");
                cs.append("    {\n");
                cs.append("        if (queryParams == null || queryParams.Count == 0)\n");
                cs.append("            return BaseUrl;\n\n");
                cs.append("        var queryString = System.Web.HttpUtility.ParseQueryString(string.Empty);\n");
                cs.append("        foreach (var param in queryParams)\n");
                cs.append("        {\n");
                cs.append("            queryString[param.Key] = param.Value;\n");
                cs.append("        }\n");
                cs.append("        return BaseUrl + \"?\" + queryString.ToString();\n");
                cs.append("    }\n\n");
            }
        }

        cs.append("    /// <summary>\n");
        cs.append("    /// Call the ").append(api.getApiName()).append(" API\n");
        cs.append("    /// </summary>\n");
        cs.append("    public async Task<HttpResponseMessage> CallApiAsync(");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            cs.append("Dictionary<string, string> queryParams = null");
        }

        if (!"GET".equals(api.getHttpMethod())) {
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                cs.append(", ");
            }
            cs.append("object requestBody = null");
        }
        cs.append(")\n");
        cs.append("    {\n");

        // Build URL
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }
            if (!queryParams.isEmpty()) {
                cs.append("        var url = BuildUrl(queryParams);\n");
            } else {
                cs.append("        var url = BaseUrl;\n");
            }
        } else {
            cs.append("        var url = BaseUrl;\n");
        }
        cs.append("        Console.WriteLine($\"Making request to: {url}\");\n\n");

        // Get the HTTP method as a string
        String httpMethod = api.getHttpMethod() != null ? api.getHttpMethod().toUpperCase() : "GET";

        if (!"GET".equals(httpMethod)) {
            cs.append("        var content = new StringContent(\n");
            cs.append("            JsonConvert.SerializeObject(requestBody ?? ");
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                cs.append(api.getRequestConfig().getSample());
            } else {
                cs.append("new {}");
            }
            cs.append("),\n");
            cs.append("            Encoding.UTF8,\n");
            cs.append("            \"application/json\");\n\n");

            cs.append("        Console.WriteLine($\"Request body: {await content.ReadAsStringAsync()}\");\n\n");

            // Use the httpMethod string directly
            if ("POST".equals(httpMethod)) {
                cs.append("        var response = await _httpClient.PostAsync(url, content);\n");
            } else if ("PUT".equals(httpMethod)) {
                cs.append("        var response = await _httpClient.PutAsync(url, content);\n");
            } else if ("PATCH".equals(httpMethod)) {
                cs.append("        var response = await _httpClient.PatchAsync(url, content);\n");
            } else if ("DELETE".equals(httpMethod)) {
                cs.append("        var response = await _httpClient.DeleteAsync(url);\n");
            } else {
                cs.append("        var response = await _httpClient.PostAsync(url, content);\n");
            }
        } else {
            cs.append("        var response = await _httpClient.GetAsync(url);\n");
        }
        cs.append("        \n");
        cs.append("        var responseBody = await response.Content.ReadAsStringAsync();\n");
        cs.append("        Console.WriteLine($\"Status Code: {(int)response.StatusCode} {response.ReasonPhrase}\");\n");
        cs.append("        Console.WriteLine($\"Response Body: {responseBody}\");\n");
        cs.append("        \n");
        cs.append("        return response;\n");
        cs.append("    }\n\n");

        cs.append("    public static async Task Main(string[] args)\n");
        cs.append("    {\n");
        cs.append("        var client = new ").append(toClassName(api.getApiName())).append("ApiClient();\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                cs.append("        var queryParams = new Dictionary<string, string>\n");
                cs.append("        {\n");
                for (ApiParameterEntity param : queryParams) {
                    cs.append("            { \"").append(param.getKey()).append("\", \"");
                    cs.append(param.getExample()).append("\" },\n");
                }
                cs.append("        };\n");
            }
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            cs.append("        var requestBody = ").append(api.getRequestConfig().getSample()).append(";\n");

            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                cs.append("        var response = await client.CallApiAsync(queryParams, requestBody);\n");
            } else {
                cs.append("        var response = await client.CallApiAsync(requestBody);\n");
            }
        } else if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            cs.append("        var response = await client.CallApiAsync(queryParams);\n");
        } else {
            cs.append("        var response = await client.CallApiAsync();\n");
        }

        cs.append("    }\n");
        cs.append("}\n");

        return cs.toString();
    }

    private String generateFunctionalPhpCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder php = new StringBuilder();
        php.append("<?php\n");
        php.append("// Auto-generated functional PHP code for ").append(api.getApiName()).append("\n\n");

        String baseUrl = fullUrl;

        php.append("// Base URL\n");
        php.append("define('BASE_URL', '").append(escapePhpString(baseUrl)).append("');\n\n");

        php.append("/**\n");
        php.append(" * Call the ").append(api.getApiName()).append(" API\n");
        php.append(" */\n");
        php.append("function callApi(");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            php.append("$params = []");
        }

        if (!"GET".equals(api.getHttpMethod())) {
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                php.append(", ");
            }
            php.append("$data = null");
        }
        php.append(") {\n");

        // Build URL with query parameters
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                php.append("    // Build URL with query parameters\n");
                php.append("    $url = BASE_URL;\n");
                php.append("    if (!empty($params)) {\n");
                php.append("        $queryString = http_build_query($params);\n");
                php.append("        $url .= '?' . $queryString;\n");
                php.append("    }\n");
            } else {
                php.append("    $url = BASE_URL;\n");
            }
        } else {
            php.append("    $url = BASE_URL;\n");
        }
        php.append("    \n");

        php.append("    // Initialize cURL\n");
        php.append("    $ch = curl_init($url);\n");
        php.append("    \n");
        php.append("    // Set headers\n");
        php.append("    $headers = [\n");
        php.append("        'Content-Type: application/json',\n");
        php.append("        'Accept: application/json',\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    php.append("        '").append(escapePhpString(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key")).append(": ");
                    php.append(escapePhpString(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "")).append("',\n");
                    break;
                case "BEARER":
                case "JWT":
                    php.append("        'Authorization: Bearer ");
                    php.append(escapePhpString(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "")).append("',\n");
                    break;
                case "BASIC":
                    php.append("        'Authorization: Basic ' . base64_encode('");
                    php.append(escapePhpString(api.getAuthConfig().getBasicUsername() != null ?
                            api.getAuthConfig().getBasicUsername() : "")).append(":");
                    php.append(escapePhpString(api.getAuthConfig().getBasicPassword() != null ?
                            api.getAuthConfig().getBasicPassword() : "")).append("'),\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    php.append("        '").append(escapePhpString(header.getKey())).append(": ");
                    php.append(escapePhpString(header.getValue())).append("',\n");
                }
            }
        }

        php.append("    ];\n");
        php.append("    \n");
        php.append("    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);\n");
        php.append("    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);\n");
        php.append("    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, '").append(api.getHttpMethod()).append("');\n");
        php.append("    \n");

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            php.append("    // Set request body\n");
            php.append("    $requestData = $data ?: ").append(api.getRequestConfig().getSample()).append(";\n");
            php.append("    $jsonData = json_encode($requestData);\n");
            php.append("    curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);\n");
            php.append("    echo \"Request body: $jsonData\\n\";\n");
            php.append("    \n");
        }

        php.append("    echo \"Making ").append(api.getHttpMethod()).append(" request to: $url\\n\";\n");
        php.append("    \n");
        php.append("    // Execute request\n");
        php.append("    $response = curl_exec($ch);\n");
        php.append("    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);\n");
        php.append("    $error = curl_error($ch);\n");
        php.append("    \n");
        php.append("    curl_close($ch);\n");
        php.append("    \n");
        php.append("    echo \"Status Code: $httpCode\\n\";\n");
        php.append("    \n");
        php.append("    if ($error) {\n");
        php.append("        echo \"cURL Error: $error\\n\";\n");
        php.append("        return null;\n");
        php.append("    }\n");
        php.append("    \n");
        php.append("    // Parse response\n");
        php.append("    $decodedResponse = json_decode($response, true);\n");
        php.append("    if (json_last_error() === JSON_ERROR_NONE) {\n");
        php.append("        echo \"Response Body:\\n\";\n");
        php.append("        print_r($decodedResponse);\n");
        php.append("        return $decodedResponse;\n");
        php.append("    } else {\n");
        php.append("        echo \"Response Body: $response\\n\";\n");
        php.append("        return $response;\n");
        php.append("    }\n");
        php.append("}\n\n");

        php.append("// Example usage\n");
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            php.append("$params = [\n");
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    php.append("    '").append(escapePhpString(param.getKey())).append("' => '");
                    php.append(escapePhpString(param.getExample())).append("',\n");
                }
            }
            php.append("];\n");
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            php.append("$data = ").append(api.getRequestConfig().getSample()).append(";\n");

            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                php.append("$result = callApi($params, $data);\n");
            } else {
                php.append("$result = callApi($data);\n");
            }
        } else if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            php.append("$result = callApi($params);\n");
        } else {
            php.append("$result = callApi();\n");
        }

        php.append("?>\n");
        return php.toString();
    }

    // Helper method for PHP string escaping
    private String escapePhpString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("$", "\\$");
    }

    private String generateFunctionalRubyCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder ruby = new StringBuilder();
        ruby.append("# Auto-generated functional Ruby code for ").append(api.getApiName()).append("\n\n");

        ruby.append("require 'net/http'\n");
        ruby.append("require 'uri'\n");
        ruby.append("require 'json'\n\n");

        String baseUrl = fullUrl;

        ruby.append("# Base URL\n");
        ruby.append("BASE_URL = '").append(baseUrl).append("'.freeze\n\n");

        ruby.append("class ").append(toClassName(api.getApiName())).append("ApiClient\n");
        ruby.append("  def initialize\n");
        ruby.append("    @uri = URI(BASE_URL)\n");
        ruby.append("  end\n\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                ruby.append("  def build_url(params = {})\n");
                ruby.append("    uri = URI(BASE_URL)\n");
                ruby.append("    uri.query = URI.encode_www_form(params) unless params.empty?\n");
                ruby.append("    uri\n");
                ruby.append("  end\n\n");
            }
        }

        ruby.append("  def call_api(");
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            ruby.append("params: {}, ");
        }
        if (!"GET".equals(api.getHttpMethod())) {
            ruby.append("body: nil");
        }
        ruby.append(")\n");

        // Build URL
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }
            if (!queryParams.isEmpty()) {
                ruby.append("    uri = build_url(params)\n");
            } else {
                ruby.append("    uri = URI(BASE_URL)\n");
            }
        } else {
            ruby.append("    uri = URI(BASE_URL)\n");
        }
        ruby.append("    \n");

        // Get HTTP method as string
        String httpMethod = api.getHttpMethod() != null ? api.getHttpMethod().toUpperCase() : "GET";
        ruby.append("    puts \"Making ").append(httpMethod).append(" request to: \\#{uri}\"\n\n");

        // Create the appropriate request object based on HTTP method
        if ("GET".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Get.new(uri)\n");
        } else if ("POST".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Post.new(uri)\n");
        } else if ("PUT".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Put.new(uri)\n");
        } else if ("DELETE".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Delete.new(uri)\n");
        } else if ("PATCH".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Patch.new(uri)\n");
        } else if ("HEAD".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Head.new(uri)\n");
        } else if ("OPTIONS".equals(httpMethod)) {
            ruby.append("    request = Net::HTTP::Options.new(uri)\n");
        } else {
            ruby.append("    request = Net::HTTP::Get.new(uri)\n");
        }

        ruby.append("    \n");
        ruby.append("    # Set headers\n");
        ruby.append("    request['Content-Type'] = 'application/json'\n");
        ruby.append("    request['Accept'] = 'application/json'\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    ruby.append("    request['").append(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key").append("'] = '");
                    ruby.append(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "").append("'\n");
                    break;
                case "BEARER":
                case "JWT":
                    ruby.append("    request['Authorization'] = 'Bearer ");
                    ruby.append(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "").append("'\n");
                    break;
                case "BASIC":
                    ruby.append("    request.basic_auth '");
                    ruby.append(api.getAuthConfig().getBasicUsername() != null ?
                            api.getAuthConfig().getBasicUsername() : "").append("', '");
                    ruby.append(api.getAuthConfig().getBasicPassword() != null ?
                            api.getAuthConfig().getBasicPassword() : "").append("'\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    ruby.append("    request['").append(header.getKey()).append("'] = '");
                    ruby.append(header.getValue()).append("'\n");
                }
            }
        }

        ruby.append("    \n");

        if (!"GET".equals(httpMethod) && !"HEAD".equals(httpMethod) && !"DELETE".equals(httpMethod) && !"OPTIONS".equals(httpMethod)) {
            ruby.append("    # Set request body\n");
            ruby.append("    request_body = body || ");
            if (api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
                ruby.append(api.getRequestConfig().getSample());
            } else {
                ruby.append("{}");
            }
            ruby.append("\n");
            ruby.append("    request.body = request_body.to_json\n");
            ruby.append("    puts \"Request body: \\#{request.body}\"\n");
            ruby.append("    \n");
        }

        ruby.append("    # Send request\n");
        ruby.append("    response = Net::HTTP.start(uri.hostname, uri.port, use_ssl: uri.scheme == 'https') do |http|\n");
        ruby.append("      http.request(request)\n");
        ruby.append("    end\n");
        ruby.append("    \n");
        ruby.append("    puts \"Status Code: \\#{response.code}\"\n");
        ruby.append("    puts \"Response Headers: \\#{response.each_header.to_h}\"\n");
        ruby.append("    \n");
        ruby.append("    # Parse response\n");
        ruby.append("    begin\n");
        ruby.append("      parsed_response = JSON.parse(response.body)\n");
        ruby.append("      puts \"Response Body: \\#{JSON.pretty_generate(parsed_response)}\"\n");
        ruby.append("      return parsed_response\n");
        ruby.append("    rescue JSON::ParserError\n");
        ruby.append("      puts \"Response Body: \\#{response.body}\"\n");
        ruby.append("      return response.body\n");
        ruby.append("    end\n");
        ruby.append("  end\n");
        ruby.append("end\n\n");

        ruby.append("# Example usage\n");
        ruby.append("client = ").append(toClassName(api.getApiName())).append("ApiClient.new\n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            ruby.append("params = {\n");
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    ruby.append("  ").append(param.getKey()).append(": '").append(param.getExample()).append("',\n");
                }
            }
            ruby.append("}\n");
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                ruby.append("result = client.call_api(params: params, body: ").append(api.getRequestConfig().getSample()).append(")\n");
            } else {
                ruby.append("result = client.call_api(body: ").append(api.getRequestConfig().getSample()).append(")\n");
            }
        } else if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            ruby.append("result = client.call_api(params: params)\n");
        } else {
            ruby.append("result = client.call_api\n");
        }

        return ruby.toString();
    }

    private String generateFunctionalGoCode(GeneratedApiEntity api, String fullUrl) {
        StringBuilder go = new StringBuilder();
        go.append("// Auto-generated functional Go code for ").append(api.getApiName()).append("\n\n");

        go.append("package main\n\n");

        go.append("import (\n");
        go.append("    \"bytes\"\n");
        go.append("    \"encoding/json\"\n");
        go.append("    \"fmt\"\n");
        go.append("    \"io\"\n");
        go.append("    \"net/http\"\n");
        go.append("    \"net/url\"\n");
        go.append("    \"os\"\n");
        go.append(")\n\n");

        String baseUrl = fullUrl;

        go.append("const (\n");
        go.append("    baseURL = \"").append(baseUrl).append("\"\n");
        go.append(")\n\n");

        go.append("type ApiClient struct {\n");
        go.append("    httpClient *http.Client\n");
        go.append("    baseURL    string\n");
        go.append("}\n\n");

        go.append("func NewApiClient() *ApiClient {\n");
        go.append("    return &ApiClient{\n");
        go.append("        httpClient: &http.Client{},\n");
        go.append("        baseURL:    baseURL,\n");
        go.append("    }\n");
        go.append("}\n\n");

        // Request body struct if applicable
        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            go.append("type RequestBody struct {\n");

            // Try to parse the sample JSON to infer fields (simplified approach)
            String sample = api.getRequestConfig().getSample();
            if (sample != null && sample.contains("{")) {
                // This is a simplified parsing - in real implementation you'd want proper JSON parsing
                sample = sample.replace("{", "").replace("}", "").trim();
                String[] pairs = sample.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replace("\"", "");
                        String value = keyValue[1].trim();
                        go.append("    ").append(toGoFieldName(key)).append(" ").append(inferGoType(value)).append(" `json:\"").append(key).append("\"`\n");
                    }
                }
            }
            go.append("}\n\n");
        }

        go.append("// CallApi makes a request to the ").append(api.getApiName()).append(" API\n");
        go.append("func (c *ApiClient) CallApi(");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            go.append("queryParams map[string]string");
        }

        if (!"GET".equals(api.getHttpMethod())) {
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                go.append(", ");
            }
            go.append("requestBody interface{}");
        }
        go.append(") (*http.Response, error) {\n");

        // Build URL
        go.append("    // Build URL\n");
        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            List<ApiParameterEntity> queryParams = new ArrayList<>();
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType())) {
                    queryParams.add(param);
                }
            }

            if (!queryParams.isEmpty()) {
                go.append("    u, err := url.Parse(c.baseURL)\n");
                go.append("    if err != nil {\n");
                go.append("        return nil, fmt.Errorf(\"failed to parse URL: %w\", err)\n");
                go.append("    }\n");
                go.append("    \n");
                go.append("    if len(queryParams) > 0 {\n");
                go.append("        q := u.Query()\n");
                go.append("        for key, value := range queryParams {\n");
                go.append("            q.Set(key, value)\n");
                go.append("        }\n");
                go.append("        u.RawQuery = q.Encode()\n");
                go.append("    }\n");
                go.append("    \n");
                go.append("    fullURL := u.String()\n");
            } else {
                go.append("    fullURL := c.baseURL\n");
            }
        } else {
            go.append("    fullURL := c.baseURL\n");
        }
        go.append("    \n");
        go.append("    fmt.Printf(\"Making ").append(api.getHttpMethod()).append(" request to: %s\\n\", fullURL)\n");
        go.append("    \n");

        // Create request body
        if (!"GET".equals(api.getHttpMethod())) {
            go.append("    // Prepare request body\n");
            go.append("    var bodyBytes []byte\n");
            go.append("    if requestBody != nil {\n");
            go.append("        var err error\n");
            go.append("        bodyBytes, err = json.Marshal(requestBody)\n");
            go.append("        if err != nil {\n");
            go.append("            return nil, fmt.Errorf(\"failed to marshal request body: %w\", err)\n");
            go.append("        }\n");
            go.append("        fmt.Printf(\"Request body: %s\\n\", string(bodyBytes))\n");
            go.append("    }\n");
            go.append("    \n");
        }

        // Create request
        go.append("    // Create request\n");
        go.append("    var req *http.Request\n");
        go.append("    var err error\n");
        go.append("    \n");

        if (!"GET".equals(api.getHttpMethod())) {
            go.append("    if bodyBytes != nil {\n");
            go.append("        req, err = http.NewRequest(\"");
            go.append(api.getHttpMethod()).append("\", fullURL, bytes.NewBuffer(bodyBytes))\n");
            go.append("    } else {\n");
            go.append("        req, err = http.NewRequest(\"");
            go.append(api.getHttpMethod()).append("\", fullURL, nil)\n");
            go.append("    }\n");
        } else {
            go.append("    req, err = http.NewRequest(\"");
            go.append(api.getHttpMethod()).append("\", fullURL, nil)\n");
        }
        go.append("    if err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"failed to create request: %w\", err)\n");
        go.append("    }\n");
        go.append("    \n");

        // Set headers
        go.append("    // Set headers\n");
        go.append("    req.Header.Set(\"Content-Type\", \"application/json\")\n");
        go.append("    req.Header.Set(\"Accept\", \"application/json\")\n");

        if (api.getAuthConfig() != null && !"NONE".equals(api.getAuthConfig().getAuthType())) {
            switch (api.getAuthConfig().getAuthType()) {
                case "API_KEY":
                    go.append("    req.Header.Set(\"");
                    go.append(api.getAuthConfig().getApiKeyHeader() != null ?
                            api.getAuthConfig().getApiKeyHeader() : "X-API-Key");
                    go.append("\", \"");
                    go.append(api.getAuthConfig().getApiKeyValue() != null ?
                            api.getAuthConfig().getApiKeyValue() : "");
                    go.append("\")\n");
                    break;
                case "BEARER":
                case "JWT":
                    go.append("    req.Header.Set(\"Authorization\", \"Bearer ");
                    go.append(api.getAuthConfig().getJwtSecret() != null ?
                            api.getAuthConfig().getJwtSecret() : "");
                    go.append("\")\n");
                    break;
                case "BASIC":
                    go.append("    req.SetBasicAuth(\"");
                    go.append(api.getAuthConfig().getBasicUsername() != null ?
                            api.getAuthConfig().getBasicUsername() : "");
                    go.append("\", \"");
                    go.append(api.getAuthConfig().getBasicPassword() != null ?
                            api.getAuthConfig().getBasicPassword() : "");
                    go.append("\")\n");
                    break;
            }
        }

        if (api.getHeaders() != null) {
            for (ApiHeaderEntity header : api.getHeaders()) {
                if (Boolean.TRUE.equals(header.getIsRequestHeader()) &&
                        header.getKey() != null && header.getValue() != null) {
                    go.append("    req.Header.Set(\"").append(header.getKey()).append("\", \"");
                    go.append(header.getValue()).append("\")\n");
                }
            }
        }
        go.append("    \n");

        // Execute request
        go.append("    // Execute request\n");
        go.append("    resp, err := c.httpClient.Do(req)\n");
        go.append("    if err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"request failed: %w\", err)\n");
        go.append("    }\n");
        go.append("    defer resp.Body.Close()\n");
        go.append("    \n");
        go.append("    fmt.Printf(\"Status Code: %d\\n\", resp.StatusCode)\n");
        go.append("    fmt.Println(\"Response Headers:\")\n");
        go.append("    for key, values := range resp.Header {\n");
        go.append("        for _, value := range values {\n");
        go.append("            fmt.Printf(\"  %s: %s\\n\", key, value)\n");
        go.append("        }\n");
        go.append("    }\n");
        go.append("    \n");
        go.append("    // Read response body\n");
        go.append("    body, err := io.ReadAll(resp.Body)\n");
        go.append("    if err != nil {\n");
        go.append("        return nil, fmt.Errorf(\"failed to read response body: %w\", err)\n");
        go.append("    }\n");
        go.append("    \n");
        go.append("    fmt.Println(\"Response Body:\")\n");
        go.append("    var prettyJSON bytes.Buffer\n");
        go.append("    if err := json.Indent(&prettyJSON, body, \"\", \"  \"); err == nil {\n");
        go.append("        fmt.Println(prettyJSON.String())\n");
        go.append("    } else {\n");
        go.append("        fmt.Println(string(body))\n");
        go.append("    }\n");
        go.append("    \n");
        go.append("    return resp, nil\n");
        go.append("}\n\n");

        go.append("func main() {\n");
        go.append("    client := NewApiClient()\n");
        go.append("    \n");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            go.append("    // Set query parameters\n");
            go.append("    queryParams := map[string]string{\n");
            for (ApiParameterEntity param : api.getParameters()) {
                if ("query".equals(param.getParameterType()) && param.getExample() != null) {
                    go.append("        \"").append(param.getKey()).append("\": \"");
                    go.append(param.getExample()).append("\",\n");
                }
            }
            go.append("    }\n");
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            go.append("    // Set request body\n");
            go.append("    requestBody := ").append(api.getRequestConfig().getSample()).append("\n");
            go.append("    \n");
        }

        go.append("    // Make the API call\n");
        go.append("    resp, err := client.CallApi(");

        if (api.getParameters() != null && !api.getParameters().isEmpty()) {
            go.append("queryParams");
        }

        if (!"GET".equals(api.getHttpMethod()) && api.getRequestConfig() != null && api.getRequestConfig().getSample() != null) {
            if (api.getParameters() != null && !api.getParameters().isEmpty()) {
                go.append(", ");
            }
            go.append("requestBody");
        }
        go.append(")\n");
        go.append("    \n");
        go.append("    if err != nil {\n");
        go.append("        fmt.Fprintf(os.Stderr, \"Error calling API: %v\\n\", err)\n");
        go.append("        os.Exit(1)\n");
        go.append("    }\n");
        go.append("    defer resp.Body.Close()\n");
        go.append("}\n");

        return go.toString();
    }

    // Helper methods for string escaping and formatting
    private String escapeJavaScriptString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }

    private String toClassName(String input) {
        if (input == null || input.isEmpty()) return "Api";
        String[] parts = input.split("[\\s_-]+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }

    private String toGoFieldName(String input) {
        if (input == null || input.isEmpty()) return "Field";
        String[] parts = input.split("[\\s_-]+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.toString();
    }

    private String inferGoType(String value) {
        value = value.trim();
        if (value.matches("-?\\d+")) {
            return "int";
        } else if (value.matches("-?\\d*\\.\\d+")) {
            return "float64";
        } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "bool";
        } else {
            return "string";
        }
    }

    // Simple enum for HTTP methods
    private enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

        public static HttpMethod fromString(String method) {
            try {
                return HttpMethod.valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                return GET;
            }
        }
    }
}