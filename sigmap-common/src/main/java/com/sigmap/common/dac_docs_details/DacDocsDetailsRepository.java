package com.sigmap.common.dac_docs_details;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DacDocsDetailsRepository extends JpaRepository<DacDocsDetails, DacDocsDetailsId>, JpaSpecificationExecutor<DacDocsDetails> {
} 