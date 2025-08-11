package com.sigmap.common.reunion;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface ReunionRepository extends JpaRepository<Reunion, Integer>, JpaSpecificationExecutor<Reunion>,
        SoftDeleteRepository<Reunion, Integer> {
    @Query("SELECT r FROM Reunion r " +
            "LEFT JOIN FETCH r.reunionPpms rp " +
            "LEFT JOIN FETCH rp.ppm ppm " +
            "LEFT JOIN FETCH ppm.ac " +
            "LEFT JOIN FETCH r.reunionDacs rd " +
            "LEFT JOIN FETCH rd.dac d " +
            "LEFT JOIN FETCH r.participants p " +
            "WHERE r.id = :id AND r.isDeleted = false")
    Optional<Reunion> findByIdWithRelations(@Param("id") Integer id);
    @Query("SELECT DISTINCT r FROM Reunion r " +
            "LEFT JOIN FETCH r.participants p " +
            "WHERE r.id = :id AND r.isDeleted = false")
    Optional<Reunion> findByIdWithParticipantsFetched(@Param("id") Integer id);
}


