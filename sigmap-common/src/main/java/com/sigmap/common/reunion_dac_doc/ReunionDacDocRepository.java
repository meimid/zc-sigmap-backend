package com.sigmap.common.reunion_dac_doc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReunionDacDocRepository extends JpaRepository<ReunionDacDoc, ReunionDacDocId>, JpaSpecificationExecutor<ReunionDacDoc> {
}
