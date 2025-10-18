package com.sigmap.common.societe;


import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteService;
import com.sigmap.common.entity.SocieteStatus;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.DuplicateActiviteIds;

import com.sigmap.common.societe.exception.HasAssociatedUserSociete;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteRepository;
import com.sigmap.common.user_societe.UserSocieteSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class SocieteServiceImpl implements SocieteService {
    private final SocieteRepository repository;
    private final RefParamService refParamService;
    private final ActiviteService activiteService;
    private final UserSocieteRepository  userSocieteRepository;

    public SocieteServiceImpl(SocieteRepository repository, RefParamService refParamService, ActiviteService activiteService, UserSocieteRepository userSocieteRepository) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.activiteService = activiteService;
        this.userSocieteRepository = userSocieteRepository;
    }

    @Override
    public List<Societe> findAll() {
        Specification<Societe> spec = SocieteSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Societe> findAll(Specification<Societe> societe, Pageable pageable) {
        return repository.findAll(societe, pageable);
    }

    @Override
    public Page<Societe> findAllDeleted(Specification<Societe> societe, Pageable pageable) {
        societe = SocieteSpecifications.deleted(true);
        return repository.findAll(societe, pageable);
    }

    @Override
    public Page<Societe> findAllDeleted(Pageable pageable) {
        Specification<Societe> com = SocieteSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Optional<Societe> findDeletedById(Integer id) {
        Specification<Societe> specification = SocieteSpecifications.id(id).and(SocieteSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Societe> findOne(Specification<Societe> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Societe findById(Integer id) throws SocieteNotFound {
        Specification<Societe> specification = SocieteSpecifications.id(id)
                .and(SocieteSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new SocieteNotFound("",List.of(id)));
    }

    @Override
    public Societe save(Societe societe) {
        return repository.save(societe);
    }

    @Override
    public Societe create(SocieteRequest request) throws RefParamNotFound {
        Societe societe =  SocieteMapper.INSTANCE.map(request);

        if(request.getTypeId() != null) {
            RefParam type=refParamService.findById(request.getTypeId());
            societe.setType(type);

        }
        if(request.getPaysId() != null) {
            RefParam pays=refParamService.findById(request.getPaysId());
            societe.setPays(pays);

        }
        if(request.getCategorieSoumissionnaireId() != null) {
            RefParam categorieSoumissionnaireId=refParamService.findById(request.getCategorieSoumissionnaireId());
            societe.setCategorieSoumissionnaire(categorieSoumissionnaireId);
        }


            societe.setStatus(SocieteStatus.EN_COURS);

        getSizeList(request.getActiviteIds());
        for(Integer i : request.getActiviteIds()){
            Activite  activite=activiteService.findById(i);
            societe.getActivites().add(activite);
        }
        return save(societe);
    }

    @Override
    public Societe update(Integer id,SocieteRequest request) throws RefParamNotFound, SocieteNotFound {
        Societe societe=findById(id);
        if(request.getTypeId() != null) {
            RefParam type=refParamService.findById(request.getTypeId());
            societe.setType(type);

        }
        if(request.getPaysId() != null) {
            RefParam pays=refParamService.findById(request.getPaysId());
            societe.setPays(pays);

        }
        if(request.getCategorieSoumissionnaireId() != null) {
            RefParam categorieSoumissionnaireId=refParamService.findById(request.getCategorieSoumissionnaireId());
            societe.setCategorieSoumissionnaire(categorieSoumissionnaireId);
        }

      /*  if(request.getStatus() != null) {
            societe.setStatus(request.getStatus());
        }
        else {
            societe.setStatus(SocieteStatus.EN_COURS);
        }*/

        getSizeList(request.getActiviteIds());
        List<Activite> newActivites = new ArrayList<>();

        for(Integer i : request.getActiviteIds()){
            Activite  activite=activiteService.findById(i);
            newActivites.add(activite);
        }
        societe.setActivites(newActivites);

        return save(societe);
    }

    @Override
    public void delete(Integer id) {
        Specification<UserSociete> userSocieteSpecification = Specification.where(
                UserSocieteSpecifications.deleted(false)
                        .and(UserSocieteSpecifications.hasSocieteId(id)));
        boolean hasAssociatedUserSociete = userSocieteRepository.count(userSocieteSpecification) > 0;
        if (hasAssociatedUserSociete) {
            throw new HasAssociatedUserSociete("", id);
        }
        repository.softDelete(id);

    }

    @Override
    public Page<Societe> findAllWithStatus(Specification<Societe> societe, SocieteStatus status, Pageable pageable) {
        Specification<Societe> com = SocieteSpecifications.deleted(false).and(SocieteSpecifications.status(status));
        return repository.findAll(com, pageable);
    }

    @Override
    public Societe updateStatus(Integer societeId, SocieteStatus status) throws SocieteNotFound {
        Societe societe = findById(societeId);
        if(status != null) {
            societe.setStatus(status);
        }
        return save(societe);
    }

    private static int getSizeList(List<Integer> listIds) throws RefParamNotFound, DuplicateActiviteIds {
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
                throw new DuplicateActiviteIds("", duplicateIds);
            }
        }
        return size;
    }
}
