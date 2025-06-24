package com.sigmap.common.pv_docs.response;

import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_docs.PvDocsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PvDocsRepository extends JpaRepository<PvDocs, PvDocsId> {
}
