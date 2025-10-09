package com.sigmap.common.activite;

import com.sigmap.common.ModeDePassationSelection.exception.RequiredValue;
import com.sigmap.common.activite.exception.ActiviteAlreadyExist;
import com.sigmap.common.activite.exception.ActiviteNotFound;
import com.sigmap.common.activite.exception.CategoriesActiviteRequired;
import com.sigmap.common.activite.mapper.ActiviteMapper;
import com.sigmap.common.activite.request.ActiviteRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ActiviteServiceImpl implements ActiviteService {
    private final ActiviteRepository repository;
    private final RefParamService refParamService;
    public ActiviteServiceImpl(ActiviteRepository repository, RefParamService refParamService) {
        this.repository = repository;
        this.refParamService = refParamService;
    }

    @Override
    public List<Activite> findByCategoriesActiviteId(Integer categoriesActiviteId) throws RefParamNotFound {
        refParamService.findById(categoriesActiviteId);
        Specification<Activite> spec = Specification.where(ActiviteSpecifications.hasActiviteId(categoriesActiviteId).and(ActiviteSpecifications.deleted(false)));
        return repository.findAll(spec);
    }

    @Override
    public List<Activite> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<Activite> findAll(Specification<Activite> Activite, Pageable pageable) {
        Activite=Activite.and(ActiviteSpecifications.deleted(false));
        return repository.findAll(Activite, pageable);
    }

    @Override
    public Page<Activite> findAllDeleted(Specification<Activite> Activite, Pageable pageable) {
        Activite=ActiviteSpecifications.deleted(true);
        return repository.findAll(Activite, pageable);
    }

    @Override
    public Page<Activite> findAllDeleted(Pageable pageable) {
        Specification<Activite> Activite = ActiviteSpecifications.deleted(true);
        return repository.findAll(Activite, pageable);
    }

    @Override
    public Optional<Activite> findDeletedById(Integer id) {
        Specification<Activite> specification = ActiviteSpecifications.deleted(true).and(ActiviteSpecifications.id(id));
        return findOne(specification);

    }

    @Override
    public Optional<Activite> findOne(Specification<Activite> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Activite findById(Integer id) throws ActiviteNotFound {
        Specification<Activite> specification = ActiviteSpecifications.deleted(false).and(ActiviteSpecifications.id(id));
        return findOne(specification).orElseThrow(() -> new ActiviteNotFound("",id));
    }

    @Override
    public Activite save(Activite Activite) {
        return repository.save(Activite);    }

    @Override
    public Activite create(ActiviteRequest request) throws RefParamNotFound {

        if(request.getLibelle()==null||request.getLibelle().isEmpty()) {
            throw new RequiredValue("");
        }
        Specification<Activite> specification = ActiviteSpecifications.deleted(false).and(ActiviteSpecifications.libelle(request.getLibelle()));

        if(findOne(specification).isPresent()) {
            throw new ActiviteAlreadyExist("", request.getLibelle());
        }
        if(request.getCategorieActiviteId()==null) {
            throw new CategoriesActiviteRequired("categories activite is required ");
        }
        RefParam categorieActivite = refParamService.findById(request.getCategorieActiviteId());

        Activite activite = ActiviteMapper.INSTANCE.mapFromRequest(request);
        activite.setCategorieActivite(categorieActivite);

        return save(activite);
    }

    @Override
    public Activite update(Integer id, ActiviteRequest request) throws ActiviteNotFound, RefParamNotFound {
        Activite activite = findById(id);


        if (request.getCategorieActiviteId() == null) {
            throw new CategoriesActiviteRequired("La catégorie activité est obligatoire");
        }
        if(request.getLibelle()==null||request.getLibelle().isEmpty()) {
            throw new RequiredValue("");
        }
        else {
            Specification<Activite> specification = ActiviteSpecifications.deleted(false).and(ActiviteSpecifications.libelle(request.getLibelle()));
            Optional<Activite> Activite1 = findOne(specification);
            if(Activite1.isPresent()&& !Objects.equals(Activite1.get().getId(), id)) {
                throw new ActiviteAlreadyExist("", request.getLibelle());
            }
            activite.setLibelle(request.getLibelle());
        }
        RefParam categorieActivite = refParamService.findById(request.getCategorieActiviteId());
        activite.setCategorieActivite(categorieActivite);
        return save(activite);
    }

    @Override
    public void delete(Integer id) throws ActiviteNotFound {
        findById(id);
        repository.softDelete(id);

    }

}
