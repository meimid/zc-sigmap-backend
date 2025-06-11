package com.sigmap.common.com;

import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.exception.DuplicateAcIdsException;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.config.Constants;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.*;

import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ComServiceImpl implements ComService {

  private final Logger logger = LoggerFactory.getLogger(ComService.class);

  private final ComRepository repository;
  private final RefParamService refParamService;
  private final AcRepository acRepository;
  @Autowired
  private KeycloakAdminService keycloakAdminService;
  @Autowired
  private UserComRepository userComRepository;
  @Autowired
  private RefParamRepository refParamRepository;

  public ComServiceImpl(
      ComRepository repository, RefParamService refParamService, AcRepository acRepository) {
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
    Specification<Com> specification =
        ComSpecifications.id(id).and(ComSpecifications.deleted(true));
    return findOne(specification);
  }

  @Override
  public Optional<Com> findOne(Specification<Com> specification) {
    return repository.findOne(specification);
  }

  @Override
  public Com findById(Integer id) throws ComNotFound {
    Specification<Com> specification =
        ComSpecifications.id(id).and(ComSpecifications.deleted(false));
    return findOne(specification).orElseThrow(() -> new ComNotFound("", List.of(id)));
  }

  @Override
  public Com save(Com com) {
    return repository.save(com);
  }

  @Override
  public Com create(ComRequest request)
      throws RefParamNotFound, AcNotFound, DuplicateAcIdsException {
    RefParam comType = refParamService.findById(request.getComTypeId());
    Com com = ComMapper.INSTANCE.map(request);

    if (comType.getId() != null) {
      com.setComType(comType);
    } else {
      com.setComType(null);
    }
    Com savedCom = repository.save(com);

    if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
      for (String userId : request.getUserIds()) {
        try {
          UserCom userCom = new UserCom();
          userCom.setUserId(userId);
          userCom.setComId(savedCom.getId());
          userCom.setIsDeleted(false);
          userCom.setStatus(ActiveStatus.ACTIF);
          userComRepository.save(userCom);
        } catch (Exception e) {
          logger.warn("Impossible d'associer l'utilisateur {} à l'COM {} : {}", userId, savedCom.getId(), e.getMessage());
        }
      }
    }

    return savedCom;
  }

  @Override
  @Transactional
  public Com update(Integer id, ComRequest request)
          throws ComNotFound, RefParamNotFound, AcNotFound, DuplicateAcIdsException, AgpmDefaultStatus {
    Com com = repository.findById(id).orElseThrow(() -> new ComNotFound("", List.of(id)));
    RefParam comType = refParamService.findById(request.getComTypeId());
    ComMapper.INSTANCE.updateFromRequest(request, com);

    if (comType.getId() != null) {
      com.setComType(comType);
    } else {
      com.setComType(null);
    }
    Com savedCom = repository.save(com);

    // 1. Récupérer les anciens UserCom liés à ce COM
    List<UserCom> existingUserComs = userComRepository.findAllByComIdAndIsDeletedFalse(id);

    // 2. Construire la nouvelle liste à partir des userIds reçus
    List<String> newUserIds = request.getUserIds() != null ? request.getUserIds() : new ArrayList<>();

    // 3. Supprimer ceux qui n'existent plus
    for (UserCom existing : existingUserComs) {
      logger.info("*************"+ existing);
      if (!newUserIds.contains(existing.getUserId())) {
        userComRepository.softDelete(existing.getComId(), existing.getUserId());
      }
    }

    // 4. Ajouter les nouveaux
    for (String userId : newUserIds) {
      logger.info("*************"+ userId);
      boolean exists = existingUserComs.stream()
              .anyMatch(e -> e.getUserId().equals(userId));
      if (!exists) {
        logger.info("************* save", userId);
        UserCom userCom = new UserCom();
        userCom.setUserId(userId);
        userCom.setComId(id);
        userCom.setStatus(ActiveStatus.ACTIF);
        userComRepository.save(userCom);
      }
    }
    return  savedCom;
  }

  private static int getSizeList(List<Integer> listIds) throws AcNotFound, DuplicateAcIdsException {
    int size = listIds.size();
    List<Integer> duplicateIds = new ArrayList<>();

    // null check for comIds elements
    for (int i = 0; i < size; i++) {
      if (listIds.get(i) == null) {
        throw new AcNotFound("", List.of(listIds.get(i)));
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
  public void delete(Integer id) throws ComHasAssociatedAc {

    repository.softDelete(id);
  }
}
