package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ClientIpHelper {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private LoggerUtil loggerUtil;

    @Autowired
    private ErrorHandlingHelper errorHandlingHelper;

    @Autowired
    public ClientIpHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public boolean validateSwaggerClientIp(String apiKey, String apiSecret) {

        // ---------------------------------
        // Validate missing API key / secret
        // ---------------------------------
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            loggerUtil.log("api-automation", "[Swagger Client IP Validation] ❌ Missing API key or secret.");
            System.out.println("[Swagger Client IP Validation] ❌ Missing API key or secret.");
            return false;
        }

        // Get server IP
        String serverIp;
        try {
            serverIp = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            serverIp = "unknown";
            loggerUtil.log("api-automation", "[Swagger Client IP Validation] ⚠ Unable to resolve server IP: " + ex.getMessage());
            System.out.println("[Swagger Client IP Validation] ⚠ Unable to resolve server IP: " + ex.getMessage());
        }

        try {
            // Query API key & secret + server IP
            String query = "SELECT COUNT(*) FROM tb_api_validation " +
                    "WHERE api_key = ? AND api_secret = ? AND server_ip = ? AND micro_service_id = 'swagger-ui'";

            Integer count = jdbcTemplate.queryForObject(query, Integer.class, apiKey, apiSecret, serverIp);

            boolean isValid = count != null && count > 0;

            if (isValid) {
                loggerUtil.log("api-automation", "[Swagger Client IP Validation] ✅ Client IP validated successfully | apiKey=" + apiKey + " | IP=" + serverIp);
                System.out.println("[Swagger Client IP Validation] ✅ Client IP validated successfully | apiKey=" + apiKey + " | IP=" + serverIp);
            } else {
                System.out.println("query::::::" + query);
                System.out.println("serverIP:::::::" + serverIp);
                System.out.println("apiKey:::::::" + apiKey);
                System.out.println("apiSecret:::::::" + apiSecret);
                loggerUtil.log("api-automation", "[Swagger Client IP Validation] ❌ Invalid client IP | apiKey=" + apiKey + " | IP=" + serverIp);
                System.out.println("[Swagger Client IP Validation] ❌ Invalid client IP | apiKey=" + apiKey + " | IP=" + serverIp);
            }

            return isValid;

        } catch (Exception e) {
            loggerUtil.log("api-automation", "[Swagger Client IP Validation] 🚨 Error validating client IP | apiKey=" + apiKey +
                    " | IP=" + serverIp + " | error=" + e.getMessage());
            System.out.println("[Swagger Client IP Validation] 🚨 Error validating client IP | apiKey=" + apiKey +
                    " | IP=" + serverIp + " | error=" + e.getMessage());
            return false;
        }
    }


    public boolean validateAPIClientIp(String apiKey, String apiSecret) {

        // ---------------------------------
        // Validate missing API key / secret
        // ---------------------------------
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            loggerUtil.log("api-automation", "[API Client IP Validation] ❌ Missing API key or secret.");
            System.out.println("[API Client IP Validation] ❌ Missing API key or secret.");
            return false;
        }

        // Get server IP
        String serverIp;
        try {
            serverIp = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            serverIp = "unknown";
            loggerUtil.log("api-automation", "[API Client IP Validation] ⚠ Unable to resolve server IP: " + ex.getMessage());
            System.out.println("[API Client IP Validation] ⚠ Unable to resolve server IP: " + ex.getMessage());
        }

        try {
            // Query API key & secret + server IP
            String query = "SELECT COUNT(*) FROM tb_api_validation " +
                    "WHERE api_key = ? AND api_secret = ? AND server_ip = ? AND micro_service_id = 'api-automation'";

            Integer count = jdbcTemplate.queryForObject(query, Integer.class, apiKey, apiSecret, serverIp);

            boolean isValid = count != null && count > 0;

            if (isValid) {
                loggerUtil.log("api-automation", "[API Client IP Validation] ✅ Client IP validated successfully | apiKey=" + apiKey + " | IP=" + serverIp);
                System.out.println("[API Client IP Validation] ✅ Client IP validated successfully | apiKey=" + apiKey + " | IP=" + serverIp);
            } else {
                loggerUtil.log("api-automation", "[API Client IP Validation] ❌ Invalid client IP | apiKey=" + apiKey + " | IP=" + serverIp);
                System.out.println("[API Client IP Validation] ❌ Invalid client IP | apiKey=" + apiKey + " | IP=" + serverIp);
            }

            return isValid;

        } catch (Exception e) {
            loggerUtil.log("api-automation", "[API Client IP Validation] 🚨 Error validating client IP | apiKey=" + apiKey +
                    " | IP=" + serverIp + " | error=" + e.getMessage());
            System.out.println("[API Client IP Validation] 🚨 Error validating client IP | apiKey=" + apiKey +
                    " | IP=" + serverIp + " | error=" + e.getMessage());
            return false;
        }
    }

}
