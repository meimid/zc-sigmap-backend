package com.sigmap.common.agpm;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.Constants;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import com.sigmap.common.security.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service implementation for managing Agpm entities.
 * Handles CRUD operations and associations with Ac and Docs.
 */
@Service
@Slf4j
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
    private final AcRepository acRepository;
    private final DocsRepository docsRepository;
    private final AgpmMapper agpmMapper;
    private final RefParamRepository refParamRepository;
    private final MessageSource messageSource;
    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;
    private final DocsService docsService;
    private final AcService acService;


    public AgpmServiceImpl(AgpmRepository repository,
                           AcRepository acRepository,
                           DocsRepository docsRepository,
                           AgpmMapper agpmMapper,
                           RefParamRepository refParamRepository,
                           MessageSource messageSource,
                           AlfrescoService alfrescoService,
                           DocumentCommuneService documentCommuneService,
                           DocsService docsService,
                           AcService acService) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
        this.alfrescoService = alfrescoService;
        this.docsService = docsService;
        this.documentCommuneService = documentCommuneService;
        this.acService = acService;

    }

    private Locale getLocal() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    public List<Agpm> findAll() {
        Specification<Agpm> spec = AgpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }


    @Override
    public Page<Agpm> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }


    @Override
    public Optional<Agpm> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Agpm findById(Integer agpmId, Integer agpmVer) throws AgpmNotFound {
        return repository.findById(new CompositeId(agpmId, agpmVer))
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));
    }

    @Override
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }


    @Override
    public Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus, IOException {
        Integer version = request.getVersion();
        if (version == null) {
            throw new AgpmNotNull("");
        }

        Integer id = request.getId();
        if (id == null) {
            id = repository.findMaxId();
            if (id == null) {
                id = 1;
            } else {
                id++;
            }
        }

        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));
        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException("", id, version);
        }

        Agpm agpm = agpmMapper.toEntity(request);
        agpm.setId(id);
        agpm.setVersion(version);

        Optional<Long> acIdOptional = Authentication.getCurrentUserAC();

        if (acIdOptional.isPresent()) {
            Long acIdLong = acIdOptional.get();
            Integer acIdInt = Math.toIntExact(acIdLong); // safe conversion from Long to Integer

            Optional<Ac> acOptional = Optional.ofNullable(acService.findById(acIdInt));
            if (acOptional.isPresent()) {
                Ac ac = acOptional.get();
                agpm.setAc(ac);
            } else {
                throw new AcNotFound("AC not found with ID: ", Collections.singletonList(acIdInt));
            }
        } else {
            throw new NotFoundException("No AC ID found for current user.");
        }

        if (request.getFiles() != null) {
            processAndSaveDocuments(request.getFiles(), agpm);
        }

        agpm.setIsDeleted(false);

        RefParam defaultStatus = refParamRepository.findById(1)
                .orElseThrow(() -> new AgpmDefaultStatus(""));
        agpm.setStatus(defaultStatus);

        return repository.save(agpm);
    }


    @Override
    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound("", List.of(agpmId), List.of(agpmVer));
        }

        Integer rowsAffected = repository.softDelete(agpmId, agpmVer, Calendar.getInstance());
        if (rowsAffected != 1) {
            throw new AgpmSoftDelete("", rowsAffected, agpmId, agpmVer);
        }
    }

    @Override
    @Transactional
    public Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, ReferenceNotFound, IOException {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm existingAgpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));

        if (Boolean.TRUE.equals(existingAgpm.getIsDeleted())) {
            throw new IllegalArgumentException(messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, agpmVer}, getLocal()));
        }

        // Determine the next version
        Integer nextVersion = repository.findMaxVersionById(agpmId);
        nextVersion = (nextVersion != null) ? nextVersion + 1 : 1;

        // Create new Agpm version
        Agpm newAgpm = new Agpm();
        newAgpm.setId(agpmId);
        newAgpm.setVersion(nextVersion);
        newAgpm.setAc(existingAgpm.getAc());
        newAgpm.setStatus(existingAgpm.getStatus());
        newAgpm.setIsDeleted(existingAgpm.getIsDeleted());

        // Copy existing Docs (including soft-deleted ones)
        List<Docs> existingDocs = existingAgpm.getDocs() != null ? new ArrayList<>(existingAgpm.getDocs()) : new ArrayList<>();
        List<Integer> existingDocIds = existingDocs.stream()
                .map(Docs::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.debug("Existing document IDs for Agpm {}:{}: {}", agpmId, agpmVer, existingDocIds);

        // Update Agpm fields from request
        agpmMapper.updateAgpmFromRequest(request, newAgpm);

        // Check Docs by docIds from the request
        List<Integer> requestedDocIds = request.getDocIds() != null ? new ArrayList<>(request.getDocIds()) : new ArrayList<>();
        List<Docs> requestedDocs = docsRepository.findByIdIn(requestedDocIds);
        if (requestedDocIds.size() != requestedDocs.size()) {
            List<Integer> foundDocIds = requestedDocs.stream().map(Docs::getId).toList();
            List<Integer> missingDocIds = requestedDocIds.stream()
                    .filter(docId -> !foundDocIds.contains(docId))
                    .collect(Collectors.toList());
            throw new DocNotFound("", missingDocIds);
        }
        log.debug("Requested document IDs: {}", requestedDocIds);

        // Update Docs association: Keep existing docs (including soft-deleted) and update/add requested ones
        List<Docs> newDocs = new ArrayList<>(existingDocs);
        if (!requestedDocs.isEmpty()) {
            // Remove requested docs from newDocs to avoid duplicates
            newDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
            // Add requested docs
            newDocs.addAll(requestedDocs);
            newAgpm.setDocs(newDocs);
        } else if (request.getDocIds() != null && request.getDocIds().isEmpty()) {
            newAgpm.setDocs(new ArrayList<>());
        } else {
            newAgpm.setDocs(newDocs);
        }

        // Process new file uploads to add new documents
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            try {
                processAndSaveDocuments(request.getFiles(), newAgpm);
                log.debug("New documents added from files to Agpm {}:{}", agpmId, nextVersion);
            } catch (IOException e) {
                log.error("Failed to process files: {}", e.getMessage());
                throw e;
            }
        }

        // Determine documents to soft delete (present in existingDocs but not in requestedDocIds)
        List<Integer> docIdsToDelete = existingDocIds.stream()
                .filter(docId -> !requestedDocIds.contains(docId))
                .collect(Collectors.toList());
        log.debug("Documents to soft delete: {}", docIdsToDelete);

        // Soft delete documents that are no longer in the request
        for (Integer docId : docIdsToDelete) {
            Optional<Docs> docToDelete = docsRepository.findById(docId);
            if (docToDelete.isPresent()) {
                docsRepository.softDelete(docToDelete.get().getId());
                log.info("Soft deleted document with ID: {} (isDeleted set to true)", docId);
            } else {
                log.warn("Document with ID {} not found for soft deletion", docId);
                throw new DocNotFound("", List.of(docId));
            }
        }

        // Set audit fields
        newAgpm.setUpdatedAt(Calendar.getInstance());
        newAgpm.setUpdatedBy("system");

        // Save the new Agpm version
        Agpm savedAgpm = repository.save(newAgpm);

        // Filter out soft-deleted Docs in the response
        if (savedAgpm.getDocs() != null) {
            savedAgpm.setDocs(savedAgpm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList()));
        }
        log.info("Returning Agpm {}:{} with filtered document IDs: {}",
                agpmId, nextVersion,
                savedAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList()));

        return savedAgpm;
    }


    @Override
    @Transactional(readOnly = true)
    public Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, IOException {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));

        if (Boolean.TRUE.equals(agpm.getIsDeleted())) {
            throw new IllegalArgumentException(messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, agpmVer}, getLocal()));
        }

        agpmMapper.updateAgpmFromRequest(request, agpm);

        // Fetch existing documents id associated with the Agpm
        List<Integer> docsIds = new ArrayList<>();
        if (agpm.getDocs() != null) {
            docsIds = agpm.getDocs().stream()
                    .map(Docs::getId)
                    .toList();
        }
        log.info("existing Docs for updating agpm .... {}", docsIds);

        List<Docs> existingDocs = agpm.getDocs() != null ? new ArrayList<>(agpm.getDocs()) : new ArrayList<>();

        // Check Docs by docIds from the request
        List<Integer> requestedDocIds = request.getDocIds() != null ? new ArrayList<>(request.getDocIds()) : new ArrayList<>();
        List<Docs> requestedDocs = docsRepository.findByIdIn(requestedDocIds);
        if (requestedDocIds.size() != requestedDocs.size()) {
            List<Integer> foundDocIds = requestedDocs.stream().map(Docs::getId).toList();
            List<Integer> missingDocIds = requestedDocIds.stream()
                    .filter(docId -> !foundDocIds.contains(docId))
                    .collect(Collectors.toList());
            throw new DocNotFound("", missingDocIds);
        }
        log.debug("Requested document IDs: {}", requestedDocIds);

        // Update the Agpm's documents: Keep existing docs (including soft-deleted) and update/add requested ones
        if (!requestedDocs.isEmpty()) {
            existingDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
            existingDocs.addAll(requestedDocs);
            agpm.setDocs(existingDocs);
        }
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            try {
                processAndSaveDocuments(request.getFiles(), agpm);
                log.debug("New documents added from files to Agpm");
            } catch (IOException e) {
                log.error("Failed to process files: {}", e.getMessage());
                throw e;
            }
        }

        List<Integer> docIdsToDelete = docsIds.stream()
                .filter(docId -> !requestedDocIds.contains(docId))
                .collect(Collectors.toList());
        log.debug("Documents to soft delete: {}", docIdsToDelete);

        for (Integer docId : docIdsToDelete) {
            Optional<Docs> docToDelete = docsRepository.findById(docId);
            if (docToDelete.isPresent()) {
                docsRepository.softDelete(docToDelete.get().getId());
                log.info("Soft deleted document with ID: {} (isDeleted set to true)", docId);
            } else {
                log.warn("Document with ID {} not found for soft deletion", docId);
                throw new DocNotFound("", List.of(docId));
            }
        }

        agpm.setUpdatedAt(Calendar.getInstance());

        // Update the Agpm's documents: Keep existing docs and update/add only the requested ones
        if (!requestedDocs.isEmpty()) {
            existingDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
            existingDocs.addAll(requestedDocs);
            agpm.setDocs(existingDocs);
        }

        // Process new file uploads to add new documents
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            try {
                processAndSaveDocuments(request.getFiles(), agpm);
                log.debug("New documents added from files to Agpm");
            } catch (IOException e) {
                log.error("Failed to process files: {}", e.getMessage());
                throw e;
            }
        }

        // Set audit fields
        agpm.setUpdatedAt(Calendar.getInstance());
        agpm.setUpdatedBy("system");

        // Save the updated Agpm entity
        Agpm updatedAgpm = repository.save(agpm);

        if (updatedAgpm.getDocs() != null) {
            updatedAgpm.setDocs(updatedAgpm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList()));
        }
        log.info("Returning Agpm with filtered document IDs : {}", updatedAgpm);

        return updatedAgpm;
    }

    @Override
    @Transactional
    public Agpm updateStatus(Integer agpmId, Integer agpmVer, Integer statusId) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));

        List<String> notFoundRefs = new ArrayList<>();

        RefParam newStatus = refParamRepository.findById(statusId).get();

        if (newStatus != null) {
            agpm.setStatus(newStatus);
        } else {
            notFoundRefs.add(messageSource.getMessage("ppm.status", new Integer[]{statusId}, getLocal()));
        }


        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" ", List.of(String.join(", ", notFoundRefs)));
        }

        agpm.setUpdatedAt(Calendar.getInstance());
        agpm.setUpdatedBy("system");

        return repository.save(agpm);
    }

    private void processAndSaveDocuments(List<MultipartFile> files, Agpm savedAgpm) throws IOException {
        UUID uuid = UUID.randomUUID();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename();

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_AGPM + "/%d/%d",
                                savedAgpm.getId(), savedAgpm.getVersion());

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = "agpm_" + uuid + "_" + extension;
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
                        log.info(alfrescoId);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);
                        if (savedAgpm.getDocs() == null) {
                            savedAgpm.setDocs(new ArrayList<>());
                        }
                        savedAgpm.getDocs().add(document);

                        docsRepository.save(document);
                    } catch (Exception e) {
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
}