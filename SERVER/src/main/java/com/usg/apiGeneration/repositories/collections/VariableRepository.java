package com.usg.apiGeneration.repositories.collections;

import com.usg.apiGeneration.entities.postgres.collections.VariableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("VariableRepositoryCollections")
public interface VariableRepository extends JpaRepository<VariableEntity, String> {

    List<VariableEntity> findByCollectionId(String collectionId);

    void deleteByCollectionId(String collectionId);
}