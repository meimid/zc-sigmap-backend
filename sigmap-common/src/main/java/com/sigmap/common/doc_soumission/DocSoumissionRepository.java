package com.sigmap.common.doc_soumission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository

public interface DocSoumissionRepository extends JpaRepository<DocSoumission, Integer>,
        JpaSpecificationExecutor<DocSoumission>{
}
