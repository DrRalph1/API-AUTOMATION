package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.utils.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ApiKeyNSecretHelper {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private LoggerUtil loggerUtil;

    @Autowired
    private ErrorHandlingHelper errorHandlingHelper;

    // Constructor injection for DataSource -> JdbcTemplate
    @Autowired
    public ApiKeyNSecretHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public boolean validateSwaggerCredentials(String apiKey, String apiSecret) {

        // ---------------------------------
        // Validate missing API key / secret
        // ---------------------------------
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            loggerUtil.log("api-automation", "[Swagger Validation] ❌ Missing API key or secret.");
            System.out.println("[Swagger Validation] ❌ Missing API key or secret.");
            return false;
        }

        try {
            // Query API key & secret for swagger-ui microservice
            String query = "SELECT COUNT(*) FROM tb_sys_api_validation " +
                    "WHERE api_key = ? AND api_secret = ? AND micro_service_id = 'swagger-ui'";

            Integer count = jdbcTemplate.queryForObject(query, Integer.class, apiKey, apiSecret);

            boolean isValid = count != null && count > 0;

            if (isValid) {
                loggerUtil.log("api-automation", "[Swagger Validation] ✅ API key & secret validated successfully | apiKey=" + apiKey);
                System.out.println("[Swagger Validation] ✅ API key & secret validated successfully | apiKey=" + apiKey);
            } else {
                loggerUtil.log("api-automation", "[Swagger Validation] ❌ Invalid API key or secret | apiKey=" + apiKey);
                System.out.println("[Swagger Validation] ❌ Invalid API key or secret | apiKey=" + apiKey);
            }

            return isValid;

        } catch (Exception e) {
            loggerUtil.log("api-automation", "[Swagger Validation] 🚨 Error during validation | apiKey=" + apiKey +
                    " | error=" + e.getMessage());
            System.out.println("[Swagger Validation] 🚨 Error during validation | apiKey=" + apiKey +
                    " | error=" + e.getMessage());
            return false;
        }
    }


    public boolean validateApiCredentials(String apiKey, String apiSecret) {

        // ---------------------------------
        // Validate missing API key / secret
        // ---------------------------------
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            loggerUtil.log("api-automation", "[API Validation] ❌ Missing API key or secret.");
            System.out.println("[API Validation] ❌ Missing API key or secret.");
            return false;
        }

        try {
            // Query API key & secret for api-automation microservice
            String query = "SELECT COUNT(*) FROM tb_sys_api_validation " +
                    "WHERE api_key = ? AND api_secret = ? AND micro_service_id = 'api-automation'";

            Integer count = jdbcTemplate.queryForObject(query, Integer.class, apiKey, apiSecret);

            boolean isValid = count != null && count > 0;

            if (isValid) {
                loggerUtil.log("api-automation", "[API Validation] ✅ API key & secret validated successfully | apiKey=" + apiKey);
                System.out.println("[API Validation] ✅ API key & secret validated successfully | apiKey=" + apiKey);
            } else {
                loggerUtil.log("api-automation", "[API Validation] ❌ Invalid API key or secret | apiKey=" + apiKey);
                System.out.println("[API Validation] ❌ Invalid API key or secret | apiKey=" + apiKey);
            }

            return isValid;

        } catch (Exception e) {
            loggerUtil.log("api-automation", "[API Validation] 🚨 Error during validation | apiKey=" + apiKey +
                    " | error=" + e.getMessage());
            System.out.println("[API Validation] 🚨 Error during validation | apiKey=" + apiKey +
                    " | error=" + e.getMessage());
            return false;
        }
    }
}
