package com.sigmap.common.lots;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LotRepository extends JpaRepository<Lot, LotsId>,
        JpaSpecificationExecutor<Lot>,
        SoftDeleteRepository<Lot, LotsId> {}