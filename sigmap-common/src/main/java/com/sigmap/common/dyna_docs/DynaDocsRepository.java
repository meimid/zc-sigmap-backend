package com.sigmap.common.dyna_docs;

import java.util.List;
import java.util.Optional;

import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.entity.CompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaDocsRepository
    extends JpaRepository<DynaDocs, CompositeId>, JpaSpecificationExecutor<DynaDocs> {

  @Query(
      "SELECT d FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.typeMarche.id = :typeMarcheId AND"
          + " d.version = (SELECT MAX(d2.version) FROM DynaDocs d2 WHERE d2.id ="
          + " d.id AND d2.typeMarche.id = :typeMarcheId AND d2.status = 'ACTIF')")

  List<DynaDocs> findAllActifForTypeMarche(@Param("typeMarcheId") Integer typeMarcheId);

  @Query(
      "SELECT d FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.typeMarche.id = :typeMarcheId AND d.modePassation.id = :modePassationId AND"
          + " d.version = (SELECT MAX(d2.version) FROM DynaDocs d2 WHERE d2.id ="
          + " d.id AND d2.typeMarche.id = :typeMarcheId AND d2.modePassation.id = :modePassationId AND d2.status = 'ACTIF')")
  List<DynaDocs> findAllActifForTypeMarcheAndModePassation(@Param("typeMarcheId") Integer typeMarcheId, @Param("modePassationId") Integer modePassationId);

  @Query("SELECT d FROM DynaDocs d WHERE d.id = :dynaDocsId AND d.version = :dynaDocsVer")
  java.util.Optional<DynaDocs> findByIdDynaDocsIdAndDynaDocsVer(@Param("dynaDocsId") Integer dynaDocsId, @Param("dynaDocsVer") Integer dynaDocsVer);


  /*@Query(
          "SELECT d FROM DynaDocs d WHERE d.id = :dynaDocsId AND d.status = 'ACTIF' AND d.version = " +
          "( SELECT MAX(dd) FROM DynaDocs dd WHERE d.status = 'ACTIF' AND dd.id = :dynaDocsId )")*/

  @Query(
          "SELECT d FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.id = :id AND"
                  + " d.version = (SELECT MAX(d2.version) FROM DynaDocs d2 WHERE d2.id ="
                  + " d.id AND d2.id = :id AND d2.status = 'ACTIF')")
  Optional<DynaDocs> findLastDynaDocsVersionById(@Param("id") Integer dynaDocsId);


  @Query("""
    SELECT d FROM DynaDocs d
    WHERE d.status = 'ACTIF'
      AND d.typeMarche.id = :typeMarcheId
      AND d.version = (
        SELECT MAX(d2.version)
        FROM DynaDocs d2
        WHERE d2.id = d.id
          AND d2.typeMarche.id = :typeMarcheId
          AND d2.status = 'ACTIF'
      )
""")
  List<DynaDocs> findAllLastVersionByTypeMarcheId(@Param("typeMarcheId") Integer typeMarcheId);

  @Query("""
    SELECT d FROM DynaDocs d
    WHERE d.status = 'ACTIF'
      AND d.typeMarche.id = :typeMarcheId
      AND d.modePassation.id = :modePassationId
      AND d.version = (
        SELECT MAX(d2.version)
        FROM DynaDocs d2
        WHERE d2.id = d.id
          AND d2.typeMarche.id = :typeMarcheId
          AND d2.modePassation.id = :modePassationId
          AND d2.dynaDocsModel.dynaDocsModelId = :modelId
          AND d2.status = 'ACTIF')""")
  List<DynaDocs> findAllLastVersionByTypeMarcheIdAndModePassationId(@Param("typeMarcheId") Integer typeMarcheId,
                                                                    @Param("modePassationId") Integer modePassationId,
                                                                    @Param("modelId") Integer modelId);




  @Query("""
    SELECT d FROM DynaDocs d
    WHERE d.status = 'ACTIF'
      AND d.version = (
        SELECT MAX(d2.version)
        FROM DynaDocs d2
        WHERE d2.id = d.id)""")
  List<DynaDocs> findAllLast();

  @Query("SELECT d.name FROM DynaDocs d WHERE d.id = :dynaDocsId AND d.version = :dynaDocsVer")
  String getDynaDocName(@Param("dynaDocsId") Integer dynaDocsId, @Param("dynaDocsVer") Integer dynaDocsVer);

  /**
   * Check if there exists an active AAO DynaDocs for the given typeMarche and modePassation
   */
  @Query("SELECT COUNT(d) FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.isAAO = true AND d.typeMarche.id = :typeMarcheId AND d.modePassation.id = :modePassationId")
  Integer existsActiveAAOByTypeMarcheAndModePassation(@Param("typeMarcheId") Integer typeMarcheId, @Param("modePassationId") Integer modePassationId);

  /**
   * Check if there exists an active AAO DynaDocs for the given typeMarche (when modePassation is null)
   */
  @Query("SELECT COUNT(d) > 0 FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.isAAO = true AND d.typeMarche.id = :typeMarcheId AND d.modePassation IS NULL")
  boolean existsActiveAAOByTypeMarche(@Param("typeMarcheId") Integer typeMarcheId);

  /**
   * Find Latest Version active AAO DynaDocs for the given typeMarche and modePassation
   */
  @Query("""
    SELECT d FROM DynaDocs d
    WHERE d.version = (
        SELECT MAX(d2.version)
        FROM DynaDocs d2
        WHERE d2.id = d.id
    )
    AND d.status = 'ACTIF'
    AND d.isAAO = true
    AND d.typeMarche.id = :typeMarcheId
    AND d.modePassation.id = :modePassationId
    """)
  List<DynaDocs> findActiveAAOByTypeMarcheAndModePassation(@Param("typeMarcheId") Integer typeMarcheId, @Param("modePassationId") Integer modePassationId);

  /**
   * Find active AAO DynaDocs for the given typeMarche (when modePassation is null)
   */
  @Query("SELECT d FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.isAAO = true AND d.typeMarche.id = :typeMarcheId AND d.modePassation IS NULL")
  List<DynaDocs> findActiveAAOByTypeMarche(@Param("typeMarcheId") Integer typeMarcheId);

  @Query("SELECT COUNT(d) > 0 FROM DynaDocs d WHERE d.dynaDocsModel.dynaDocsModelId = :modelId")
  boolean existsByDynaDocsModelId(@Param("modelId") Integer modelId);

}
