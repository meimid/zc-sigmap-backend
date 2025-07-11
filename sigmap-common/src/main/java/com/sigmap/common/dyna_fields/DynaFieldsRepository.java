package com.sigmap.common.dyna_fields;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaFieldsRepository
    extends JpaRepository<DynaFields, Integer>, JpaSpecificationExecutor<DynaFields> {

  List<DynaFields> findByDynaDocsIdAndDynaDocsVer(Integer dynaDocsId, Integer dynaDocsVer);

  @Query(
          "SELECT d FROM DynaFields d WHERE d.dynaDocsId = :dynaDocsId AND d.dynaDocsVer = :dynaDocsVer AND d.status = 'ACTIF'")
  List<DynaFields> findByDynaDocsIdAndVer(@Param("dynaDocsId")Integer dynaDocsId,@Param("dynaDocsVer") Integer dynaDocsVer);
}
