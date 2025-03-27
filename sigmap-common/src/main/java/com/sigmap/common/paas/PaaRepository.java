package com.sigmap.common.paas;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaaRepository extends JpaRepository<Paa, CompositeId>,JpaSpecificationExecutor<Paa>,
        SoftDeleteRepository<Paa, Integer> {
}
