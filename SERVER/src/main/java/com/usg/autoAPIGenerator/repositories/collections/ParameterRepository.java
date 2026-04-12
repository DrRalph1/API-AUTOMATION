package com.usg.autoAPIGenerator.repositories.collections;

import com.usg.autoAPIGenerator.dtos.collections.ParameterDTO;
import com.usg.autoAPIGenerator.entities.postgres.collections.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ParametersRepositoryCollections")
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {

    List<ParameterEntity> findByRequestId(String requestId);

    @Query("SELECT new com.usg.autoAPIGenerator.dtos.collections.ParameterDTO(" +
            "p.id, p.key, p.value, p.description, p.isEnabled) " +
            "FROM ParametersEntityCollections p WHERE p.request.id = :requestId")
    List<ParameterDTO> findParameterDTOsByRequestId(@Param("requestId") String requestId);

    @Modifying
    @Query("DELETE FROM ParametersEntityCollections p WHERE p.request.id = :requestId")
    void deleteByRequestId(@Param("requestId") String requestId);
}