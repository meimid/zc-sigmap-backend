package com.sigmap.common.process_verbal_reunion_doc;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessVerbalReunionDocRespository extends JpaRepository<ProcessVerbalReunionDoc, ProcessVerbalReunionDocId>, JpaSpecificationExecutor<ProcessVerbalReunionDoc> {

}
