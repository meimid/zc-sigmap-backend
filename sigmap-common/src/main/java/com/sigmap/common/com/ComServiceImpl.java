package com.sigmap.common.com;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcSpecifications;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcAlreadyHaveCom;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.exception.DuplicateAcIdsException;
import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.com.exception.CNCMPPNotFound;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.exception.PermanentComAlreadyExist;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.sigmap.common.security.Authentication;
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
import org.springframework.context.annotation.Lazy;
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
  private final AdressService adressService;
  private final AcService acService;
  private final DocumentCommuneService documentCommuneService;
  private final DocsRepository docsRepository;
  public ComServiceImpl(
          ComRepository repository, RefParamService refParamService, AcRepository acRepository, AdressService adressService, @Lazy AcService acService, DocumentCommuneService documentCommuneService, DocsRepository docsRepository) {
    this.repository = repository;
    this.refParamService = refParamService;
    this.acRepository = acRepository;
      this.adressService = adressService;
      this.acService = acService;
      this.documentCommuneService = documentCommuneService;
      this.docsRepository = docsRepository;
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
    Com  com= findOne(specification).orElseThrow(() -> new ComNotFound("", List.of(id)));
    List<Docs> docs=com.getDocs().stream().filter(doc->!doc.getIsDeleted()).collect(Collectors.toList());
    com.setDocs(docs);
    return com;
  }

  @Override
  public Com save(Com com) {
    return repository.save(com);
  }

  @Override
  public Com create(ComRequest request)
          throws RefParamNotFound, AcNotFound, DuplicateAcIdsException, ComNotFound, IOException {
    Specification<RefParam> refParamSpecification=RefParamSpecifications.code("CNCMP-P").and(RefParamSpecifications.deleted(false));
    List<RefParam> refParams=refParamService.findListRefParamSpecification(refParamSpecification);
    if (!refParams.isEmpty()) {
      for(RefParam refParam:refParams){
        Integer id = refParam.getId();
        Specification<Com>comSpecification =ComSpecifications.hasComTypeId(id).and(ComSpecifications.deleted(false));
        List<Com> com=repository.findAll(comSpecification);
        log.info("Com count:"+com.size());
        if(!com.isEmpty()&& Objects.equals(request.getComTypeId(), id)) {
          throw new PermanentComAlreadyExist("",refParam.getCode());
        }
      }

    }
    RefParam comType = refParamService.findByIdWithoutSystemType(request.getComTypeId());
    Com com = ComMapper.INSTANCE.map(request);

    if (comType.getId() != null) {
      com.setComType(comType);
    } else {
      com.setComType(null);
    }
    if(request.getTutelId()!=null ) {
      Specification<Com> specCom=ComSpecifications.hasTute(request.getTutelId()).and(ComSpecifications.deleted(false));
      if(!findAll(specCom, Pageable.ofSize(1)).isEmpty()) {
        throw new AcAlreadyHaveCom("Tutel already have com",request.getTutelId());
      }
      else {
        Ac tutel = acService.findById(request.getTutelId());
        com.setTutel(tutel);
      }
    }

    Specification<RefParam> specPays= RefParamSpecifications.code("MR");
    RefParam pays=refParamService.findOne(specPays).orElseThrow(()-> new RefParamNotFound("",null));
    if(request.getTheAdresse()!=null) {
      Adresse adresse=adressService.create(request.getTheAdresse());
      adresse.setPays(pays);
      com.setTheAdresse(adresse);
    }
    else{
      AdresseRequest adresseRequest=new AdresseRequest();
      Adresse adresse=adressService.create(adresseRequest);
      adresse.setPays(pays);
      com.setTheAdresse(adresse);
    }

    Com savedCom = save(com);

    if (request.getFiles() != null) {
      documentCommuneService.processAndSaveDocumentsForCom(request.getFiles(), savedCom, Constants.PATH_ENTREPOT_GED_COM);
      log.info("*****************************************{}",com.getDocs().size());
      save(savedCom);
    }

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
 // @Transactional
  public Com update(Integer id, ComRequest request)
          throws ComNotFound, RefParamNotFound, AcNotFound, DuplicateAcIdsException, DocNotFound, IOException {
    Com com = repository.findById(id).orElseThrow(() -> new ComNotFound("", List.of(id)));
    Specification<RefParam> refParamSpecification=RefParamSpecifications.code("CNCMP-P").and(RefParamSpecifications.deleted(false));
    List<RefParam> refParams=refParamService.findListRefParamSpecification(refParamSpecification);
    if (!refParams.isEmpty()) {
      for(RefParam refParam:refParams){
        Integer refParamId = refParam.getId();
        Specification<Com>comSpecification =ComSpecifications.hasComTypeId(refParamId).and(ComSpecifications.deleted(false));
        List<Com> optionalCom=repository.findAll(comSpecification);
        if(!optionalCom.isEmpty()&&!optionalCom.get(0).getId().equals(com.getId())&& Objects.equals(request.getComTypeId(), refParamId)) {
          throw new PermanentComAlreadyExist("",refParam.getCode());
        }
      }
    }
    RefParam comType = refParamService.findByIdWithoutSystemType(request.getComTypeId());
    ComMapper.INSTANCE.updateFromRequest(request, com);

    if(request.getTutelId()!=null ) {
      Specification<Com> specCom=ComSpecifications.hasTute(request.getTutelId()).and(ComSpecifications.deleted(false));
      if(!findAll(specCom, Pageable.ofSize(1)).isEmpty()) {
        throw new AcAlreadyHaveCom("Tutel already have com",request.getTutelId());
      }
      else {
        Ac tutel = acService.findById(request.getTutelId());
        com.setTutel(tutel);
      }
    }
    if (comType.getId() != null) {
      com.setComType(comType);
    } else {
      com.setComType(null);
    }
    Specification<RefParam> specPays= RefParamSpecifications.code("MR");
    RefParam pays=refParamService.findOne(specPays).orElseThrow(()-> new RefParamNotFound("",null));

    if(request.getTheAdresse()!=null&& Objects.equals(com.getTheAdresse().getId(), request.getTheAdresse().getId())) {
      Adresse theAdress=adressService.update(request.getTheAdresse());
      theAdress.setPays(pays);
      com.setTheAdresse(theAdress);
    }
    else if (com.getTheAdresse()==null&&request.getTheAdresse()!=null){

      Adresse theAddress=adressService.create(request.getTheAdresse());
      theAddress.setPays(pays);
      com.setTheAdresse(theAddress);
      }
    else {
      AdresseRequest adresseRequest=new AdresseRequest();
      adresseRequest.setPaysId(pays.getId());
      Adresse theAddress=adressService.create(adresseRequest);
      theAddress.setPays(pays);
      com.setTheAdresse(theAddress);
    }


    List<Docs> existingDocs = com.getDocs() != null ? new ArrayList<>(com.getDocs()) : new ArrayList<>();
    List<Integer> requestedDocIds = request.getDocIds() != null ? new ArrayList<>(request.getDocIds()) : new ArrayList<>();
    List<Docs> requestedDocs = docsRepository.findByIdIn(requestedDocIds);
    List<Integer> existingDocIds = com.getDocs() != null
            ? com.getDocs().stream().map(Docs::getId).toList()
            : new ArrayList<>();
    if (!requestedDocs.isEmpty()) {
      existingDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
      existingDocs.addAll(requestedDocs);
      com.setDocs(existingDocs);
    }

    if (requestedDocIds.size() != requestedDocs.size()) {
      List<Integer> foundDocIds = requestedDocs.stream().map(Docs::getId).toList();
      List<Integer> missingDocIds = requestedDocIds.stream()
              .filter(docId -> !foundDocIds.contains(docId))
              .collect(Collectors.toList());
      throw new DocNotFound("Documents not found", missingDocIds);
    }
    List<Integer> docIdsToDelete = existingDocIds.stream()
            .filter(docId -> !requestedDocIds.contains(docId))
            .toList();
List<Integer> documentsIdsToDelete=docsRepository.findAllById(docIdsToDelete).stream().filter(doc->!doc.getIsDeleted()).map(Docs::getId).toList();

    documentCommuneService.deleteDocuments(documentsIdsToDelete);
      if (request.getFiles() != null) {
        documentCommuneService.processAndSaveDocumentsForCom(request.getFiles(), com, Constants.PATH_ENTREPOT_GED_COM);
    }
     save(com);

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
        //userComRepository.softDelete(existing.getComId(), existing.getUserId());
        manyallySoftDelete(existing.getComId() , existing.getUserId());
      }

    }


    // call new methode

   // manuallyDelete(existingUserComs, newUserIds);





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


    return  findById(id);
  }

