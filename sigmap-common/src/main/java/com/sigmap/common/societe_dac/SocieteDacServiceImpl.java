package com.sigmap.common.societe_dac;


import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.DacSpecifications;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.entity.MethodePaiement;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteSpecifications;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_dac.exceptions.SocieteDacAlreadyExistsException;
import com.sigmap.common.societe_dac.exceptions.SocieteDacNotFoundException;
import com.sigmap.common.societe_dac.request.SocieteDacRequest;
import com.sigmap.common.societe_dac.request.SocieteDacRequestForWebsite;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.exception.UserSocieteNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocieteDacServiceImpl implements SocieteDacService {

    private final SocieteDacRepository societeDacRepository;
    private final DacRepository dacRepository;
    private final SocieteRepository societeRepository;
    private final DocumentCommuneService documentCommuneService;
    private final UserSocieteService userSocieteService;
    private final AlfrescoService alfrescoService;


    @Override
    @Transactional(readOnly = true)
    public Page<SocieteDac> findAll(Specification<SocieteDac> societeDac, Pageable pageable) {
        return societeDacRepository.findAll(societeDac, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocieteDac> findAllNotDeleted(Specification<SocieteDac> societeDac, Pageable pageable) {
        societeDac = SociteDacSpecifications.deleted(false);
        return societeDacRepository.findAll(societeDac, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SocieteDac findById(Integer dacId, Integer dacVersion, Integer societeId) {
        log.debug("Recherche de SocieteDac avec id: {}, dacVersion: {}, societeId: {}", dacId, dacVersion, societeId);

        // verifier l'existence des field de request
        if(dacId == null || dacVersion == null || societeId == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVersion, societeId sont obligatoires");
        }

        return societeDacRepository.findById(dacId, dacVersion, societeId).
                orElseThrow(() -> new SocieteDacNotFoundException(dacId, dacVersion, societeId));
    }

    @Override
    @Transactional
    public SocieteDac create(SocieteDacRequest request) throws SocieteNotFound, IOException {
        // verifier l'existence des field de request
        if(request.getDacId() == null || request.getDacVer() == null || request.getSocieteId() == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, societeId sont obligatoires");
        }

        //verifier le mode de paiyement
        if(request.getMethodePaiement() == null) {
            throw new IllegalArgumentException("Le champs methodePaiement est obligatoire");
        }


        // verifier l'existence du SocieteDac
        Optional<SocieteDac> existingSocieteDac = societeDacRepository.findById(request.getDacId(), request.getDacVer(), request.getSocieteId());
        if(existingSocieteDac.isPresent()) {
            throw new SocieteDacAlreadyExistsException(request.getDacId(), request.getDacVer(), request.getSocieteId());
        }

        // verifier l'existence du Dac
        Specification<Dac> specificationForDac = DacSpecifications.id(request.getDacId(),request.getDacVer())
                .and(DacSpecifications.searchByDeleted(false));

        Dac dac = dacRepository .findOne(specificationForDac)
                .orElseThrow(() -> new DacNotFound("", request.getDacId(), request.getDacVer()));

        // Vérification de l'existence de la societe
        Specification<Societe> specificationForSocite = SocieteSpecifications.id(request.getSocieteId())
                .and(SocieteSpecifications.deleted(false));
        Societe societe = societeRepository.findOne(specificationForSocite)
                .orElseThrow(() -> new SocieteNotFound("", List.of(request.getSocieteId())));

        Integer IdJustifPaiement = null;
        try {
            if (request.getMethodePaiement() == MethodePaiement.MANUEL ) {
                IdJustifPaiement =  documentCommuneService.processAndSaveDocumentsForPaymentDac(request.getJustifPaiementFile(), request.getDacId(), request.getDacVer());
            }
        }
        catch (IOException e) {
            throw new IOException("An error occurred while processing the file: " + e.getMessage());
        }
        // initialise id
        SocieteDacID societeDacId = new SocieteDacID(dac, societe);

        SocieteDac societeDac = new SocieteDac();
        societeDac.setId(societeDacId);
        societeDac.setMethodePaiement(request.getMethodePaiement());
        societeDac.setJustifPaiementId(IdJustifPaiement);
        societeDac = societeDacRepository.save(societeDac);
        return societeDac;

    }


    @Override
    @Transactional
    public SocieteDac update(SocieteDacRequest request) {
        log.debug("Modification de SocieteDac avec DacId: {}, DacVersion: {}, Societeid: {}", request.getDacId(), request.getDacVer(), request.getSocieteId());

        // verifier l'existence des field de request
        if(request.getDacId() == null || request.getDacVer() == null || request.getSocieteId() == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, societeId sont obligatoires");
        }

        // verification de l'existace du SocieteDac
        Optional<SocieteDac> existingSocieteDac = societeDacRepository.findById(request.getDacId(), request.getDacVer(), request.getSocieteId());
        if(!existingSocieteDac.isPresent()) {
            throw new SocieteDacNotFoundException(request.getDacId(), request.getDacVer(), request.getSocieteId());
        }

        // update SocieteDac
         SocieteDac societeDac = existingSocieteDac.get();

        societeDac.setMethodePaiement(request.getMethodePaiement());
        societeDac.setJustifPaiementId(request.getJustifPaiementId());
        return societeDacRepository.save(societeDac);

    }


    @Override
    @Transactional
    public SocieteDac updateForShortListing(Integer dacId, MultipartFile file, String userConnecteId) throws IOException {
        log.debug("Modification de SocieteDac avec pour la short listing avec DacId: {}", dacId);

        // verifier l'existence des field de request
        if(dacId == null ) {
            throw new IllegalArgumentException("Le champs dacId et obligatoire");
        }

        // get the last version of published DAC from DB with given Id

        Optional<Dac> dacWithLastVer = dacRepository.findPublishedDacForUser(dacId, userConnecteId);
        // verification de l'existace du SocieteDac
        if(dacWithLastVer.isEmpty()) {
            throw new DacNotFound(dacId);
        }

        //get socite by user connected Id
        UserSociete userSocite = userSocieteService.findByUserId(userConnecteId);

        if(userSocite==null){
            throw new UserSocieteNotFound(userConnecteId);
        }

        Dac existingDac = dacWithLastVer.get();

        Optional<SocieteDac> existingSocieteDac = societeDacRepository.findById(existingDac.getDacId(), existingDac.getDacVer(), userSocite.getSocieteId());

        if(existingSocieteDac.isEmpty()) {
            throw new SocieteDacNotFoundException(existingDac.getDacId(), userSocite.getSocieteId());
        }

        // update SocieteDac
        SocieteDac societeDac = existingSocieteDac.get();

        Integer IdJustifPaiement = null;
        try {
            if (societeDac.getMethodePaiement() == MethodePaiement.NON_ATTRIBUE ) {
                societeDac.setMethodePaiement(MethodePaiement.MANUEL);
                IdJustifPaiement =  documentCommuneService.processAndSaveDocumentsForPaymentDac(file, existingDac.getDacId(), existingDac.getDacVer());
            }
        }
        catch (IOException e) {
            throw new IOException("An error occurred while processing the file: " + e.getMessage());
        }

        societeDac.setJustifPaiementId(IdJustifPaiement);
        societeDac.setStatusPaiement(null);
        return societeDacRepository.save(societeDac);

    }

    @Override
    @Transactional
    public void delete(Integer dacId, Integer dacVersion, Integer societeId) {
        log.debug("Suppression de SocieteDac avec DacId: {}, DacVersion: {}, Societeid: {}", dacId, dacVersion, societeId);

        // verification de l'existace du SocieteDac
        Optional<SocieteDac> existingSocieteDac = societeDacRepository.findById(dacId, dacVersion, societeId);
        if(!existingSocieteDac.isPresent()) {
            throw new SocieteDacNotFoundException(dacId, dacVersion, societeId);
        }
        Calendar now = Calendar.getInstance();

        societeDacRepository.softDeleteCompositeKey(dacId, dacVersion, societeId,now);
    }

    @Override
    @Transactional
    public SocieteDac approuverPaiement(Integer dacId, Integer dacVersion, Integer societeId) {
        log.debug("approbation de paiement de SocieteDac avec DacId: {}, DacVersion: {}, Societeid: {}", dacId, dacVersion, societeId);
        // verifier l'existence des field de request
        if(dacId == null || dacVersion == null || societeId == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, societeId sont obligatoires");
        }

        // verification de l'existace du SocieteDac
        Optional<SocieteDac> existingSocieteDac = societeDacRepository.findById(dacId, dacVersion, societeId);
        if(!existingSocieteDac.isPresent()) {
            throw new SocieteDacNotFoundException(dacId, dacVersion, societeId);
        }

        // update payment status of SocieteDac with approuve (true)
        SocieteDac societeDac = existingSocieteDac.get();

        societeDac.setStatusPaiement(true);
        return societeDacRepository.save(societeDac);

    }

    @Override
    @Transactional
    public SocieteDac rejectePaiement(Integer dacId, Integer dacVersion, Integer societeId) {
        log.debug("rejet du paiement de SocieteDac avec DacId: {}, DacVersion: {}, Societeid: {}", dacId, dacVersion, societeId);
        // verifier l'existence des field de request
        if(dacId == null || dacVersion == null || societeId == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, societeId sont obligatoires");
        }

        // verification de l'existace du SocieteDac
        Optional<SocieteDac> existingSocieteDac = societeDacRepository.findById(dacId, dacVersion, societeId);
        if(!existingSocieteDac.isPresent()) {
            throw new SocieteDacNotFoundException(dacId, dacVersion, societeId);
        }

        // update payment status of SocieteDac with reject (false)
        SocieteDac societeDac = existingSocieteDac.get();

        societeDac.setStatusPaiement(false);
        return societeDacRepository.save(societeDac);

    }


    @Override
    public Page<SocieteDac> findAllForAdmin( Pageable pageable) {

        Page<SocieteDac> societeDac = societeDacRepository.findAllForAdmin( pageable);

        return societeDac;
    }


    @Override
    public byte[] downloadPaymentJustif(String alfrescoId) throws IOException {

        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(""));
        if (dummyContent.getFile()!=null) {
            return dummyContent.getFile();
        }
        else {
            throw new IOException("Error while downloading document.");
        }

    }

    public Optional <DocumentResponseDTO> getDocumentInformation(String alfrescoId) {
        return Optional.ofNullable(alfrescoService.getDocumentById(alfrescoId));
    }
}
