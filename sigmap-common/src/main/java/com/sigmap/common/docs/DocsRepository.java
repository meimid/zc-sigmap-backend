package com.sigmap.common.docs;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for Docs entity.
 * Provides methods to interact with the docs table.
 */
public interface DocsRepository extends JpaRepository<Docs, Integer> {
    /**
     * Finds Docs entities by a list of IDs.
     *
     * @param ids List of doc IDs to find
     * @return List of Docs entities
     */
    List<Docs> findByIdIn(List<Integer> ids);
}