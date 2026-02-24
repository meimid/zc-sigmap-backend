package com.sigmap.common.appel_offre;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.appel_offre.exception.AppelOffreNotFound;
import com.sigmap.common.appel_offre.mapper.AppelOffreMapper;
import com.sigmap.common.appel_offre.request.AppelOffreEditRequest;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreResponseById;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.StatusAppelOffre;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_ac.UserAcSpecifications;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

import static com.sigmap.common.config.ModePassationEtape.DEUX_ETAPES;
import static com.sigmap.common.config.ModePassationEtape.ETAPE_UNIQUE;

@Slf4j
@Service
@AllArgsConstructor
public class AppelOffreServiceImpl implements AppelOffreService {

    private final AppelOffreRepository appelOffreRepository;
    private final PpmRepository ppmRepository;
    private final RealisationRepository realisationRepository;
    private final UserAcRepository userAcRepository;
    private final AcService acService;
    private final DacRepository dacRepository;
    private final PpmService ppmService;
    private final CurrentUserService currentUserService;
    private static String lastYear = String.valueOf(Year.now().getValue());
    private static int counter = 0;

    @Override
    public AppelOffre save(AppelOffre appelOffre) {
        return appelOffreRepository.save(appelOffre);
    }

    @Override
    public List<AppelOffre> findAll() {
        return appelOffreRepository.getAllNotDeleted();
    }

    @Override
    public Page<AppelOffre> findAll(Specification<AppelOffre> spec, Pageable pageable) {
        log.debug("Fetching All AppelOffre ");
        boolean isAdmin = Authentication.hasRole("ADMIN_CENTRAL") || Authentication.hasRole("SUPER_ADMIN");
        if (isAdmin) {
        spec = spec.and(AppelOffreSpecifications.deleted(false));
        return appelOffreRepository.findAll(spec, pageable);
    }

        List<Integer> userAcs = currentUserService.getCurrentUserAcIds();
        List<Integer> userComs = currentUserService.getCurrentUserComIds();

        if ((userAcs == null || userAcs.isEmpty()) && (userComs == null || userComs.isEmpty())) {
            return Page.empty(pageable);
        }

        return appelOffreRepository.findAllFilteredByUser(userAcs, userComs, pageable);
    }

    @Override
    public AppelOffreResponseById findById(Integer id) throws PpmNotFound, AcNotFound {

        log.debug("Fetching AppelOffre with Id: {} ", id);
        if (id == null){
            throw new IllegalArgumentException("you must have Id");
        }

        AppelOffre appelOffre = appelOffreRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new AppelOffreNotFound(id));

        // Récupérer le PPM avec la dernière version
        Optional<Ppm> ppmOptional = ppmRepository.finPpmByIdAndVersion(appelOffre.getPpm().getId(),appelOffre.getPpm().getVersion());
        if (ppmOptional.isEmpty()) {
            AppelOffreResponseById appelOffreResponseById = new AppelOffreResponseById();
            appelOffreResponseById.getPpm().setStatus("PPM non trouvé pour l'ID: " + appelOffre.getPpm().getId());
            return appelOffreResponseById;
        }

        Ppm ppm = ppmOptional.get();

        // Préparer l'objet de réponse
        AppelOffreResponseById response = new AppelOffreResponseById();