/*

  void manuallyDelete(List<UserCom> existingUserComs, List<String> newUserIds) {

    // 3. Supprimer ceux qui n'existent plus
    for (UserCom existing : existingUserComs) {
      if (!newUserIds.contains(existing.getUserId())) {
        if(existing.isResponsable()) {
          throw new UserResponsibleForCom("",existing.getUserId(),existing.getComId());
        }
        //userComRepository.softDelete(existing.getComId(), existing.getUserId());
        manyallySoftDelete(existing.getComId() , existing.getUserId());
      }

    }
  }

*/

  void manyallySoftDelete (Integer comId , String userId ) {
    Optional<UserCom> uscom = userComRepository.findByUserIdAndComId(userId,comId);

    String ConnectedUserID =Authentication.getCurrentUserId().orElse("system");
    if(uscom.isPresent()) {

      uscom.get().setIsDeleted(true);
      uscom.get().setDeletedAt(Calendar.getInstance());
      uscom.get().setDeletedBy(ConnectedUserID);

      userComRepository.save(uscom.get());

    }
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
  public void delete(Integer id) throws ComHasAssociatedAc, ComNotFound {
    findById(id);
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

  @Override
  public Com getCommissionDeTutelle(Integer acId) throws ComNotFound, AcNotFound {
    Integer tutelId;
    Ac ac=acService.findById(acId);
    if(ac.isIstTutel()){
      tutelId=ac.getId();
    }
    else{
      tutelId=ac.getTutel().getId();
    }
    Specification<Com> specCom=ComSpecifications.hasTute(tutelId).and(ComSpecifications.deleted(false));
    Page<Com> page = findAll(specCom, Pageable.ofSize(1));
    if (page.hasContent()) {
      Com com= page.getContent().get(0);
      return com;
    } else {
      throw new ComNotFound("Com not found for tutel ID: " + tutelId, null);
    }
  }

  @Override
  public Com getCommissionPrivee(Integer acId) throws ComNotFound, AcNotFound {
    Ac ac=acService.findById(acId);
    if(!ac.isIstTutel()){
    Specification<Com> specCom=ComSpecifications.hasTute(acId).and(ComSpecifications.deleted(false));
    Page<Com> page = findAll(specCom, Pageable.ofSize(1));
    if (page.hasContent() ) {
      Com com= page.getContent().get(0);
      return com;
    } else {
      throw new ComNotFound("Com not found for ac :",null);
    }
    }else{
      if(ac.getCom()!=null){
        Com com= ac.getCom();
        return com;
      }
      else{
        throw new ComNotFound("Com not found for ac ",null);
      }
    }
  }

  @Override
  public List<Com> getAllCommissionsDeTutelles(Integer acId) {
    Specification<Com> spec = ComSpecifications.findActiveComsWithActiveTutelAuthority();
    List<Com> coms= repository.findAll(spec).stream().filter(com->!com.getTutel().getId().equals(acId)).collect(Collectors.toList());
    return coms;
  }

  @Override
  public List<Com> getAllSpecialCommissions() {
    Specification<Com> specification=ComSpecifications.special(true).and(ComSpecifications.deleted(false));
    return repository.findAll(specification);
  }

  @Override
  public Com findCNCMPP() throws CNCMPPNotFound {
    Specification<RefParam> refParamSpecification=RefParamSpecifications.code("CNCMP-P").and(RefParamSpecifications.deleted(false));
    List<RefParam> refParams=refParamService.findListRefParamSpecification(refParamSpecification);
    if (!refParams.isEmpty()) {
      for(RefParam refParam:refParams){
        Integer id = refParam.getId();
        Specification<Com>comSpecification =ComSpecifications.hasComTypeId(id).and(ComSpecifications.deleted(false));
        List<Com> com=repository.findAll(comSpecification);
        log.info("Com count:"+com.size());
        if(!com.isEmpty()) {
          return com.get(0);
        }
      }
    }
    throw new CNCMPPNotFound("");
  }

}
