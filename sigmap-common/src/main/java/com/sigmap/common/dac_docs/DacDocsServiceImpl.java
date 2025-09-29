package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocDetailsDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.DacDocsDetailsRepository;
import com.sigmap.common.dac_docs_details.DacDocsDetailsService;
import com.sigmap.common.dac_docs_details.DacDocsDetailsSpecification;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsRepository;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.config.Constants;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Override
    @Transactional
    public DacDocs uploadStaticDoc(Integer dacId, Integer dynaDocsId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }
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
        // Persist to ensure ID fields present when saving file path
        row = dacDocsRepository.save(row);

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

                List<DacDocsDetails> detailsList =
                        dacDocsDetailsService.createAll(entity, detailsReq);

                Set<DacDocsDetails> details = DacDocsMapper.INSTANCE.detailsToSet(detailsList);

                entity.setDacDocsDetails(details);
            }

        }

            return entity;
    }


    @Override
    @Transactional
    public List<DacDocs> createDacDocsList(List<DacDocsRequest> requests) {
        List<DacDocs> result = new ArrayList<>();

        for (DacDocsRequest request : requests) {
            // 1- get the DynaDocs with last version
            Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaDocsId());

            // 2- test if the existingDynaDocs is empty
            if (existingDynaDocs.isEmpty()) {
                throw new RuntimeException("DynaDocs not found with id: " + request.getDynaDocsId());
            }

            // 3- get the DynaFields with last dynaDocs version for the given id
            List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
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

            // Upsert DacDocs for latest DAC version
            Optional<DacDocs> existingRowOpt = dacDocsRepository.findDacDocsBYPKs(
                    existingDac.getDacId(), existingDac.getDacVer(), existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(), request.getDynaFieldsId());

            DacDocs row = existingRowOpt.orElseGet(DacDocs::new);
            DacDocsId id = new DacDocsId();
            id.setDacId(existingDac.getDacId());
            id.setDacVer(existingDac.getDacVer());
            id.setDynaDocsId(existingDynaDocs.get().getId());
            id.setDynaDocsVer(existingDynaDocs.get().getVersion());
            id.setDynaFieldsId(request.getDynaFieldsId());
            row.setId(id);
            row.setDac(existingDac);
            DynaFields targetField = existingDynaFields.stream()
                    .filter(f -> f.getDynaFieldsId().equals(request.getDynaFieldsId()))
                    .findFirst().orElse(null);
            if (targetField == null) {
                throw new RuntimeException("DynaField not found with id: " + request.getDynaFieldsId());
            }
            row.setDynaField(targetField);
            row.setSourceId(request.getSourceId());
            row.setFieldValue(request.getFieldValue());
            DacDocs savedRow = dacDocsRepository.save(row);

            // Always refresh details for LIST/TABLE: delete existing then recreate from request
            if (targetField.getType() == DynaFieldType.LIST || targetField.getType() == DynaFieldType.TABLE) {
                dacDocsDetailsRepository.deleteAll(
                        dacDocsDetailsRepository.findAll(Specification.where(DacDocsDetailsSpecification.byDacId(existingDac.getDacId()))
                                .and(DacDocsDetailsSpecification.byDacVer(existingDac.getDacVer()))
                                .and(DacDocsDetailsSpecification.byDynaDocsId(existingDynaDocs.get().getId()))
                                .and(DacDocsDetailsSpecification.byDynaDocsVer(existingDynaDocs.get().getVersion()))
                                .and(DacDocsDetailsSpecification.byDynaFieldsId(request.getDynaFieldsId()))
                        )
                );
                if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                    List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(savedRow, DacDocsMapper.INSTANCE.mapDetails(request.getDetails()));
                    savedRow.setDacDocsDetails(new HashSet<>(detailsList));
                    savedRow = dacDocsRepository.save(savedRow);
                }
            }

            result.add(savedRow);
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
                List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(savedDacDocs, DacDocsMapper.INSTANCE.mapDetails(request.getDetails()));
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

            String key = dacDoc.getId().getDynaDocsId() + ":" + dacDoc.getId().getDynaDocsVer() + ":" + dacDoc.getId().getDynaFieldsId();
            List<DacDocsDetails> details = detailsByDoc.getOrDefault(key, Collections.emptyList());
            List<DacDocDetailsDTO> detailsDTOs = details.stream().map(detail -> {
                DacDocDetailsDTO dto = new DacDocDetailsDTO();
                dto.setDynaFieldsDetailsId(detail.getDynaFieldsDetailsId());
                dto.setRowId(detail.getRowId());
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
                // Versioning for non-BROUILLON status
                Dac newDac = new Dac();
                newDac.setDacId(existingDac.getDacId());
                newDac.setDacVer(existingDac.getDacVer());
                newDac.setPpm(existingDac.getPpm());
                newDac.setRealisation(existingDac.getRealisation());
                newDac.setTypeMarche(existingDac.getTypeMarche());
                newDac.setStatus(existingDac.getStatus());
                newDac.setReference(existingDac.getReference());
                newDac = dacRepository.save(newDac);

                // Clone previous dac_docs snapshot to the new version
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
                    dacDocsRepository.save(clone);
                }

                // Apply batch updates to the new DAC version
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

                    //find all dacDocs with ids
                    Optional<DacDocs> targetInNew = dacDocsRepository.findDacDocsBYPKs(
                            newDac.getDacId(), newDac.getDacVer(), existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion(), req.getDynaFieldsId());


                    DacDocs currentDacDoc = targetInNew.orElse(null);
                    // No-op check: If existing and new values are the same, skip update
                    if (currentDacDoc != null && !Objects.equals(currentDacDoc.getFieldValue(), req.getFieldValue()) ) {
                        allUpdatedDacDocs.add(currentDacDoc); // Add the existing one to the result
                        continue;
                    }

                    DacDocs row = targetInNew.orElseGet(DacDocs::new); // Reuse or create new
                    DacDocsId id = new DacDocsId();
                    id.setDacId(newDac.getDacId());
                    id.setDacVer(newDac.getDacVer());
                    id.setDynaDocsId(existingDynaDocs.get().getId());
                    id.setDynaDocsVer(existingDynaDocs.get().getVersion());
                    id.setDynaFieldsId(req.getDynaFieldsId());
                    row.setId(id);
                    row.setDac(newDac);
                    DynaFields targetField = existingDynaFields.stream().
                            filter(f -> f.getDynaFieldsId().equals(req.getDynaFieldsId())).findFirst().orElse(null);
                    row.setDynaField(targetField);
                    row.setSourceId(req.getSourceId());
                    row.setFieldValue(req.getFieldValue());
                    DacDocs savedRow = dacDocsRepository.save(row);
                    log.info("saved row is .......................... {}",savedRow);
                    
                    // Handle DacDocsDetails for TABLE/LIST fields
                    log.info("*************** ****************** ********** ************* {}, {}",
                             req.getDynaFieldsId(), 
                            targetField.getType());
                    
                    if (targetField.getType() == DynaFieldType.LIST || targetField.getType() == DynaFieldType.TABLE) {
                        log.info("Field type is TABLE/LIST, processing details... .......................................................");
                        if (req.getDetails() != null && !req.getDetails().isEmpty()) {
                            log.info("Creating {} details for field {} ..............................", req.getDetails().size(), req.getDynaFieldsId());
                            List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(savedRow, DacDocsMapper.INSTANCE.mapDetails(req.getDetails()));
                            savedRow.setDacDocsDetails(new HashSet<>(detailsList));
                            // Save the entity again to ensure the details association is persisted
                            savedRow = dacDocsRepository.save(savedRow);
                            log.info("Successfully created {} details", detailsList.size());
                        } else {
                            log.info("No details to process for field {}", req.getDynaFieldsId());
                        }
                    } else {
                        log.info("Field type is not TABLE/LIST: {}", targetField != null ? targetField.getType() : "null");
                    }
                    allUpdatedDacDocs.add(savedRow);
                }
            }
        }
        return allUpdatedDacDocs;
    }
}
