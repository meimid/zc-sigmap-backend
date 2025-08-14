package com.sigmap.common.ac_seuil;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AcSeuilRepository extends JpaRepository<AcSeuil, AcSeuilId>,
                                           JpaSpecificationExecutor<AcSeuil> {
}
