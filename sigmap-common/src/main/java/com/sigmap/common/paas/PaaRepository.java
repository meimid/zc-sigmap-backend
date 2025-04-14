package com.sigmap.common.paas;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PaaRepository extends JpaRepository<Paa, CompositeId>,JpaSpecificationExecutor<Paa>,
        CompositeSoftDeleteRepository<Paa, Integer> {
}
