package com.sigmap.common.parametrage_process_dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ParametrageProcessDacRepository extends JpaRepository<ParametrageProcessDac, ParametrageProcessDacId>,
        JpaSpecificationExecutor<ParametrageProcessDac> {
}
