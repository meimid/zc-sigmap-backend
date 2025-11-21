package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacId;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.request.DacDoccsAndDacRequest;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocDetailsDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.DacDocsDetailsRepository;
import com.sigmap.common.dac_docs_details.DacDocsDetailsService;
import com.sigmap.common.dac_docs_details.DacDocsDetailsSpecification;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixId;
import com.sigmap.common.dac_prix.DacPrixRepository;
import com.sigmap.common.dac_prix.DacPrixService;
import com.sigmap.common.dac_prix.exceptions.DacPrixAlreadyExist;
import com.sigmap.common.dac_prix.request.DacPrixRequest;
import com.sigmap.common.dac_prix.request.DacPrixRequestForDac;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsRepository;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsRepository;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.config.Constants;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieRepository;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.ref_params.RefParam;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DacDocsServiceImpl implements DacDocsService{

    @Autowired
    private DacDocsRepository dacDocsRepository;
    @Autowired
    private DynaFieldsRepository dynaFieldsRepository;
    @Autowired
    private DacDocsMapper dacDocsMapper;
    @Autowired
    DacRepository dacRepository;
    @Autowired
    DynaDocsRepository dynaDocsRepository;
    @Autowired
    DacDocsDetailsService dacDocsDetailsService;
    @Autowired
    DacDocsDetailsRepository dacDocsDetailsRepository;
    @Autowired
    private DocsRepository docsRepository;
    @Autowired
    private DocumentCommuneService documentCommuneService;
    @Autowired
    private DynaFieldsDetailsRepository dynaFieldsDetailsRepository;
    @Autowired
    private DacPrixRepository dacPrixRepository;
    @Autowired
    private DacPrixService dacPrixService;
    @Autowired
    private MonnaieRepository monnaieRepository;
    @Override
    @Transactional
    public DacDocs uploadStaticDoc(Integer dacId, Integer dynaDocsId, MultipartFile file) {
        // Resolve latest DynaDocs and verify static
        Optional<DynaDocs> dynaDocsOpt = dynaDocsRepository.findLastDynaDocsVersionById(dynaDocsId);
        if (dynaDocsOpt.isEmpty()) {
            throw new RuntimeException("DynaDocs not found with id: " + dynaDocsId);
        }
        DynaDocs dynaDocs = dynaDocsOpt.get();
        if (!Boolean.TRUE.equals(dynaDocs.getIsStatic())) {
            throw new RuntimeException("DynaDocs is not static: " + dynaDocsId);
        }
        // Resolve latest DAC
        Dac dac = dacRepository.findDacWithLatestVersionByDacId(dacId);
        if (dac == null) {
            throw new RuntimeException("Dac not found with id: " + dacId);
        }

        // Determine target DynaField for this static document
        List<DynaFields> fields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(dynaDocs.getId(), dynaDocs.getVersion());
        DynaFields field = null;
        if (fields != null && !fields.isEmpty()) {
            // 1) Prefer __DEFAULT__ placeholder
            field = fields.stream().filter(f -> "__DEFAULT__".equals(f.getName())).findFirst().orElse(null);
            // 2) Else prefer SYSTEM type
            if (field == null) {
                field = fields.stream().filter(f -> f.getType() == DynaFieldType.SYSTEM).findFirst().orElse(null);
            }
            // 3) Else take the lowest order
            if (field == null) {
                field = fields.stream().min(Comparator.comparing(DynaFields::getDynaFieldsOrder, Comparator.nullsLast(Integer::compareTo))).orElse(null);
            }
        }
        // 4) If still none, create a placeholder __DEFAULT__ like seeding logic
        if (field == null) {
            Integer maxId = dynaFieldsRepository.findMaxFieldIdForDoc(dynaDocs.getId(), dynaDocs.getVersion());
            DynaFields placeholder = new DynaFields();
            placeholder.setDynaFieldsId(maxId + 1);
            placeholder.setDynaDocsId(dynaDocs.getId());
            placeholder.setDynaDocsVer(dynaDocs.getVersion());
            placeholder.setName("__DEFAULT__");
            placeholder.setRef("__DEFAULT__");
            placeholder.setLabel("__DEFAULT__");
            placeholder.setType(DynaFieldType.SYSTEM);
            placeholder.setSource(null);
            placeholder.setSourceField(null);
            placeholder.setDynaFieldsOrder(Integer.MAX_VALUE);
            placeholder.setStatus(com.sigmap.common.entity.ActiveStatus.ACTIF);
            field = dynaFieldsRepository.save(placeholder);
        }

        // Upsert row for (dacId, latest dacVer, dynaDocsId, latest dynaDocsVer, chosen dynaFieldsId)
        Optional<DacDocs> existingOpt = dacDocsRepository.findDacDocsBYPKs(
                dac.getDacId(),
                dac.getDacVer(),
                dynaDocs.getId(),
                dynaDocs.getVersion(),
                field.getDynaFieldsId());
        DacDocs row = existingOpt.orElseGet(DacDocs::new);
        if (row.getId() == null) {
            DacDocsId id = new DacDocsId();
            id.setDacId(dac.getDacId());
            id.setDacVer(dac.getDacVer());
            id.setDynaDocsId(dynaDocs.getId());
            id.setDynaDocsVer(dynaDocs.getVersion());
            id.setDynaFieldsId(field.getDynaFieldsId());
            row.setId(id);
            row.setDac(dac);
            row.setDynaField(field);
            row.setSourceId(row.getSourceId() == null ? "" : row.getSourceId());
        }
        // Persist to ensure ID fields present when saving/unlinking
        row = dacDocsRepository.save(row);

        // If no file provided: unlink existing doc (set docId = null) and return
        if (file == null || file.isEmpty()) {
            if (row.getDocs() != null) {
                row.setDocs(null);
                row = dacDocsRepository.save(row);
            }
            return row;
        }

        try {
            Integer newDocId = documentCommuneService.processAndSaveDocumentsForDacDocs(file, row, Constants.PATH_ENTREPOT_GED_DAC_DOCS);
            // Link doc
            Optional<Docs> docOpt = docsRepository.findById(newDocId);
            if (docOpt.isEmpty()) {
                throw new RuntimeException("Saved document not found with id: " + newDocId);
            }
            row.setDocs(docOpt.get());
            row = dacDocsRepository.save(row);
            return row;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DacDocs createDacDocs (DacDocsRequest request) {

        // 1- get the DynaDocs with last version
        Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaDocsId());

        // 2- test if the existingDynaDocs is empty
        if (existingDynaDocs.isEmpty()) {
            throw new RuntimeException("DynaDocs not found with id: " + request.getDynaDocsId());
        }

        // 3- get the DynaFields with last dynaDocs version for the given id
        List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
        if (existingDynaFields.isEmpty()) {
            throw new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId());
        }
        log.debug("existingDynaFields............. : {}", existingDynaFields);

        // 4-get the last dac (id and version) with the given id
        Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
        log.debug("existingDac............. : {}", existingDac);

        // 5- verifier l'existence du dac
        if (existingDac == null) {
            throw new RuntimeException("Dac not found with id: " + request.getDacId());
        }
        //6- create DacDocs and save
        DacDocs entity = dacDocsMapper.toEntity(request);
        log.debug("entity............. : {}", entity);

        // Set scalar fields
        entity.setSourceId(request.getSourceId());
        entity.setFieldValue(request.getFieldValue());

        // Set composite key
        DacDocsId id = new DacDocsId();
        id.setDacId(existingDac.getDacId());
        id.setDacVer(existingDac.getDacVer());
        id.setDynaDocsId(existingDynaDocs.get().getId());
        id.setDynaDocsVer(existingDynaDocs.get().getVersion());

        // Find the matching DynaFields by dynaFieldsId
        DynaFields matchedDynaField = existingDynaFields.stream()
                .filter(field -> field.getDynaFieldsId().equals(request.getDynaFieldsId()))
                .findFirst()
                .map(field -> {
                    id.setDynaFieldsId(field.getDynaFieldsId());
                    return field;
                })
                .orElse(null);
        if (matchedDynaField == null) {
            throw new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId());
        }
        entity.setId(id);

        // Set relationships
        entity.setDac(existingDac);
        entity.setDynaField(matchedDynaField);

        entity = dacDocsRepository.save(entity);

        // test if there are any details and if the field type is list or table
        log.debug("Dac Docs created with ID: {}", entity.getId());
        if (matchedDynaField.getType() == DynaFieldType.LIST || matchedDynaField.getType() == DynaFieldType.TABLE)
        {

            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                List<DacDocsDetailsRequest> detailsReq =
                        DacDocsMapper.INSTANCE.mapDetails(request.getDetails());

                // Normalize dynaFieldsDetailsId for LIST fields: ensure it references the single column id
                if (matchedDynaField.getType() == DynaFieldType.LIST) {
                    List<DynaFieldsDetails> cols = dynaFieldsDetailsRepository.findAllByDocAndField(
                            entity.getId().getDynaDocsId(), entity.getId().getDynaDocsVer(), matchedDynaField.getDynaFieldsId());
                    Integer singleColId = cols != null && !cols.isEmpty() ? cols.get(0).getDynaFieldsDetailsId() : null;
                    if (singleColId != null) {
                        for (DacDocsDetailsRequest dr : detailsReq) {
                            if (dr.getDynaFieldsDetailsId() == null || dr.getDynaFieldsDetailsId().equals(matchedDynaField.getDynaFieldsId())) {
                                dr.setDynaFieldsDetailsId(singleColId);
                            }
                        }
                    }
                }

                List<DacDocsDetails> detailsList =
                        dacDocsDetailsService.createAll(entity, detailsReq);

                Set<DacDocsDetails> details = DacDocsMapper.INSTANCE.detailsToSet(detailsList);

                entity.setDacDocsDetails(details);
            }

        }

            return entity;
    }

    public static DacPrix dacPrxMapperToEntity(BigDecimal prix, Dac dac, Monnaie monnaie) {
        DacPrix dacPrix = new DacPrix();
        DacPrixId id = new DacPrixId(dac, monnaie);
        dacPrix.setId(id);
        dacPrix.setPrix(prix);
        return dacPrix;
    }

    @Transactional
    public void savePrixDac(DacPrixRequestForDac request , Integer dacId, Integer dacVer) throws MonnaieNotFound {
        log.debug("Saving/Upserting DacPrix: {}", request);

        // Validate request fields
        if (dacId == null || dacVer == null ||
                request.getMonnaieId() == null || request.getPrix() == null) {
            throw new IllegalArgumentException("dacId, dacVer, monnaieId and prix are required");
        }

        // Get Dac
        DacId dacIdPK = new DacId(dacId, dacVer);
        Dac dac = dacRepository.findById(dacIdPK)
                .orElseThrow(() -> new DacNotFound("", dacId, dacVer));

        // Get Monnaie
        Monnaie monnaie = monnaieRepository.findById(request.getMonnaieId())
                .orElseThrow(() -> new MonnaieNotFound("", List.of(request.getMonnaieId())));

        // Find existing OR create new
        Optional<DacPrix> existingDacPrix = dacPrixRepository.findById(dacId, dacVer, request.getMonnaieId());

        log.info("existingDacPrix............. : {}", existingDacPrix);

        DacPrix dacPrix;
        if (existingDacPrix.isPresent()) {
            // UPDATE EXISTING - Just change the prix!
            dacPrix = existingDacPrix.get();
            dacPrix.setPrix(request.getPrix());
            log.debug("Updated existing DacPrix for {}/{}/{}", dacId, dacVer, request.getMonnaieId());
        } else {
            // CREATE NEW using Mapper
            dacPrix = dacPrxMapperToEntity(request.getPrix(), dac, monnaie);
            log.debug("Created new DacPrix for {}/{}/{}", dacId, dacVer, request.getMonnaieId());
        }

        // Save (CREATE or UPDATE)
        dacPrix = dacPrixRepository.save(dacPrix);
        log.info("Saved DacPrix: {}", dacPrix);
    }


    @Override
    @Transactional
    public List<DacDocs> createDacDocsList(DacDoccsAndDacRequest dacDoccsAndDacRequest) throws MonnaieNotFound {

        List<DacDocs> result = new ArrayList<>();

        // Validate input
        if (dacDoccsAndDacRequest.getRequests() == null || dacDoccsAndDacRequest.getRequests().isEmpty()) {
            throw new RuntimeException("No DacDocs requests provided");
        }
        if (dacDoccsAndDacRequest.getDacEditRequest() == null) {
            throw new RuntimeException("Dac request is required");
        }

        // 1-  get the Dac from the DacRequest
        Dac existDac = dacRepository.findDacByDacIdAndDacVer(dacDoccsAndDacRequest.getDacEditRequest().getDacId(), dacDoccsAndDacRequest.getDacEditRequest().getDacVer())
                .orElseThrow(() -> new DacNotFound("",dacDoccsAndDacRequest.getDacEditRequest().getDacId(), dacDoccsAndDacRequest.getDacEditRequest().getDacVer()));

        //update dac values
        existDac.setDateLimiteSoumission(dacDoccsAndDacRequest.getDacEditRequest().getDateLimiteSoumission());
        existDac.setDateLimiteRecptionDemande(dacDoccsAndDacRequest.getDacEditRequest().getDateLimiteRecptionDemande());
        existDac.setPayant(dacDoccsAndDacRequest.getDacEditRequest().isPayant());

        //update dac and save
        dacRepository.save(existDac);

        // test if payant is true then
        // SAVE PRIX DAC LIST
        if (dacDoccsAndDacRequest.getDacEditRequest().isPayant()) {

            List<DacPrixRequestForDac> prixRequests = dacDoccsAndDacRequest.getDacEditRequest().getDacPrixRequests();

            if (prixRequests == null || prixRequests.isEmpty()) {
                throw new IllegalArgumentException("DacPrixRequests are required when payant = true");
            }
            // 1-1Fetch existing DacPrix in DB
            List<DacPrix> existingPrixList = dacPrixRepository.findByDac(existDac.getDacId(), existDac.getDacVer());

            //1-2 Determine which Monnaies are requested
            Set<Integer> requestedMonnaieIds = prixRequests.stream()
                    .map(DacPrixRequestForDac::getMonnaieId)
                    .collect(Collectors.toSet());

            // 1-3 Delete DacPrix not present in request but present in DB
            List<DacPrix> toDelete = existingPrixList.stream()
                    .filter(p -> !requestedMonnaieIds.contains(p.getId().getMonnaie().getId())).toList();

            if (!toDelete.isEmpty() && existDac.getStatus() != ProcessStatus.BROUILLON) {
                dacPrixRepository.softDelete(toDelete);
                log.info("Deleted {} obsolete DacPrix records for DAC {}/{}", toDelete.size(), existDac.getDacId(), existDac.getDacVer());
            }

            if (!toDelete.isEmpty() && existDac.getStatus() == ProcessStatus.BROUILLON) {
                dacPrixService.defenitiveDelete(toDelete);
                log.info("Deleted {} obsolete DacPrix records for DAC {}/{}", toDelete.size(), existDac.getDacId(), existDac.getDacVer());
            }


            for (DacPrixRequestForDac prixRequest : prixRequests) {
                savePrixDac(prixRequest,existDac.getDacId(),existDac.getDacVer());
                log.info("saved {} DacPrix for DAC {}/{}",prixRequests.size(),existDac.getDacId(),existDac.getDacVer());
            }
        }

        List<DacDocsRequest> requests = dacDoccsAndDacRequest.getRequests();

        for (DacDocsRequest request : requests) {

            // 1- Get the DynaDocs with the last version
            Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaDocsId());
            if (existingDynaDocs.isEmpty()) {
                throw new RuntimeException("DynaDocs not found with id: " + request.getDynaDocsId());
            }
            log.debug("existingDynaDocs: {}", existingDynaDocs.get());

            // 2- Get the DynaFields with the last DynaDocs version for the given id
            List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndVer(
                    existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
            if (existingDynaFields.isEmpty()) {
                throw new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId());
            }
            log.debug("existingDynaFields: {}", existingDynaFields);

            // 3- Get the last Dac (id and version) with the given id
            Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
            if (existingDac == null) {
                throw new RuntimeException("Dac not found with id: " + request.getDacId());
            }
            log.debug("existingDac: {}", existingDac);

            // 4- Create or update DacDocs
            DacDocs entity = dacDocsMapper.toEntity(request);
            log.debug("entity: {}", entity);

            // Set scalar fields
            entity.setSourceId(request.getSourceId());
            entity.setFieldValue(request.getFieldValue());

            // Set composite key
            DacDocsId id = new DacDocsId();
            id.setDacId(existingDac.getDacId());
            id.setDacVer(existingDac.getDacVer());
            id.setDynaDocsId(existingDynaDocs.get().getId());
            id.setDynaDocsVer(existingDynaDocs.get().getVersion());

            // Find the matching DynaFields by dynaFieldsId
            DynaFields matchedDynaField = existingDynaFields.stream()
                    .filter(field -> field.getDynaFieldsId().equals(request.getDynaFieldsId()))
                    .findFirst()
                    .map(field -> {
                        id.setDynaFieldsId(field.getDynaFieldsId());
                        return field;
                    })
                    .orElseThrow(() -> new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId()));
            entity.setId(id);

            // Set relationships
            entity.setDac(existingDac);
            entity.setDynaField(matchedDynaField);

            // Save the DacDocs entity
            entity = dacDocsRepository.save(entity);
            log.debug("DacDocs created with ID: {}", entity.getId());

            // 5- Handle LIST/TABLE fields
            if (matchedDynaField.getType() == DynaFieldType.LIST || matchedDynaField.getType() == DynaFieldType.TABLE) {
                if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                    List<DacDocsDetailsRequest> detailsReq = DacDocsMapper.INSTANCE.mapDetails(request.getDetails());

                    // Normalize dynaFieldsDetailsId for LIST fields: ensure it references the single column id
                    if (matchedDynaField.getType() == DynaFieldType.LIST) {
                        List<DynaFieldsDetails> cols = dynaFieldsDetailsRepository.findAllByDocAndField(
                                entity.getId().getDynaDocsId(), entity.getId().getDynaDocsVer(), matchedDynaField.getDynaFieldsId());
                        Integer singleColId = cols != null && !cols.isEmpty() ? cols.get(0).getDynaFieldsDetailsId() : null;
                        if (singleColId != null) {
                            for (DacDocsDetailsRequest dr : detailsReq) {
                                if (dr.getDynaFieldsDetailsId() == null || dr.getDynaFieldsDetailsId().equals(matchedDynaField.getDynaFieldsId())) {
                                    dr.setDynaFieldsDetailsId(singleColId);
                                }
                            }
                        }
                    }

                    // Delete existing details only if new details are provided
                    dacDocsDetailsRepository.deleteAll(
                            dacDocsDetailsRepository.findAll(Specification.where(DacDocsDetailsSpecification.byDacId(existingDac.getDacId()))
                                    .and(DacDocsDetailsSpecification.byDacVer(existingDac.getDacVer()))
                                    .and(DacDocsDetailsSpecification.byDynaDocsId(existingDynaDocs.get().getId()))
                                    .and(DacDocsDetailsSpecification.byDynaDocsVer(existingDynaDocs.get().getVersion()))
                                    .and(DacDocsDetailsSpecification.byDynaFieldsId(request.getDynaFieldsId())))
                    );

                    List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(entity, detailsReq);
                    entity.setDacDocsDetails(new HashSet<>(detailsList));
                    entity = dacDocsRepository.save(entity);
                }
            }

            result.add(entity);
        }

        return result;
    }

    @Override
    @Transactional
    public DacDocs updateDacDocs( DacDocsRequest request) {

        // Resolve DAC latest and decide strategy by status
        Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
        if (existingDac == null) {
            throw new RuntimeException("Dac not found with id: " + request.getDacId() );
        }
        if (existingDac.getStatus() == com.sigmap.common.entity.ProcessStatus.BROUILLON) {
            return updateDacDocsInPlace(request);
        }
        return updateDacDocsWithVersioning(request);
    }

    @Override
    @Transactional
    public DacDocs updateDacDocsInPlace(DacDocsRequest request) {
        // 1- resolve target DynaDocs and DynaField
        Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaDocsId());
        if (existingDynaDocs.isEmpty()) {
            throw new RuntimeException("DynaDocs not found with id: " + request.getDynaDocsId());
        }
        List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
        boolean fieldExists = existingDynaFields.stream().anyMatch(f -> f.getDynaFieldsId().equals(request.getDynaFieldsId()));
        if (!fieldExists) {
            throw new RuntimeException("DynaField not found with id: " + request.getDynaFieldsId());
        }

        // 2- load latest DAC
        Dac dac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
        if (dac == null) throw new RuntimeException("Dac not found with id: " + request.getDacId());

        // 3- upsert in place for this dac version
        Optional<DacDocs> existing = dacDocsRepository.findDacDocsBYPKs(
                dac.getDacId(), dac.getDacVer(), existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(), request.getDynaFieldsId());
        if (existing.isPresent()) {
            DacDocs row = existing.get();
            row.setFieldValue(request.getFieldValue());
            row.setSourceId(request.getSourceId());
            DacDocs savedDacDocs = dacDocsRepository.save(row);
            
            // Handle DacDocsDetails for the updated/created DacDocs
            DynaFields targetDynaField = existingDynaFields.stream().filter(f -> f.getDynaFieldsId().equals(request.getDynaFieldsId())).findFirst().orElse(null);
            if (targetDynaField != null && (targetDynaField.getType() == DynaFieldType.LIST || targetDynaField.getType() == DynaFieldType.TABLE)) {
                // Clear existing details for this DacDocs before re-creating/updating
                dacDocsDetailsRepository.deleteAll(
                        dacDocsDetailsRepository.findAll(Specification.where(DacDocsDetailsSpecification.byDacId(dac.getDacId()))
                                .and(DacDocsDetailsSpecification.byDacVer(dac.getDacVer()))
                                .and(DacDocsDetailsSpecification.byDynaDocsId(existingDynaDocs.get().getId()))
                                .and(DacDocsDetailsSpecification.byDynaDocsVer(existingDynaDocs.get().getVersion()))
                                .and(DacDocsDetailsSpecification.byDynaFieldsId(request.getDynaFieldsId()))
                        )
                );
                if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                    List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(savedDacDocs, DacDocsMapper.INSTANCE.mapDetails(request.getDetails()));
                    savedDacDocs.setDacDocsDetails(new HashSet<>(detailsList));
                    // Save the entity again to ensure the details association is persisted
                    savedDacDocs = dacDocsRepository.save(savedDacDocs);
                }
            }
            return savedDacDocs;
        }
        // create row if missing in-place
        DacDocs row = new DacDocs();
        DacDocsId id = new DacDocsId();
        id.setDacId(dac.getDacId());
        id.setDacVer(dac.getDacVer());
        id.setDynaDocsId(existingDynaDocs.get().getId());
        id.setDynaDocsVer(existingDynaDocs.get().getVersion());
        id.setDynaFieldsId(request.getDynaFieldsId());
        row.setId(id);
        row.setDac(dac);
        DynaFields targetField = existingDynaFields.stream().filter(f -> f.getDynaFieldsId().equals(request.getDynaFieldsId())).findFirst().orElse(null);
        row.setDynaField(targetField);
        row.setSourceId(request.getSourceId());
        row.setFieldValue(request.getFieldValue());
        DacDocs savedDacDocs = dacDocsRepository.save(row);

        // Handle DacDocsDetails for the newly created DacDocs
        if (targetField != null && (targetField.getType() == DynaFieldType.LIST || targetField.getType() == DynaFieldType.TABLE)) {
            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                List<DacDocsDetailsRequest> mapped = DacDocsMapper.INSTANCE.mapDetails(request.getDetails());
                if (targetField.getType() == DynaFieldType.LIST) {
                    List<DynaFieldsDetails> cols = dynaFieldsDetailsRepository.findAllByDocAndField(
                            savedDacDocs.getId().getDynaDocsId(), savedDacDocs.getId().getDynaDocsVer(), targetField.getDynaFieldsId());
                    Integer singleColId = cols != null && !cols.isEmpty() ? cols.get(0).getDynaFieldsDetailsId() : null;
                    if (singleColId != null) {
                        for (DacDocsDetailsRequest dr : mapped) {
                            if (dr.getDynaFieldsDetailsId() == null || dr.getDynaFieldsDetailsId().equals(targetField.getDynaFieldsId())) {
                                dr.setDynaFieldsDetailsId(singleColId);
                            }
                        }
                    }
                }
                List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(savedDacDocs, mapped);
                savedDacDocs.setDacDocsDetails(new HashSet<>(detailsList));
                    // Save the entity again to ensure the details association is persisted
                    savedDacDocs = dacDocsRepository.save(savedDacDocs);
            }
        }
        return savedDacDocs;
    }

    @Override
    @Transactional
    public DacDocs updateDacDocsWithVersioning(DacDocsRequest request) {
        // 1- resolve target DynaDocs and DynaField
        Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaDocsId());
        if (existingDynaDocs.isEmpty()) {
            throw new RuntimeException("DynaDocs not found with id: " + request.getDynaDocsId());
        }
        List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
        boolean fieldExists = existingDynaFields.stream().anyMatch(f -> f.getDynaFieldsId().equals(request.getDynaFieldsId()));
        if (!fieldExists) {
            throw new RuntimeException("DynaField not found with id: " + request.getDynaFieldsId());
        }

        // 2- load latest DAC and create new DAC version
        Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
        if (existingDac == null) {
            throw new RuntimeException("Dac not found with id: " + request.getDacId() );
        }
        int nextVer = existingDac.getDacVer() + 1;
        Dac newDac = new Dac();
        newDac.setDacId(existingDac.getDacId());
        newDac.setDacVer(nextVer);
        newDac.setPpm(existingDac.getPpm());
        newDac.setRealisation(existingDac.getRealisation());
        newDac.setTypeMarche(existingDac.getTypeMarche());
        newDac.setStatus(existingDac.getStatus());
        newDac.setReference(existingDac.getReference());
        newDac.setDateLimiteSoumission(existingDac.getDateLimiteSoumission());
        newDac.setDateLimiteRecptionDemande(existingDac.getDateLimiteRecptionDemande());
        newDac.setPayant(existingDac.isPayant());
        newDac.setShortListing(existingDac.isShortListing());
        newDac.setParentDacId(existingDac.getParentDacId());
        newDac.setDynaDocsModel(existingDac.getDynaDocsModel());
        newDac = dacRepository.save(newDac);

        // 3- Clone previous dac_docs snapshot to the new version
        List<DacDocs> priorDocs = dacDocsRepository.findByDacIdAndDacVer(existingDac.getDacId(), existingDac.getDacVer());
        for (DacDocs prior : priorDocs) {
            DacDocs clone = new DacDocs();
            DacDocsId id = new DacDocsId();
            id.setDacId(newDac.getDacId());
            id.setDacVer(newDac.getDacVer());
            id.setDynaDocsId(prior.getId().getDynaDocsId());
            id.setDynaDocsVer(prior.getId().getDynaDocsVer());
            id.setDynaFieldsId(prior.getId().getDynaFieldsId());
            clone.setId(id);
            clone.setDac(newDac);
            clone.setDynaField(prior.getDynaField());
            clone.setSourceId(prior.getSourceId());
            clone.setFieldValue(prior.getFieldValue());
            DacDocs savedDacDocs = dacDocsRepository.save(clone);
        }

        // 4- Apply the change on the targeted field in the new DAC version (create if missing)
        Optional<DacDocs> targetInNew = dacDocsRepository.findDacDocsBYPKs(
                newDac.getDacId(), newDac.getDacVer(), existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(), request.getDynaFieldsId());
        DacDocs updated;
        if (targetInNew.isPresent()) {
            DacDocs row = targetInNew.get();
            row.setFieldValue(request.getFieldValue());
            row.setSourceId(request.getSourceId());
            updated = dacDocsRepository.save(row);
        } else {
            DacDocs row = new DacDocs();
            DacDocsId id = new DacDocsId();
            id.setDacId(newDac.getDacId());
            id.setDacVer(newDac.getDacVer());
            id.setDynaDocsId(existingDynaDocs.get().getId());
            id.setDynaDocsVer(existingDynaDocs.get().getVersion());
            id.setDynaFieldsId(request.getDynaFieldsId());
            row.setId(id);
            row.setDac(newDac);
            DynaFields targetField = existingDynaFields.stream().filter(f -> f.getDynaFieldsId().equals(request.getDynaFieldsId())).findFirst().orElse(null);
            row.setDynaField(targetField);
            row.setSourceId(request.getSourceId());
            row.setFieldValue(request.getFieldValue());
            updated = dacDocsRepository.save(row);


        // Handle DacDocsDetails for the newly created DacDocs and save it in case of table or list
        if (targetField != null && (targetField.getType() == DynaFieldType.LIST || targetField.getType() == DynaFieldType.TABLE)) {
            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(updated, DacDocsMapper.INSTANCE.mapDetails(request.getDetails()));
                updated.setDacDocsDetails(new HashSet<>(detailsList));
                // Save the entity again to ensure the details association is persisted
                updated = dacDocsRepository.save(updated);
            }
        }
        }
        return updated;
    }

    @Override
    public List<DacDocs> findOneById(Integer dacId, Integer dacVer) {
        return dacDocsRepository.findDacByID(dacId, dacVer);
    }
    @Override
    public Optional<DacDocs> findById(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
        return dacDocsRepository.findDacDocsBYPKs(dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
    }

    @Override
    public List<DacDocResponseDTO> findByDacIdAndVer(Integer dacId, Integer dacVer) {
        List<DacDocs> dacDocsList = dacDocsRepository.findAll(
                Specification.where(DacDocsSpecifications.byDacId(dacId))
                        .and(DacDocsSpecifications.byDacVer(dacVer))
        );

        // Fetch all details for these dacDocs
        List<DacDocsDetails> allDetails = dacDocsDetailsRepository.findAll(
                Specification.where(DacDocsDetailsSpecification.byDacId(dacId))
                        .and(DacDocsDetailsSpecification.byDacVer(dacVer))
        );

        // Group details by dac_docs identity (dynaDocsId, dynaDocsVer, dynaFieldsId)
        Map<String, List<DacDocsDetails>> detailsByDoc = allDetails.stream().collect(
                Collectors.groupingBy(d ->
                         d.getDac().getId().getDynaDocsId() + ":" +
                         d.getDac().getId().getDynaDocsVer() + ":" +
                         d.getDac().getId().getDynaFieldsId()
                )
        );

        List<DacDocResponseDTO> responseList = new ArrayList<>();
        for (DacDocs dacDoc : dacDocsList) {
            DacDocResponseDTO response = new DacDocResponseDTO();
            response.setDacId(dacDoc.getId().getDacId());
            response.setDacVer(dacDoc.getId().getDacVer());
            response.setDynaDocId(dacDoc.getId().getDynaDocsId());
            response.setDynaDocVersion(dacDoc.getId().getDynaDocsVer());
            response.setDynaFieldId(dacDoc.getId().getDynaFieldsId());
            response.setSourceId(dacDoc.getSourceId());
            response.setFieldValue(dacDoc.getFieldValue());

            // Ensure SYSTEM fields return their computed value even if not persisted after setFieldValue
            try {
                if (dacDoc.getDynaField() != null && dacDoc.getDynaField().getType() == DynaFieldType.SYSTEM) {
                    String computed = resolveSystemFieldValue(dacDoc.getDynaField(), dacDoc.getDac());
                    if (computed != null && (response.getFieldValue() == null || response.getFieldValue().isBlank())) {
                        response.setFieldValue(computed);
                    }
                }
            } catch (Exception ignore) {
                log.error("Error resolving system field value {}", ignore.getMessage());
            }

            log.debug("response -------------- {}", response);

            String key = dacDoc.getId().getDynaDocsId() + ":" + dacDoc.getId().getDynaDocsVer() + ":" + dacDoc.getId().getDynaFieldsId();
            List<DacDocsDetails> details = detailsByDoc.getOrDefault(key, Collections.emptyList());
            List<DacDocDetailsDTO> detailsDTOs = details.stream().map(detail -> {
                DacDocDetailsDTO dto = new DacDocDetailsDTO();
                dto.setDynaFieldsDetailsId(detail.getDynaFieldsDetailsId());
                dto.setRowId(detail.getRowId());
                DynaFieldsDetails dynaFieldDetails=dynaFieldsDetailsRepository.findByDynaDocsIdAndDynaDocsVerAndDynaFieldsDetailsId(detail.getDynaDocsId(),detail.getDynaDocsVer(),detail.getDynaFieldsDetailsId());
                dto.setColumnPos(dynaFieldDetails.getColumnPos());
                dto.setSourceId(detail.getSourceId());
                dto.setFieldValue(detail.getFieldValue());
                // These are redundant at the detail level, but can be set if needed
                dto.setDynaDocId(dacDoc.getId().getDynaDocsId());
                dto.setDynaDocVersion(dacDoc.getId().getDynaDocsVer());
                dto.setDynaFieldId(dacDoc.getId().getDynaFieldsId());
                return dto;
            }).collect(Collectors.toList());
            response.setDacDocDetails(detailsDTOs);
            responseList.add(response);
        }
        return responseList;
    }

    // Helpers to compute SYSTEM field values like logic in DacServiceImpl
    private String resolveSystemFieldValue(DynaFields field, Dac dac) {
        String source = field.getSource();
        String sourceField = field.getSourceField();
        if (source == null || sourceField == null || dac == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        switch (source) {
            case "ppm": {
                if (dac.getPpm() == null) return null;
                switch (sourceField) {
                    case "nom": return dac.getPpm().getNom();
                    case "created_at":
                        if (dac.getPpm().getUpdatedAt() != null) {
                            return sdf.format(new Date(dac.getPpm().getCreatedAt().getTimeInMillis()));
                        }
                        return null;
                    default: return null;
                }
            }
            case "ac": {
                if (dac.getPpm() == null || dac.getPpm().getAc() == null) return null;
                switch (sourceField) {
                    case "nom": return dac.getPpm().getAc().getNom();
                    case "email": return dac.getPpm().getAc().getEmail();
                    case "adresse":
                        if (dac.getPpm().getAc().getAdresse() != null) {
                            return (dac.getPpm().getAc().getAdresse().getAdresse() + " " +
                                    dac.getPpm().getAc().getAdresse().getCompAdresse() + " " +
                                    dac.getPpm().getAc().getAdresse().getCodePostal() + " " +
                                    dac.getPpm().getAc().getAdresse().getVille());
                        }
                        return null;
                    default: return null;
                }
            }
            case "realisation": {
                if (dac.getRealisation() == null) return null;
                switch (sourceField) {
                    case "realisationEnvisagees", "realisation_envisagees":
                        return dac.getRealisation().getRealisationEnvisagees();
                    case "beneficiaire":
                        return dac.getRealisation().getBeneficiaire();
                    case "sourceFinancement", "source_financement": {
                        if (dac.getRealisation().getFinancements() == null || dac.getRealisation().getFinancements().isEmpty()) return null;
                        StringBuilder sb = new StringBuilder();
                        int i = 1;
                        Set<String> seen = new LinkedHashSet<>();
                        for (Financement fin : dac.getRealisation().getFinancements()) {
                            if (Boolean.TRUE.equals(fin.getIsDeleted())) continue;
                            String label = getRefParamLabel(fin.getSourceFinancement());
                            if (label == null) continue;
                            String trimmed = label.trim();
                            if (trimmed.isEmpty()) continue;
                            if (!seen.add(trimmed)) continue;
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(i++).append("- ").append(trimmed);
                        }
                        return sb.isEmpty() ? null : sb.toString();
                    }
                    case "typeMarcher", "type_marcher":
                        return dac.getRealisation().getTypeMarcher() != null ? getRefParamLabel(dac.getRealisation().getTypeMarcher()) : null;
                    case "modeSelection", "mode_selection":
                        return dac.getRealisation().getModeSelection() != null ? getRefParamLabel(dac.getRealisation().getModeSelection()) : null;
                    case "modePassation", "mode_passation":
                        return dac.getRealisation().getModePassation() != null ? dac.getRealisation().getModePassation().getLibelle() : null;
                    case "nomLot", "nom_lot": {
                        if (dac.getRealisation().getLots() == null || dac.getRealisation().getLots().isEmpty()) return null;
                        StringBuilder sb = new StringBuilder();
                        int i = 1;
                        Set<String> seen = new LinkedHashSet<>();
                        for (Lot lot : dac.getRealisation().getLots()) {
                            if (Boolean.TRUE.equals(lot.getIsDeleted())) continue;
                            String name = lot.getDescription();
                            if (name == null) continue;
                            String trimmed = name.trim();
                            if (trimmed.isEmpty()) continue;
                            if (!seen.add(trimmed)) continue;
                            if (!sb.isEmpty()) sb.append("\n");
                            sb.append(i++).append("- ").append(trimmed);
                        }
                        return sb.isEmpty() ? null : sb.toString();
                    }
                    case "budgetEstime", "budget_estime":
                        return toString(dac.getRealisation().getBudgetEstime());
                    case "date_prev_lps":
                        return dac.getRealisation().getDatePrevLps() != null ? dac.getRealisation().getDatePrevLps().format(localDateFormatter) : null;
                    case "date_prev_ac":
                        return dac.getRealisation().getDatePrevAc() != null ? dac.getRealisation().getDatePrevAc().format(localDateFormatter) : null;
                    case "date_prev_dp":
                        return dac.getRealisation().getDatePrevDp() != null ? dac.getRealisation().getDatePrevDp().format(localDateFormatter) : null;
                    case "date_prev_ap":
                        return dac.getRealisation().getDatePrevAp() != null ? dac.getRealisation().getDatePrevAp().format(localDateFormatter) : null;
                    default:
                        return null;
                }
            }
            default:
                return null;
        }
    }

    private String toString(BigDecimal number) {
        return number != null ? number.toPlainString() : null;
    }
    private String getRefParamLabel(RefParam ref) {
        return ref == null ? null : ref.getDescription();
    }

    @Override
    public List<DacDocs> findByDacIdAndDacVer(Integer dacId, Integer dacVer) {
        return dacDocsRepository.findByDacIdAndDacVer(dacId, dacVer);
    }

    @Override
    @Transactional
    public List<DacDocs> createDacDocsBatch(List<DacDocsRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<DacDocsRequest>> requestsByDacId = requests.stream()
                .collect(Collectors.groupingBy(DacDocsRequest::getDacId));

        List<DacDocs> allUpdatedDacDocs = new ArrayList<>();

        for (Map.Entry<Integer, List<DacDocsRequest>> entry : requestsByDacId.entrySet()) {
            Integer dacId = entry.getKey();
            List<DacDocsRequest> dacRequests = entry.getValue();

            Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(dacId);
            if (existingDac == null) {
                throw new RuntimeException("Dac not found with id: " + dacId);
            }

            if (existingDac.getStatus() == ProcessStatus.BROUILLON) {
                log.info("DAC status is BROUILLON, using updateDacDocsInPlace for {} requests", dacRequests.size());
                // Update in place for BROUILLON status
                for (DacDocsRequest req : dacRequests) {
                    log.info("Processing BROUILLON request for field: dynaFieldsId={}, detailsCount={}", 
                             req.getDynaFieldsId(), 
                             req.getDetails() != null ? req.getDetails().size() : 0);
                    allUpdatedDacDocs.add(updateDacDocsInPlace(req));
                }
            } else {
                // Check for changes before creating new version
                boolean hasChanges = detectChanges(existingDac, dacRequests);
                
                if (!hasChanges) {
                    log.info("No changes detected for DAC {}, returning existing DacDocs", dacId);
                    // Return existing DacDocs for all requested fields
                    for (DacDocsRequest req : dacRequests) {
                        Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(req.getDynaDocsId());
                        if (existingDynaDocs.isPresent()) {
                            Optional<DacDocs> existingDacDoc = dacDocsRepository.findDacDocsBYPKs(
                                    existingDac.getDacId(), existingDac.getDacVer(),
                                    existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(),
                                    req.getDynaFieldsId());
                            existingDacDoc.ifPresent(allUpdatedDacDocs::add);
                        }
                    }
                    continue;
                }

                log.info("Changes detected for DAC {}, creating new version", dacId);
                // Create new version for non-BROUILLON status
                int nextVer = existingDac.getDacVer() + 1;
                Dac newDac = new Dac();
                newDac.setDacId(existingDac.getDacId());
                newDac.setDacVer(nextVer);
                newDac.setPpm(existingDac.getPpm());
                newDac.setRealisation(existingDac.getRealisation());
                newDac.setTypeMarche(existingDac.getTypeMarche());
                newDac.setStatus(existingDac.getStatus());
                newDac.setReference(existingDac.getReference());
                newDac.setDateLimiteSoumission(existingDac.getDateLimiteSoumission());
                newDac.setDateLimiteRecptionDemande(existingDac.getDateLimiteRecptionDemande());
                newDac.setPayant(existingDac.isPayant());
                newDac.setShortListing(existingDac.isShortListing());
                newDac.setParentDacId(existingDac.getParentDacId());
                newDac.setProcessInstanceId(existingDac.getProcessInstanceId());
                newDac.setModePassation(existingDac.getModePassation());
                newDac.setDynaDocsModel(existingDac.getDynaDocsModel());
                newDac = dacRepository.save(newDac);

                // First, clone ALL existing dac_docs to the new version (even with null fieldValue)
                List<DacDocs> priorDocs = dacDocsRepository.findByDacIdAndDacVer(existingDac.getDacId(), existingDac.getDacVer());
                Map<String, DacDocs> clonedDocsMap = new HashMap<>();
                for (DacDocs prior : priorDocs) {
                    DacDocs clone = new DacDocs();
                    DacDocsId id = new DacDocsId();
                    id.setDacId(newDac.getDacId());
                    id.setDacVer(newDac.getDacVer());
                    id.setDynaDocsId(prior.getId().getDynaDocsId());
                    id.setDynaDocsVer(prior.getId().getDynaDocsVer());
                    id.setDynaFieldsId(prior.getId().getDynaFieldsId());
                    clone.setId(id);
                    clone.setDac(newDac);
                    clone.setDynaField(prior.getDynaField());
                    clone.setSourceId(prior.getSourceId());
                    // Set fieldValue to null initially - will be updated from request if provided
                    clone.setFieldValue(null);
                    DacDocs savedClone = dacDocsRepository.save(clone);
                    
                    // Clone ALL DacDocsDetails for this DacDocs to the new version
                    List<DacDocsDetails> priorDetails = dacDocsDetailsRepository.findAll(
                            Specification.where(DacDocsDetailsSpecification.byDacId(existingDac.getDacId()))
                                    .and(DacDocsDetailsSpecification.byDacVer(existingDac.getDacVer()))
                                    .and(DacDocsDetailsSpecification.byDynaDocsId(prior.getId().getDynaDocsId()))
                                    .and(DacDocsDetailsSpecification.byDynaDocsVer(prior.getId().getDynaDocsVer()))
                                    .and(DacDocsDetailsSpecification.byDynaFieldsId(prior.getId().getDynaFieldsId()))
                    );
                    
                    if (!priorDetails.isEmpty()) {
                        log.info("Cloning {} DacDocsDetails for field {} to new version", 
                                priorDetails.size(), prior.getId().getDynaFieldsId());
                        for (DacDocsDetails priorDetail : priorDetails) {
                            DacDocsDetails detailClone = new DacDocsDetails();
                            detailClone.setDacId(newDac.getDacId());
                            detailClone.setDacVer(newDac.getDacVer());
                            detailClone.setDynaDocsId(savedClone.getId().getDynaDocsId());
                            detailClone.setDynaDocsVer(savedClone.getId().getDynaDocsVer());
                            detailClone.setDynaFieldsId(savedClone.getId().getDynaFieldsId());
                            detailClone.setRowId(priorDetail.getRowId());
                            detailClone.setDynaFieldsDetailsId(priorDetail.getDynaFieldsDetailsId());
                            detailClone.setSourceId(priorDetail.getSourceId());
                            // Set fieldValue to null initially - will be updated from request if provided
                            detailClone.setFieldValue(null);
                            detailClone.setDac(savedClone);
                            dacDocsDetailsRepository.save(detailClone);
                        }
                    }
                    
                    // Store in map for quick lookup
                    String key = prior.getId().getDynaDocsId() + ":" + 
                                prior.getId().getDynaDocsVer() + ":" + 
                                prior.getId().getDynaFieldsId();
                    clonedDocsMap.put(key, savedClone);
                    log.info("Cloned DacDocs and {} details for new version: {}", priorDetails.size(), savedClone);
                }

                // Create a map of requests by key for quick lookup
                Map<String, DacDocsRequest> requestsByKey = new HashMap<>();
                for (DacDocsRequest req : dacRequests) {
                    Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(req.getDynaDocsId());
                    if (existingDynaDocs.isPresent()) {
                        String key = existingDynaDocs.get().getId() + ":" + 
                                    existingDynaDocs.get().getVersion() + ":" + 
                                    req.getDynaFieldsId();
                        requestsByKey.put(key, req);
                    }
                }

                // Update cloned DacDocs with values from request, or create new ones if not in prior docs
                for (DacDocsRequest req : dacRequests) {
                    Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(req.getDynaDocsId());
                    if (existingDynaDocs.isEmpty()) {
                        throw new RuntimeException("DynaDocs not found with id: " + req.getDynaDocsId());
                    }
                    List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
                    boolean fieldExists = existingDynaFields.stream().anyMatch(f -> f.getDynaFieldsId().equals(req.getDynaFieldsId()));
                    if (!fieldExists) {
                        throw new RuntimeException("DynaField not found with id: " + req.getDynaFieldsId());
                    }

                    String key = existingDynaDocs.get().getId() + ":" + 
                                existingDynaDocs.get().getVersion() + ":" + 
                                req.getDynaFieldsId();
                    
                    DacDocs row;
                    if (clonedDocsMap.containsKey(key)) {
                        // Update existing cloned DacDocs with values from request
                        row = clonedDocsMap.get(key);
                    } else {
                        // Create new DacDocs if it doesn't exist in prior version
                        row = new DacDocs();
                        DacDocsId id = new DacDocsId();
                        id.setDacId(newDac.getDacId());
                        id.setDacVer(newDac.getDacVer());
                        id.setDynaDocsId(existingDynaDocs.get().getId());
                        id.setDynaDocsVer(existingDynaDocs.get().getVersion());
                        id.setDynaFieldsId(req.getDynaFieldsId());
                        row.setId(id);
                        row.setDac(newDac);
                    }
                    
                    DynaFields targetField = existingDynaFields.stream().
                            filter(f -> f.getDynaFieldsId().equals(req.getDynaFieldsId())).findFirst().orElse(null);
                    row.setDynaField(targetField);
                    row.setSourceId(req.getSourceId());
                    row.setFieldValue(req.getFieldValue());
                    DacDocs savedRow = dacDocsRepository.save(row);
                    log.info("Saved DacDocs with values from request: dacId={}, dacVer={}, dynaFieldsId={}", 
                            savedRow.getId().getDacId(), savedRow.getId().getDacVer(), savedRow.getId().getDynaFieldsId());
                    
                    // Verify savedRow has the new version
                    if (!savedRow.getId().getDacVer().equals(newDac.getDacVer())) {
                        log.error("ERROR: savedRow has wrong dacVer! Expected {}, got {}", 
                                newDac.getDacVer(), savedRow.getId().getDacVer());
                        throw new RuntimeException("DacDocs was saved with wrong version. Expected: " + 
                                newDac.getDacVer() + ", got: " + savedRow.getId().getDacVer());
                    }
                    
                    // Handle DacDocsDetails for TABLE/LIST fields - update with values from request
                    if (targetField != null && (targetField.getType() == DynaFieldType.LIST || targetField.getType() == DynaFieldType.TABLE)) {
                        log.info("Field type is TABLE/LIST, updating details for field {} with values from request. DacVer={}", 
                                req.getDynaFieldsId(), savedRow.getId().getDacVer());
                        
                        // Clear any existing cloned details for this field (they were cloned with null values)
                        // We'll replace them with values from request
                        List<DacDocsDetails> detailsToDelete = dacDocsDetailsRepository.findAll(
                                Specification.where(DacDocsDetailsSpecification.byDacId(newDac.getDacId()))
                                        .and(DacDocsDetailsSpecification.byDacVer(newDac.getDacVer()))
                                        .and(DacDocsDetailsSpecification.byDynaDocsId(existingDynaDocs.get().getId()))
                                        .and(DacDocsDetailsSpecification.byDynaDocsVer(existingDynaDocs.get().getVersion()))
                                        .and(DacDocsDetailsSpecification.byDynaFieldsId(req.getDynaFieldsId()))
                        );
                        log.info("Deleting {} cloned details (with null values) for field {} in version {}", 
                                detailsToDelete.size(), req.getDynaFieldsId(), newDac.getDacVer());
                        dacDocsDetailsRepository.deleteAll(detailsToDelete);
                        
                        // Create new DacDocsDetails from request (with actual values from request)
                        if (req.getDetails() != null && !req.getDetails().isEmpty()) {
                            log.info("Creating {} details for field {} from request. Using DacDocs with dacVer={}", 
                                    req.getDetails().size(), req.getDynaFieldsId(), savedRow.getId().getDacVer());
                            List<DacDocsDetailsRequest> mapped = DacDocsMapper.INSTANCE.mapDetails(req.getDetails());
                            log.info("Mapped {} detail requests for field {}", mapped.size(), req.getDynaFieldsId());
                            
                            if (targetField.getType() == DynaFieldType.LIST) {
                                List<DynaFieldsDetails> cols = dynaFieldsDetailsRepository.findAllByDocAndField(
                                        savedRow.getId().getDynaDocsId(), savedRow.getId().getDynaDocsVer(), targetField.getDynaFieldsId());
                                Integer singleColId = cols != null && !cols.isEmpty() ? cols.get(0).getDynaFieldsDetailsId() : null;
                                if (singleColId != null) {
                                    for (DacDocsDetailsRequest dr : mapped) {
                                        if (dr.getDynaFieldsDetailsId() == null || dr.getDynaFieldsDetailsId().equals(targetField.getDynaFieldsId())) {
                                            dr.setDynaFieldsDetailsId(singleColId);
                                        }
                                    }
                                }
                            }
                            
                            // Verify savedRow version before creating details
                            log.info("About to create details using DacDocs with dacId={}, dacVer={}", 
                                    savedRow.getId().getDacId(), savedRow.getId().getDacVer());
                            
                            List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(savedRow, mapped);
                            log.info("Created {} DacDocsDetails entities for field {}", detailsList.size(), req.getDynaFieldsId());
                            
                            // Verify details were created with correct version
                            for (DacDocsDetails detail : detailsList) {
                                log.info("Created detail: rowId={}, colId={}, fieldValue='{}', dacId={}, dacVer={}", 
                                        detail.getRowId(), detail.getDynaFieldsDetailsId(), detail.getFieldValue(),
                                        detail.getDacId(), detail.getDacVer());
                                
                                // Verify version is correct
                                if (!detail.getDacVer().equals(newDac.getDacVer())) {
                                    log.error("ERROR: Detail created with wrong dacVer! Expected {}, got {}", 
                                            newDac.getDacVer(), detail.getDacVer());
                                    throw new RuntimeException("DacDocsDetails was created with wrong version. Expected: " + 
                                            newDac.getDacVer() + ", got: " + detail.getDacVer());
                                }
                            }
                            
                            savedRow.setDacDocsDetails(new HashSet<>(detailsList));
                            savedRow = dacDocsRepository.save(savedRow);
                            
                            // Load details explicitly from database to ensure they're persisted and loaded
                            List<DacDocsDetails> loadedDetails = dacDocsDetailsRepository.findAll(
                                    Specification.where(DacDocsDetailsSpecification.byDacId(savedRow.getId().getDacId()))
                                            .and(DacDocsDetailsSpecification.byDacVer(savedRow.getId().getDacVer()))
                                            .and(DacDocsDetailsSpecification.byDynaDocsId(savedRow.getId().getDynaDocsId()))
                                            .and(DacDocsDetailsSpecification.byDynaDocsVer(savedRow.getId().getDynaDocsVer()))
                                            .and(DacDocsDetailsSpecification.byDynaFieldsId(savedRow.getId().getDynaFieldsId()))
                            );
                            
                            log.info("Loaded {} details from database for field {} in version {}", 
                                    loadedDetails.size(), req.getDynaFieldsId(), savedRow.getId().getDacVer());
                            
                            // Verify all loaded details have correct version
                            for (DacDocsDetails detail : loadedDetails) {
                                if (!detail.getDacVer().equals(newDac.getDacVer())) {
                                    log.error("ERROR: Loaded detail has wrong dacVer! Expected {}, got {}", 
                                            newDac.getDacVer(), detail.getDacVer());
                                }
                            }
                            
                            savedRow.setDacDocsDetails(new HashSet<>(loadedDetails));
                            
                            // Verify the relationship is set
                            log.info("DacDocs entity has {} details in relationship after loading", savedRow.getDacDocsDetails().size());
                            
                            log.info("Successfully created and loaded {} details from request for new version {}", 
                                    loadedDetails.size(), newDac.getDacVer());
                        } else {
                            // Request explicitly has no details (empty list) - this means remove all details
                            // Details are already deleted above, so just log
                            log.info("No details in request for field {}, all details removed for new version", req.getDynaFieldsId());
                        }
                    } else {
                        // For non-TABLE/LIST fields, ensure details are empty
                        savedRow.setDacDocsDetails(new HashSet<>());
                    }
                    allUpdatedDacDocs.add(savedRow);
                }
            }
        }
        return allUpdatedDacDocs;
    }

    /**
     * Detects if there are any changes between existing DacDocs/DacDocsDetails and the request values
     * @param existingDac The existing DAC with latest version
     * @param requests List of DacDocsRequest to compare against
     * @return true if changes are detected, false otherwise
     */
    private boolean detectChanges(Dac existingDac, List<DacDocsRequest> requests) {
        log.info("detectChanges called for DAC {} version {} with {} requests", 
                existingDac.getDacId(), existingDac.getDacVer(), requests.size());
        
        for (DacDocsRequest req : requests) {
            log.info("Checking changes for request: dacId={}, dynaFieldsId={}, dynaDocsId={}, detailsCount={}", 
                    req.getDacId(), req.getDynaFieldsId(), req.getDynaDocsId(), 
                    req.getDetails() != null ? req.getDetails().size() : 0);
            Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(req.getDynaDocsId());
            if (existingDynaDocs.isEmpty()) {
                // New DynaDocs - this is a change
                return true;
            }

            // Check DacDocs fieldValue change
            Optional<DacDocs> existingDacDoc = dacDocsRepository.findDacDocsBYPKs(
                    existingDac.getDacId(), existingDac.getDacVer(),
                    existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(),
                    req.getDynaFieldsId());

            if (existingDacDoc.isEmpty()) {
                // New field - this is a change
                return true;
            }

            DacDocs existing = existingDacDoc.get();
            
            // Compare fieldValue
            if (!Objects.equals(existing.getFieldValue(), req.getFieldValue())) {
                log.info("Change detected in fieldValue for field {}: old='{}', new='{}'", 
                        req.getDynaFieldsId(), existing.getFieldValue(), req.getFieldValue());
                return true;
            }

            // Check DacDocsDetails changes for TABLE/LIST fields
            List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(
                    existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
            DynaFields targetField = existingDynaFields.stream()
                    .filter(f -> f.getDynaFieldsId().equals(req.getDynaFieldsId()))
                    .findFirst()
                    .orElse(null);

            if (targetField != null && (targetField.getType() == DynaFieldType.LIST || targetField.getType() == DynaFieldType.TABLE)) {
                // Get existing details
                List<DacDocsDetails> existingDetails = dacDocsDetailsRepository.findAll(
                        Specification.where(DacDocsDetailsSpecification.byDacId(existingDac.getDacId()))
                                .and(DacDocsDetailsSpecification.byDacVer(existingDac.getDacVer()))
                                .and(DacDocsDetailsSpecification.byDynaDocsId(existingDynaDocs.get().getId()))
                                .and(DacDocsDetailsSpecification.byDynaDocsVer(existingDynaDocs.get().getVersion()))
                                .and(DacDocsDetailsSpecification.byDynaFieldsId(req.getDynaFieldsId()))
                );

                log.info("Checking details changes for field {}: existing count={}, request count={}", 
                        req.getDynaFieldsId(), 
                        existingDetails.size(), 
                        req.getDetails() != null ? req.getDetails().size() : 0);

                // Compare details
                if (req.getDetails() == null || req.getDetails().isEmpty()) {
                    if (!existingDetails.isEmpty()) {
                        log.info("Change detected in details for field {}: existing has {} details, request has none", 
                                req.getDynaFieldsId(), existingDetails.size());
                        return true;
                    }
                } else {
                    // Create normalized copy of request details for comparison (don't modify original)
                    List<DacDocsRequest.DacDocsDetail> normalizedRequestDetails = new ArrayList<>();
                    for (DacDocsRequest.DacDocsDetail original : req.getDetails()) {
                        DacDocsRequest.DacDocsDetail copy = new DacDocsRequest.DacDocsDetail();
                        copy.setDynaFieldsDetailsId(original.getDynaFieldsDetailsId());
                        copy.setSourceId(original.getSourceId());
                        copy.setFieldValue(original.getFieldValue());
                        copy.setRowId(original.getRowId());
                        normalizedRequestDetails.add(copy);
                    }
                    
                    // Normalize request details for LIST type
                    if (targetField.getType() == DynaFieldType.LIST) {
                        List<DynaFieldsDetails> cols = dynaFieldsDetailsRepository.findAllByDocAndField(
                                existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(), targetField.getDynaFieldsId());
                        Integer singleColId = cols != null && !cols.isEmpty() ? cols.get(0).getDynaFieldsDetailsId() : null;
                        if (singleColId != null) {
                            for (DacDocsRequest.DacDocsDetail detail : normalizedRequestDetails) {
                                if (detail.getDynaFieldsDetailsId() == null || detail.getDynaFieldsDetailsId().equals(targetField.getDynaFieldsId())) {
                                    detail.setDynaFieldsDetailsId(singleColId);
                                }
                            }
                        }
                    }

                    // Compare details count
                    if (existingDetails.size() != normalizedRequestDetails.size()) {
                        log.info("Change detected in details count for field {}: existing={}, request={}", 
                                req.getDynaFieldsId(), existingDetails.size(), normalizedRequestDetails.size());
                        return true;
                    }

                    // Log existing details for debugging
                    log.info("Existing details for field {}:", req.getDynaFieldsId());
                    for (DacDocsDetails existingDetail : existingDetails) {
                        log.info("  - rowId={}, colId={}, fieldValue='{}'", 
                                existingDetail.getRowId(), existingDetail.getDynaFieldsDetailsId(), existingDetail.getFieldValue());
                    }
                    
                    // Log request details for debugging
                    log.info("Request details for field {}:", req.getDynaFieldsId());
                    for (DacDocsRequest.DacDocsDetail reqDetail : normalizedRequestDetails) {
                        log.info("  - rowId={}, colId={}, fieldValue='{}'", 
                                reqDetail.getRowId(), reqDetail.getDynaFieldsDetailsId(), reqDetail.getFieldValue());
                    }
                    
                    // Build map of existing details by key (rowId:dynaFieldsDetailsId)
                    Map<String, DacDocsDetails> existingDetailsMap = existingDetails.stream()
                            .collect(Collectors.toMap(
                                    d -> {
                                        Integer rowId = d.getRowId() != null ? d.getRowId() : 0;
                                        Integer colId = d.getDynaFieldsDetailsId() != null ? d.getDynaFieldsDetailsId() : 0;
                                        return rowId + ":" + colId;
                                    },
                                    d -> d,
                                    (a, b) -> {
                                        log.warn("Duplicate key found in existing details, keeping first: {}", a);
                                        return a;
                                    }
                            ));

                    log.info("Comparing {} existing details with {} request details for field {}", 
                            existingDetails.size(), normalizedRequestDetails.size(), req.getDynaFieldsId());
                    
                    // Compare each request detail with existing
                    for (DacDocsRequest.DacDocsDetail reqDetail : normalizedRequestDetails) {
                        Integer reqRowId = reqDetail.getRowId() != null ? reqDetail.getRowId() : 0;
                        Integer reqColId = reqDetail.getDynaFieldsDetailsId() != null ? reqDetail.getDynaFieldsDetailsId() : 0;
                        String key = reqRowId + ":" + reqColId;
                        
                        log.debug("Checking request detail: key='{}', rowId={}, colId={}, fieldValue='{}'", 
                                key, reqRowId, reqColId, reqDetail.getFieldValue());
                        
                        DacDocsDetails existingDetail = existingDetailsMap.get(key);
                        
                        // If detail doesn't exist, it's a change
                        if (existingDetail == null) {
                            log.info("Change detected in details for field {}: new detail with key '{}' (rowId={}, colId={}) not found in existing", 
                                    req.getDynaFieldsId(), key, reqRowId, reqColId);
                            return true;
                        }
                        
                        // Compare fieldValue (handle null/empty strings)
                        String existingValue = existingDetail.getFieldValue() != null ? existingDetail.getFieldValue() : "";
                        String requestValue = reqDetail.getFieldValue() != null ? reqDetail.getFieldValue() : "";
                        
                        log.debug("Comparing fieldValue for key '{}': existing='{}', request='{}'", 
                                key, existingValue, requestValue);
                        
                        if (!existingValue.equals(requestValue)) {
                            log.info("Change detected in details fieldValue for field {}: rowId={}, colId={}, old='{}', new='{}'", 
                                    req.getDynaFieldsId(), reqRowId, reqColId, existingValue, requestValue);
                            return true;
                        }
                        
                        // Compare sourceId (handle null/empty strings)
                        String existingSourceId = existingDetail.getSourceId() != null ? existingDetail.getSourceId() : "";
                        String requestSourceId = reqDetail.getSourceId() != null ? reqDetail.getSourceId() : "";
                        if (!existingSourceId.equals(requestSourceId)) {
                            log.info("Change detected in details sourceId for field {}: rowId={}, colId={}, old='{}', new='{}'", 
                                    req.getDynaFieldsId(), reqRowId, reqColId, existingSourceId, requestSourceId);
                            return true;
                        }
                    }
                    
                    // Check if any existing details were removed (not present in request)
                    Set<String> requestKeys = normalizedRequestDetails.stream()
                            .map(d -> {
                                Integer rowId = d.getRowId() != null ? d.getRowId() : 0;
                                Integer colId = d.getDynaFieldsDetailsId() != null ? d.getDynaFieldsDetailsId() : 0;
                                return rowId + ":" + colId;
                            })
                            .collect(Collectors.toSet());
                    
                    for (DacDocsDetails existingDetail : existingDetails) {
                        Integer existingRowId = existingDetail.getRowId() != null ? existingDetail.getRowId() : 0;
                        Integer existingColId = existingDetail.getDynaFieldsDetailsId() != null ? existingDetail.getDynaFieldsDetailsId() : 0;
                        String existingKey = existingRowId + ":" + existingColId;
                        if (!requestKeys.contains(existingKey)) {
                            log.info("Change detected in details for field {}: existing detail with key '{}' (rowId={}, colId={}) removed", 
                                    req.getDynaFieldsId(), existingKey, existingRowId, existingColId);
                            return true;
                        }
                    }
                }
            } else {
                log.debug("Field {} is not TABLE/LIST type, skipping details comparison", req.getDynaFieldsId());
            }
        }
        log.info("No changes detected for DAC {} version {}", existingDac.getDacId(), existingDac.getDacVer());
        return false;
    }
}
