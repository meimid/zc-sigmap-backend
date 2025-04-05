package com.sigmap.common.ref_types;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RefTypeRepository extends JpaRepository<RefType, Integer>, JpaSpecificationExecutor<RefType>,
        SoftDeleteRepository<RefType, Integer> {}
