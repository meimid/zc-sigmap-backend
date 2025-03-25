package com.sigmap.common.ppms;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PpmRepository extends JpaRepository<Ppm, Integer>,
        JpaSpecificationExecutor<Ppm>,
        SoftDeleteRepository<Ppm, Integer> {}

