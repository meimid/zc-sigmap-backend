package com.sigmap.common.doc_soumission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface DocSoumissionRepository extends JpaRepository<DocSoumission, Integer>,
        JpaSpecificationExecutor<DocSoumission>{

    @Query("SELECT ds FROM DocSoumission ds WHERE ds.id NOT IN ( SELECT dds.docSoumissionId FROM DacDocSoumission dds WHERE dds.dacId = :dacId ) ")
    List<DocSoumission> findNotExistingDocSoumissionOfDacDocSoumissionByDacId(@Param("dacId")Integer dacId);
}
