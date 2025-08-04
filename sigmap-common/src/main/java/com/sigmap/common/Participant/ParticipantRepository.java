package com.sigmap.common.Participant;

import com.sigmap.common.pv_membre_present.PvMembrePresent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId>,
        JpaSpecificationExecutor<Participant> {
    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Transactional
    @Query("UPDATE Participant pr SET pr.isDeleted = true, pr.deletedAt = CURRENT_TIMESTAMP, pr.deletedBy = :#{principal.subject} " +
            "WHERE pr.id = :id AND pr.reunionId = :reunionId")
    void softDelete(@Param("id") Integer id,
                    @Param("reunionId") Integer reunionId);

    Optional<Participant> findParticipantByIdAndReunionId(ParticipantId id, Integer reunionId);
    Optional<Participant> findParticipantByUserIdAndReunionId(String userId, Integer reunionId);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("UPDATE Participant p SET p.isDeleted = true, p.deletedAt = CURRENT_TIMESTAMP, p.deletedBy = :#{principal.subject} WHERE p IN :members")
    void softDeleteAll(@Param("members") List<Participant> members);
}
