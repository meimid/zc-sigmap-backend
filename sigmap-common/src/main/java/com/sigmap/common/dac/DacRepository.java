package com.sigmap.common.dac;

import com.sigmap.common.dac_docs.DacDocs;
import java.util.List;
import java.util.Optional;

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


  @Query("SELECT d FROM Dac d WHERE d.dacId = :dacId AND d.dacVer = " +
          "(SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = :dacId) AND d.isDeleted = false")
  Dac findDacWithLatestVersionByDacId(@Param("dacId") Integer dacId);

  @Query("SELECT d FROM Dac d WHERE d.dacId = :dacId AND d.dacVer = :dacVer")
  Optional<Dac> findDacByDacIdAndDacVer(@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);


  /**
   * Récupère les clés composites keys (id, version) des réalisations qui sont liées à des DAC
   * @return liste des clés primaires des réalisations liées à des DAC
   */

 // @Query("SELECT DISTINCT d.realisation.id, d.realisation.version FROM Dac d WHERE d.realisation IS NOT NULL and d.isDeleted = false")
 // List<Object[]> findRealisationKeysWithDac();

  //@Query("SELECT d FROM Dac d WHERE d.realisation.id = :id and d.realisation.version = :version and d.isDeleted = false")
  //Optional<Dac> findByRealisationIdAndVersion(@Param("id") Integer id, @Param("version") Integer version);


  @Query("SELECT DISTINCT d.realisation.id, d.realisation.version FROM Dac d WHERE d.realisation IS NOT NULL and d.isDeleted = false")
  List<Object[]> findRealisationKeysWithDac();

  @Query("SELECT d FROM Dac d WHERE d.realisation.id = :id and d.realisation.version = :version and d.isDeleted = false")
  Optional<Dac> findByRealisationIdAndVersion(@Param("id") Integer id, @Param("version") Integer version);

  @Query("SELECT COUNT (*) FROM Dac d")
  Integer findMaxDacNumber () ;

  @Query("SELECT d FROM Dac d WHERE d.isDeleted = false AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false)")
  org.springframework.data.domain.Page<Dac> findLatestPerDac(org.springframework.data.domain.Pageable pageable);

  /**
   * Find the maximum version for a given DAC ID.
   *
   * @param id the DAC ID
   * @return the maximum version number for the specified ID, or null if not found
   */
  @Query("SELECT MAX(d.dacVer) FROM Dac d WHERE d.dacId = :id AND d.isDeleted = false")
  Integer findMaxVersionById(@Param("id") Integer id);

}
