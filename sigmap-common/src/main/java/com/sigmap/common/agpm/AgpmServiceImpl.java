package com.sigmap.common.agpm;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.Constants;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
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

    public AgpmServiceImpl(AgpmRepository repository,
                           AcRepository acRepository,
                           DocsRepository docsRepository,
                           AgpmMapper agpmMapper,
                           RefParamRepository refParamRepository,
                           MessageSource messageSource,
                           AlfrescoService alfrescoService,
                           DocumentCommuneService documentCommuneService
                           ) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
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

        // Manually set Ac with ID 1 (will be retrived from keycloack)
        Ac ac = new Ac();
        ac.setId(1);
        agpm.setAc(ac);

//        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
//            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
//            if (docs.size() != request.getDocIds().size()) {
//                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
//                List<Integer> missingDocIds = request.getDocIds().stream()
//                        .filter(docId -> !foundDocIds.contains(docId)).toList();
//                throw new DocNotFound("", missingDocIds);
//            }
//            agpm.setDocs(docs);
//        } else if (request.getDocIds() != null) {
//            agpm.setDocs(List.of());
//        }

        if(request.getFiles() != null) {
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

        Integer nextVersion = repository.findMaxVersionById(agpmId);
        nextVersion = (nextVersion != null) ? nextVersion + 1 : 1;

        Agpm newAgpm = new Agpm();
        newAgpm.setId(agpmId);
        newAgpm.setVersion(nextVersion);
        newAgpm.setAc(existingAgpm.getAc());
        newAgpm.setDocs(existingAgpm.getDocs() != null ? new ArrayList<>(existingAgpm.getDocs()) : null);
        newAgpm.setStatus(existingAgpm.getStatus());
        newAgpm.setIsDeleted(existingAgpm.getIsDeleted());

        agpmMapper.updateAgpmFromRequest(request, newAgpm);

        // Update Docs association if provided
    /*    if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new DocNotFound("", missingDocIds);
            }
            newAgpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            newAgpm.setDocs(List.of());
        }*/

        if(request.getFiles() != null) {
            processAndSaveDocuments(request.getFiles(), newAgpm);
        }

        List<String> notFoundRefs = new ArrayList<>();
        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound("", List.of(String.join(", ", notFoundRefs)));
        }

        return repository.save(newAgpm);
    }

    @Override
    @Transactional
    public Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));

        if (Boolean.TRUE.equals(agpm.getIsDeleted())) {
            throw new IllegalArgumentException(messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, agpmVer}, getLocal()));
        }

        agpmMapper.updateAgpmFromRequest(request, agpm);

        // Update Docs association if provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new DocNotFound("", missingDocIds);
            }
            agpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            agpm.setDocs(List.of());
        }

        // Set audit fields
        agpm.setUpdatedAt(Calendar.getInstance());
        agpm.setUpdatedBy("system");

        // Save the updated Agpm entity
        return repository.save(agpm);
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

                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_AGPM + LocalDate.now().getYear() + "/%d/%d",
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
                       /* if (document.getPaas() == null) {
                            document.setPaas(new ArrayList<>());
                        }
                        if (document.getPpms() == null) {
                            document.setPpms(new ArrayList<>());
                        }
                        document.getPaas().add(savedPaa);*/
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