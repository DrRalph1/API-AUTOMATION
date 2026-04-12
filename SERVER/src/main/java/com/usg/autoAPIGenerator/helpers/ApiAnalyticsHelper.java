// helpers/ApiAnalyticsHelper.java
package com.usg.autoAPIGenerator.helpers;

import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiAnalyticsDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.ApiDetailsResponseDTO;
import com.usg.autoAPIGenerator.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.autoAPIGenerator.entities.postgres.apiGenerationEngine.GeneratedApiEntity;
import com.usg.autoAPIGenerator.repositories.apiGenerationEngine.ApiExecutionLogRepository;

import java.time.LocalDateTime;

/**
 * Interface for API analytics and helper methods
 * Different database implementations can have different ways of handling these
 */
public interface ApiAnalyticsHelper {

    /**
     * Add average execution time to response
     */
    void addAverageExecutionTime(GeneratedApiResponseDTO response, Double avgTime);

    /**
     * Add average execution time to details response
     */
    void addAverageExecutionTimeToDetails(ApiDetailsResponseDTO response, Double avgTime);

    /**
     * Build API analytics
     */
    ApiAnalyticsDTO buildApiAnalytics(ApiExecutionLogRepository logRepository,
                                      String apiId,
                                      LocalDateTime startDate,
                                      LocalDateTime endDate);

    /**
     * Get code base request ID from API
     */
    String getCodeBaseRequestId(GeneratedApiEntity api);

    /**
     * Get collections collection ID from API
     */
    String getCollectionsCollectionId(GeneratedApiEntity api);

    /**
     * Get documentation collection ID from API
     */
    String getDocumentationCollectionId(GeneratedApiEntity api);
}