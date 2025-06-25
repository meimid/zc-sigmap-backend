package com.sigmap.common.pv_docs;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PvDocsRepository extends JpaRepository<PvDocs, PvDocsId> {

    @Query("SELECT d.name FROM PvDocs pvd JOIN Docs d ON pvd.docId = d.id WHERE pvd.ppmId = :ppmId AND pvd.ppmVersion = :ppmVersion AND pvd.processVerbalId = :processVerbalId AND d.isDeleted = false")
    List<String> findDocumentNamesByPpmAndProcessVerbal(@Param("ppmId") Integer ppmId,
                                                        @Param("ppmVersion") Integer ppmVersion,
                                                        @Param("processVerbalId") String processVerbalId);
    List<PvDocs> findByPpmIdAndPpmVersionAndProcessVerbalId(Integer ppmId, Integer ppmVer, String processVerbalId);
    @Modifying
    @Transactional
    @Query("DELETE FROM PvDocs d WHERE d.ppmId = :ppmId AND d.ppmVersion = :ppmVersion AND d.processVerbalId = :processVerbalId")
    void deleteByPpmIdAndPpmVersionAndProcessVerbalId(
            @Param("ppmId") Integer ppmId,
            @Param("ppmVersion") Integer ppmVersion,
            @Param("processVerbalId") String processVerbalId);

}

