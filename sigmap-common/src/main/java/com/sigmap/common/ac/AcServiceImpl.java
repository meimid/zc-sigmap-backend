package com.sigmap.common.ac;

import com.sigmap.common.ac.exception.*;
import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class AcServiceImpl implements AcService{

    private final Logger logger = LoggerFactory.getLogger(AcService.class);

    private final AcRepository repository;
    private final ComRepository comRepository;

    private final KeycloakAdminService keycloakAdminService;

    private final UserAcRepository userAcRepository;

    private final RefParamRepository refParamRepository;

    private final AdressService adressService;

    private final ComService comService;


    public AcServiceImpl(AcRepository repository, ComRepository comRepository, KeycloakAdminService keycloakAdminService, UserAcRepository userAcRepository, RefParamRepository refParamRepository, AdressService adressService, ComService comService) {
        this.repository = repository;
        this.comRepository = comRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.userAcRepository = userAcRepository;
        this.refParamRepository = refParamRepository;
        this.adressService = adressService;
        this.comService = comService;
    }


    @Override
    public List<Ac> findAll() {
        Specification<Ac> ac = AcSpecifications.deleted(false);
        return repository.findAll(ac);
    }

    @Override
    public Page<Ac> findAll(Specification<Ac> ac, Pageable pageable) {
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Specification<Ac> ac, Pageable pageable) {
        ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Pageable pageable) {
        Specification<Ac> ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Optional<Ac> findDeletedById(Integer id) {
        Specification<Ac> specification = AcSpecifications.id(id).and(AcSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ac> findOne(Specification<Ac> specification) {

        return repository.findOne(specification);
    }

    @Override
    public Ac findById(Integer id) throws AcNotFound {
        Specification<Ac> specification = AcSpecifications.id(id)
                .and(AcSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new AcNotFound("", List.of(id)));

    }

    @Override
    public Ac save(Ac ac) {
        return repository.save(ac);
    }

    @Override
    @Transactional
    public Ac create(AcRequest request) throws ComNotFound, RefParamNotFound, AcNotFound {
        if(request.getNom() == null || request.getNom().isEmpty()) {
            throw new AcAttributeRequire("","nom");
        }
         else if(request.getTutelId()==null&&(!request.isIstTutel()) ) {
             throw new AcAttributeRequire("","tutel");
        }
       /*  else if(request.getContact() == null || request.getContact().isEmpty()) {
             throw new AcAttributeRequire("","contact");
        }*/
     /*   else if(request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AcAttributeRequire("","email");
        }*/
      /*  else if (!request.getEmail().matches(Constants.EMAIL_REGEX)) {
            throw new AcAttributeRequiredAndValid("", "email");
        }*/
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.nom(request.getNom()));

        if(findOne(specification).isPresent()) {
            throw new AcNomAlreadyExist("", request.getNom());
        }
        Ac ac = AcMapper.INSTANCE.map(request);
        if(request.getTutelId()!=null ) {
            Ac tutel= findById(request.getTutelId());
            if(tutel.isIstTutel()){
            ac.setTutel(tutel);
            }
            else {
                throw new AcAttributeRequire("","tutel");
            }
        }

        if(request.getAdresse()!=null) {
           Adresse adresse=adressService.create(request.getAdresse());
           ac.setAdresse(adresse);
        }
        if(request.getComId()!=null) {
            Com com=comService.findById(request.getComId());
            ac.setCom(com);
        }

        Ac savedAc = repository.save(ac);

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String userId : request.getUserIds()) {
                try {
                    UserAc userAc = new UserAc();
                    userAc.setUserId(userId);
                    userAc.setAcId(savedAc.getId());
                    userAc.setIsDeleted(false);
                    userAc.setStatus(ActiveStatus.ACTIF);
                    userAcRepository.save(userAc);
                } catch (Exception e) {
                    logger.warn("Impossible d'associer l'utilisateur {} à l'AC {} : {}", userId, savedAc.getId(), e.getMessage());
                }
            }
        }

        return savedAc;
    }


    @Override
    @Transactional
    public Ac update(Integer id, AcRequest request) throws AcNotFound, ComNotFound, DuplicateComIdsException, UserAcDefaultStatus, RefParamNotFound {
        if(request.getNom() == null || request.getNom().isEmpty()) {
            throw new AcAttributeRequire("","nom");
        }
        else if(request.getTutelId()==null&&!request.isIstTutel()){
            throw new AcAttributeRequire("","tutel");
        }
       /* else if(request.getContact() == null || request.getContact().isEmpty()) {
            throw new AcAttributeRequire("","contact");
        }*/
  /*      else if(request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AcAttributeRequire("","email");
        }
        else if (!request.getEmail().matches(Constants.EMAIL_REGEX)) {
            throw new AcAttributeRequiredAndValid("", "email");
        }*/
        Ac ac = findById(id);
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.nom(request.getNom()));

        if(findOne(specification).isPresent()&& !findOne(specification).get().getId().equals(id)) {
            throw new AcNomAlreadyExist("", request.getNom());
        }
        AcMapper.INSTANCE.updateAcFromRequest(request, ac);
        if(request.getTutelId()!=null ) {
            Ac tutel= findById(request.getTutelId());
            if(tutel.isIstTutel()){
                ac.setTutel(tutel);
            }
            else {
                throw new AcAttributeRequire("","tutel");
            }
        }

        if(request.getAdresse()!=null&& Objects.equals(ac.getAdresse().getId(), request.getAdresse().getId())) {
            Adresse adresse=adressService.update(request.getAdresse());
            ac.setAdresse(adresse);
        }
        if(request.getComId()!=null) {
            Com com=comService.findById(request.getComId());
            ac.setCom(com);
        }
        Ac savedAc = repository.save(ac);

        // Seulement si SUPER_ADMIN ou ADMIN_CENTRAL
        boolean hasRightToAffectUsers = Authentication.hasRole("SUPER_ADMIN") || Authentication.hasRole("ADMIN_CENTRAL");
        if (hasRightToAffectUsers) {

            // 1. Récupérer les anciens UserAc liés à ce AC
            List<UserAc> existingUserAcs = userAcRepository.findAllByAcIdAndIsDeletedFalse(id);

            // 2. Construire la nouvelle liste à partir des userIds reçus
            List<String> newUserIds = request.getUserIds() != null ? request.getUserIds() : new ArrayList<>();

            // 3. Supprimer ceux qui n'existent plus
            for (UserAc existing : existingUserAcs) {
                if (!newUserIds.contains(existing.getUserId())) {
                    userAcRepository.softDelete(existing.getAcId(), existing.getUserId());
                }
            }

            // 4. Ajouter les nouveaux
            for (String userId : newUserIds) {
                boolean exists = existingUserAcs.stream()
                        .anyMatch(e -> e.getUserId().equals(userId));
                if (!exists) {
                    UserAc userAc = new UserAc();
                    userAc.setUserId(userId);
                    userAc.setAcId(id);
                    userAc.setStatus(ActiveStatus.ACTIF);
                    userAcRepository.save(userAc);
                }
            }
        }

        return savedAc;
    }



    private static int getSizeList(List<Integer> listIds) throws ComNotFound, DuplicateComIdsException {
        int size = listIds.size();
        List<Integer> duplicateIds = new ArrayList<>();

        // null check for comIds elements
        for (int i = 0; i < size; i++) {
            if (listIds.get(i) == null ) {
                throw new ComNotFound("" , List.of(listIds.get(i)));
            }
            for (int j = i + 1; j < size; j++) {
                if (listIds.get(i).equals(listIds.get(j))) {
                    duplicateIds.add(listIds.get(j));
                }
            }
            if (!duplicateIds.isEmpty()) {
                throw new DuplicateComIdsException("", duplicateIds);
            }
        }
        return size;
    }

    @Override
    public void delete(Integer id) throws  AcHasAssociatedAgpm {
        boolean hasAssociatedAgpm = repository.hasAssociatedAgpm(id);
        if (hasAssociatedAgpm) {
            throw new AcHasAssociatedAgpm("" , id);
        }
        boolean hasAssociatedPpm = repository.hasAssociatedPpm(id);
        if (hasAssociatedPpm) {
            throw new AcHasAssociatedPpm("" , id);
        }
        boolean hasAssociatedPaa = repository.hasAssociatedPaa(id);
        if (hasAssociatedPaa) {
            throw new AcHasAssociatedPaa("" , id);
        }

        boolean hasAssociatedAc = repository.hasAssociatedAc(id);
        if (hasAssociatedAc) {
            throw new AcHasAssociatedAc("" , id);
        }

        repository.softDelete(id);
    }

    @Override
    public List<Ac> findAllTutel(Specification<Ac> ac) {
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.isItTutel(true));
        return repository.findAll(specification);
    }
}
