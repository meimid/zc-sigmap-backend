package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModeDePassationSelectionImpl implements ModeDePassationSelectionService {
    private final ModeDePassationSelectionRepository repository;
    private final RefParamService refParamService;
    public ModeDePassationSelectionImpl(ModeDePassationSelectionRepository repository, RefParamService refParamService) {
        this.repository = repository;
        this.refParamService = refParamService;
    }

    @Override
    public List<ModeDePassationSelection> findByCategorieId(Integer categorieId) throws RefParamNotFound {
       refParamService.findById(categorieId);
        Specification<ModeDePassationSelection> spec = Specification.where(ModeDePassationSelectionSpecifications.hascategorieId(categorieId).and(ModeDePassationSelectionSpecifications.deleted(false)));
        return repository.findAll(spec);
    }
}
