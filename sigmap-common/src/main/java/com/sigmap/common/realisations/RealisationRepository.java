package com.sigmap.common.realisations;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RealisationRepository extends JpaRepository<Realisation, CompositeId>,
        JpaSpecificationExecutor<Realisation>,
        SoftDeleteRepository<Realisation, Integer> {}