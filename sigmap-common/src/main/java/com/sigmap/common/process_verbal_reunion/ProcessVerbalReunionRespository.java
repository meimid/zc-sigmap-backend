package com.sigmap.common.process_verbal_reunion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessVerbalReunionRespository extends JpaRepository<ProcessVerbalReunion, Integer>, JpaSpecificationExecutor<ProcessVerbalReunion> {
}
