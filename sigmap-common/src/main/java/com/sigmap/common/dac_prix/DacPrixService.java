package com.sigmap.common.dac_prix;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.dac_prix.exceptions.DacPrixNotFound;
import com.sigmap.common.dac_prix.request.DacPrixRequest;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DacPrixService {
    DacPrix save(DacPrixRequest request) throws MonnaieNotFound;
    
    DacPrix update(DacPrixRequest request) ;
    
    DacPrix findById(Integer dacId, Integer dacVer, Integer monnaieId);
    Page<DacPrix> getAllDacPrix(Specification<DacPrix> spec, Pageable pageable);
}
