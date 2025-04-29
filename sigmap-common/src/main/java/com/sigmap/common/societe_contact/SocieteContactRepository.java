package com.sigmap.common.societe_contact;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface SocieteContactRepository extends JpaRepository<SocieteContact, SocieteContactId>,
        JpaSpecificationExecutor<SocieteContact>{


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE SocieteContact s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP, s.deletedBy = :#{principal.subject} WHERE s.societeContactId.societe.id = :societeId AND s.societeContactId.contact.id = :contactId")
    Integer softDelete(@Param("societeId") Integer societeId, @Param("contactId") Integer contactId);

    @Query("SELECT s from SocieteContact s where s.societeContactId.societe.id=:societeId and s.societeContactId.contact.id=:contactid and s.isDeleted=false " )
    Optional<SocieteContact> findByIds(@Param("societeId") Integer societeId, @Param("contactid") Integer contactid);

    @Query("select s from SocieteContact s where s.societeContactId.societe.id=:societeId and s.isDeleted = false ")
    List<SocieteContact> findBySocieteId(Integer societeId);
    @Query("select s from SocieteContact s where s.societeContactId.societe.id=:societeId and s.isDeleted = false")
    Page<SocieteContact> findBySocieteId(Integer societeId, Specification<SocieteContact> societeContact, Pageable pageable);
    @Query("select s from SocieteContact s where s.societeContactId.societe.id=:societeId and s.isDeleted = true")
    Page<SocieteContact> findSocieteContactsDeletedBySocieteId(Integer societeId, Pageable pageable);
    @Query("select s from SocieteContact s where s.societeContactId.societe.id=:societeId and s.isDeleted = true")
    Page<SocieteContact> findSocieteContactsDeletedBySocieteId(Integer societeId, Specification<SocieteContact> societeContact, Pageable pageable);


}
