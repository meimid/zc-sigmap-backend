package com.sigmap.common.dac;

import com.sigmap.common.dac_docs.DacDocs;
import java.util.List;
import java.util.Optional;

import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  
  @Query("SELECT d FROM Dac d WHERE d.appelOffre.realisation.id = :realisationId AND d.appelOffre.realisation.version = :realisationVer AND d.isDeleted = false and d.status = 'PUBLIE' and d.shortListing = true")
  List<Dac> findPublishedDacsByRealisationIdAndVersion(
      @Param("realisationId") Integer realisationId, 
      @Param("realisationVer") Integer realisationVer);


  /**
   * Récupère les clés composites keys (id, version) des réalisations qui sont liées à des DAC
   * @return liste des clés primaires des réalisations liées à des DAC
   */

 // @Query("SELECT DISTINCT d.realisation.id, d.realisation.version FROM Dac d WHERE d.realisation IS NOT NULL and d.isDeleted = false")
 // List<Object[]> findRealisationKeysWithDac();

  //@Query("SELECT d FROM Dac d WHERE d.realisation.id = :id and d.realisation.version = :version and d.isDeleted = false")
  //Optional<Dac> findByRealisationIdAndVersion(@Param("id") Integer id, @Param("version") Integer version);


  @Query("SELECT DISTINCT d.appelOffre.realisation.id, d.appelOffre.realisation.version FROM Dac d WHERE d.appelOffre.realisation IS NOT NULL and d.isDeleted = false")
  List<Object[]> findRealisationKeysWithDac();

  @Query("SELECT DISTINCT d.appelOffre.realisation.id, d.appelOffre.realisation.version FROM Dac d WHERE d.appelOffre.realisation IS NOT NULL and d.isDeleted = false AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false) AND d.status = 'PUBLIE'")
  List<Object[]> findRealisationKeysWithPublishedDac();

  @Query("SELECT DISTINCT d.appelOffre.realisation.id, d.appelOffre.realisation.version FROM Dac d WHERE d.appelOffre.realisation IS NOT NULL AND d.isDeleted = false AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false) AND d.status <> 'PUBLIE'")
  List<Object[]> findRealisationKeysWithNonPublishedDac();

  @Query("SELECT DISTINCT d.appelOffre.realisation.id, d.appelOffre.realisation.version, COUNT(d) " +
          "FROM Dac d WHERE d.appelOffre.realisation IS NOT NULL AND d.isDeleted = false " +
          "GROUP BY d.appelOffre.realisation.id, d.appelOffre.realisation.version")
  List<Object[]> findRealisationKeysWithDacCount();

  @Query("SELECT d FROM Dac d WHERE d.appelOffre.realisation.id = :id and d.appelOffre.realisation.version = :version and d.isDeleted = false")
  List<Dac> findByRealisationIdAndVersion(@Param("id") Integer id, @Param("version") Integer version);

  @Query("SELECT COUNT (*) FROM Dac d")
  Integer findMaxDacNumber () ;

  @Query("SELECT COUNT (DISTINCT d.dacId) FROM Dac d WHERE d.appelOffre.realisation.id = :id AND d.appelOffre.realisation.version = :version AND d.isDeleted = false")
  Integer countDistinctDacByRealisation(@Param("id") Integer id, @Param("version") Integer version);

  @Query("SELECT d FROM Dac d WHERE d.isDeleted = false AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false)")
  Page<Dac> findLatestPerDac(Pageable pageable);

  @Query("SELECT d FROM Dac d " +
          "LEFT JOIN d.appelOffre.ppm p " +
          "LEFT JOIN p.ac a " +
          "WHERE d.isDeleted = false " +
          "AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false) " +
          "AND a.id IN :acIds")
  Page<Dac> findLatestPerDacByAcIds(@Param("acIds") List<Integer> acIds, Pageable pageable);

  /**
   * Find the maximum version for a given DAC ID.
   *
   * @param id the DAC ID
   * @return the maximum version number for the specified ID, or null if not found
   */
  @Query("SELECT MAX(d.dacVer) FROM Dac d WHERE d.dacId = :id AND d.isDeleted = false")
  Integer findMaxVersionById(@Param("id") Integer id);

  @Query("SELECT COUNT(d) FROM Dac d WHERE d.appelOffre.id = :appelOffreId AND d.isDeleted = false AND d.appelOffre.isDeleted = false ")
  Integer getDacCreatedNumberForAppelOffre(@Param("appelOffreId") Integer appelOffreId);

  /**
   * Find latest version of DACs for a specific Appel d'Offre.
   * Returns only the latest version of each DAC where appelOffre.id matches.
   *
   * @param appelOffreId The Appel d'Offre ID to filter by
   * @param pageable Pagination information
   * @return Page of latest DACs for the specified Appel d'Offre
   */
  @Query("SELECT d FROM Dac d " +
         "WHERE d.appelOffre.id = :appelOffreId " +
         "AND d.isDeleted = false " +
         "AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false)")
  Page<Dac> findLatestDacsByAppelOffre(@Param("appelOffreId") Integer appelOffreId, Pageable pageable);

  /**
   * Find all published DACs for the website.
   * Returns only the latest version of each DAC where status is PUBLIE,
   * shortListing is false, and etapeDac is "1".
   * Results are ordered by updatedAt descending (most recently modified first).
   *
   * @param pageable Pagination information
   * @return Page of published DACs
   */
  @Query("SELECT d FROM Dac d " +
         "WHERE d.status = 'PUBLIE' " +
         "AND d.shortListing = false " +
         "AND d.etapeDac = '1' " +
         "AND d.isDeleted = false " +
         "AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false) " +
         "ORDER BY d.updatedAt DESC")
  Page<Dac> findPublishedDacsForWebsite(Pageable pageable);

  /**
   * Find published DACs for a specific user based on their associated societies.
   * Returns only DACs where shortListing=true or etapeDac='2', and only the latest version.
   *
   * @param userId The authenticated user ID
   * @param pageable Pagination information
   * @return Page of published DACs for the user
   */
  @Query("""
      SELECT d FROM Dac d
      WHERE d.status = 'PUBLIE'
      AND d.isDeleted = false
      AND (d.shortListing = true OR d.etapeDac = '2')
      AND d.dacVer = (
          SELECT MAX(sd.id.dac.dacVer)
          FROM SocieteDac sd
          WHERE sd.id.dac.dacId = d.dacId
          AND sd.id.societe.id IN (
              SELECT us.societeId FROM UserSociete us 
              WHERE us.userId = :userId AND us.isDeleted = false
          )
      )
      ORDER BY d.updatedAt DESC
      """)
  Page<Dac> findPublishedDacsForUser(@Param("userId") String userId, Pageable pageable);

  @Query("SELECT d FROM Dac d " +
         "WHERE d.dacId = :dacId " +
         "AND d.status = 'PUBLIE' " +
         "AND d.isDeleted = false " +
         "AND (d.shortListing = true OR d.etapeDac = '2') " +
         "AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false) " +
         "AND EXISTS ( " +
         "    SELECT sd FROM SocieteDac sd " +
         "    WHERE sd.id.dac.dacId = d.dacId " +
         "    AND sd.id.dac.dacVer = d.dacVer " +
         "    AND sd.id.societe.id IN ( " +
         "        SELECT us.societeId FROM UserSociete us " +
         "        WHERE us.userId = :userId AND us.isDeleted = false " +
         "    ) " +
         ")")
  Optional<Dac> findPublishedDacByIdForUser(@Param("dacId") Integer dacId, @Param("userId") String userId);

  /**
   * Find a specific published DAC by its ID for the website.
   * Ensures it's the latest version, status is PUBLIE, shortListing is false,
   * etapeDac is '1', and not deleted.
   *
   * @param dacId The DAC ID
   * @return Optional containing the DAC if found and criteria are met
   */
  @Query("SELECT d FROM Dac d " +
         "WHERE d.dacId = :dacId " +
         "AND d.status = 'PUBLIE' " +
         "AND d.shortListing = false " +
         "AND d.etapeDac = '1' " +
         "AND d.isDeleted = false " +
         "AND d.dacVer = (SELECT MAX(dd.dacVer) FROM Dac dd WHERE dd.dacId = d.dacId AND dd.isDeleted = false)")
  Optional<Dac> findPublishedDacByIdForWebsite(@Param("dacId") Integer dacId);

  @Query("""
      SELECT d FROM Dac d
      WHERE d.status = 'PUBLIE'
      AND d.isDeleted = false
      AND (d.shortListing = true OR d.etapeDac = '2')
      AND d.dacId = :dacId
      AND d.dacVer = (
          SELECT MAX(sd.id.dac.dacVer)
          FROM SocieteDac sd
          WHERE sd.id.dac.dacId = :dacId
          AND sd.id.societe.id IN (
              SELECT us.societeId FROM UserSociete us 
              WHERE us.userId = :userId AND us.isDeleted = false
          )
      )
      ORDER BY d.updatedAt DESC
      """)
  Optional<Dac> findPublishedDacForUser(@Param("dacId") Integer dacId, @Param("userId") String userId);


  @Query("""
      SELECT d FROM Dac d
      WHERE d.status = 'PUBLIE'
      AND d.isDeleted = false
      AND (d.shortListing = false OR d.etapeDac = '1')
      AND d.dacId = :dacId
      AND d.dacVer = (
          SELECT MAX(dac.dacVer)
          FROM Dac dac
          WHERE dac.dacId = :dacId
      )
      ORDER BY d.updatedAt DESC
      """)
  Optional<Dac> findPublishedDacForUserNotShortListed(@Param("dacId") Integer dacId, @Param("userId") String userId);

}
