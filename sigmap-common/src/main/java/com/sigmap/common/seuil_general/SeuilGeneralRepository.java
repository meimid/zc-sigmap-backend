package com.sigmap.common.seuil_general;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeuilGeneralRepository extends JpaRepository<SeuilGeneral, Integer>,
                                                JpaSpecificationExecutor<SeuilGeneral>,
                                                SoftDeleteRepository<SeuilGeneral, Integer> {

    @Query("SELECT s FROM SeuilGeneral s WHERE s.typeMarcher.id = :typeMarcherId")
    SeuilGeneral findByTypeMarcherId(@Param("typeMarcherId") Integer typeMarcherId);

}
