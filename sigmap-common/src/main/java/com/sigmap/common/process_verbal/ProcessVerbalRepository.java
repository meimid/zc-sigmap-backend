package com.sigmap.common.process_verbal;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessVerbalRepository extends JpaRepository<ProcessVerbal, Long>  {
    ProcessVerbal findByPpmIdAndPpmVersionAndProcessVerbalId(Integer ppmId, Integer ppmVersion, String processVerbalId);
}
