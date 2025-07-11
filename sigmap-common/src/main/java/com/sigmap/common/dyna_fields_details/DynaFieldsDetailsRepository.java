package com.sigmap.common.dyna_fields_details;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaFieldsDetailsRepository
    extends JpaRepository<DynaFieldsDetails, DynaFieldsDetailsId>,
        JpaSpecificationExecutor<DynaFieldsDetails> {

  List<DynaFieldsDetails> findByDynaFieldsId(Integer dynaFieldsId);


  @Query("SELECT dfd FROM DynaFieldsDetails dfd WHERE dfd.dynaDocsId = :dynaDocsId AND dfd.dynaDocsVer = :dynaDocsVer" +
      " AND dfd.dynaFieldsId = :dynaFieldsId ")
  Optional<DynaFieldsDetails> findAllDynaFieldsDetails(@Param("dynaDocsId")Integer dynaDocsId,
                                                       @Param("dynaDocsVer")Integer dynaDocsVer,
                                                       @Param("dynaFieldsId")Integer dynaFieldsId);
}
