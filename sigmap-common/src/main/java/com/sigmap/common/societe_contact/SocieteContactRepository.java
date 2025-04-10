package com.sigmap.common.societe_contact;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocieteContactRepository extends JpaRepository<SocieteContact, SocieteContactId>,
        JpaSpecificationExecutor<SocieteContact>{


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE SocieteContact s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP, s.deletedBy = :#{principal.subject} WHERE s.societeContactId.societe.id = :societeId AND s.societeContactId.contact.id = :contactId")
    Integer softDelete(@Param("societeId") Integer societeId, @Param("contactId") Integer contactId);

    @Query("SELECT s from SocieteContact s where s.societeContactId.societe.id=:societeId and s.societeContactId.contact.id=:contactid" )
    Optional<SocieteContact> findByIds(@Param("societeId") Integer societeId, @Param("contactid") Integer contactid);


}
