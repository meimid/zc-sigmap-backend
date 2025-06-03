package com.sigmap.common.dyna_docs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DynaDocsRepository extends JpaRepository<DynaDocs, Integer> {

    @Query("SELECT d FROM DynaDocs d WHERE d.status = 1 AND d.categorieId.id = :categorieId " +
            "AND d.dynaDocsVer = (SELECT MAX(d2.dynaDocsVer) FROM DynaDocs d2 " +
            "WHERE d2.dynaDocsId = d.dynaDocsId AND d2.categorieId.id = :categorieId AND d2.status = 1)")
    List<DynaDocs> findAllActifForCategorie(@Param("categorieId") Integer categorieId);
}

