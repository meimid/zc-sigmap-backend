package com.sigmap.common.societe;


import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteService;
import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.entity.SocieteStatus;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.*;

import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteGlobalRequest;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressService;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteRepository;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.UserSocieteSpecifications;
import com.sigmap.common.user_societe.exception.SocieteIdRequiredException;
import com.sigmap.common.user_societe.exception.UserIdRequiredException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
public class SocieteServiceImpl implements SocieteService {
    private final SocieteRepository repository;
    private final RefParamService refParamService;
    private final ActiviteService activiteService;
    private final UserSocieteRepository  userSocieteRepository;
    private final AdressService adressService;
    private final SocieteAddressService societeAddressService;
    private final UserSocieteService userSocieteService;

    public SocieteServiceImpl(SocieteRepository repository, RefParamService refParamService, ActiviteService activiteService, UserSocieteRepository userSocieteRepository, AdressService adressService, SocieteAddressService societeAddressService, UserSocieteService userSocieteService) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.activiteService = activiteService;
        this.userSocieteRepository = userSocieteRepository;
        this.adressService = adressService;
        this.societeAddressService = societeAddressService;
        this.userSocieteService = userSocieteService;
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
    @Transactional
    public Societe create(SocieteGlobalRequest request) throws RefParamNotFound, SocieteNotFound {


        Societe societe =  SocieteMapper.INSTANCE.map(request.getSocieteRequest());

        if (request.getSocieteRequest().getTypeId() == null) {
            throw new TypeIsRequiredException("Type is required", societe.getId());
        } else {
            RefParam type = refParamService.findById(request.getSocieteRequest().getTypeId());
            societe.setType(type);
        }


        if (request.getSocieteRequest().getCategorieSoumissionnaireId() == null) {
            throw new CategorieSoumissionnaireIsRequiredException("Categorie soumissionnaire is required", societe.getId());
        } else {
            RefParam cat = refParamService.findById(request.getSocieteRequest().getCategorieSoumissionnaireId());
            societe.setCategorieSoumissionnaire(cat);
        }

        if (request.getSocieteRequest().getActiviteIds() == null || request.getSocieteRequest().getActiviteIds().isEmpty()) {
            throw new ActiviteIsRequiredException("Activite is required", societe.getId());
        }
        if(request.getAddressRequest()==null){
            throw new AddressRequiredException("");
        }
        if(request.getAddressRequest().getAdresse()==null){
            throw new AddressRequiredException("");

        }
        if( request.getAddressRequest().getCodePostal()==null) {
            throw new CodePostalRequiredException ("");
        }
        if(request.getAddressRequest().getPaysId()==null){
            throw new PaysIsRequiredException("");
        }
        if(request.getAddressRequest().getTel1()==null){
            throw new TelIsRequiredException ("");
        }
        if(request.getAddressRequest().getEmail()==null||request.getUserSocieteRequest().getEmail()==null){
            throw new EmailRequiredException ("");
        }
        if(request.getUserSocieteRequest().getNom()==null){
            throw new NameIsRequiredException ("");
        }

        /*if (request.getSocieteRequest().getNif() == null) {
            throw new NifIsRequiredException("NIF is required", societe.getId());
        }*/
      /*  if (request.getSocieteRequest().getDateNif() == null) {
            throw new DateNifIsRequiredException("Date NIF is required", societe.getId());
        }*/
       /* if (request.getSocieteRequest().getNumCnss() == null) {
            throw new NumCnssIsRequiredException("Num CNSS is required", societe.getId());
        }*/

        if (request.getUserSocieteRequest().getUserId() == null || request.getUserSocieteRequest().getUserId().isBlank()) {
            throw new UserIdRequiredException();
        }


        societe.setStatus(SocieteStatus.EN_COURS);

        getSizeList(request.getSocieteRequest().getActiviteIds());
        for(Integer i : request.getSocieteRequest().getActiviteIds()){
            Activite  activite=activiteService.findById(i);
            societe.getActivites().add(activite);
        }
        Societe savedSociete = save(societe);

        Adresse address=adressService.create(request.getAddressRequest());
        SocieteAddress societeAddress=new SocieteAddress();
        societeAddress.setAddressId(address.getId());
        societeAddress.setAddress(address);
        societeAddress.setSocieteId(savedSociete.getId());
        societeAddress.setType(savedSociete.getType());
       SocieteAddress savedSocieteAdress= societeAddressService.save(societeAddress);
        request.getUserSocieteRequest().setSocieteId(savedSociete.getId());
        UserSociete savedUserSociete= userSocieteService.create(request.getUserSocieteRequest());
        savedSociete.getSocieteAddresses().add(savedSocieteAdress);
        savedSociete.getUsersSociete().add(savedUserSociete);
        return savedSociete;

    }

    @Override
    public Societe update(Integer id,SocieteRequest request) throws RefParamNotFound, SocieteNotFound {
        Societe societe=findById(id);
        if (request.getTypeId() == null) {
            throw new TypeIsRequiredException("Type is required", societe.getId());
        } else {
            RefParam type = refParamService.findById(request.getTypeId());
            societe.setType(type);
        }


        if (request.getCategorieSoumissionnaireId() == null) {
            throw new CategorieSoumissionnaireIsRequiredException("Categorie soumissionnaire is required", societe.getId());
        } else {
            RefParam cat = refParamService.findById(request.getCategorieSoumissionnaireId());
            societe.setCategorieSoumissionnaire(cat);
        }

        if (request.getActiviteIds() == null || request.getActiviteIds().isEmpty()) {
            throw new ActiviteIsRequiredException("Activite is required", societe.getId());
        }
       /* if (request.getNif() == null) {
            throw new NifIsRequiredException("NIF is required", societe.getId());
        }*/
        /*if (request.getDateNif() == null) {
            throw new DateNifIsRequiredException("Date NIF is required", societe.getId());
        }*/
       /* if (request.getNumCnss() == null) {
            throw new NumCnssIsRequiredException("Num CNSS is required", societe.getId());
        }*/

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

        Societe savedSociete= save(societe);
      return   findById(savedSociete.getId());

    }

    @Override
    public void delete(Integer id) throws SocieteNotFound {
        findById(id);
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
