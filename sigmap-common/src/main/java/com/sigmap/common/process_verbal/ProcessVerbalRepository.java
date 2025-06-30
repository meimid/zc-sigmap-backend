package com.sigmap.common.process_verbal;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessVerbalRepository extends JpaRepository<ProcessVerbal, ProcessVerbalId>{
    /**
     * Soft deletes a ProcessVerbal entity using its specific three-part composite key.
     * The deletedBy value is automatically sourced from the current security principal (e.g., logged-in user).
     *
     * @param ppmId The 'ppm_id' part of the key.
     * @param ppmVersion The 'ppm_ver' part of the key.
     * @param processVerbalId The 'process_verbal_id' part of the key.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProcessVerbal pv SET pv.isDeleted = true, pv.deletedAt = CURRENT_TIMESTAMP, pv.deletedBy = :#{principal.subject} " +
            "WHERE pv.ppmId = :ppmId AND pv.ppmVersion = :ppmVersion AND pv.processVerbalId = :processVerbalId")
    void softDelete(@Param("ppmId") Integer ppmId,
                    @Param("ppmVersion") Integer ppmVersion,
                    @Param("processVerbalId") String processVerbalId);


    /**
     * An overloaded version that allows specifying the deletedBy user and a specific deletion time.
     *
     * @param ppmId The 'ppm_id' part of the key.
     * @param ppmVersion The 'ppm_ver' part of the key.
     * @param processVerbalId The 'process_verbal_id' part of the key.
     * @param deletedBy The identifier of the user performing the deletion.
     * @param deletedAt The specific timestamp for the deletion.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProcessVerbal pv SET pv.isDeleted = true, pv.deletedAt = :deletedAt, pv.deletedBy = :deletedBy " +
            "WHERE pv.ppmId = :ppmId AND pv.ppmVersion = :ppmVersion AND pv.processVerbalId = :processVerbalId")
    void softDelete(@Param("ppmId") Integer ppmId,
                    @Param("ppmVersion") Integer ppmVersion,
                    @Param("processVerbalId") String processVerbalId,
                    @Param("deletedBy") String deletedBy,
                    @Param("deletedAt") LocalDateTime deletedAt);

    Optional<ProcessVerbal> findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
            Integer ppmId,
            Integer ppmVersion,
            String processVerbalId
    );

    ProcessVerbal findByPpmIdAndPpmVersionAndProcessVerbalId(Integer ppmId, Integer ppmVersion, String processVerbalId);

    Optional  <ProcessVerbal> findTopByPpmIdAndPpmVersionAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer ppmId,
            Integer ppmVersion
    );
    Optional<ProcessVerbal> findTopByPpmIdAndPpmVersionAndTaskIdAndProcessInstanceIdAndIsDeletedFalse(Integer ppmId,
                                                                                                         Integer ppmVersion,
                                                                                                         String taskId,
                                                                                                         String processInstanceId);
}
