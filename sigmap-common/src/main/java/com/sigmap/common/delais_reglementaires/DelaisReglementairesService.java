package com.sigmap.common.delais_reglementaires;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.delais_reglementaires.exceptions.DelaisReglementairesNotFound;
import com.sigmap.common.delais_reglementaires.request.DelaisReglementairesRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface DelaisReglementairesService {

    DelaisReglementaires save(DelaisReglementairesRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound;

    DelaisReglementaires update(DelaisReglementairesRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound, DelaisReglementairesNotFound;

    Page<DelaisReglementaires> getAllDelaisReglementaires(Specification<DelaisReglementaires> spec, Pageable pageable);

   // Page<DelaisReglementaires> findByTypeMarcherId(Integer typeMarcherId, Pageable pageable) throws DelaisReglementairesNotFound;

    Optional<DelaisReglementaires> findOne(Specification<DelaisReglementaires> specification);
}
