package com.sigmap.common.reunion_ppm_doc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReunionPpmDocRepository extends JpaRepository<ReunionPpmDoc, ReunionPpmDocId>, JpaSpecificationExecutor<ReunionPpmDoc> {
}
