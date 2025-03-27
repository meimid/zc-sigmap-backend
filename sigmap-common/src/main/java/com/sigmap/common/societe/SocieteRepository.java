package com.sigmap.common.societe;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SocieteRepository  extends JpaRepository<Societe, Integer>,
        JpaSpecificationExecutor<Societe>,
        SoftDeleteRepository<Societe, Integer> {}

