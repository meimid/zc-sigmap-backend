package com.sigmap.common.ref_params;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RefParamRepository  extends JpaRepository<RefParam, Integer>,
        JpaSpecificationExecutor<RefParam>,
        SoftDeleteRepository<RefParam, Integer> {}
