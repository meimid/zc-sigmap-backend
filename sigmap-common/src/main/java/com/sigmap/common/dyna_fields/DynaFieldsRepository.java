package com.sigmap.common.dyna_fields;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaFieldsRepository
    extends JpaRepository<DynaFields, Integer>, JpaSpecificationExecutor<DynaFields> {

  List<DynaFields> findByDynaDocsIdAndDynaDocsVer(Integer dynaDocsId, Integer dynaDocsVer);
}
