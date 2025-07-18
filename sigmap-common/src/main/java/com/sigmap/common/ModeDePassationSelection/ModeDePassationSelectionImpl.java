package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.ModeDePassationSelection.exception.DuplicateCategorieIdsException;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionAlredyExist;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ModeDePassationSelection.mappers.ModeDePassationSelectionMapper;
import com.sigmap.common.ModeDePassationSelection.request.ModeDePassationSelectionRequest;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcSpecifications;
import com.sigmap.common.ac.exception.AcNomAlreadyExist;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public List<ModeDePassationSelection> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<ModeDePassationSelection> findAll(Specification<ModeDePassationSelection> modeDePassationSelection, Pageable pageable) {
     modeDePassationSelection=ModeDePassationSelectionSpecifications.deleted(false);
     return repository.findAll(modeDePassationSelection, pageable);
    }

    @Override
    public Page<ModeDePassationSelection> findAllDeleted(Specification<ModeDePassationSelection> modeDePassationSelection, Pageable pageable) {
        modeDePassationSelection=ModeDePassationSelectionSpecifications.deleted(true);
        return repository.findAll(modeDePassationSelection, pageable);
    }

    @Override
    public Page<ModeDePassationSelection> findAllDeleted(Pageable pageable) {
        Specification<ModeDePassationSelection> modeDePassationSelection = ModeDePassationSelectionSpecifications.deleted(true);
        return repository.findAll(modeDePassationSelection, pageable);
    }

    @Override
    public Optional<ModeDePassationSelection> findDeletedById(Integer id) {
        Specification<ModeDePassationSelection> specification = ModeDePassationSelectionSpecifications.deleted(true);
        return findOne(specification);

    }

    @Override
    public Optional<ModeDePassationSelection> findOne(Specification<ModeDePassationSelection> specification) {
        return repository.findOne(specification);
    }

    @Override
    public ModeDePassationSelection findById(Integer id) throws ModeDePassationSelectionNotFound {
        Specification<ModeDePassationSelection> specification = ModeDePassationSelectionSpecifications.deleted(false).and(ModeDePassationSelectionSpecifications.id(id));
        return findOne(specification).orElseThrow(() -> new ModeDePassationSelectionNotFound("",id));
    }

    @Override
    public ModeDePassationSelection save(ModeDePassationSelection modeDePassationSelection) {
        return repository.save(modeDePassationSelection);    }

    @Override
    public ModeDePassationSelection create(ModeDePassationSelectionRequest request) throws RefParamNotFound {
        Specification<ModeDePassationSelection> specification = ModeDePassationSelectionSpecifications.deleted(false).and(ModeDePassationSelectionSpecifications.libelle(request.getLibelle()));

        if(findOne(specification).isPresent()) {
            throw new ModeDePassationSelectionAlredyExist("", request.getLibelle());
        }
        if(!request.getCotegorieIds().isEmpty()) {
            getSizeList(request.getCotegorieIds());
        }
        ModeDePassationSelection modeDePassationSelection = ModeDePassationSelectionMapper.INSTANCE.mapFromRequest(request);
        for(Integer i : request.getCotegorieIds()) {
            RefParam categorie = refParamService.findById(i);
            if(modeDePassationSelection.getCategories() == null) {
                modeDePassationSelection.setCategories(new ArrayList<>());
            }
            modeDePassationSelection.getCategories().add(categorie);
        }
        return save(modeDePassationSelection);
    }

    @Override
    public ModeDePassationSelection update(Integer id, ModeDePassationSelectionRequest request) throws ModeDePassationSelectionNotFound, RefParamNotFound {
        ModeDePassationSelection modeDePassationSelection = findById(id);
        if(!request.getCotegorieIds().isEmpty()) {
            getSizeList(request.getCotegorieIds());
        }
        if(request.getLibelle() != null) {
            modeDePassationSelection.setLibelle(request.getLibelle());
        }
        List<Integer> oldModePassationIds=modeDePassationSelection.getCategories().stream().map(c -> c.getId()).collect(Collectors.toList());
        for(Integer i : request.getCotegorieIds()) {
            if(!oldModePassationIds.contains(i)) {
            RefParam categorie = refParamService.findById(i);
            modeDePassationSelection.getCategories().add(categorie);
            }
        }
        for(Integer i :oldModePassationIds) {
            if( !request.getCotegorieIds().contains(i)) {
                    RefParam categorie = refParamService.findById(i);
                    modeDePassationSelection.getCategories().remove(categorie);
            }
        }
        return save(modeDePassationSelection);
    }

    @Override
    public void delete(Integer id) throws ModeDePassationSelectionNotFound {
        findById(id);
        repository.softDelete(id);

    }

    private static int getSizeList(List<Integer> listIds) throws RefParamNotFound, DuplicateCategorieIdsException {
        int size = listIds.size();
        List<Integer> duplicateIds = new ArrayList<>();

        // null check for categorieIds elements
        for (int i = 0; i < size; i++) {
            if (listIds.get(i) == null ) {
                throw new RefParamNotFound("" , List.of(listIds.get(i)));
            }
            for (int j = i + 1; j < size; j++) {
                if (listIds.get(i).equals(listIds.get(j))) {
                    duplicateIds.add(listIds.get(j));
                }
            }
            if (!duplicateIds.isEmpty()) {
                throw new DuplicateCategorieIdsException("", duplicateIds);
            }
        }
        return size;
    }
}
