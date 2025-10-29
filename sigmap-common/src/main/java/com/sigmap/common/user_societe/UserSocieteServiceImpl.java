package com.sigmap.common.user_societe;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import com.sigmap.common.keycloak_website_admin.service.KeycloakWebisteAdminService;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.SocieteSpecifications;
import com.sigmap.common.societe.exception.SocieteNeedToBeValidate;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.user_societe.exception.*;
import com.sigmap.common.user_societe.mapper.UserSocieteMapper;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserSocieteServiceImpl implements UserSocieteService {
    private final UserSocieteRepository repository;
    private  final SocieteRepository societeRepository;
    private final KeycloakWebisteAdminService keycloakAdminService;

    private final KeycloakTokenService tokenService;

    public UserSocieteServiceImpl(UserSocieteRepository repository, SocieteRepository societeRepository, KeycloakWebisteAdminService keycloakAdminService, KeycloakTokenService tokenService) {
        this.repository = repository;
        this.societeRepository = societeRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.tokenService = tokenService;

    }

    @Override
    public List<UserSociete> findAll() {
        Specification<UserSociete> spec = UserSocieteSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<UserSociete> findAll(Specification<UserSociete> userSociete, Pageable pageable) {
      return   repository.findAll(userSociete, pageable);
    }

    @Override
    public List<UserSociete> findAllList(Specification<UserSociete> spec) {
        Specification<UserSociete> finalSpec = Specification.where(spec).and(UserSocieteSpecifications.deleted(false));
        return repository.findAll(finalSpec);
    }



    @Override
    public Optional<UserSociete> findDeletedById(String userId, Integer societeId) {
        Specification<UserSociete> finalSpec = Specification.where(UserSocieteSpecifications.deleted(true)).and(UserSocieteSpecifications.userId(userId)).and(UserSocieteSpecifications.societeId(societeId));

        return findOne(finalSpec);
    }

    @Override
    public Optional<UserSociete> findOne(Specification<UserSociete> specification) {
        return repository.findOne(specification);
    }

    @Override
    public UserSociete findById(String userId, Integer societeId) throws UserSocieteNotFound {
        Specification<UserSociete> finalSpec = Specification.where(UserSocieteSpecifications.deleted(false)).and(UserSocieteSpecifications.userId(userId)).and(UserSocieteSpecifications.societeId(societeId));

        return findOne(finalSpec).orElseThrow(()->new UserSocieteNotFound("",userId,societeId));
    }

    @Override
    public UserSociete save(UserSociete userSociete) {
        return repository.save(userSociete);
    }
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());
        return headers;
    }
    @Override
    public UserSociete create(UserSocieteRequest request) throws SocieteNotFound {
        HttpHeaders headers = createAuthHeaders();
        log.info("UserSocieteServiceImpl.create {}",request.toString());
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new UserIdRequiredException();
        }

        if (request.getSocieteId() == null) {
            throw new SocieteIdRequiredException();
        }
        Specification<UserSociete> finalSpec = Specification.where(UserSocieteSpecifications.deleted(false)).and(UserSocieteSpecifications.userId(request.getUserId()));
        Optional<UserSociete> userCheck = findOne(finalSpec);
        if(userCheck.isPresent()){
            throw new UserAlreadyRegisteredToOtherCompany("");
        }
        keycloakAdminService.getUserDetails(request.getUserId(), headers);
        Specification<Societe> specificationSociete = SocieteSpecifications.id(request.getSocieteId())
                .and(SocieteSpecifications.deleted(false));
        Societe societe= societeRepository.findOne(specificationSociete)
                .orElseThrow(() -> new SocieteNotFound("",List.of(request.getSocieteId())));

        UserSociete userSociete = UserSocieteMapper.INSTANCE.mapFromRequest(request);
        userSociete.setResponsable(findBySocieteId(request.getSocieteId()).isEmpty());
        userSociete.setSociete(societe);
        userSociete.setStatus(ActiveStatus.NON_ACTIF);
        return repository.save(userSociete);
    }

    @Override
    public UserSociete update(String userId,Integer societeId, UserSocieteRequest request) throws SocieteNotFound, UserSocieteNotFound {
        if (userId == null || request.getUserId().isBlank()) {
            throw new UserIdRequiredException();
        }

        if (societeId == null) {
            throw new SocieteIdRequiredException();
        }
        HttpHeaders headers = createAuthHeaders();
        UserSociete userSociete=findById(userId,societeId);
        boolean responsable=userSociete.getResponsable();
        ActiveStatus activeStatus=userSociete.getStatus();
        keycloakAdminService.getUserDetails(request.getUserId(), headers);
        Specification<Societe> specificationSociete = SocieteSpecifications.id(request.getSocieteId())
                .and(SocieteSpecifications.deleted(false));
        Societe societe= societeRepository.findOne(specificationSociete)
                .orElseThrow(() -> new SocieteNotFound("",List.of(request.getSocieteId())));

        userSociete =UserSocieteMapper.INSTANCE.mapFromRequest(request);
        userSociete.setUserId(userId);
        userSociete.setSocieteId(societeId);
        userSociete.setResponsable(responsable);
        userSociete.setStatus(activeStatus);

        return save(userSociete);

    }

    @Override
    public void delete(String userId,Integer societeId) {
        findById(userId,societeId);
        repository.softDelete(userId,societeId);

    }

    @Override
    public List<UserSociete> findBySocieteId(Integer societeId) throws SocieteNotFound {
        Specification<Societe> specificationSociete = SocieteSpecifications.id(societeId)
                .and(SocieteSpecifications.deleted(false));
        societeRepository.findOne(specificationSociete)
                .orElseThrow(() -> new SocieteNotFound("",List.of(societeId)));
        Specification<UserSociete> spec = Specification.where(UserSocieteSpecifications.deleted(false)).and(UserSocieteSpecifications.societeId(societeId));
       return findAllList(spec);

    }

    @Override
    public UserSociete findByUserId(String userId) throws UserSocieteNotFound {
        Specification<UserSociete> spec = Specification.where(UserSocieteSpecifications.deleted(false)).and(UserSocieteSpecifications.userId(userId));

        return findOne(spec).orElseThrow(()->new UserSocieteNotFound("",userId,null));

    }
    @Transactional
    @Override
    public UserSociete makeUserSocieteResponsable(String userId, Integer societeId) throws SocieteNotFound {
        UserSociete userSociete = findById(userId, societeId);

        if (userSociete.getStatus().equals(ActiveStatus.NON_ACTIF)) {
            throw new UserNeedToBeActive("");
        }

        Societe societe = userSociete.getSociete();
        if (!societe.getStatus().equals(SocieteStatus.VALIDEE)) {
            throw new SocieteNeedToBeValidate("", societeId);
        }

        List<UserSociete> userSocieteList = findBySocieteId(societeId);
        userSocieteList.forEach(u -> u.setResponsable(false));
        repository.saveAll(userSocieteList);

        userSociete.setResponsable(true);
        return save(userSociete);
    }

    @Override
    public UserSociete toggleUserSocieteStatus(String userId, Integer societeId) throws UserSocieteNotFound {
        UserSociete userSociete = findById(userId, societeId);
        if(userSociete.getResponsable()&&userSociete.getStatus().equals(ActiveStatus.ACTIF)){
            throw new CantResponsableNonActif("User is not active", userId);
        }

        if (userSociete.getStatus() == ActiveStatus.ACTIF) {
            userSociete.setStatus(ActiveStatus.NON_ACTIF);
        } else {
            userSociete.setStatus(ActiveStatus.ACTIF);
        }

        return save(userSociete);
    }



}
