package com.sigmap.common.parametrage_process_dac;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.parametrage_process_dac.exeptions.ParametrageProcessDacNotFound;
import com.sigmap.common.parametrage_process_dac.request.ParametrageProcessDacRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface ParametrageProcessDacService {
    ParametrageProcessDac save(ParametrageProcessDacRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound;

    ParametrageProcessDac update(ParametrageProcessDacRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound, ParametrageProcessDacNotFound;

    Page<ParametrageProcessDac> findAll(Specification<ParametrageProcessDac> spec, Pageable pageable);
    
    Optional<ParametrageProcessDac> findOne(Specification<ParametrageProcessDac> specification);

    Optional<ParametrageProcessDac> findOne(Integer typeMarcherId, Integer modePassationId);
}
