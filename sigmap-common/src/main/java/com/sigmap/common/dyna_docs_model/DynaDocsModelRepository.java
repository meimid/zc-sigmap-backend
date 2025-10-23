package com.sigmap.common.dyna_docs_model;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaDocsModelRepository extends JpaRepository<DynaDocsModel, Integer>,
                                                 JpaSpecificationExecutor<DynaDocsModel>,
                                                 SoftDeleteRepository<DynaDocsModel, Integer> {
}
