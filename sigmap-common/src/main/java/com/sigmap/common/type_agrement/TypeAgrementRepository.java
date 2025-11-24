package com.sigmap.common.type_agrement;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeAgrementRepository extends JpaRepository<TypeAgrement, Integer>,
        JpaSpecificationExecutor<TypeAgrement>,
        SoftDeleteRepository<TypeAgrement, Integer> {
}
