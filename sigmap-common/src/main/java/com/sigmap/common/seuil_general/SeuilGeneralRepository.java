package com.sigmap.common.seuil_general;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SeuilGeneralRepository extends JpaRepository<SeuilGeneral, Integer>,
                                                JpaSpecificationExecutor<SeuilGeneral>,
                                                SoftDeleteRepository<SeuilGeneral, Integer> {

}
