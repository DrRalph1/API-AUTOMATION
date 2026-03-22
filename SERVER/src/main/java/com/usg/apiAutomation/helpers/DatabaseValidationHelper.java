// helpers/DatabaseValidationHelper.java
package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.dtos.apiGenerationEngine.ApiSourceObjectDTO;
import com.usg.apiAutomation.dtos.apiGenerationEngine.CollectionInfoDTO;
import com.usg.apiAutomation.repositories.apiGenerationEngine.GeneratedAPIRepository;

import java.util.Map;
import java.util.function.Function;

public interface DatabaseValidationHelper {

    void validateApiCodeUniqueness(GeneratedAPIRepository repository, String apiCode);

    void validateApiCodeUniquenessOnUpdate(GeneratedAPIRepository repository, String oldCode, String newCode);

    CollectionInfoDTO validateAndGetCollectionInfo(CollectionInfoDTO collectionInfo);

    void validateApiStatus(String status);

    Map<String, Object> validateSourceObject(
            Object schemaService,
            ApiSourceObjectDTO sourceObject,
            Function<ApiSourceObjectDTO, Map<String, Object>> detailsProvider);
}