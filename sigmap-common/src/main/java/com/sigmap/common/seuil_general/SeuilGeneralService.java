package com.sigmap.common.seuil_general;

import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.seuil_general.exception.SeuilGeneralNotFound;
import com.sigmap.common.seuil_general.request.SeuilGeneralRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface SeuilGeneralService {

    SeuilGeneral save(SeuilGeneralRequest request) throws RefParamNotFound;

    SeuilGeneral update(Integer id, SeuilGeneralRequest request) throws RefParamNotFound, SeuilGeneralNotFound;

    Page<SeuilGeneral> getAllSeuilGenerals(Specification<SeuilGeneral> spec, Pageable pageable);

    SeuilGeneral findById(Integer id) throws SeuilGeneralNotFound;

    Optional<SeuilGeneral> findOne(Specification<SeuilGeneral> specification);

    void delete(Integer id) throws SeuilGeneralNotFound;
}
