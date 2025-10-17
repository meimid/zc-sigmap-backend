package com.sigmap.common.dac_doc_soumission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DacDocSoumissionRepository extends JpaRepository<DacDocSoumission, DacDocSoumissionId>, JpaSpecificationExecutor<DacDocSoumission> {
}
