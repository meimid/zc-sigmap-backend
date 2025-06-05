package com.sigmap.common.dyna_fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynaFieldsRepository extends JpaRepository<DynaFields, Integer> {

    List<DynaFields> findByDynaDocsIdAndDynaDocsVer(Integer dynaDocsId, Integer dynaDocsVer);
}

