package com.usg.apiAutomation.helpers;

import com.usg.apiAutomation.dtos.collections.*;
import com.usg.apiAutomation.entities.*;
import com.usg.apiAutomation.entities.collections.*;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollectionMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static CollectionDTO toDTO(CollectionEntity entity) {
        if (entity == null) return null;

        CollectionDTO dto = new CollectionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setExpanded(entity.isExpanded());
        dto.setFavorite(entity.isFavorite());
        dto.setEditing(entity.isEditing());
        dto.setOwner(entity.getOwner());
        dto.setColor(entity.getColor());
        dto.setTags(entity.getTags());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(ISO_FORMATTER));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(ISO_FORMATTER));
        }

        dto.setRequestsCount(entity.getFolders().stream()
                .mapToInt(f -> f.getRequests().size())
                .sum());

        dto.setFolderCount(entity.getFolders().size());

        if (entity.getVariables() != null) {
            dto.setVariables(entity.getVariables().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static CollectionDetailsResponseDTO toDetailsDTO(CollectionEntity entity) {
        if (entity == null) return null;

        CollectionDetailsResponseDTO dto = new CollectionDetailsResponseDTO();
        dto.setCollectionId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(ISO_FORMATTER));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(ISO_FORMATTER));
        }

        dto.setTotalRequests(entity.getFolders().stream()
                .mapToInt(f -> f.getRequests().size())
                .sum());

        dto.setTotalFolders(entity.getFolders().size());
        dto.setFavorite(entity.isFavorite());
        dto.setOwner(entity.getOwner());
        dto.setComments(entity.getComments());

        if (entity.getLastActivity() != null) {
            dto.setLastActivity(entity.getLastActivity().format(ISO_FORMATTER));
        }

        if (entity.getVariables() != null) {
            dto.setVariables(entity.getVariables().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        if (entity.getFolders() != null) {
            dto.setFolders(entity.getFolders().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static FolderDTO toDTO(FolderEntity entity) {
        if (entity == null) return null;

        FolderDTO dto = new FolderDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setExpanded(entity.isExpanded());
        dto.setEditing(entity.isEditing());
        dto.setRequestCount(entity.getRequestCount());

        if (entity.getRequests() != null) {
            dto.setRequests(entity.getRequests().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static RequestDTO toDTO(RequestEntity entity) {
        if (entity == null) return null;

        RequestDTO dto = new RequestDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setEditing(entity.isEditing());
        dto.setStatus(entity.getStatus());
        dto.setBody(entity.getBody());
        dto.setTests(entity.getTests());
        dto.setPreRequestScript(entity.getPreRequestScript());
        dto.setSaved(entity.isSaved());

        if (entity.getLastModified() != null) {
            dto.setLastModified(entity.getLastModified().format(ISO_FORMATTER));
        }

        if (entity.getAuthConfig() != null) {
            dto.setAuth(toAuthDTO(entity.getAuthConfig()));
        }

        if (entity.getHeaders() != null) {
            dto.setHeaders(entity.getHeaders().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        if (entity.getParams() != null) {
            dto.setParams(entity.getParams().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        if (entity.getCollection() != null) {
            dto.setCollectionId(entity.getCollection().getId());
        }

        if (entity.getFolder() != null) {
            dto.setFolderId(entity.getFolder().getId());
        }

        return dto;
    }

    public static RequestDetailsResponseDTO toRequestDetailsDTO(RequestEntity entity) {
        if (entity == null) return null;

        RequestDetailsResponseDTO dto = new RequestDetailsResponseDTO();
        dto.setRequestId(entity.getId());
        dto.setName(entity.getName());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setAuthType(entity.getAuthType());
        dto.setPreRequestScript(entity.getPreRequestScript());
        dto.setTests(entity.getTests());
        dto.setSaved(entity.isSaved());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(ISO_FORMATTER));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(ISO_FORMATTER));
        }

        if (entity.getCollection() != null) {
            dto.setCollectionId(entity.getCollection().getId());
        }

        if (entity.getFolder() != null) {
            dto.setFolderId(entity.getFolder().getId());
        }

        if (entity.getAuthConfig() != null) {
            dto.setAuthConfig(toAuthDTO(entity.getAuthConfig()));
        }

        if (entity.getHeaders() != null) {
            dto.setHeaders(entity.getHeaders().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        if (entity.getParams() != null) {
            dto.setParameters(entity.getParams().stream()
                    .map(CollectionMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        // Create BodyDTO
        BodyDTO body = new BodyDTO();
        if (entity.getBody() != null && !entity.getBody().isEmpty()) {
            body.setType("raw");
            body.setRawType("json");
            body.setContent(entity.getBody());
        } else {
            body.setType("none");
        }
        dto.setBody(body);

        return dto;
    }

    public static VariableDTO toDTO(VariableEntity entity) {
        if (entity == null) return null;

        VariableDTO dto = new VariableDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setType(entity.getType());
        dto.setEnabled(entity.isEnabled());

        return dto;
    }

    public static HeaderDTO toDTO(HeaderEntity entity) {
        if (entity == null) return null;

        HeaderDTO dto = new HeaderDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());

        return dto;
    }

    public static ParameterDTO toDTO(ParameterEntity entity) {
        if (entity == null) return null;

        ParameterDTO dto = new ParameterDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());

        return dto;
    }

    public static AuthConfigDTO toAuthDTO(AuthConfigEntity entity) {
        if (entity == null) return null;

        AuthConfigDTO dto = new AuthConfigDTO();
        dto.setType(entity.getType());
        dto.setToken(entity.getToken());
        dto.setTokenType(entity.getTokenType());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setAddTo(entity.getAddTo());

        return dto;
    }
}