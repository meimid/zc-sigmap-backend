package com.sigmap.common.modified_dyna_docs_for_dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModifiedDynaDocsRepository extends JpaRepository<ModifiedDynaDocs, Integer> {

    @Query("SELECT md FROM ModifiedDynaDocs md WHERE md.dac.dacId = :dacId AND md.dac.dacVer = :dacVer And md.status = 'ACTIF'")
    List<ModifiedDynaDocs> findByDacIdAndVersion(@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);

}
