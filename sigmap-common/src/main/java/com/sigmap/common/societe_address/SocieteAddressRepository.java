package com.sigmap.common.societe_address;

import com.sigmap.common.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SocieteAddressRepository  extends JpaRepository<SocieteAddress, Integer>,
        JpaSpecificationExecutor<SocieteAddress>,
        SoftDeleteRepository<SocieteAddress, Integer> {
    Page<SocieteAddress> findSocieteAddressBySocieteId(Integer societeId, Pageable pageable);
}
