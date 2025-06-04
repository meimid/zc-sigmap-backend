package com.sigmap.common.dyna_docs;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaDocsRepository
    extends JpaRepository<DynaDocs, DynaDocId>, JpaSpecificationExecutor<DynaDocs> {

  @Query(
      "SELECT d FROM DynaDocs d WHERE d.status = 1 AND d.categorieId.id = :categorieId AND"
          + " d.dynaDocsVer = (SELECT MAX(d2.dynaDocsVer) FROM DynaDocs d2 WHERE d2.dynaDocsId ="
          + " d.dynaDocsId AND d2.categorieId.id = :categorieId AND d2.status = 1)")
  List<DynaDocs> findAllActifForCategorie(@Param("categorieId") Integer categorieId);
}
