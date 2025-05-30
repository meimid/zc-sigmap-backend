package com.sigmap.common.dyna_docs_fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynaDocsFieldsRepository extends JpaRepository<DynaDocsFields, DynaDocsFieldsId> {

    @Query("SELECT d FROM DynaDocsFields d  WHERE d.id.dynaDocsId.id.dynaDocsId = :dynaDocsId AND " +
            " d.id.dynaDocsId.id.dynaDocsVer = :dynaDocsVer AND d.status = :status order by d.dynaFieldOrder asc " )
    List<DynaDocsFields> findByIdDynaDocsIdAndIdDynaDocsVerAndStatus( @Param("dynaDocsId") Integer dynaDocsId,
                                                                      @Param("dynaDocsVer") Integer dynaDocsVer,
                                                                      @Param("status") Integer status);
}
