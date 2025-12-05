package com.sigmap.common.ac;

import com.sigmap.common.ac.exception.*;
import com.sigmap.common.ac.response.LogoResponse;
import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.FileStorageProperties;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_ac.UserAcSpecifications;
import com.sigmap.common.user_historique_ac.UserAcHistoriqueService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.testcontainers.shaded.com.google.common.io.Files.getFileExtension;

@Service
@Slf4j
public class AcServiceImpl implements AcService{

    private final Logger logger = LoggerFactory.getLogger(AcService.class);

    private final AcRepository repository;
    private final ComRepository comRepository;

    private final KeycloakAdminService keycloakAdminService;

    private final UserAcRepository userAcRepository;

    private final RefParamRepository refParamRepository;

    private final AdressService adressService;

    private final ComService comService;
    private final RefParamService refParamService;
    private final UserAcHistoriqueService userAcHistoriqueService;

    @Autowired
    private FileStorageProperties fileStorageProperties;


    public AcServiceImpl(AcRepository repository, ComRepository comRepository, KeycloakAdminService keycloakAdminService,
                         UserAcRepository userAcRepository, RefParamRepository refParamRepository, AdressService adressService,
                         ComService comService, RefParamService refParamService, UserAcHistoriqueService userAcHistoriqueService) {
        this.repository = repository;
        this.comRepository = comRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.userAcRepository = userAcRepository;
        this.refParamRepository = refParamRepository;
        this.adressService = adressService;
        this.comService = comService;
        this.refParamService = refParamService;
        this.userAcHistoriqueService = userAcHistoriqueService;
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
    public Ac create(AcRequest request) throws ComNotFound, RefParamNotFound, AcNotFound, IOException {
        if(request.getNom() == null || request.getNom().isEmpty()) {
            throw new AttributeRequire("","nom");
        }
          if(request.getTutelId()==null&&(!request.isIstTutel()) ) {
             throw new AttributeRequire("","tutel");
        }
         if(request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AttributeRequire("","email");
        }
       /*  else if(request.getContact() == null || request.getContact().isEmpty()) {
             throw new AcAttributeRequire("","contact");
        }*/

      /*  else if (!request.getEmail().matches(Constants.EMAIL_REGEX)) {
            throw new AcAttributeRequiredAndValid("", "email");
        }*/
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.nom(request.getNom()));

        if(findOne(specification).isPresent()) {
            throw new AcNomAlreadyExist("", request.getNom());
        }
        Ac ac = AcMapper.INSTANCE.map(request);
        if(request.getTutelId()!=null&&!request.isIstTutel() ) {
            Ac tutel= findById(request.getTutelId());
            if(tutel.isIstTutel()){
            ac.setTutel(tutel);
            }
            else {
                throw new AttributeRequire("","tutel");
            }
        }
        Specification<RefParam> specPays= RefParamSpecifications.code("MR");
        RefParam pays=refParamService.findOne(specPays).orElseThrow(()-> new RefParamNotFound("",null));
        if(request.getAdresse()!=null) {
           Adresse adresse=adressService.create(request.getAdresse());
           adresse.setPays(pays);
           ac.setAdresse(adresse);
        }
        else{
            AdresseRequest adresseRequest=new AdresseRequest();
           Adresse adresse=adressService.create(adresseRequest);
            adresse.setPays(pays);
            ac.setAdresse(adresse);
        }
        if(request.getComId()!=null) {
            Com com=comService.findById(request.getComId());
            ac.setCom(com);
        }
        if(request.isIstTutel()){
            ac.setTutel(null);
        }
        else if(ac.getTutel()!=null){
            ac.setIstTutel(false);
        }
        if(ac.getTypeAffCommission()!=null) {
            ac.setTypeAffCommission(request.getTypeAffCommission());
        }
        Ac savedAc = repository.save(ac);
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            String originalFilename = StringUtils.cleanPath(request.getLogoFile().getOriginalFilename());
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFilename = "logo-ac-" + savedAc.getId() + "." + extension;

            Path logoDir = Paths.get(fileStorageProperties.getAcLogoPath(), String.valueOf(savedAc.getId()));
            Files.createDirectories(logoDir);

            Path targetPath = logoDir.resolve(newFilename);

            if (savedAc.getLogo() != null) {
                Path oldFilePath = logoDir.resolve(Paths.get(savedAc.getLogo()).getFileName());
                Files.deleteIfExists(oldFilePath);
            }
            Files.createDirectories(targetPath.getParent());

            request.getLogoFile().transferTo(targetPath.toFile());

            savedAc.setLogo(newFilename);
            repository.save(savedAc);
        }
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String userId : request.getUserIds()) {
                try {
                    UserAc userAc = new UserAc();
                    userAc.setUserId(userId);
                    userAc.setAcId(savedAc.getId());
                    userAc.setIsDeleted(false);
                    userAc.setStatus(ActiveStatus.ACTIF);
                    userAcRepository.save(userAc);
                    userAcHistoriqueService.createStartHistory(userId, savedAc);
                } catch (Exception e) {
                    logger.warn("Impossible d'associer l'utilisateur {} à l'AC {} : {}", userId, savedAc.getId(), e.getMessage());
                }
            }
        }

        return savedAc;
    }


    @Override
    @Transactional
    public Ac update(Integer id, AcRequest request) throws AcNotFound, ComNotFound, DuplicateComIdsException, UserAcDefaultStatus, RefParamNotFound, IOException {
        if(request.getNom() == null || request.getNom().isEmpty()) {
            throw new AttributeRequire("","nom");
        }
         if(request.getTutelId()==null&&!request.isIstTutel()){
            throw new AttributeRequire("","tutel");
        }
         if(request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AttributeRequire("","email");
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
                throw new AttributeRequire("","tutel");
            }
        }
        Specification<RefParam> specPays= RefParamSpecifications.code("MR");
        RefParam pays=refParamService.findOne(specPays).orElseThrow(()-> new RefParamNotFound("",null));

        if(request.getAdresse()!=null&& Objects.equals(ac.getAdresse().getId(), request.getAdresse().getId())) {
            Adresse adresse=adressService.update(request.getAdresse());
            adresse.setPays(pays);
            ac.setAdresse(adresse);
        }
        else if (ac.getAdresse()==null&&request.getAdresse()!=null){
            Adresse theAdress=adressService.create(request.getAdresse());
            theAdress.setPays(pays);
            ac.setAdresse(theAdress);
        }
        else{
            AdresseRequest adresseRequest=new AdresseRequest();
            adresseRequest.setPaysId(pays.getId());
            Adresse theAddress=adressService.create(adresseRequest);
            theAddress.setPays(pays);
            ac.setAdresse(theAddress);
        }
        if(request.getComId()!=null) {
            Com com=comService.findById(request.getComId());
            ac.setCom(com);
        }
        if(ac.isIstTutel()){
            ac.setTutel(null);
        }
        else if(ac.getTutel()!=null){
            ac.setIstTutel(false);
        }
        ac.setId(ac.getId());
        ac.setTutel(ac.getTutel());
        ac.setIstTutel(ac.isIstTutel());
        if(ac.getTypeAffCommission()!=null) {
        ac.setTypeAffCommission(request.getTypeAffCommission());
        }
        Ac savedAc = repository.save(ac);

        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            String originalFilename = StringUtils.cleanPath(request.getLogoFile().getOriginalFilename());
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFilename = "logo-ac-" + savedAc.getId() + "." + extension;

            Path logoDir = Paths.get(fileStorageProperties.getAcLogoPath(), String.valueOf(savedAc.getId()));
            Files.createDirectories(logoDir);

            Path targetPath = logoDir.resolve(newFilename);

            if (savedAc.getLogo() != null) {
                Path oldFilePath = logoDir.resolve(savedAc.getLogo());
                if (Files.exists(oldFilePath) && Files.isRegularFile(oldFilePath)) {
                    Files.delete(oldFilePath);
                }
            }

            request.getLogoFile().transferTo(targetPath.toFile());

            savedAc.setLogo(newFilename);
            repository.save(savedAc);
        }

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
                    //create end for no more existing ons
                    userAcHistoriqueService.createEndHistory(existing.getUserId(), existing.getAcId());
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

                    //create start for new users
                    userAcHistoriqueService.createStartHistory(userId, ac);
                }
            }
        }

        // hnaa na3mlou updates li jdod

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
        List<UserAc> users=userAcRepository.findAllByAcIdAndIsDeletedFalse(id);
        if(!users.isEmpty()) {
            throw new AcHasAssociatedUser("",id);
        }

        repository.softDelete(id);
    }

    @Override
    public List<Ac> findAllTutel(Specification<Ac> ac) {
        Specification<Ac> specification = AcSpecifications.deleted(false).and(AcSpecifications.isItTutel(true));
        return repository.findAll(specification);
    }

    @Override
    public List<Ac> findAllByComId(Integer comId) throws ComNotFound {
        comService.findById(comId);
        Specification<Ac> spec = AcSpecifications.findByComId(comId).and(AcSpecifications.deleted(false));
        return repository.findAll(spec);
    }


    @Override
    public List<Ac> findAllAcsWithNoCom() {
        // Step 1: Spec to get Ac with com == null
        Specification<Ac> spec = AcSpecifications.hasComNull();

        // Step 2: Get all Ac IDs that are used as 'tutel' in any Com
        Set<Integer> usedTutelAcIds = comService.findAll().stream().filter(com->!com.getIsDeleted())
                .map(Com::getTutel)           // get the Ac used as tutel
                .filter(Objects::nonNull)
                .map(Ac::getId)
                .collect(Collectors.toSet());

        // Step 3: Filter out those whose ID is in the used set
        return repository.findAll(spec).stream()
                .filter(ac -> !usedTutelAcIds.contains(ac.getId())&&!ac.getIsDeleted()&& !(ac.getCom().getComType().getCode() =="CPMP"))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves photo bytes for a given AC ID
     * @param acId the ID of the AC
     * @return byte array of the photo
     * @throws RuntimeException if AC not found or photo cannot be read
     */
    @Override
    public LogoResponse getAcPhoto(Integer acId, String basePath) throws AcNotFound {
        log.info("Retrieving photo for AC ID: {} using base path: {}", acId, basePath);

        Ac ac = findById(acId);
        String logoPath = ac.getLogo();

        // 1. Return null if there is no logo path in the database.
        if (!StringUtils.hasText(logoPath)) {
            log.warn("No logo path found in the database for AC ID: {}", acId);
            return null;
        }

        try {
            // 2. Try to read the file from the disk.
            byte[] photoBytes = readPhotoFromFileSystem(acId, logoPath, basePath);
            String extension = getFileExtension(logoPath);
            return new LogoResponse(photoBytes, extension);

        } catch (RuntimeException e) {
            // 3. If readPhotoFromFileSystem throws an exception (file not found),
            //    log it and return null instead of crashing.
            log.warn("Could not find or read photo file for AC ID: {}. Reason: {}", acId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<Ac> getAcsByUserId(String userId) {
        Specification<UserAc> specification= UserAcSpecifications.activeAcsByUserId(userId);
        List<UserAc> userAcs = userAcRepository.findAll(specification);

        return userAcs.stream().map(UserAc::getAc).toList();
    }

    // This private method now takes the basePath as an argument
    private byte[] readPhotoFromFileSystem(Integer acId, String logoPath, String basePath) {
        if (basePath == null || basePath.trim().isEmpty()) {
            throw new RuntimeException("Base path was not provided by the calling service.");
        }

        try {
            // Build the path dynamically using the provided base path
            Path fullPath = Paths.get(basePath, acId.toString(), logoPath);

            if (!Files.exists(fullPath)) {
                log.error("Photo file does not exist: {}", fullPath);
                throw new RuntimeException("Photo file not found: " + logoPath);
            }

            log.info("Reading photo file from: {}", fullPath);
            return Files.readAllBytes(fullPath);

        } catch (IOException e) {
            log.error("Error reading photo file: {}", logoPath, e);
            throw new RuntimeException("Could not read photo file: " + logoPath, e);
        }
    }

    @Override
    public Integer getComIdByAcId(Integer acId) throws AcNotAssociatedCom {
        return repository.findById(acId)
                .map(ac -> ac.getCom() != null ? ac.getCom().getId() : null)
                .orElseThrow(() -> new AcNotAssociatedCom ("Aucune commission n’est liée à cet AC. "));
    }

}
