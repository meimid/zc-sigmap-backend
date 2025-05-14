package com.sigmap.common.dac_docs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DacDocsRepository extends JpaRepository<DacDocs, DacDocsId> {
}
