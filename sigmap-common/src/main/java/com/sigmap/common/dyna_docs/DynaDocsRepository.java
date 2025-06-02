package com.sigmap.common.dyna_docs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DynaDocsRepository extends JpaRepository<DynaDocs, Integer> {
    List<DynaDocs> findAllByIdIn(Set<DynaDocID> ids);
    @Query("SELECT d FROM DynaDocs d WHERE d.status = 1 AND d.categorieId.id = :categorieId " +
            "AND d.id.dynaDocsVer = (SELECT MAX(d2.id.dynaDocsVer) FROM DynaDocs d2 " +
            "WHERE d2.id.dynaDocsId = d.id.dynaDocsId AND d2.categorieId.id = :categorieId AND d2.status = 1)")
    List<DynaDocs> findAllActifForCategorie(@Param("categorieId") Integer categorieId);
}

