package com.sigmap.common.dyna_docs_model;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynaDocsModelRepository extends JpaRepository<DynaDocsModel, Integer>,
                                                 JpaSpecificationExecutor<DynaDocsModel>,
                                                 SoftDeleteRepository<DynaDocsModel, Integer> {

    @Query(" SELECT d from DynaDocsModel d where d.isDeleted = false")
    List<DynaDocsModel> findAllNotDeleted();
}
