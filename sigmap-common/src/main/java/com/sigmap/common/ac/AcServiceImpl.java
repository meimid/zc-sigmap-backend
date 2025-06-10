package com.sigmap.common.ac;

import com.sigmap.common.ac.exception.*;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcId;
import com.sigmap.common.user_ac.UserAcRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcServiceImpl implements AcService{

    private final Logger logger = LoggerFactory.getLogger(AcService.class);

    private final AcRepository repository;
    private final ComRepository comRepository;

    private final KeycloakAdminService keycloakAdminService;

    private final UserAcRepository userAcRepository;

    private final RefParamRepository refParamRepository;


    public AcServiceImpl(AcRepository repository, ComRepository comRepository, KeycloakAdminService keycloakAdminService, UserAcRepository userAcRepository, RefParamRepository refParamRepository) {
        this.repository = repository;
        this.comRepository = comRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.userAcRepository = userAcRepository;
        this.refParamRepository = refParamRepository;

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
    public Ac create(AcRequest request) throws ComNotFound, RefParamNotFound {
        if(request.getNom() == null) {
            throw new AcAttributeRequire("","nom");
        }
         else if(request.getTutelId()==null){
             throw new AcAttributeRequire("","tutel");
        }
         else if(request.getContact() == null) {
             throw new AcAttributeRequire("","contact");
        }
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.nom(request.getNom()));

        if(findOne(specification).isPresent()) {
            throw new AcNomAlreadyExist("", request.getNom());
        }
        Ac ac = AcMapper.INSTANCE.map(request);

            RefParam tutel= refParamRepository.findById(request.getTutelId()).
                    orElseThrow(() -> new RefParamNotFound("", List.of(request.getTutelId())));
            ac.setTutel(tutel);


        Ac savedAc = repository.save(ac);

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String userId : request.getUserIds()) {
                try {
                    UserAc userAc = new UserAc();
                    userAc.setUserId(userId);
                    userAc.setAcId(savedAc.getId());
                    userAc.setIsDeleted(false);
                    RefParam defaultStatus = refParamRepository.findById(Constants.ACTIF_STATUS)
                            .orElseThrow(() -> new UserAcDefaultStatus("Default status not found"));
                    userAc.setStatus(defaultStatus);
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
        Ac ac = findById(id);
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.nom(request.getNom()));

        if(findOne(specification).isPresent()&& !findOne(specification).get().getId().equals(id)) {
            throw new AcNomAlreadyExist("", request.getNom());
        }
        AcMapper.INSTANCE.updateAcFromRequest(request, ac);
        if(request.getTutelId() != null) {
        RefParam tutel= refParamRepository.findById(request.getTutelId()).
                orElseThrow(() -> new RefParamNotFound("", List.of(request.getTutelId())));
        ac.setTutel(tutel);
        }
        Ac savedAc = repository.save(ac);

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
                RefParam defaultStatus = refParamRepository.findById(Constants.ACTIF_STATUS)
                        .orElseThrow(() -> new UserAcDefaultStatus("Default status not found"));
                userAc.setStatus(defaultStatus);
                userAcRepository.save(userAc);
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

        repository.softDelete(id);
    }
}
