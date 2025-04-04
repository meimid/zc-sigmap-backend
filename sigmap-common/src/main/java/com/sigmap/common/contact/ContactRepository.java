package com.sigmap.common.contact;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository  extends JpaRepository<Contact, Integer>,
        JpaSpecificationExecutor<Contact>,
        SoftDeleteRepository<Contact, Integer> {}

