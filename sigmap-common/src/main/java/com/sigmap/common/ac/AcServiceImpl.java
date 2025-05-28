package com.sigmap.common.ac;

import com.sigmap.common.ac.exception.*;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
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

    public AcServiceImpl(AcRepository repository, ComRepository comRepository, KeycloakAdminService keycloakAdminService) {
        this.repository = repository;
        this.comRepository = comRepository;
        this.keycloakAdminService = keycloakAdminService;
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
    public Ac create(AcRequest request) throws ComNotFound {
        Ac ac = AcMapper.INSTANCE.map(request);
        Ac savedAc = repository.save(ac);
        /*if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String userId : request.getUserIds()) {
                try {
                    keycloakAdminService.updateUserAttribute(userId, "ac", savedAc.getId().toString());
                } catch (Exception e) {
                    // Logger et continuer
                    logger.warn("Impossible de mettre à jour l'utilisateur {} : {}", userId, e.getMessage());
                }
            }
        }*/
        return  savedAc;
    }


    @Override
    public Ac update(Integer id, AcRequest request) throws AcNotFound, ComNotFound, DuplicateComIdsException {
        Ac ac = findById(id);
        AcMapper.INSTANCE.updateAcFromRequest(request, ac);

   /*     // **Gestion des utilisateurs liés à cet Ac via Keycloak**
        if (request.getUserIds() != null) {
            // 1. Récupérer les users actuellement liés à cet Ac dans Keycloak
            List<UserDTO> currentUsers = keycloakAdminService.getUsersByAc(id);

            Set<String> currentUserIds = currentUsers.stream()
                    .map(UserDTO::getId)
                    .collect(Collectors.toSet());

            Set<String> newUserIds = new HashSet<>(request.getUserIds());

            // 2. Users à ajouter (nouveaux)
            Set<String> toAdd = new HashSet<>(newUserIds);
            toAdd.removeAll(currentUserIds);

            // 3. Users à retirer (plus dans la liste)
            Set<String> toRemove = new HashSet<>(currentUserIds);
            toRemove.removeAll(newUserIds);

            // 4. Mettre à jour Keycloak : ajouter attribut ac=id
            for (String userIdToAdd : toAdd) {
                keycloakAdminService.updateUserAttribute(userIdToAdd, "ac", id.toString());
            }

            // 5. Mettre à jour Keycloak : supprimer attribut ac (mettre à null)
            for (String userIdToRemove : toRemove) {
                keycloakAdminService.updateUserAttribute(userIdToRemove, "ac", null);
            }
        }*/


        return repository.save(ac);
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
