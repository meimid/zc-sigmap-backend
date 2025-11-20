package com.sigmap.common.activite;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Integer>,
        JpaSpecificationExecutor<Activite>,
        SoftDeleteRepository<Activite, Integer> {
}

