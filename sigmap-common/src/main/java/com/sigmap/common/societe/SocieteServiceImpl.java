package com.sigmap.common.societe;


import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteService;
import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.adresse.exceptions.AdresseNotFound;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.SocieteStatus;

import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.keycloak_website_admin.service.KeycloakWebisteAdminService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.*;

import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteRepository;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.UserSocieteSpecifications;
import com.sigmap.common.user_societe.exception.UserIdRequiredException;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SocieteServiceImpl implements SocieteService {
    private final SocieteRepository repository;
    private final RefParamService refParamService;
    private final ActiviteService activiteService;
    private final UserSocieteRepository  userSocieteRepository;
    private final AdressService adressService;
    private final UserSocieteService userSocieteService;
    private final KeycloakWebisteAdminService keycloakWebisteAdminService;
    private final KeycloakAdminService keycloakAdminService;

    @Autowired
    MailService mailService;
    public SocieteServiceImpl(SocieteRepository repository, RefParamService refParamService, ActiviteService activiteService, UserSocieteRepository userSocieteRepository, AdressService adressService, UserSocieteService userSocieteService, KeycloakWebisteAdminService keycloakWebisteAdminService, KeycloakAdminService keycloakAdminService


    ) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.activiteService = activiteService;
        this.userSocieteRepository = userSocieteRepository;
        this.adressService = adressService;
        this.userSocieteService = userSocieteService;
        this.keycloakWebisteAdminService = keycloakWebisteAdminService;
        this.keycloakAdminService = keycloakAdminService;
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
    public Page<Societe> findAllNotEncours(Specification<Societe> societe, Pageable pageable) {
        Specification<Societe> notEnCoursSpec = societe.and((root, query, cb) ->
                cb.notEqual(root.get("status"), SocieteStatus.EN_COURS)
        );
        return repository.findAll(notEnCoursSpec, pageable);    }

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
    public Societe create(SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {


        Societe societe =  SocieteMapper.INSTANCE.map(request);

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
        if(request.getAdresseRequest()==null){
            throw new AddressRequiredException("");
        }
        if(request.getUserResquest()==null){
            throw new UserIdRequiredException("User is required");
        }
        if(request.getAdresseRequest().getAdresse()==null){
            throw new AddressRequiredException("");

        }
        if( request.getAdresseRequest().getCodePostal()==null) {
            throw new CodePostalRequiredException ("");
        }
        if(request.getAdresseRequest().getPaysId()==null){
            throw new PaysIsRequiredException("");
        }
        if(request.getAdresseRequest().getTel1()==null){
            throw new TelIsRequiredException ("");
        }
        if(request.getAdresseRequest().getEmail()==null||request.getUserResquest().getEmail()==null){
            throw new EmailRequiredException ("");
        }
        if(request.getUserResquest().getFirstName()==null||request.getUserResquest().getLastName()==null){
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

        if (request.getUserResquest().getId() == null || request.getUserResquest().getId().isBlank()) {
            throw new UserIdRequiredException();
        }


        societe.setStatus(SocieteStatus.EN_COURS);

        getSizeList(request.getActiviteIds());
        for(Integer i : request.getActiviteIds()){
            Activite  activite=activiteService.findById(i);
            societe.getActivites().add(activite);
        }
        if(request.getAdresseRequest()!=null){
            if(request.getAdresseRequest().getPaysId()==null){
                throw new PaysIsRequiredException ("");
            }
            Adresse address=adressService.create(request.getAdresseRequest());
            societe.setAddress(address);

        }
        Societe savedSociete = save(societe);



        UserSocieteRequest userSocieteRequest= mapFromRequest(request,savedSociete);

        UserSociete savedUserSociete= userSocieteService.create(userSocieteRequest);
        savedSociete.getUsersSociete().add(savedUserSociete);
        return savedSociete;

    }

    @Override
    public Societe update(Integer id,SocieteRequest request) throws RefParamNotFound, SocieteNotFound {
        if(request.getAdresseRequest()==null){
            throw new AddressRequiredException("");
        }
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
        if(request.getAdresseRequest()!=null){
            if(request.getAdresseRequest().getId()!=null&&request.getAdresseRequest().getId()!=societe.getAddress().getId()){
                throw new AdresseNotFound("",request.getAdresseRequest().getId());
            }
            if(request.getAdresseRequest().getPaysId()==null){
                throw new PaysIsRequiredException ("");
            }
            adressService.update(request.getAdresseRequest());
        }
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
    @Transactional
    @Override
    public Societe updateStatus(Integer societeId, boolean status) throws SocieteNotFound {
        Societe societe = findById(societeId);
        if(!status ) {
            societe.setStatus(SocieteStatus.REJETEE);

            Specification<UserSociete> spec = Specification.where(UserSocieteSpecifications.deleted(false)).and(UserSocieteSpecifications.societeId(societeId));
          List  <UserSociete> users=  userSocieteRepository.findAll(spec);
          users.forEach(user -> {user.setStatus(ActiveStatus.NON_ACTIF);
              keycloakWebisteAdminService.disableWebsiteUser(user.getUserId());});
          userSocieteRepository.saveAll(users);

        }
        else {
            societe.setStatus(SocieteStatus.VALIDEE);

            String responsableId = societe.getUsersSociete().stream().filter(UserSociete::getResponsable).findFirst().get().getUserId();
            UserSociete userSociete = userSocieteService.findByUserId(responsableId);
            userSociete.setStatus(ActiveStatus.ACTIF);
            userSocieteRepository.save(userSociete);

            try {
                keycloakWebisteAdminService.validateWebsiteUser(responsableId);
                if (responsableId != null) {
                    try {
                        String tmpPassword = keycloakWebisteAdminService.generateTemporaryPasswordForInscription();
                        UserDTO user = keycloakAdminService.updatePassword(responsableId, tmpPassword);
                        try {
                            mailService.sendEditPasswordForAdminEmail(user, tmpPassword, responsableId);
                            log.info("Password reset email sent successfully to {} on first attempt", user.getEmail());
                        } catch (MessagingException | IOException e) {
                            log.warn("Failed to send password reset email to {} on first attempt: {}", user.getEmail(), e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } catch (Exception e) {
                        log.info("error when updating password {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
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
     UserSocieteRequest mapFromRequest(SocieteRequestInscription request,Societe savedSociete) {
         UserSocieteRequest userSocieteRequest=new UserSocieteRequest();
         userSocieteRequest.setUserId(request.getUserResquest().getId());
         userSocieteRequest.setSocieteId(savedSociete.getId());
         if (request.getUserResquest().getEmail() != null) {
             userSocieteRequest.setEmail(request.getUserResquest().getEmail());
         }

         if (request.getUserResquest().getLastName() != null) {
             userSocieteRequest.setNom(request.getUserResquest().getLastName());
         }

         if (request.getUserResquest().getFirstName() != null) {
             userSocieteRequest.setPrenom(request.getUserResquest().getFirstName());
         }

         if (request.getUserResquest().getCin() != null) {
             userSocieteRequest.setNumCin(request.getUserResquest().getCin());
         }

         if (request.getUserResquest().getDateCIN() != null) {
             userSocieteRequest.setDateDelivranceCin(request.getUserResquest().getDateCIN());
         }

         if (request.getUserResquest().getMobile() != null) {
             userSocieteRequest.setTel1(request.getUserResquest().getMobile());
         }

         if (request.getUserResquest().getFixe() != null) {
             userSocieteRequest.setTel2(request.getUserResquest().getFixe());
         }
         return userSocieteRequest;
     }

}
