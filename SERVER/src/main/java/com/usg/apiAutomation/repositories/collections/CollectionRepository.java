package com.usg.apiAutomation.repositories.collections;

import com.usg.apiAutomation.entities.postgres.collections.CollectionEntity;
import com.usg.apiAutomation.entities.postgres.collections.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("CollectionRepositoryCollections")
public interface CollectionRepository extends JpaRepository<CollectionEntity, String> {

    List<CollectionEntity> findByOwner(String owner);

    List<CollectionEntity> findByOwnerAndIsFavoriteTrue(String owner);

    @Query(value = "SELECT * FROM tb_col_parameters WHERE request_id = :requestId", nativeQuery = true)
    List<ParameterEntity> findParametersByRequestId(@Param("requestId") String requestId);

    @Query("SELECT c FROM CollectionEntityCollections c LEFT JOIN FETCH c.variables WHERE c.id = :id")
    Optional<CollectionEntity> findByIdWithVariables(@Param("id") String id);

    @Query("SELECT c FROM CollectionEntityCollections c LEFT JOIN FETCH c.folders f LEFT JOIN FETCH f.requests WHERE c.id = :id")
    Optional<CollectionEntity> findByIdWithFoldersAndRequests(@Param("id") String id);

    @Query("SELECT COUNT(c) FROM CollectionEntityCollections c WHERE c.owner = :owner")
    long countByOwner(@Param("owner") String owner);

    void deleteByOwnerAndId(String owner, String id);

}