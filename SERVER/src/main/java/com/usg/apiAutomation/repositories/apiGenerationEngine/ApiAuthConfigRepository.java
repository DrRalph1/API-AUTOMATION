package com.usg.apiAutomation.repositories.apiGenerationEngine;

import com.usg.apiAutomation.entities.postgres.apiGenerationEngine.ApiAuthConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface ApiAuthConfigRepository extends JpaRepository<ApiAuthConfigEntity, String> {

    // Find auth config by API ID (since it's a OneToOne relationship)
    Optional<ApiAuthConfigEntity> findByGeneratedApiId(String apiId);

    // Check if API has auth config
    boolean existsByGeneratedApiId(String apiId);

    // Delete auth config by API ID
    void deleteByGeneratedApiId(String apiId);

    // Custom query to fetch only non-sensitive fields if needed
    @Query("SELECT new map(" +
            "a.id as id, " +
            "a.authType as authType, " +
            "a.apiKeyHeader as apiKeyHeader, " +
            "a.apiKeyLocation as apiKeyLocation, " +
            "a.apiKeyPrefix as apiKeyPrefix, " +
            "a.basicUsername as basicUsername, " +
            "a.basicRealm as basicRealm, " +
            "a.jwtIssuer as jwtIssuer, " +
            "a.jwtAudience as jwtAudience, " +
            "a.jwtExpiration as jwtExpiration, " +
            "a.jwtAlgorithm as jwtAlgorithm, " +
            "a.oauthClientId as oauthClientId, " +
            "a.oauthTokenUrl as oauthTokenUrl, " +
            "a.oauthAuthUrl as oauthAuthUrl, " +
            "a.oauthScopes as oauthScopes, " +
            "a.requiredRoles as requiredRoles, " +
            "a.customAuthFunction as customAuthFunction, " +
            "a.validateSession as validateSession, " +
            "a.checkObjectPrivileges as checkObjectPrivileges, " +
            "a.ipWhitelist as ipWhitelist, " +
            "a.rateLimitRequests as rateLimitRequests, " +
            "a.rateLimitPeriod as rateLimitPeriod, " +
            "a.enableRateLimiting as enableRateLimiting, " +
            "a.auditLevel as auditLevel, " +
            "a.corsOrigins as corsOrigins, " +
            "a.corsCredentials as corsCredentials) " +
            "FROM ApiAuthConfigEntity a " +
            "WHERE a.generatedApi.id = :apiId")
    Optional<Map<String, Object>> findAuthConfigMetadataByApiId(@Param("apiId") String apiId);
}