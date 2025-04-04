package com.sigmap.common.societe_contact;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
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

    @Query("SELECT s from SocieteContact s where s.societeContactId.societe.id=:id1 and s.societeContactId.contact.id=:id2" )
    Optional<SocieteContact> findByIds(@Param("id1") Integer id1, @Param("id2") Integer id2);


}
