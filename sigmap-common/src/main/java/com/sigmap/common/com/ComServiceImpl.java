package com.sigmap.common.com;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.exception.DuplicateAcIdsException;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamHasAssociatedCom;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComServiceImpl implements ComService{

    private final Logger logger = LoggerFactory.getLogger(ComService.class);

    private final ComRepository repository;
    private final RefParamService refParamService;
    private final AcRepository acRepository;
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    public ComServiceImpl(ComRepository repository, RefParamService refParamService, AcRepository acRepository) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.acRepository = acRepository;
    }

    @Override
    public List<Com> findAll() {
        Specification<Com> spec = ComSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Com> findAll(Specification<Com> com, Pageable pageable) {
        return repository.findAll(com, pageable);
    }

    @Override
    public Page<Com> findAllDeleted(Specification<Com> com, Pageable pageable) {
        com = ComSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Page<Com> findAllDeleted(Pageable pageable) {
        Specification<Com> com = ComSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Optional<Com> findDeletedById(Integer id) {
        Specification<Com> specification = ComSpecifications.id(id).and(ComSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Com> findOne(Specification<Com> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Com findById(Integer id) throws ComNotFound {
        return repository.findById(id)
                .orElseThrow(() -> new ComNotFound("", List.of(id)));
    }

    @Override
    public Com save(Com com) {
        return repository.save(com);
    }

    @Override
    public Com create(ComRequest request) throws RefParamNotFound, AcNotFound, DuplicateAcIdsException {
        RefParam comType = refParamService.findById(request.getComTypeId());
        Com com = ComMapper.INSTANCE.map(request);

        if (comType.getId() != null) {
            com.setComType(comType);
        } else {
            com.setComType(null);
        }

        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            List<Integer> acIds = request.getAcIds();

            int size = getSizeList(acIds);

            // Check if all Ac IDs exist
            var acs = acRepository.findAllById(acIds);
            if (acs.size() != size) {
                List<Integer> existingAcIds = acs.stream()
                        .map(Ac::getId)
                        .toList();
                List<Integer> nonExistentAcIds = acIds.stream()
                        .filter(acId -> !existingAcIds.contains(acId))
                        .toList();
                throw new AcNotFound("", nonExistentAcIds);
            }
            com.setAcs(acs);
        }
        Com savedCom = repository.save(com);
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String userId : request.getUserIds()) {
                try {
                    keycloakAdminService.updateUserAttribute(userId, "com", savedCom.getId().toString());
                } catch (Exception e) {
                    // Logger et continuer
                    logger.warn("Impossible de mettre à jour l'utilisateur {} : {}", userId, e.getMessage());
                }
            }
        }
        return savedCom;

    }

    @Override
    public Com update(Integer id, ComRequest request) throws ComNotFound, RefParamNotFound, AcNotFound, DuplicateAcIdsException {
        Com com = repository.findById(id)
                .orElseThrow(() -> new ComNotFound("", List.of(id)));
        RefParam comType = refParamService.findById(request.getComTypeId());
        ComMapper.INSTANCE.updateFromRequest(request, com);

        if (comType.getId() != null) {
            com.setComType(comType);
        } else {
            com.setComType(null);
        }

        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            List<Integer> acIds = request.getAcIds();

            int size = getSizeList(acIds);

            // Check if all Ac IDs exist
            var acs = acRepository.findAllById(acIds);
            if (acs.size() != size) {
                List<Integer> existingAcIds = acs.stream()
                        .map(Ac::getId)
                        .toList();
                List<Integer> nonExistentAcIds = acIds.stream()
                        .filter(acId -> !existingAcIds.contains(acId))
                        .toList();
                throw new AcNotFound("", nonExistentAcIds);
            }
            com.setAcs(acs);
        }

        // **Gestion des utilisateurs liés à cet Com via Keycloak**
        if (request.getUserIds() != null) {
            // 1. Récupérer les users actuellement liés à cet Com dans Keycloak
            List<UserDTO> currentUsers = keycloakAdminService.getUsersByCom(id);

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

            // 4. Mettre à jour Keycloak : ajouter attribut com=id
            for (String userIdToAdd : toAdd) {
                keycloakAdminService.updateUserAttribute(userIdToAdd, "com", id.toString());
            }

            // 5. Mettre à jour Keycloak : supprimer attribut com (mettre à null)
            for (String userIdToRemove : toRemove) {
                keycloakAdminService.updateUserAttribute(userIdToRemove, "com", null);
            }
        }

        return repository.save(com);
    }

    private static int getSizeList(List<Integer> listIds) throws AcNotFound, DuplicateAcIdsException {
        int size = listIds.size();
        List<Integer> duplicateIds = new ArrayList<>();

        // null check for comIds elements
        for (int i = 0; i < size; i++) {
            if (listIds.get(i) == null ) {
                throw new AcNotFound("" , List.of(listIds.get(i)));
            }
            for (int j = i + 1; j < size; j++) {
                if (listIds.get(i).equals(listIds.get(j))) {
                    duplicateIds.add(listIds.get(j));
                }
            }
            if (!duplicateIds.isEmpty()) {
                throw new DuplicateAcIdsException("", duplicateIds);
            }
        }
        return size;
    }

    @Override
    public void delete (Integer id) throws ComHasAssociatedAc {
        boolean hasAssociatedAc = repository.hasAssociatedAc(id);
        if (hasAssociatedAc) {
            throw new ComHasAssociatedAc("",id);
        }
        repository.softDelete(id);
    }
}
