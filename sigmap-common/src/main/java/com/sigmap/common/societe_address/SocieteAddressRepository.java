package com.sigmap.common.societe_address;

import com.sigmap.common.repository.SoftDeleteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface SocieteAddressRepository  extends JpaRepository<SocieteAddress, Integer>,
        JpaSpecificationExecutor<SocieteAddress>,
        SoftDeleteRepository<SocieteAddress, Integer> {

    @Query("select sa from SocieteAddress sa where sa.societe.id= :societeId and sa.isDeleted=false and sa.societe.isDeleted=false")
    Page<SocieteAddress> findSocieteAddressBySocieteId(Integer societeId, Pageable pageable);
}
