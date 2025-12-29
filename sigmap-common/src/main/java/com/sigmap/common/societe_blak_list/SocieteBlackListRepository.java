package com.sigmap.common.societe_blak_list;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocieteBlackListRepository extends JpaRepository<SocieteBlackList, SocieteBlackListId>, JpaSpecificationExecutor<SocieteBlackList>
{

    @Query("""
   SELECT s
   FROM SocieteBlackList s
   WHERE s.isDeleted = false
     AND s.societeId = :societeId
     AND s.dateEffet <= CURRENT_TIMESTAMP
     AND (s.delai IS NULL OR s.delai >= CURRENT_TIMESTAMP)
""")
    Optional<SocieteBlackList> testForBlackListed(@Param("societeId") Integer societeId);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Transactional
    @Query("UPDATE SocieteBlackList sb SET sb.isDeleted = true, sb.deletedAt = CURRENT_TIMESTAMP, sb.deletedBy = :#{principal.subject} " +
            "WHERE sb.id = :id AND sb.societeId = :societeId")
    void softDelete(@Param("id") Integer id,
            @Param("societeId") Integer societeId);
}


