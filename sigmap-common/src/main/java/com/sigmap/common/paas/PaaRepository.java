package com.sigmap.common.paas;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaaRepository extends JpaRepository<Paa, CompositeId>,JpaSpecificationExecutor<Paa>,
        CompositeSoftDeleteRepository<Paa, Integer> {
    @Query("SELECT MAX(p.id) FROM Paa p")
    Integer findMaxId();

    /**
     * Finds the maximum version for a PAA with the given ID
     *
     * @param id the PAA ID
     * @return the maximum version, or null if no versions found
     */
    @Query("SELECT MAX(p.version) FROM Paa p WHERE p.id = :id AND p.isDeleted = false")
    Integer findMaxVersionById(@Param("id") Integer id);

    @Query("SELECT p FROM Paa p WHERE p.annee = :currentYear")
    List<Paa> findByCurrentYear(@Param("currentYear") String currentYear);
}
