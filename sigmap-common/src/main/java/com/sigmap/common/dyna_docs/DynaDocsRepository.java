package com.sigmap.common.dyna_docs;

import java.util.List;

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
      "SELECT d FROM DynaDocs d WHERE d.status = 'ACTIF' AND d.categorieId.id = :categorieId AND"
          + " d.version = (SELECT MAX(d2.version) FROM DynaDocs d2 WHERE d2.id ="
          + " d.id AND d2.categorieId.id = :categorieId AND d2.status = 'ACTIF')")

  List<DynaDocs> findAllActifForCategorie(@Param("categorieId") Integer categorieId);

  @Query("SELECT d FROM DynaDocs d WHERE d.id = :dynaDocsId AND d.version = :dynaDocsVer")
  java.util.Optional<DynaDocs> findByIdDynaDocsIdAndDynaDocsVer(@Param("dynaDocsId") Integer dynaDocsId, @Param("dynaDocsVer") Integer dynaDocsVer);
}
