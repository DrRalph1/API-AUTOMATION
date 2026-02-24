package com.usg.apiAutomation.services;

import com.usg.apiAutomation.dtos.documentation.*;
import com.usg.apiAutomation.entities.postgres.documentation.*;
import com.usg.apiAutomation.repositories.postgres.documentation.*;
import com.usg.apiAutomation.utils.LoggerUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentationService {

    private final LoggerUtil loggerUtil;
    private final ObjectMapper objectMapper;

    @PersistenceContext(unitName = "postgres")
    private EntityManager entityManager;

    // Repositories
    private final APICollectionRepository collectionRepository;
    private final FolderRepository folderRepository;
    private final APIEndpointRepository endpointRepository;
    private final HeaderRepository headerRepository;
    private final ParameterRepository parameterRepository;
    private final ResponseExampleRepository responseExampleRepository;
    private final CodeExampleRepository codeExampleRepository;
    private final ChangelogRepository changelogRepository;
    private final EnvironmentRepository environmentRepository;
    private final NotificationRepository notificationRepository;
    private final MockServerRepository mockServerRepository;
    private final MockEndpointRepository mockEndpointRepository;
    private final PublishedDocumentationRepository publishedDocumentationRepository;
    private final DocumentationSettingsRepository settingsRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    @PostConstruct
    public void init() {
        log.info("DocumentationService initialized with database repositories");
    }

    // ========== API COLLECTION METHODS ==========

    public APICollectionResponseDTO getAPICollections(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting API collections for user: {}", requestId, performedBy);
            loggerUtil.log("documentation", "Getting API collections for user: " + performedBy);

            // Get collections from database
            List<APICollectionEntity> collections = collectionRepository.findAll();

            // If no collections for this user, get all collections (or you can return empty)
            if (collections.isEmpty()) {
                collections = collectionRepository.findAll();
            }

            List<APICollectionDTO> collectionDTOs = collections.stream()
                    .map(this::convertToCollectionDto)
                    .collect(Collectors.toList());

            APICollectionResponseDTO response = new APICollectionResponseDTO(collectionDTOs);

            log.info("Request ID: {}, Retrieved {} API collections", requestId, collectionDTOs.size());
            return response;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving API collections: {}", requestId, e.getMessage(), e);
            return new APICollectionResponseDTO(Collections.emptyList());
        }
    }

    public APICollectionDTO getAPICollectionById(String requestId, String collectionId) {
        try {
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));
            return convertToCollectionDto(collection);
        } catch (Exception e) {
            log.error("Error retrieving collection by id: {}", e.getMessage());
            throw new RuntimeException("Collection not found");
        }
    }

    public APICollectionDTO createAPICollection(String requestId, APICollectionDTO collectionDto, String performedBy) {
        try {
            APICollectionEntity collection = new APICollectionEntity();
            collection.setName(collectionDto.getName());
            collection.setDescription(collectionDto.getDescription());
            collection.setVersion(collectionDto.getVersion());
            collection.setOwner(performedBy);
            collection.setType(collectionDto.getType());
            collection.setFavorite(collectionDto.isFavorite());
            collection.setExpanded(collectionDto.isExpanded());
            collection.setColor(collectionDto.getColor());
            collection.setStatus(collectionDto.getStatus());
            collection.setBaseUrl(collectionDto.getBaseUrl());
            collection.setTags(collectionDto.getTags());
            collection.setCreatedBy(performedBy);
            collection.setUpdatedBy(performedBy);

            APICollectionEntity saved = collectionRepository.save(collection);

            log.info("Created new API collection: {} with ID: {}", saved.getName(), saved.getId());
            return convertToCollectionDto(saved);

        } catch (Exception e) {
            log.error("Error creating API collection: {}", e.getMessage());
            throw new RuntimeException("Failed to create collection");
        }
    }

    public APICollectionDTO updateAPICollection(String requestId, String collectionId, APICollectionDTO collectionDto, String performedBy) {
        try {
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            collection.setName(collectionDto.getName());
            collection.setDescription(collectionDto.getDescription());
            collection.setVersion(collectionDto.getVersion());
            collection.setType(collectionDto.getType());
            collection.setFavorite(collectionDto.isFavorite());
            collection.setExpanded(collectionDto.isExpanded());
            collection.setColor(collectionDto.getColor());
            collection.setStatus(collectionDto.getStatus());
            collection.setBaseUrl(collectionDto.getBaseUrl());
            collection.setTags(collectionDto.getTags());
            collection.setUpdatedBy(performedBy);

            APICollectionEntity updated = collectionRepository.save(collection);

            log.info("Updated API collection: {} with ID: {}", updated.getName(), updated.getId());
            return convertToCollectionDto(updated);

        } catch (Exception e) {
            log.error("Error updating API collection: {}", e.getMessage());
            throw new RuntimeException("Failed to update collection");
        }
    }

    public void deleteAPICollection(String requestId, String collectionId, String performedBy) {
        try {
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            collectionRepository.delete(collection);

            log.info("Deleted API collection with ID: {}", collectionId);

        } catch (Exception e) {
            log.error("Error deleting API collection: {}", e.getMessage());
            throw new RuntimeException("Failed to delete collection");
        }
    }

    // ========== FOLDER METHODS ==========

    public List<FolderDTO> getFolders(String requestId, String collectionId) {
        try {
            List<FolderEntity> folders = folderRepository.findByCollectionId(collectionId);
            return folders.stream()
                    .map(this::convertToFolderDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving folders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public FolderDTO createFolder(String requestId, String collectionId, FolderDTO folderDTO, String performedBy) {
        try {
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            FolderEntity folder = new FolderEntity();
            folder.setName(folderDTO.getName());
            folder.setDescription(folderDTO.getDescription());
            folder.setCollection(collection);
            folder.setDisplayOrder(folderDTO.getDisplayOrder());
            folder.setCreatedBy(performedBy);
            folder.setUpdatedBy(performedBy);

            if (folderDTO.getParentFolderId() != null) {
                FolderEntity parent = folderRepository.findById(folderDTO.getParentFolderId())
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));
                folder.setParentFolder(parent);
            }

            FolderEntity saved = folderRepository.save(folder);

            // Update collection's folder count
            collection.setTotalFolders(collection.getTotalFolders() + 1);
            collectionRepository.save(collection);

            log.info("Created folder: {} in collection: {}", saved.getName(), collectionId);
            return convertToFolderDTO(saved);

        } catch (Exception e) {
            log.error("Error creating folder: {}", e.getMessage());
            throw new RuntimeException("Failed to create folder");
        }
    }

    // ========== API ENDPOINT METHODS ==========

    /**
     * Get folders for a specific collection
     * @param requestId The request ID for logging
     * @param collectionId The collection ID
     * @return List of FolderDTO objects
     */
    public List<FolderDTO> getAPIFolders(String requestId, String collectionId) {
        try {
            log.info("Request ID: {}, Getting folders for collection: {}", requestId, collectionId);

            // Check if collection exists
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));

            // Get folders from repository
            List<FolderEntity> folderEntities = folderRepository.findByCollectionId(collectionId);

            List<FolderDTO> folderDTOs = folderEntities.stream()
                    .map(this::convertToFolderDTO)
                    .collect(Collectors.toList());

            // Add endpoint counts to each folder
            folderDTOs.forEach(folder -> {
                int endpointCount = folderRepository.countEndpointsInFolder(folder.getId());
                folder.setEndpointCount(endpointCount);
            });

            log.info("Request ID: {}, Retrieved {} folders for collection: {}",
                    requestId, folderDTOs.size(), collectionId);

            return folderDTOs;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving folders for collection {}: {}",
                    requestId, collectionId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve folders for collection: " + collectionId, e);
        }
    }

    /**
     * Get folders with hierarchy information (parent-child relationships)
     * @param requestId The request ID for logging
     * @param collectionId The collection ID
     * @return List of FolderDTO objects with parent-child relationships preserved
     */
    public List<FolderDTO> getAPIFoldersWithHierarchy(
            String requestId,
            String collectionId) {

        try {
            log.info("Request ID: {}, Getting folder hierarchy for collection: {}", requestId, collectionId);

            // Check if collection exists
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));

            // Get all folders for the collection
            List<FolderEntity> allFolders = folderRepository.findByCollectionId(collectionId);

            // Create a map for quick lookup
            Map<String, FolderDTO> folderMap = new HashMap<>();
            List<FolderDTO> rootFolders = new ArrayList<>();

            // First pass: create all DTOs
            for (FolderEntity entity : allFolders) {
                FolderDTO dto = convertToFolderDTO(entity);
                int endpointCount = folderRepository.countEndpointsInFolder(entity.getId());
                dto.setEndpointCount(endpointCount);
                dto.setSubFolders(new ArrayList<>());
                folderMap.put(dto.getId(), dto);
            }

            // Second pass: establish parent-child relationships
            for (FolderEntity entity : allFolders) {
                FolderDTO dto = folderMap.get(entity.getId());

                if (entity.getParentFolder() != null && folderMap.containsKey(entity.getParentFolder().getId())) {
                    FolderDTO parentDto =
                            folderMap.get(entity.getParentFolder().getId());
                    parentDto.getSubFolders().add(dto);
                } else {
                    rootFolders.add(dto);
                }
            }

            log.info("Request ID: {}, Retrieved folder hierarchy for collection: {} with {} root folders",
                    requestId, collectionId, rootFolders.size());

            return rootFolders;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving folder hierarchy for collection {}: {}",
                    requestId, collectionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get root folders only (folders with no parent)
     * @param requestId The request ID for logging
     * @param collectionId The collection ID
     * @return List of root FolderDTO objects
     */
    public List<FolderDTO> getRootFolders(String requestId, String collectionId) {
        try {
            log.info("Request ID: {}, Getting root folders for collection: {}", requestId, collectionId);

            List<FolderEntity> rootFolderEntities = folderRepository.findRootFoldersByCollectionId(collectionId);

            List<FolderDTO> rootFolderDTOs = rootFolderEntities.stream()
                    .map(this::convertToFolderDTO)
                    .collect(Collectors.toList());

            // Add endpoint counts
            rootFolderDTOs.forEach(folder -> {
                int endpointCount = folderRepository.countEndpointsInFolder(folder.getId());
                folder.setEndpointCount(endpointCount);
            });

            log.info("Request ID: {}, Retrieved {} root folders for collection: {}",
                    requestId, rootFolderDTOs.size(), collectionId);

            return rootFolderDTOs;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving root folders for collection {}: {}",
                    requestId, collectionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get subfolders for a specific parent folder
     * @param requestId The request ID for logging
     * @param parentFolderId The parent folder ID
     * @return List of child FolderDTO objects
     */
    public List<FolderDTO> getSubFolders(String requestId, String parentFolderId) {
        try {
            log.info("Request ID: {}, Getting subfolders for parent folder: {}", requestId, parentFolderId);

            List<FolderEntity> subFolderEntities = folderRepository.findByParentFolderId(parentFolderId);

            List<FolderDTO> subFolderDTOs = subFolderEntities.stream()
                    .map(this::convertToFolderDTO)
                    .collect(Collectors.toList());

            // Add endpoint counts
            subFolderDTOs.forEach(folder -> {
                int endpointCount = folderRepository.countEndpointsInFolder(folder.getId());
                folder.setEndpointCount(endpointCount);
            });

            log.info("Request ID: {}, Retrieved {} subfolders for parent folder: {}",
                    requestId, subFolderDTOs.size(), parentFolderId);

            return subFolderDTOs;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving subfolders for parent folder {}: {}",
                    requestId, parentFolderId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public APIEndpointResponseDTO getAPIEndpoints(String requestId, HttpServletRequest req, String performedBy,
                                                  String collectionId, String folderId) {
        try {
            log.info("Request ID: {}, Getting API endpoints for collection: {}, folder: {}",
                    requestId, collectionId, folderId);

            List<APIEndpointEntity> endpoints = new ArrayList<>();

            if (folderId != null && !folderId.isEmpty()) {
                log.debug("Request ID: {}, Using native query for folder: {}", requestId, folderId);

                try {
                    // Use native query with a custom mapper to handle JSON issues
                    String sql = "SELECT * FROM tb_doc_api_endpoints WHERE folder_id = ?";
                    endpoints = entityManager.createNativeQuery(sql, APIEndpointEntity.class)
                            .setParameter(1, folderId)
                            .getResultList();
                    log.debug("Request ID: {}, Native query returned {} endpoints", requestId, endpoints.size());
                } catch (Exception e) {
                    log.error("Request ID: {}, Native query failed: {}", requestId, e.getMessage());

                    // Last resort: Try to get count only
                    try {
                        String countSql = "SELECT COUNT(*) FROM tb_doc_api_endpoints WHERE folder_id = ?";
                        BigInteger count = (BigInteger) entityManager.createNativeQuery(countSql)
                                .setParameter(1, folderId)
                                .getSingleResult();
                        log.info("Request ID: {}, Folder has {} endpoints but couldn't retrieve them due to JSON error",
                                requestId, count);
                    } catch (Exception ex) {
                        log.error("Request ID: {}, Count query also failed: {}", requestId, ex.getMessage());
                    }

                    return new APIEndpointResponseDTO(Collections.emptyList(), collectionId, folderId, 0);
                }
            } else {
                String sql = "SELECT * FROM tb_doc_api_endpoints WHERE collection_id = ?";
                endpoints = entityManager.createNativeQuery(sql, APIEndpointEntity.class)
                        .setParameter(1, collectionId)
                        .getResultList();
            }

            // Convert to DTOs safely, handling JSON errors per endpoint
            List<APIEndpointDTO> endpointDTOs = new ArrayList<>();
            for (APIEndpointEntity endpoint : endpoints) {
                try {
                    APIEndpointDTO dto = convertToEndpointDtoSafely(endpoint);
                    if (dto != null) {
                        endpointDTOs.add(dto);
                    }
                } catch (Exception e) {
                    log.error("Error converting endpoint {}: {}", endpoint.getId(), e.getMessage());
                    // Still add a basic DTO with minimal info
                    APIEndpointDTO basicDto = new APIEndpointDTO();
                    basicDto.setId(endpoint.getId());
                    basicDto.setName(endpoint.getName());
                    basicDto.setMethod(endpoint.getMethod());
                    basicDto.setUrl(endpoint.getUrl());
                    basicDto.setDescription(endpoint.getDescription() + " (Rate limit data corrupted)");
                    endpointDTOs.add(basicDto);
                }
            }

            APIEndpointResponseDTO response = new APIEndpointResponseDTO(
                    endpointDTOs,
                    collectionId,
                    folderId,
                    endpointDTOs.size()
            );

            log.info("Request ID: {}, Retrieved {} API endpoints", requestId, endpointDTOs.size());
            return response;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving API endpoints: {}", requestId, e.getMessage(), e);
            return new APIEndpointResponseDTO(Collections.emptyList(), collectionId, folderId, 0);
        }
    }

    // Add this safe conversion method
    private APIEndpointDTO convertToEndpointDtoSafely(APIEndpointEntity entity) {
        APIEndpointDTO dto = new APIEndpointDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setTags(entity.getTags() != null ? new ArrayList<>(entity.getTags()) : new ArrayList<>());

        if (entity.getUpdatedAt() != null) {
            dto.setLastModified(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        dto.setRequiresAuth(entity.isRequiresAuth());
        dto.setDeprecated(entity.isDeprecated());

        // Handle folder ID safely
        if (entity.getFolder() != null) {
            try {
                dto.setFolder(entity.getFolder().getId());
            } catch (Exception e) {
                log.warn("Could not get folder ID for endpoint {}: {}", entity.getId(), e.getMessage());
            }
        }

        // Handle collection ID safely
        if (entity.getCollection() != null) {
            try {
                dto.setCollectionId(entity.getCollection().getId());
            } catch (Exception e) {
                log.warn("Could not get collection ID for endpoint {}: {}", entity.getId(), e.getMessage());
            }
        }

        dto.setCategory(entity.getCategory());

        // Handle rate limit safely - if it fails, set null
        try {
            if (entity.getRateLimit() != null) {
                dto.setRateLimit(entity.getRateLimit());
            }
        } catch (Exception e) {
            log.warn("Failed to parse rate limit for endpoint {}: {}", entity.getId(), e.getMessage());
            // Don't set rate limit - leave it null
        }

        return dto;
    }



    // Add this helper method
    private long countEndpointsByCollectionId(String collectionId) {
        try {
            return endpointRepository.countByCollectionId(collectionId);
        } catch (Exception e) {
            log.error("Error counting endpoints: {}", e.getMessage());
            return 0;
        }
    }

    public EndpointDetailResponseDTO getEndpointDetails(String requestId, HttpServletRequest req, String performedBy,
                                                        String collectionId, String endpointId) {
        try {
            log.info("Request ID: {}, Getting endpoint details for: {}", requestId, endpointId);

            APIEndpointEntity endpoint = endpointRepository.findById(endpointId)
                    .orElseThrow(() -> new RuntimeException("Endpoint not found"));

            EndpointDetailResponseDTO details = convertToEndpointDetailResponseDTO(endpoint);

            log.info("Request ID: {}, Retrieved details for endpoint: {}", requestId, endpointId);
            return details;

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving endpoint details: {}", requestId, e.getMessage(), e);
            return createFallbackEndpointDetails(endpointId);
        }
    }

    public APIEndpointDTO createEndpoint(String requestId, String collectionId, APIEndpointDTO endpointDto, String performedBy) {
        try {
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            APIEndpointEntity endpoint = new APIEndpointEntity();
            endpoint.setName(endpointDto.getName());
            endpoint.setMethod(endpointDto.getMethod());
            endpoint.setUrl(endpointDto.getUrl());
            endpoint.setDescription(endpointDto.getDescription());
            endpoint.setCollection(collection);
            endpoint.setRequiresAuth(endpointDto.getRequiresAuth());
            endpoint.setDeprecated(endpointDto.getDeprecated());
            endpoint.setRateLimit(endpointDto.getRateLimit());
            endpoint.setApiVersion(collection.getVersion());
            endpoint.setCategory(endpointDto.getCategory());
            endpoint.setTags(endpointDto.getTags());
            endpoint.setCreatedBy(performedBy);
            endpoint.setUpdatedBy(performedBy);
            endpoint.setLastModifiedBy(performedBy);

            if (endpointDto.getFolder() != null) {
                FolderEntity folder = folderRepository.findById(endpointDto.getFolder())
                        .orElse(null);
                endpoint.setFolder(folder);
            }

            // Set request body example if provided
            if (endpointDto.getRequestBody() != null) {
                try {
                    Map<String, Object> bodyMap = objectMapper.readValue(endpointDto.getRequestBody(), Map.class);
                    endpoint.setRequestBodyExample(bodyMap);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse request body example: {}", e.getMessage());
                }
            }

            APIEndpointEntity saved = endpointRepository.save(endpoint);

            // Update collection's endpoint count
            collection.setTotalEndpoints(collection.getTotalEndpoints() + 1);
            collectionRepository.save(collection);

            // Add headers if provided
            if (endpointDto.getHeaders() != null) {
                for (HeaderDTO headerDTO : endpointDto.getHeaders()) {
                    HeaderEntity header = new HeaderEntity();
                    header.setKey(headerDTO.getKey());
                    header.setValue(headerDTO.getValue());
                    header.setDescription(headerDTO.getDescription());
                    header.setRequired(headerDTO.isRequired());
                    header.setEndpoint(saved);
                    headerRepository.save(header);
                }
            }

            log.info("Created endpoint: {} in collection: {}", saved.getName(), collectionId);
            return convertToEndpointDto(saved);

        } catch (Exception e) {
            log.error("Error creating endpoint: {}", e.getMessage());
            throw new RuntimeException("Failed to create endpoint");
        }
    }

    public APIEndpointDTO updateEndpoint(String requestId, String endpointId, APIEndpointDTO endpointDto, String performedBy) {
        try {
            APIEndpointEntity endpoint = endpointRepository.findById(endpointId)
                    .orElseThrow(() -> new RuntimeException("Endpoint not found"));

            endpoint.setName(endpointDto.getName());
            endpoint.setMethod(endpointDto.getMethod());
            endpoint.setUrl(endpointDto.getUrl());
            endpoint.setDescription(endpointDto.getDescription());
            endpoint.setRequiresAuth(endpointDto.getRequiresAuth());
            endpoint.setDeprecated(endpointDto.getDeprecated());
            endpoint.setRateLimit(endpointDto.getRateLimit());
            endpoint.setCategory(endpointDto.getCategory());
            endpoint.setTags(endpointDto.getTags());
            endpoint.setUpdatedBy(performedBy);
            endpoint.setLastModifiedBy(performedBy);

            APIEndpointEntity saved = endpointRepository.save(endpoint);

            log.info("Updated endpoint: {}", endpointId);
            return convertToEndpointDto(saved);

        } catch (Exception e) {
            log.error("Error updating endpoint: {}", e.getMessage());
            throw new RuntimeException("Failed to update endpoint");
        }
    }

    public void deleteEndpoint(String requestId, String endpointId, String performedBy) {
        try {
            APIEndpointEntity endpoint = endpointRepository.findById(endpointId)
                    .orElseThrow(() -> new RuntimeException("Endpoint not found"));

            String collectionId = endpoint.getCollection().getId();

            // Delete related entities
            headerRepository.deleteByEndpointId(endpointId);
            parameterRepository.deleteByEndpointId(endpointId);
            responseExampleRepository.deleteByEndpointId(endpointId);

            endpointRepository.delete(endpoint);

            // Update collection's endpoint count
            APICollectionEntity collection = endpoint.getCollection();
            collection.setTotalEndpoints(collection.getTotalEndpoints() - 1);
            collectionRepository.save(collection);

            log.info("Deleted endpoint: {}", endpointId);

        } catch (Exception e) {
            log.error("Error deleting endpoint: {}", e.getMessage());
            throw new RuntimeException("Failed to delete endpoint");
        }
    }

    // ========== CODE EXAMPLES METHODS ==========

    public CodeExampleResponseDTO getCodeExamples(String requestId, HttpServletRequest req, String performedBy,
                                                  String endpointId, String language) {
        try {
            log.info("Request ID: {}, Getting code examples for endpoint: {}, language: {}",
                    requestId, endpointId, language);

            APIEndpointEntity endpoint = endpointRepository.findById(endpointId)
                    .orElseThrow(() -> new RuntimeException("Endpoint not found"));

            List<CodeExampleEntity> examples;
            if (language != null && !language.isEmpty()) {
                examples = codeExampleRepository.findByEndpointIdAndLanguage(endpointId, language)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            } else {
                examples = codeExampleRepository.findByEndpointId(endpointId);
            }

            if (examples.isEmpty()) {
                // Generate default example if none exists
                return generateDefaultCodeExample(endpointId, endpoint, language);
            }

            CodeExampleEntity example = examples.get(0);
            return new CodeExampleResponseDTO(
                    example.getLanguage(),
                    endpointId,
                    example.getCode(),
                    example.getDescription()
            );

        } catch (Exception e) {
            log.error("Error retrieving code examples: {}", e.getMessage());
            return new CodeExampleResponseDTO(language, endpointId, "",
                    "Error retrieving code examples: " + e.getMessage());
        }
    }

    public CodeExampleEntity createCodeExample(String requestId, String endpointId,
                                               CodeExampleResponseDTO exampleDTO, String performedBy) {
        try {
            APIEndpointEntity endpoint = endpointRepository.findById(endpointId)
                    .orElseThrow(() -> new RuntimeException("Endpoint not found"));

            CodeExampleEntity example = new CodeExampleEntity();
            example.setLanguage(exampleDTO.getLanguage());
            example.setCode(exampleDTO.getCodeExample());
            example.setDescription(exampleDTO.getDescription());
            example.setEndpoint(endpoint);
            example.setDefault(false);

            CodeExampleEntity saved = codeExampleRepository.save(example);
            log.info("Created code example for endpoint: {} in language: {}", endpointId, exampleDTO.getLanguage());
            return saved;

        } catch (Exception e) {
            log.error("Error creating code example: {}", e.getMessage());
            throw new RuntimeException("Failed to create code example");
        }
    }

    // ========== SEARCH METHODS ==========

    public SearchDocumentationResponseDTO searchDocumentation(String requestId, HttpServletRequest req, String performedBy,
                                                              String searchQuery, String searchType, int maxResults) {
        try {
            log.info("Request ID: {}, Searching documentation with query: {}, type: {}",
                    requestId, searchQuery, searchType);

            long startTime = System.currentTimeMillis();
            List<SearchResultDTO> results = new ArrayList<>();

            // Search in collections
            if (searchType == null || searchType.equals("all") || searchType.equals("collections")) {
                List<APICollectionEntity> collections = collectionRepository.searchCollections(searchQuery);
                results.addAll(collections.stream()
                        .limit(maxResults / 3)
                        .map(c -> convertToSearchResultDTO(c, searchQuery))
                        .collect(Collectors.toList()));
            }

            // Search in endpoints
            if (searchType == null || searchType.equals("all") || searchType.equals("endpoints")) {
                List<APIEndpointEntity> endpoints = endpointRepository.searchEndpoints(searchQuery);
                results.addAll(endpoints.stream()
                        .limit(maxResults / 3)
                        .map(e -> convertToSearchResultDTO(e, searchQuery))
                        .collect(Collectors.toList()));
            }

            // Limit total results
            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }

            // Save search history
            SearchHistoryEntity history = new SearchHistoryEntity();
            history.setQuery(searchQuery);
            history.setSearchType(searchType);
            history.setUserId(performedBy);
            history.setResultCount(results.size());
            history.setSearchTimeMs(System.currentTimeMillis() - startTime);
            searchHistoryRepository.save(history);

            log.info("Request ID: {}, Found {} search results for query: {}",
                    requestId, results.size(), searchQuery);

            return new SearchDocumentationResponseDTO(results, searchQuery, results.size());

        } catch (Exception e) {
            log.error("Request ID: {}, Error searching documentation: {}", requestId, e.getMessage(), e);
            return new SearchDocumentationResponseDTO(Collections.emptyList(), searchQuery, 0);
        }
    }

    public List<String> getPopularSearches(String userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            LocalDateTime since = LocalDateTime.now().minusDays(30);

            List<Object[]> topSearches = searchHistoryRepository.findTopSearchesByUser(userId, since, pageable);

            return topSearches.stream()
                    .map(obj -> (String) obj[0])
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting popular searches: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ========== PUBLISH DOCUMENTATION METHODS ==========

    public PublishDocumentationResponseDTO publishDocumentation(String requestId, HttpServletRequest req, String performedBy,
                                                                String collectionId, String title,
                                                                String visibility, String customDomain) {
        try {
            log.info("Request ID: {}, Publishing documentation for collection: {}", requestId, collectionId);

            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            // Deactivate any existing active publication
            publishedDocumentationRepository.findActiveByCollectionId(collectionId)
                    .ifPresent(existing -> {
                        existing.setActive(false);
                        publishedDocumentationRepository.save(existing);
                    });

            // Create new publication
            PublishedDocumentationEntity publication = new PublishedDocumentationEntity();
            publication.setCollection(collection);
            publication.setTitle(title != null ? title : collection.getName() + " Documentation");
            publication.setVisibility(visibility != null ? visibility : "private");
            publication.setCustomDomain(customDomain);
            publication.setPublishedBy(performedBy);
            publication.setVersion(collection.getVersion());
            publication.setActive(true);

            // Generate URL
            String baseUrl = customDomain != null ? "https://" + customDomain : "https://docs.fintech.com";
            String randomId = UUID.randomUUID().toString().substring(0, 8);
            String publishedUrl = baseUrl + "/view/" + randomId + "/" + collectionId;
            publication.setPublishedUrl(publishedUrl);

            PublishedDocumentationEntity saved = publishedDocumentationRepository.save(publication);

            log.info("Request ID: {}, Documentation published successfully with URL: {}", requestId, publishedUrl);

            return new PublishDocumentationResponseDTO(
                    saved.getPublishedUrl(),
                    collectionId,
                    "Documentation published successfully with " + visibility + " visibility"
            );

        } catch (Exception e) {
            log.error("Request ID: {}, Error publishing documentation: {}", requestId, e.getMessage(), e);
            return new PublishDocumentationResponseDTO("", collectionId,
                    "Error publishing documentation: " + e.getMessage());
        }
    }

    public List<PublishedDocumentationEntity> getPublishedDocumentation(String collectionId) {
        return publishedDocumentationRepository.findByCollectionId(collectionId);
    }

    public void unpublishDocumentation(String publicationId) {
        try {
            PublishedDocumentationEntity publication = publishedDocumentationRepository.findById(publicationId)
                    .orElseThrow(() -> new RuntimeException("Publication not found"));

            publication.setActive(false);
            publishedDocumentationRepository.save(publication);

            log.info("Unpublished documentation with ID: {}", publicationId);

        } catch (Exception e) {
            log.error("Error unpublishing documentation: {}", e.getMessage());
            throw new RuntimeException("Failed to unpublish documentation");
        }
    }

    // ========== ENVIRONMENT METHODS ==========

    public EnvironmentResponseDTO getEnvironments(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting environments for user: {}", requestId, performedBy);

            List<EnvironmentEntity> environments = environmentRepository.findByCreatedBy(performedBy);

            if (environments.isEmpty()) {
                environments = environmentRepository.findAll();
            }

            List<EnvironmentDTO> environmentDTOs = environments.stream()
                    .map(this::convertToEnvironmentDTO)
                    .collect(Collectors.toList());

            // Update last used for active environment
            environments.stream()
                    .filter(EnvironmentEntity::isActive)
                    .findFirst()
                    .ifPresent(env -> {
                        env.setLastUsed(LocalDateTime.now());
                        environmentRepository.save(env);
                    });

            log.info("Request ID: {}, Retrieved {} environments", requestId, environmentDTOs.size());
            return new EnvironmentResponseDTO(environmentDTOs);

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving environments: {}", requestId, e.getMessage(), e);
            return new EnvironmentResponseDTO(Collections.emptyList());
        }
    }

    public EnvironmentDTO createEnvironment(EnvironmentDTO environmentDto, String performedBy) {
        try {
            EnvironmentEntity environment = new EnvironmentEntity();
            environment.setName(environmentDto.getName());
            environment.setBaseUrl(environmentDto.getBaseUrl());
            environment.setActive(environmentDto.isActive());
            environment.setDescription(environmentDto.getDescription());
            environment.setApiKey(environmentDto.getApiKey());
            environment.setSecret(environmentDto.getSecret());
            environment.setVariables(environmentDto.getVariables());
            environment.setCreatedBy(performedBy);

            // If this is set as active, deactivate others
            if (environmentDto.isActive()) {
                deactivateOtherEnvironments(performedBy);
            }

            EnvironmentEntity saved = environmentRepository.save(environment);
            log.info("Created environment: {}", saved.getName());
            return convertToEnvironmentDTO(saved);

        } catch (Exception e) {
            log.error("Error creating environment: {}", e.getMessage());
            throw new RuntimeException("Failed to create environment");
        }
    }

    public EnvironmentDTO updateEnvironment(String environmentId, EnvironmentDTO environmentDto, String performedBy) {
        try {
            EnvironmentEntity environment = environmentRepository.findById(environmentId)
                    .orElseThrow(() -> new RuntimeException("Environment not found"));

            environment.setName(environmentDto.getName());
            environment.setBaseUrl(environmentDto.getBaseUrl());
            environment.setDescription(environmentDto.getDescription());
            environment.setApiKey(environmentDto.getApiKey());
            environment.setSecret(environmentDto.getSecret());
            environment.setVariables(environmentDto.getVariables());

            // Handle activation
            if (environmentDto.isActive() && !environment.isActive()) {
                deactivateOtherEnvironments(performedBy);
                environment.setActive(true);
            } else if (!environmentDto.isActive()) {
                environment.setActive(false);
            }

            EnvironmentEntity saved = environmentRepository.save(environment);
            log.info("Updated environment: {}", saved.getName());
            return convertToEnvironmentDTO(saved);

        } catch (Exception e) {
            log.error("Error updating environment: {}", e.getMessage());
            throw new RuntimeException("Failed to update environment");
        }
    }

    public void setActiveEnvironment(String environmentId, String performedBy) {
        try {
            deactivateOtherEnvironments(performedBy);

            EnvironmentEntity environment = environmentRepository.findById(environmentId)
                    .orElseThrow(() -> new RuntimeException("Environment not found"));

            environment.setActive(true);
            environment.setLastUsed(LocalDateTime.now());
            environmentRepository.save(environment);

            log.info("Set active environment: {}", environment.getName());

        } catch (Exception e) {
            log.error("Error setting active environment: {}", e.getMessage());
            throw new RuntimeException("Failed to set active environment");
        }
    }

    private void deactivateOtherEnvironments(String performedBy) {
        List<EnvironmentEntity> activeEnvs = environmentRepository.findActiveEnvironmentsByUser(performedBy);
        activeEnvs.forEach(env -> {
            env.setActive(false);
            environmentRepository.save(env);
        });
    }

    // ========== NOTIFICATION METHODS ==========

    public NotificationResponseDTO getNotifications(String requestId, HttpServletRequest req, String performedBy) {
        try {
            log.info("Request ID: {}, Getting notifications for user: {}", requestId, performedBy);

            List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(performedBy);
            long unreadCount = notificationRepository.countUnreadByUserId(performedBy);

            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(this::convertToNotificationDTO)
                    .collect(Collectors.toList());

            log.info("Request ID: {}, Retrieved {} notifications ({} unread)",
                    requestId, notificationDTOs.size(), unreadCount);

            return new NotificationResponseDTO(notificationDTOs);

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving notifications: {}", requestId, e.getMessage(), e);
            return new NotificationResponseDTO(Collections.emptyList());
        }
    }

    public NotificationDTO createNotification(NotificationDTO notificationDto, String userId) {
        try {
            NotificationEntity notification = new NotificationEntity();
            notification.setTitle(notificationDto.getTitle());
            notification.setMessage(notificationDto.getMessage());
            notification.setType(notificationDto.getType());
            notification.setUserId(userId);
            notification.setIcon(notificationDto.getIcon());
            notification.setActionUrl(notificationDto.getActionUrl());
            notification.setCollectionId(notificationDto.getCollectionId());
            notification.setEndpointId(notificationDto.getEndpointId());
            notification.setRead(false);

            // Set expiration (30 days from now)
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            NotificationEntity saved = notificationRepository.save(notification);
            log.info("Created notification for user: {}", userId);
            return convertToNotificationDTO(saved);

        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage());
            throw new RuntimeException("Failed to create notification");
        }
    }

    public void markNotificationAsRead(String notificationId) {
        try {
            NotificationEntity notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            notification.setRead(true);
            notificationRepository.save(notification);

            log.info("Marked notification as read: {}", notificationId);

        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage());
        }
    }

    public void markAllNotificationsAsRead(String userId) {
        try {
            notificationRepository.markAllAsRead(userId);
            log.info("Marked all notifications as read for user: {}", userId);
        } catch (Exception e) {
            log.error("Error marking all notifications as read: {}", e.getMessage());
        }
    }

    public void deleteNotification(String notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
            log.info("Deleted notification: {}", notificationId);
        } catch (Exception e) {
            log.error("Error deleting notification: {}", e.getMessage());
        }
    }

    // ========== CHANGELOG METHODS ==========

    public ChangelogResponseDTO getChangelog(String requestId, HttpServletRequest req, String performedBy,
                                             String collectionId) {
        try {
            log.info("Request ID: {}, Getting changelog for collection: {}", requestId, collectionId);

            List<ChangelogEntryEntity> entries = changelogRepository.findByCollectionIdOrderByDateDesc(collectionId);

            List<ChangelogEntryDTO> entryDTOs = entries.stream()
                    .map(this::convertToChangelogEntryDTO)
                    .collect(Collectors.toList());

            log.info("Request ID: {}, Retrieved {} changelog entries", requestId, entryDTOs.size());
            return new ChangelogResponseDTO(entryDTOs, collectionId);

        } catch (Exception e) {
            log.error("Request ID: {}, Error retrieving changelog: {}", requestId, e.getMessage(), e);
            return new ChangelogResponseDTO(Collections.emptyList(), collectionId);
        }
    }

    public ChangelogEntryDTO createChangelogEntry(String collectionId, ChangelogEntryDTO entryDto, String performedBy) {
        try {
            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            ChangelogEntryEntity entry = new ChangelogEntryEntity();
            entry.setVersion(entryDto.getVersion());
            entry.setDate(entryDto.getDate());
            entry.setType(entryDto.getType());
            entry.setAuthor(performedBy);
            entry.setCollection(collection);
            entry.setChanges(entryDto.getChanges());

            ChangelogEntryEntity saved = changelogRepository.save(entry);
            log.info("Created changelog entry for collection: {} version: {}", collectionId, entryDto.getVersion());
            return convertToChangelogEntryDTO(saved);

        } catch (Exception e) {
            log.error("Error creating changelog entry: {}", e.getMessage());
            throw new RuntimeException("Failed to create changelog entry");
        }
    }

    // ========== MOCK SERVER METHODS ==========

    public GenerateMockResponseDTO generateMockServer(String requestId, HttpServletRequest req, String performedBy,
                                                      String collectionId, Map<String, String> options) {
        try {
            log.info("Request ID: {}, Generating mock server for collection: {}", requestId, collectionId);

            APICollectionEntity collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Collection not found"));

            // Check if mock server already exists
            Optional<MockServerEntity> existingMock = mockServerRepository.findByCollectionId(collectionId);

            MockServerEntity mockServer;
            if (existingMock.isPresent()) {
                mockServer = existingMock.get();
                // Deactivate old endpoints
                mockEndpointRepository.findByMockServerId(mockServer.getId()).forEach(endpoint -> {
                    endpoint.setEnabled(false);
                    mockEndpointRepository.save(endpoint);
                });
            } else {
                mockServer = new MockServerEntity();
                mockServer.setCollection(collection);
                mockServer.setCreatedBy(performedBy);

                String randomId = UUID.randomUUID().toString().substring(0, 8);
                mockServer.setMockServerUrl("https://mock.fintech.com/" + randomId);
                mockServer.setActive(true);
                mockServer.setDescription("Mock server for " + collection.getName());

                // Set expiration (7 days from now)
                mockServer.setExpiresAt(LocalDateTime.now().plusDays(7));
            }

            mockServer.setUpdatedAt(LocalDateTime.now());
            MockServerEntity savedMockServer = mockServerRepository.save(mockServer);

            // Generate mock endpoints from collection's endpoints
            List<APIEndpointEntity> endpoints = endpointRepository.findByCollectionId(collectionId);
            List<MockEndpointDTO> mockEndpointDTOs = new ArrayList<>();

            for (APIEndpointEntity endpoint : endpoints) {
                MockEndpointEntity mockEndpoint = new MockEndpointEntity();
                mockEndpoint.setMethod(endpoint.getMethod());
                mockEndpoint.setPath(endpoint.getUrl().replace(collection.getBaseUrl(), ""));
                mockEndpoint.setStatusCode(200);
                mockEndpoint.setResponseDelay(200); // Default 200ms delay
                mockEndpoint.setEnabled(true);
                mockEndpoint.setMockServer(savedMockServer);
                mockEndpoint.setSourceEndpoint(endpoint);
                mockEndpoint.setDescription("Mock for " + endpoint.getName());

                // Generate sample response
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("success", true);
                responseBody.put("id", UUID.randomUUID().toString());
                responseBody.put("timestamp", LocalDateTime.now().toString());
                responseBody.put("message", "Mock response for " + endpoint.getName());
                mockEndpoint.setResponseBody(responseBody);

                MockEndpointEntity saved = mockEndpointRepository.save(mockEndpoint);
                mockEndpointDTOs.add(convertToMockEndpointDTO(saved));
            }

            log.info("Request ID: {}, Generated mock server with {} endpoints", requestId, mockEndpointDTOs.size());

            return new GenerateMockResponseDTO(
                    mockEndpointDTOs,
                    collectionId,
                    "Mock server generated successfully"
            );

        } catch (Exception e) {
            log.error("Request ID: {}, Error generating mock server: {}", requestId, e.getMessage(), e);
            return new GenerateMockResponseDTO(Collections.emptyList(), collectionId,
                    "Error generating mock server: " + e.getMessage());
        }
    }

    public MockServerEntity getMockServer(String collectionId) {
        return mockServerRepository.findByCollectionId(collectionId)
                .orElse(null);
    }

    public void deactivateMockServer(String mockServerId) {
        try {
            MockServerEntity mockServer = mockServerRepository.findById(mockServerId)
                    .orElseThrow(() -> new RuntimeException("Mock server not found"));

            mockServer.setActive(false);
            mockServerRepository.save(mockServer);

            // Deactivate all endpoints
            mockEndpointRepository.findByMockServerId(mockServerId).forEach(endpoint -> {
                endpoint.setEnabled(false);
                mockEndpointRepository.save(endpoint);
            });

            log.info("Deactivated mock server: {}", mockServerId);

        } catch (Exception e) {
            log.error("Error deactivating mock server: {}", e.getMessage());
        }
    }

    // ========== SETTINGS METHODS ==========

    public DocumentationSettingsDTO getSettings(String userId) {
        try {
            DocumentationSettingsEntity settings = settingsRepository.findByUserId(userId)
                    .orElseGet(() -> createDefaultSettings(userId));

            return convertToSettingsDTO(settings);

        } catch (Exception e) {
            log.error("Error getting settings: {}", e.getMessage());
            return createDefaultSettingsDTO();
        }
    }

    public DocumentationSettingsDTO updateSettings(String userId, DocumentationSettingsDTO settingsDTO) {
        try {
            DocumentationSettingsEntity settings = settingsRepository.findByUserId(userId)
                    .orElse(new DocumentationSettingsEntity());

            settings.setUserId(userId);
            settings.setAutoSave(settingsDTO.isAutoSave());
            settings.setDarkMode(settingsDTO.isDarkMode());
            settings.setDefaultLanguage(settingsDTO.getDefaultLanguage());
            settings.setDefaultEnvironment(settingsDTO.getDefaultEnvironment());
            settings.setShowLineNumbers(settingsDTO.isShowLineNumbers());
            settings.setWordWrap(settingsDTO.isWordWrap());
            settings.setFontSize(settingsDTO.getFontSize());
            settings.setFontFamily(settingsDTO.getFontFamily());
            settings.setTheme(settingsDTO.getTheme());
            settings.setShowSidebar(settingsDTO.isShowSidebar());
            settings.setCompactMode(settingsDTO.isCompactMode());

            DocumentationSettingsEntity saved = settingsRepository.save(settings);
            log.info("Updated settings for user: {}", userId);
            return convertToSettingsDTO(saved);

        } catch (Exception e) {
            log.error("Error updating settings: {}", e.getMessage());
            throw new RuntimeException("Failed to update settings");
        }
    }

    private DocumentationSettingsEntity createDefaultSettings(String userId) {
        DocumentationSettingsEntity settings = new DocumentationSettingsEntity();
        settings.setUserId(userId);
        settings.setAutoSave(true);
        settings.setDarkMode(false);
        settings.setDefaultLanguage("javascript");
        settings.setDefaultEnvironment("sandbox");
        settings.setShowLineNumbers(true);
        settings.setWordWrap(true);
        settings.setFontSize(14);
        settings.setFontFamily("Monaco");
        settings.setTheme("light");
        settings.setShowSidebar(true);
        settings.setCompactMode(false);
        return settingsRepository.save(settings);
    }

    private DocumentationSettingsDTO createDefaultSettingsDTO() {
        DocumentationSettingsDTO dto = new DocumentationSettingsDTO();
        dto.setAutoSave(true);
        dto.setDarkMode(false);
        dto.setDefaultLanguage("javascript");
        dto.setDefaultEnvironment("sandbox");
        dto.setShowLineNumbers(true);
        dto.setWordWrap(true);
        dto.setFontSize(14);
        dto.setFontFamily("Monaco");
        dto.setTheme("light");
        dto.setShowSidebar(true);
        dto.setCompactMode(false);
        return dto;
    }

    // ========== CACHE MANAGEMENT - REMOVED ==========

    // All cache-related methods have been removed:
    // - documentationCache map removed
    // - CACHE_TTL_MS constant removed
    // - clearDocumentationCache method removed
    // - clearUserCache method removed
    // - clearCollectionCache method removed
    // - clearEndpointCache method removed
    // - isCacheExpired method removed
    // - DocCache inner class removed

    // ========== CONVERSION METHODS ==========

    private APICollectionDTO convertToCollectionDto(APICollectionEntity entity) {
        APICollectionDTO dto = new APICollectionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        dto.setOwner(entity.getOwner());
        dto.setType(entity.getType());
        dto.setFavorite(entity.isFavorite());
        dto.setExpanded(entity.isExpanded());
        dto.setUpdatedAt(formatDateTimeToString(entity.getUpdatedAt()));
        dto.setCreatedAt(formatDateTimeToString(entity.getCreatedAt()));
        dto.setTotalEndpoints(entity.getTotalEndpoints());
        dto.setTotalFolders(entity.getTotalFolders());
        dto.setColor(entity.getColor());
        dto.setStatus(entity.getStatus());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setTags(entity.getTags());
        return dto;
    }

    private FolderDTO convertToFolderDTO(FolderEntity entity) {
        FolderDTO dto = new FolderDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCollectionId(entity.getCollection().getId());
        dto.setParentFolderId(entity.getParentFolder() != null ? entity.getParentFolder().getId() : null);
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private APIEndpointDTO convertToEndpointDto(APIEndpointEntity entity) {
        APIEndpointDTO dto = new APIEndpointDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setTags(entity.getTags());

        if (entity.getUpdatedAt() != null) {
            dto.setLastModified(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        dto.setRequiresAuth(entity.isRequiresAuth());
        dto.setDeprecated(entity.isDeprecated());
        dto.setFolder(entity.getFolder() != null ? entity.getFolder().getId() : null);
        dto.setCollectionId(entity.getCollection() != null ? entity.getCollection().getId() : null);
        dto.setCategory(entity.getCategory());

        // Handle rate limit
        if (entity.getRateLimit() != null) {
            try {
                dto.setRateLimit(entity.getRateLimit());
            } catch (Exception e) {
                log.warn("Failed to parse rate limit: {}", e.getMessage());
            }
        }

        return dto;
    }

    private EndpointDetailResponseDTO convertToEndpointDetailResponseDTO(APIEndpointEntity entity) {
        EndpointDetailResponseDTO dto = new EndpointDetailResponseDTO();
        dto.setEndpointId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setTags(entity.getTags());
        dto.setLastModified(formatDateTimeToString(entity.getUpdatedAt()));
        dto.setVersion(entity.getApiVersion());
        dto.setRequiresAuthentication(entity.isRequiresAuth());

        // Handle rate limit as Map
        if (entity.getRateLimit() != null) {
            dto.setRateLimit(entity.getRateLimit());

            // Create a formatted string version
            Map<String, Object> rateLimit = entity.getRateLimit();
            StringBuilder formatted = new StringBuilder();
            if (rateLimit.containsKey("requestsPerMinute")) {
                formatted.append(rateLimit.get("requestsPerMinute")).append(" requests per minute");
            }
            if (rateLimit.containsKey("burstCapacity")) {
                formatted.append(" (burst: ").append(rateLimit.get("burstCapacity")).append(")");
            }
            dto.setFormattedRateLimit(formatted.toString());
        }

        dto.setDeprecated(entity.isDeprecated());
        dto.setRateLimitInfo(entity.getRateLimit());

        // Convert headers
        List<HeaderDTO> headerDTOs = entity.getHeaders().stream()
                .map(this::convertToHeaderDTO)
                .collect(Collectors.toList());
        dto.setHeaders(headerDTOs);

        // Convert parameters
        List<ParameterDTO> parameterDTOs = entity.getParameters().stream()
                .map(this::convertToParameterDTO)
                .collect(Collectors.toList());
        dto.setParameters(parameterDTOs);

        // Convert request body
        if (entity.getRequestBodyExample() != null) {
            try {
                dto.setRequestBodyExample(objectMapper.writeValueAsString(entity.getRequestBodyExample()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize request body: {}", e.getMessage());
            }
        }

        // Convert response examples
        List<ResponseExampleDTO> responseExampleDTOs = entity.getResponseExamples().stream()
                .map(this::convertToResponseExampleDTO)
                .collect(Collectors.toList());
        dto.setResponseExamples(responseExampleDTOs);

        // Convert changelog
        List<ChangelogEntryDTO> changelogDTOs = entity.getCollection().getChangelog().stream()
                .map(this::convertToChangelogEntryDTO)
                .collect(Collectors.toList());
        dto.setChangelog(changelogDTOs);

        return dto;
    }

    private HeaderDTO convertToHeaderDTO(HeaderEntity entity) {
        return new HeaderDTO(
                entity.getKey(),
                entity.getValue(),
                entity.getDescription(),
                entity.isRequired()
        );
    }

    private ParameterDTO convertToParameterDTO(ParameterEntity entity) {
        return new ParameterDTO(
                entity.getName(),
                entity.getType(),
                entity.getIn(),
                entity.isRequired(),
                entity.getDescription(),
                entity.getExample()
        );
    }

    private ResponseExampleDTO convertToResponseExampleDTO(ResponseExampleEntity entity) {
        ResponseExampleDTO dto = new ResponseExampleDTO();
        dto.setStatusCode(entity.getStatusCode());
        dto.setDescription(entity.getDescription());
        dto.setContentType(entity.getContentType());

        if (entity.getExample() != null) {
            try {
                dto.setExample(objectMapper.writeValueAsString(entity.getExample()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize response example: {}", e.getMessage());
            }
        }

        dto.setSchema(entity.getSchema());

        List<HeaderDTO> headerDTOs = entity.getHeaders().stream()
                .map(this::convertToHeaderDTO)
                .collect(Collectors.toList());
        dto.setHeaders(headerDTOs);

        return dto;
    }

    private EnvironmentDTO convertToEnvironmentDTO(EnvironmentEntity entity) {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setActive(entity.isActive());
        dto.setDescription(entity.getDescription());
        dto.setApiKey(entity.getApiKey());
        dto.setSecret(entity.getSecret());
        dto.setVariables(entity.getVariables());
        dto.setLastUsed(entity.getLastUsed() != null ?
                formatDateTimeToString(entity.getLastUsed()) : null);
        return dto;
    }

    private NotificationDTO convertToNotificationDTO(NotificationEntity entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setRead(entity.isRead());
        dto.setTime(getTimeAgo(entity.getCreatedAt()));
        dto.setIcon(entity.getIcon());
        dto.setActionUrl(entity.getActionUrl());
        dto.setCollectionId(entity.getCollectionId());
        dto.setEndpointId(entity.getEndpointId());
        return dto;
    }

    private ChangelogEntryDTO convertToChangelogEntryDTO(ChangelogEntryEntity entity) {
        return new ChangelogEntryDTO(
                entity.getVersion(),
                entity.getDate(),
                entity.getChanges(),
                entity.getType()
        );
    }

    private MockEndpointDTO convertToMockEndpointDTO(MockEndpointEntity entity) {
        MockEndpointDTO dto = new MockEndpointDTO();
        dto.setId(entity.getId());
        dto.setMethod(entity.getMethod());
        dto.setPath(entity.getPath());
        dto.setStatusCode(entity.getStatusCode());
        dto.setResponseDelay(entity.getResponseDelay());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());

        if (entity.getResponseBody() != null) {
            try {
                dto.setResponseBody(objectMapper.writeValueAsString(entity.getResponseBody()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize response body: {}", e.getMessage());
            }
        }

        List<HeaderDTO> headerDTOs = entity.getResponseHeaders().stream()
                .map(this::convertToHeaderDTO)
                .collect(Collectors.toList());
        dto.setResponseHeaders(headerDTOs);

        return dto;
    }

    private DocumentationSettingsDTO convertToSettingsDTO(DocumentationSettingsEntity entity) {
        DocumentationSettingsDTO dto = new DocumentationSettingsDTO();
        dto.setAutoSave(entity.isAutoSave());
        dto.setDarkMode(entity.isDarkMode());
        dto.setDefaultLanguage(entity.getDefaultLanguage());
        dto.setDefaultEnvironment(entity.getDefaultEnvironment());
        dto.setShowLineNumbers(entity.isShowLineNumbers());
        dto.setWordWrap(entity.isWordWrap());
        dto.setFontSize(entity.getFontSize());
        dto.setFontFamily(entity.getFontFamily());
        dto.setTheme(entity.getTheme());
        dto.setShowSidebar(entity.isShowSidebar());
        dto.setCompactMode(entity.isCompactMode());

        if (entity.getCustomSettings() != null) {
            dto.setCustomSettings(new HashMap<>(entity.getCustomSettings()));
        }

        return dto;
    }

    private SearchResultDTO convertToSearchResultDTO(APICollectionEntity collection, String query) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setId(collection.getId());
        dto.setTitle(collection.getName());
        dto.setType("Collection");
        dto.setCategory("Collection");
        dto.setDescription(collection.getDescription());
        dto.setRelevanceScore(calculateRelevance(collection.getName(), collection.getDescription(), query));
        dto.setCollection(collection.getName());
        dto.setEndpointUrl(collection.getBaseUrl());
        dto.setLastUpdated(formatDateTimeToString(collection.getUpdatedAt()));
        return dto;
    }

    private SearchResultDTO convertToSearchResultDTO(APIEndpointEntity endpoint, String query) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setId(endpoint.getId());
        dto.setTitle(endpoint.getName());
        dto.setType("Endpoint");
        dto.setCategory(endpoint.getCategory());
        dto.setDescription(endpoint.getDescription());
        dto.setRelevanceScore(calculateRelevance(endpoint.getName(), endpoint.getDescription(), query));
        dto.setCollection(endpoint.getCollection().getName());
        dto.setEndpointUrl(endpoint.getUrl());
        dto.setLastUpdated(formatDateTimeToString(endpoint.getUpdatedAt()));
        return dto;
    }

    private int calculateRelevance(String title, String description, String query) {
        String lowerQuery = query.toLowerCase();
        int score = 50; // Base score

        if (title.toLowerCase().contains(lowerQuery)) {
            score += 30;
        }
        if (description != null && description.toLowerCase().contains(lowerQuery)) {
            score += 20;
        }

        return Math.min(score, 100);
    }

    // ========== HELPER METHODS ==========

    private CodeExampleResponseDTO generateDefaultCodeExample(String endpointId, APIEndpointEntity endpoint, String language) {
        String defaultLanguage = language != null ? language : "curl";
        String codeExample = generateDefaultCode(endpoint, defaultLanguage);

        return new CodeExampleResponseDTO(
                defaultLanguage,
                endpointId,
                codeExample,
                "Auto-generated code example"
        );
    }

    private String generateDefaultCode(APIEndpointEntity endpoint, String language) {
        String method = endpoint.getMethod();
        String url = endpoint.getUrl();

        switch (language) {
            case "curl":
                return String.format(
                        "curl -X %s \"%s\" \\\n" +
                                "  -H \"Content-Type: application/json\" \\\n" +
                                "  -H \"Authorization: Bearer YOUR_ACCESS_TOKEN\"",
                        method, url
                );
            case "javascript":
                return String.format(
                        "fetch('%s', {\n" +
                                "  method: '%s',\n" +
                                "  headers: {\n" +
                                "    'Content-Type': 'application/json',\n" +
                                "    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'\n" +
                                "  }\n" +
                                "})",
                        url, method
                );
            case "python":
                return String.format(
                        "import requests\n\n" +
                                "headers = {\n" +
                                "    'Content-Type': 'application/json',\n" +
                                "    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'\n" +
                                "}\n\n" +
                                "response = requests.%s('%s', headers=headers)",
                        method.toLowerCase(), url
                );
            default:
                return String.format("// Code example for %s", endpoint.getName());
        }
    }

    private EndpointDetailResponseDTO createFallbackEndpointDetails(String endpointId) {
        EndpointDetailResponseDTO details = new EndpointDetailResponseDTO();
        details.setEndpointId(endpointId);
        details.setName("Endpoint Not Found");
        details.setMethod("GET");
        details.setUrl("https://api.fintech.com/v2.1/unknown");
        details.setDescription("Fallback endpoint details - endpoint not found in database");
        details.setCategory("General");
        details.setTags(Arrays.asList("fallback", "not-found"));
        details.setLastModified(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setVersion("v1.0");
        details.setRequiresAuthentication(true);

        // Create a rate limit map instead of a string
        Map<String, Object> rateLimitMap = new HashMap<>();
        rateLimitMap.put("strategy", "token_bucket");
        rateLimitMap.put("requestsPerMinute", 60);
        rateLimitMap.put("burstCapacity", 100);
        details.setRateLimit(rateLimitMap);

        // Also set the formatted string version
        details.setFormattedRateLimit("60 requests per minute (burst: 100)");

        // Set rate limit info (could be the same map)
        details.setRateLimitInfo(rateLimitMap);

        details.setDeprecated(false);

        // Initialize empty lists to avoid null pointers
        details.setHeaders(new ArrayList<>());
        details.setParameters(new ArrayList<>());
        details.setResponseExamples(new ArrayList<>());
        details.setChangelog(new ArrayList<>());

        return details;
    }

    private String formatDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();

        if (seconds < 60) return "just now";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";

        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    // ========== STATISTICS METHODS ==========

    public DocumentationStatsDTO getDocumentationStats(String userId) {
        DocumentationStatsDTO stats = new DocumentationStatsDTO();

        stats.setTotalCollections((int) collectionRepository.count());
        stats.setTotalEndpoints((int) endpointRepository.count());
        stats.setPublishedCollections((int) publishedDocumentationRepository.findByPublishedBy(userId).size());
        stats.setTotalCodeExamples((int) codeExampleRepository.count());

        // Get counts by method
        Map<String, Integer> endpointsByMethod = new HashMap<>();
        for (String method : Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH")) {
            endpointsByMethod.put(method, 0);
        }

        // You would need to implement custom queries for these stats
        stats.setEndpointByMethod(endpointsByMethod);

        return stats;
    }
}