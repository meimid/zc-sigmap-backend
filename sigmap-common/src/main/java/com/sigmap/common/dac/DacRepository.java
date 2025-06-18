package com.sigmap.common.dac;

import com.sigmap.common.dac_docs.DacDocs;
import java.util.List;

import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DacRepository extends JpaRepository<Dac, DacId> ,
        JpaSpecificationExecutor<Dac>,
        SoftDeleteRepository<MenuItem, Integer> {

  @Query("SELECT d FROM DacDocs d WHERE d.id.dacId = :dacId AND d.id.dacVer = :dacVer")
  List<DacDocs> findByDacIdAndDacVer(
      @Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);
      
  /**
   * Récupère les clés composites keys (id, version) des réalisations qui sont liées à des DAC
   * @return liste des clés primaires des réalisations liées à des DAC
   */
  @Query("SELECT DISTINCT d.realisation.id, d.realisation.version FROM Dac d WHERE d.realisation IS NOT NULL and d.isDeleted = false")
  List<Object[]> findRealisationKeysWithDac();
}
