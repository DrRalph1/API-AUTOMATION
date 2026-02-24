package com.usg.apiAutomation.repositories.postgres.collections;

import com.usg.apiAutomation.dtos.collections.ParameterDTO;
import com.usg.apiAutomation.entities.postgres.collections.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ParametersRepositoryCollections")
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {

    List<ParameterEntity> findByRequestId(String requestId);

    @Query("SELECT new com.usg.apiAutomation.dtos.collections.ParameterDTO(" +
            "p.id, p.key, p.value, p.description, p.isEnabled) " +
            "FROM ParametersEntityCollections p WHERE p.request.id = :requestId")
    List<ParameterDTO> findParameterDTOsByRequestId(@Param("requestId") String requestId);

    void deleteByRequestId(String requestId);
}