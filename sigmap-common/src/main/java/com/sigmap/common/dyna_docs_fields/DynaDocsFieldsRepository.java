package com.sigmap.common.dyna_docs_fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaDocsFieldsRepository extends JpaRepository<DynaDocsFields, DynaDocsFieldsId> {
}
