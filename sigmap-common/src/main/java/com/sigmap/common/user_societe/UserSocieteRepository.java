package com.sigmap.common.user_societe;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSocieteRepository extends JpaRepository<UserSociete, UserSocieteId>,
        JpaSpecificationExecutor<UserSociete> {
    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Transactional
    @Query("UPDATE UserSociete us SET us.isDeleted = true, us.deletedAt = CURRENT_TIMESTAMP, us.deletedBy = :#{principal.subject} " +
            "WHERE us.userId = :userId AND us.societeId = :societeId")
    void softDelete(@Param("userId") Integer userId,
                    @Param("societeId") Integer societeId);
}