        // Mapper le PPM vers PpmResponseDto
        if (ppm.getDocs() != null) {
            List<Docs> nonDeletedDocs = ppm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());
            ppm.setDocs(nonDeletedDocs);
        }

        Optional<Realisation> realisationOptional = realisationRepository.findRealisationByIdAndVersion(appelOffre.getRealisation().getId(), appelOffre.getRealisation().getVersion());

        Realisation realisation = realisationOptional.get();

        realisation.getFinancements().stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                .collect(Collectors.toList());



        PpmResponseDto ppmResponse = PpmMapper.INSTANCE.mapTAo(ppm);
        response.setPpm(ppmResponse);
        RealisationResponseDTO realisationResponseDTO = RealisationMapper.INSTANCE.mapForAo(realisation);
        response.setId(appelOffre.getId());
        response.setReference(appelOffre.getReference());
        response.setNom(appelOffre.getNom());
        response.setDescription(appelOffre.getDescription());
        response.setEtape(appelOffre.getEtape());
        response.setModePassation(appelOffre.getRealisation().getModePassation().getType());
        response.setLotSepares(appelOffre.getLotSepares());
        response.setRealisation(realisationResponseDTO);
        response.setStatusAppelOffre(appelOffre.getStatusAppelOffre());
        response.setCouldCreateDac(couldCreateDac (appelOffre, id));

        return response;
    }

    @Override
    @Transactional
    public AppelOffre create(CreateAppelOffreRequest request) throws RealisationNotFoundException {
        //AppelOffre appelOffre = appelOffreMapper.createRequestToEntity(request);

        if (request.getPpmId() == null || request.getPpmVer() == null ||
            request.getRealisationId() == null || request.getRealisationVer() == null){
            throw new RuntimeException("AppelOffre must have ppmId, ppmVer, realisationId and  realisationVer  ");
        }

        AppelOffre appelOffre = new AppelOffre();

        // fetch PPM by ID
        CompositeId ppmId = new CompositeId(request.getPpmId(), request.getPpmVer());
        Ppm ppm = ppmRepository.findById(ppmId)
                .orElseThrow(() -> new PpmIdNotFound(request.getPpmId(), request.getPpmVer()));

        appelOffre.setPpm(ppm);

        // fetch realisation by ID
        CompositeId realId = new CompositeId(request.getRealisationId(), request.getRealisationVer());
        Realisation realisation = realisationRepository.findById(realId)
                .orElseThrow(() -> new RealisationNotFoundException(request.getRealisationId(), request.getRealisationVer()));

        appelOffre.setRealisation(realisation);

        //set the other fields

        //fro the ref it take ppm.nom/annéé/number
        //String reference = "";  AO_AbreviationAC_Annee_Num inc
        appelOffre.setReference("AO_".concat(ppm.getAc().getNom().concat(generateReference(ppm.getAnnee()))));

        appelOffre.setNom(request.getNom());
        appelOffre.setDescription(request.getDescription());
        appelOffre.setLotSepares(request.getLotSepares());
        appelOffre.setStatusAppelOffre(request.getStatusAppelOffre());

        save(appelOffre);

        return appelOffre;
    }

    @Override
    @Transactional
    public AppelOffre update(Integer id, AppelOffreEditRequest request){
        AppelOffre existingAppelOffre = appelOffreRepository.findById(id)
                .orElseThrow(() -> new AppelOffreNotFound( id));

        existingAppelOffre.setNom(request.getNom());
        existingAppelOffre.setDescription(request.getDescription());
        existingAppelOffre.setLotSepares(request.getLotSepares());

        save(existingAppelOffre);
        return existingAppelOffre;
    }

    @Override
    public List<Ppm> getAllPpmsWithAcsforConnectedUser() {
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new RuntimeException(" user Id not found");
        }
        List<Ac> acList = acService.getAcsByUserId(currentUserId.get());
        List<Integer> acIds = acList.stream().map(Ac::getId).toList();

        // get all ppm that are published, not deleted, have realisation and their acs are for connected user
        Specification<Ppm> specification = PpmSpecifications.isPublished()
                .and(PpmSpecifications.isNotDeleted())
                .and(PpmSpecifications.ppmAc(acIds))
                .and(PpmSpecifications.hasRealisations())
                .and(PpmSpecifications.isLatestVersion())
                .and(PpmSpecifications.isPublished());

        List<Ppm> foundPpms = ppmRepository.findAll(specification);

        if (foundPpms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Ppm> PpmsWithFreeRealisation = ppmRepository.findPpmsWithFreeRealisation(foundPpms);

        return PpmsWithFreeRealisation;
    }

    @Override
    public List<Realisation> getRealisationsNotRelatedToAppelOffreWithStatusCreeOrCloturer(Integer ppmId,Integer ppmVer) {
        List<StatusAppelOffre> statuses = Arrays.asList(StatusAppelOffre.CREE, StatusAppelOffre.CLOTURER);
        List<Realisation> foundRealisation =realisationRepository.findRealisationsNotLinkedToAppelOffreWithStatus(ppmId, ppmVer, statuses);
        return foundRealisation;
    }

    public List<Ac> getAcsByUserId() {
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        if (currentUserId.isEmpty()){
            throw new RuntimeException(" user Id not found");
        }
        Specification<UserAc> specification= UserAcSpecifications.activeAcsByUserId(currentUserId.get());
        List<UserAc> userAcs = userAcRepository.findAll(specification);
        List<Ac> acList = userAcs.stream().map(UserAc::getAc).toList();
        return acList;
    }

    public static String generateReference(String currentYear) {

        if (!Objects.equals(currentYear, lastYear)) {
            lastYear = currentYear;
            counter = 1;
        } else {
            counter++;
        }

        return currentYear + "/" + counter;
    }

    private Boolean couldCreateDac (AppelOffre appelOffre, Integer id){

        boolean create;
        if (appelOffre == null){
            throw new RuntimeException("apple offre could not be null");
        }

        int nbrDac = dacRepository.getDacCreatedNumberForAppelOffre(id);

        if ( appelOffre.getRealisation().getModePassation().getEtapes().equals(ETAPE_UNIQUE) && nbrDac < 1){
            create = true;
        }
        else if (appelOffre.getRealisation().getModePassation().getEtapes().equals(ETAPE_UNIQUE) && nbrDac >= 1){
            create = false;
        }
        else if (appelOffre.getRealisation().getModePassation().getEtapes().equals(DEUX_ETAPES) && nbrDac < 2){
            create = true;
        }
        else create =false;

        return create;

    }
}
