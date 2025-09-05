package com.sigmap.common.ac_seuil;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac_seuil.exeptions.AcSeuilNotFound;
import com.sigmap.common.ac_seuil.request.AcSeuilRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface AcSeuilService {

    AcSeuil save(AcSeuilRequest request) throws AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound;

    AcSeuil update(AcSeuilRequest request) throws AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound, AcSeuilNotFound;

    Page<AcSeuil> getAllAcSeuils(Specification<AcSeuil> spec, Pageable pageable);

    Page<AcSeuil> findById(Integer acId, Pageable pageable) throws AcSeuilNotFound;

    Optional<AcSeuil> findOne(Specification<AcSeuil> specification);
}
