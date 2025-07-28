package com.sigmap.common.com;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcSpecifications;
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
import com.sigmap.common.ref_params.exceptions.RefParamHasAssociatedSocieteAddress;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.*;

import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressSpecifications;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import com.sigmap.common.user_com.exception.UserComNotFound;
import com.sigmap.common.user_com.exception.UserResponsibleForCom;
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
    @Autowired
    private UserAcRepository userAcRepository;
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

    if(request.getResponsableId() != null && !request.getResponsableId().isEmpty()) {
      UserCom userCom = new UserCom();
      userCom.setUserId(request.getResponsableId());
      userCom.setComId(savedCom.getId());
      userCom.setIsDeleted(false);
      userCom.setStatus(ActiveStatus.ACTIF);
      userCom.setResponsable(true);
      userComRepository.save(userCom);
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
        if(existing.isResponsable()) {
          throw new UserResponsibleForCom("",existing.getUserId(),existing.getComId());
        }
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
    if(request.getResponsableId() != null && !request.getResponsableId().isEmpty()) {
      existingUserComs.forEach(el -> el.setResponsable(false));
     Optional<UserCom>  userCom= userComRepository.findByUserIdAndComId(request.getResponsableId(),id);
      if(userCom.isPresent()&&!userCom.get().isResponsable()) {
        userComRepository.saveAll(existingUserComs);
        UserCom newResponsable=userCom.get();
        newResponsable.setResponsable(true);
        newResponsable.setStatus(ActiveStatus.ACTIF);
        userComRepository.save(newResponsable);
      }
      else{
        userComRepository.saveAll(existingUserComs);
        UserCom newResponsable=new UserCom();
        newResponsable.setUserId(request.getResponsableId());
        newResponsable.setComId(id);
        newResponsable.setStatus(ActiveStatus.ACTIF);
        newResponsable.setResponsable(true);
        userComRepository.save(newResponsable);
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
    Specification<Ac> societeAddressSpec = Specification.where(
            AcSpecifications.deleted(false)
                    .and(AcSpecifications.hasCom(id))
    );
    boolean hasAssociatedSocieteAddress = acRepository.count(societeAddressSpec) > 0;
    if (hasAssociatedSocieteAddress) {
      throw new ComHasAssociatedAc("", id);
    }

    repository.softDelete(id);
  }

  @Override
  @Transactional
  public UserCom makeUserResponsable(String userId,Integer comId) {
    UserCom userCom=userComRepository.findByUserIdAndComId(userId,comId).orElseThrow(() -> new UserComNotFound("", userId,comId));
    List<UserCom> userComs=userComRepository.findAllByComIdAndIsDeletedFalse(comId);
     userComs.forEach(el -> el.setResponsable(false));
    userCom.setResponsable(true);
    userComRepository.saveAll(userComs);
    return userCom;
  }

  @Override
  public List<Com> getComsWithTypeCPMP() throws RefParamNotFound {
    logger.info("Retrieving Com entities with comType CPMP");
    RefParam cpmpType = refParamRepository.findByCode("CPMP")
            .orElseThrow(() -> new RefParamNotFound("CPMP type not found", List.of(Integer.valueOf("CPMP"))));
    Specification<Com> spec = Specification.where(ComSpecifications.hasComTypeId(cpmpType.getId()))
            .and(ComSpecifications.deleted(false));
    List<Com> coms = repository.findAll(spec);
    logger.info("Found {} Com entities with comType CPMP", coms.size());
    return coms;
  }

  @Override
  public List<Com> getComsWithTypeCNCMP() throws RefParamNotFound {
    logger.info("Retrieving Com entities with comType CNCMP");
    RefParam cncmpType = refParamRepository.findByCode("CNCMP")
            .orElseThrow(() -> new RefParamNotFound("CNCMP type not found", List.of(Integer.valueOf("CNCMP"))));
    Specification<Com> spec = Specification.where(ComSpecifications.hasComTypeId(cncmpType.getId()))
            .and(ComSpecifications.deleted(false));
    List<Com> coms = repository.findAll(spec);
    logger.info("Found {} Com entities with comType CNCMP", coms.size());
    return coms;
  }

}
