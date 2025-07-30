package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.ModeDePassationSelection.exception.DuplicateTypeMarcherIdsException;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionAlredyExist;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ModeDePassationSelection.exception.RequiredValue;
import com.sigmap.common.ModeDePassationSelection.mappers.ModeDePassationSelectionMapper;
import com.sigmap.common.ModeDePassationSelection.request.ModeDePassationSelectionRequest;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    public List<ModeDePassationSelection> findBytypeMarcherId(Integer typeMarcherId) throws RefParamNotFound {
       refParamService.findById(typeMarcherId);
        Specification<ModeDePassationSelection> spec = Specification.where(ModeDePassationSelectionSpecifications.hastypeMarcherId(typeMarcherId).and(ModeDePassationSelectionSpecifications.deleted(false)));
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
        Specification<ModeDePassationSelection> specification = ModeDePassationSelectionSpecifications.deleted(true).and(ModeDePassationSelectionSpecifications.id(id));
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

        if(request.getLibelle()==null||request.getLibelle().isEmpty()) {
            throw new RequiredValue("");
        }
        Specification<ModeDePassationSelection> specification = ModeDePassationSelectionSpecifications.deleted(false).and(ModeDePassationSelectionSpecifications.libelle(request.getLibelle()));

        if(findOne(specification).isPresent()) {
            throw new ModeDePassationSelectionAlredyExist("", request.getLibelle());
        }
        if(!request.getTypeMarchersIds().isEmpty()) {
            getSizeList(request.getTypeMarchersIds());
        }
        ModeDePassationSelection modeDePassationSelection = ModeDePassationSelectionMapper.INSTANCE.mapFromRequest(request);
        for(Integer i : request.getTypeMarchersIds()) {
            RefParam typeMarcher = refParamService.findById(i);
            if(modeDePassationSelection.getTypeMarchers() == null) {
                modeDePassationSelection.setTypeMarchers(new ArrayList<>());
            }
            modeDePassationSelection.getTypeMarchers().add(typeMarcher);
        }
        return save(modeDePassationSelection);
    }

    @Override
    public ModeDePassationSelection update(Integer id, ModeDePassationSelectionRequest request) throws ModeDePassationSelectionNotFound, RefParamNotFound {
        ModeDePassationSelection modeDePassationSelection = findById(id);

        if(!request.getTypeMarchersIds().isEmpty()) {
            getSizeList(request.getTypeMarchersIds());
        }

        if(request.getLibelle()==null||request.getLibelle().isEmpty()) {
            throw new RequiredValue("");
        }
        else {
            Specification<ModeDePassationSelection> specification = ModeDePassationSelectionSpecifications.deleted(false).and(ModeDePassationSelectionSpecifications.libelle(request.getLibelle()));
            Optional<ModeDePassationSelection> modeDePassationSelection1 = findOne(specification);
            if(modeDePassationSelection1.isPresent()&& !Objects.equals(modeDePassationSelection1.get().getId(), id)) {
                throw new ModeDePassationSelectionAlredyExist("", request.getLibelle());
            }
            modeDePassationSelection.setLibelle(request.getLibelle());
        }
        List<Integer> oldModePassationIds=modeDePassationSelection.getTypeMarchers().stream().map(c -> c.getId()).collect(Collectors.toList());
        for(Integer i : request.getTypeMarchersIds()) {
            if(!oldModePassationIds.contains(i)) {
            RefParam typeMarcher = refParamService.findById(i);
            modeDePassationSelection.getTypeMarchers().add(typeMarcher);
            }
        }
        for(Integer i :oldModePassationIds) {
            if( !request.getTypeMarchersIds().contains(i)) {
                    RefParam typeMarcher = refParamService.findById(i);
                    modeDePassationSelection.getTypeMarchers().remove(typeMarcher);
            }
        }
        return save(modeDePassationSelection);
    }

    @Override
    public void delete(Integer id) throws ModeDePassationSelectionNotFound {
        findById(id);
        repository.softDelete(id);

    }

    private static int getSizeList(List<Integer> listIds) throws RefParamNotFound, DuplicateTypeMarcherIdsException {
        int size = listIds.size();
        List<Integer> duplicateIds = new ArrayList<>();

        // null check for typeMarchersIds elements
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
                throw new DuplicateTypeMarcherIdsException("", duplicateIds);
            }
        }
        return size;
    }
}
