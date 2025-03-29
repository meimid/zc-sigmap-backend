package com.sigmap.common.societe_address;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SocieteAddressRepository  extends JpaRepository<SocieteAddress, Integer>,
        JpaSpecificationExecutor<SocieteAddress>,
        SoftDeleteRepository<SocieteAddress, Integer> {}
