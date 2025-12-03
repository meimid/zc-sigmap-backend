package com.sigmap.common.reunionPpm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReunionPpmRepository extends JpaRepository<ReunionPpm, ReunionPpmId>, JpaSpecificationExecutor<ReunionPpm> {

    @Query("""
        SELECT rp FROM ReunionPpm rp
        WHERE rp.ppmId = :ppmId AND rp.ppmVer = :ppmVer
        ORDER BY rp.reunionId DESC
    """)
    List<ReunionPpm> findAllByPpm(Integer ppmId, Integer ppmVer);

    default ReunionPpm findLatest(Integer ppmId, Integer ppmVer) {
        List<ReunionPpm> list = findAllByPpm(ppmId, ppmVer);
        return list.isEmpty() ? null : list.get(0);
    }

}
