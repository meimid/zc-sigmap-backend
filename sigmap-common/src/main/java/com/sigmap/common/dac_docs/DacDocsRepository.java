package com.sigmap.common.dac_docs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DacDocsRepository extends JpaRepository<DacDocs, DacDocsId> {
    @Query("SELECT d FROM DacDocs d WHERE d.id.dac.id.dacId = :dacId AND d.id.dac.id.dacVer = :dacVer")
    List<DacDocs> findByDacIdAndDacVer(@Param("dacId") Integer dacId, @Param("dacVer")Integer dacVer);
}
