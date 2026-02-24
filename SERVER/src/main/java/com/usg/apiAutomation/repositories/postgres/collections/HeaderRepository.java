package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.dtos.collections.HeaderDTO;
import com.usg.apiAutomation.entities.postgres.collections.HeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("HeaderRepositoryCollections")
public interface HeaderRepository extends JpaRepository<HeaderEntity, String> {

    List<HeaderEntity> findByRequestId(String requestId);

    @Query("SELECT new com.usg.apiAutomation.dtos.collections.HeaderDTO(" +
            "h.id, h.key, h.value, h.description, h.isEnabled) " +
            "FROM ParametersEntityCollections h WHERE h.request.id = :requestId")
    List<HeaderDTO> findHeaderDTOsByRequestId(@Param("requestId") String requestId);

    void deleteByRequestId(String requestId);
}