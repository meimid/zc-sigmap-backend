package com.sigmap.common.dyna_docs;

import java.util.List;
import java.util.Optional;

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


  @Query("SELECT d.name FROM DynaDocs d WHERE d.id = :dynaDocsId AND d.version = :dynaDocsVer")
  String getDynaDocName(@Param("dynaDocsId") Integer dynaDocsId, @Param("dynaDocsVer") Integer dynaDocsVer);


}
