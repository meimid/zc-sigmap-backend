package com.sigmap.common.direction;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface DirectionRepository extends JpaRepository<Direction, DirectionId>,
        JpaSpecificationExecutor<Direction>{


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Direction d SET d.isDeleted = true, d.deletedAt = CURRENT_TIMESTAMP, d.deletedBy = :#{principal.subject} WHERE d.id = :directionId AND d.acId = :acId")
    Integer softDelete(@Param("directionId") Integer directionId, @Param("acId") Integer acId);


}
