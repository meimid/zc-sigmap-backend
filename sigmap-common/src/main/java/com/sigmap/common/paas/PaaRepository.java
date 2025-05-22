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

    @Query(" SELECT p FROM Paa p WHERE p.isDeleted = false AND p.annee = :currentYear AND p.version = (" +
            "   SELECT MAX (p2.version) FROM Paa p2 WHERE p2.id = p.id AND p2.annee = :currentYear AND p2.isDeleted = false)")
    Optional<Paa> findByCurrentYear(@Param("currentYear") String currentYear);
}
