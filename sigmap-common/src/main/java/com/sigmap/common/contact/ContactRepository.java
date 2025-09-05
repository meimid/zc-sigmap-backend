package com.sigmap.common.contact;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface ContactRepository  extends JpaRepository<Contact, Integer>,
        JpaSpecificationExecutor<Contact>,
        SoftDeleteRepository<Contact, Integer> {
    @Query("SELECT sc.societeContactId.contact FROM SocieteContact sc WHERE sc.societeContactId.societe.id = :societeId and sc.societeContactId.contact.isDeleted=false and sc.societeContactId.societe.isDeleted=false")
    Page<Contact> findContactsBySocieteId(@Param("societeId") Integer societeId, Pageable pageable);


}

