package com.sigmap.common.ac;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AcRepository extends JpaRepository<Ac, Integer>,
        JpaSpecificationExecutor<Ac>,
        SoftDeleteRepository<Ac, Integer> {}

