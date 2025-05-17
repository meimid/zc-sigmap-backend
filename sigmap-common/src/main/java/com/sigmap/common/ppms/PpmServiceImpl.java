package com.sigmap.common.ppms;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.Constants;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PpmServiceImpl implements PpmService {
    private final PpmRepository repository;
    private final AcRepository acRepository;
    private final RealisationRepository realisationRepository;
    private final RefParamRepository refParamRepository;
    private final DocsRepository docsRepository;
    private final RealisationService realisationService;
    private final LotRepository lotRepository;

    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;
    private final DocsService docsService;
    private final AcService acService;

    /**
     * Constructor.
     *
     * @param repository             ppm repository
     *                               //TODO
     *                               //     * @param acService ac service
     * @param realisationRepository
     * @param alfrescoService
     * @param documentCommuneService
     * @param docsService
     * @param acService
     */
    public PpmServiceImpl(PpmRepository repository, AcRepository acRepository, RealisationRepository realisationRepository, RefParamRepository refParamRepository, DocsRepository docsRepository, RealisationService realisationService, LotRepository lotRepository, AlfrescoService alfrescoService, DocumentCommuneService documentCommuneService, DocsService docsService, AcService acService) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.realisationRepository = realisationRepository;
        this.refParamRepository = refParamRepository;
        this.docsRepository = docsRepository;
        this.realisationService = realisationService;
        this.lotRepository = lotRepository;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.docsService = docsService;
        this.acService = acService;
    }

    @Override
    public List<Ppm> findAll() {
        Specification<Ppm> spec = PpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Ppm> findAllDeleted(Specification<Ppm> spec, Pageable pageable) {
        Specification<Ppm> deletedSpec = PpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }



    @Override
    public Page<Ppm> findAllDeleted(Pageable pageable) {
        Specification<Ppm> spec = PpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Ppm> findDeletedById(Integer ppmId, Integer ppmVer) {
        Specification<Ppm> specification = PpmSpecifications.id(new CompositeId(ppmId, ppmVer))
                .and(PpmSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ppm> findOne(Specification<Ppm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Ppm> findById(Integer ppmId, Integer ppmVer) {
        return repository.findById(new CompositeId(ppmId, ppmVer));
    }

    @Override
    public Optional<Ppm> findLatestById(Integer ppmId) {
        // Find the maximum version for this PPM ID
        Integer maxVersion = repository.findMaxVersionById(ppmId);

        if (maxVersion == null) {
            // No versions found
            return Optional.empty();
        }

        // Return the PPM with the latest version
        return repository.findById(new CompositeId(ppmId, maxVersion));
    }

    @Override
    public Ppm save(Ppm ppm) {
        return repository.save(ppm);
    }

    @Transactional
    @Override
    public Ppm create(PpmRequest request) {
        Integer id = request.getId();
        if (id == null) {
            Integer maxId = repository.findMaxId();
            id = (maxId != null) ? maxId + 1 : 1;
            request.setId(id);
        }

        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        Ppm ppm = new Ppm();
        ppm.setId(id);
        ppm.setVersion(version);
        ppm.setRef(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setAnnee(request.getAnnee());
        ppm.setBudgetEstime(request.getBudgetEstime());

        // Set status if provided
        if (request.getStatus() != null) {
            ppm.setStatus(refParamRepository.findById(request.getStatus().getId())
                    .orElseThrow(() -> new RuntimeException("Status with ID " + request.getStatus().getId() + " not found")));
        }

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            ppm.setAc(ac);
        }

        // Set empty realisations and docs initially
        ppm.setRealisations(new ArrayList<>());
        ppm.setDocs(new ArrayList<>());

        // Save Ppm without associations
        Ppm savedPpm = repository.save(ppm);

        // Create and set realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();

            for (RealisationRequest realisationRequest : request.getRealisations()) {
                // Create a new Realisation
                Realisation realisation = new Realisation();

                // Generate ID if not provided
                Integer realisationId = realisationRequest.getId();
                if (realisationId == null) {
                    Integer maxRealisationId = realisationRepository.findMaxId();
                    realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;
                }

                // Always use version 1 for newly created realisations
                realisation.setId(realisationId);
                realisation.setVersion(1);

                // Set all fields from the request
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setService(realisationRequest.getService());
                realisation.setDirection(realisationRequest.getDirection());
                realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                realisation.setMonais(realisationRequest.getMonais());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                realisation.setModeSelection(realisationRequest.getModeSelection());
                realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                // Set up the relationship with PPM
                List<Ppm> ppmList = new ArrayList<>();
                ppmList.add(savedPpm);
                realisation.setPpms(ppmList);

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    Set<Lot> lots = new HashSet<>();

                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        Lot lot = new Lot();

                        // Generate ID if not provided
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            Integer maxLotId = lotRepository.findMaxId();
                            lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                        }

                        // Always use version 1 for newly created lots
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());

                        // Save the lot
                        lot = lotRepository.save(lot);
                        lots.add(lot);
                    }

                    realisation.setLots(lots);
                }

                // Save the realisation
                realisation = realisationRepository.save(realisation);
                realisations.add(realisation);
            }

            // Update the PPM with the list of realisations
            savedPpm.setRealisations(realisations);
        }

        // Save again to persist associations
        return repository.save(savedPpm);
    }

    @Override
    public Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request) {
        return update(ppmId, ppmVer, request, false);
    }

    @Override
    public Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request, boolean isUpdatable) {
        // Find the existing PPM
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);
        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVer + " was not found in the database.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException("Cannot update a deleted Ppm with ID " + ppmId + " and version " + ppmVer);
        }

        if (isUpdatable) {
            // Create a new version
            Ppm newPpm = new Ppm();
            newPpm.setId(ppmId);
            newPpm.setVersion(ppmVer + 1); // Increment version
            // Set fields from PpmRequest, falling back to existing values if null
            newPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
            newPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
            newPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
            newPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
            newPpm.setIsDeleted(false);

            // Update status if provided
            if (request.getStatus() != null) {
                newPpm.setStatus(refParamRepository.findById(request.getStatus().getId())
                        .orElseThrow(() -> new RuntimeException("Status with ID " + request.getStatus().getId() + " not found")));
            } else {
                newPpm.setStatus(existingPpm.getStatus()); // Preserve existing status
            }

            if (request.getAcId() != null) {
                Ac ac = acRepository.findById(request.getAcId())
                        .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
                newPpm.setAc(ac);
            } else {
                newPpm.setAc(existingPpm.getAc()); // Preserve existing Ac
            }

            return repository.save(newPpm);
        } else {
            // Update existing version
            existingPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
            existingPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
            existingPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
            existingPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());

            // Update status if provided
            if (request.getStatus() != null) {
                existingPpm.setStatus(refParamRepository.findById(request.getStatus().getId())
                        .orElseThrow(() -> new RuntimeException("Status with ID " + request.getStatus().getId() + " not found")));
            } else {
                existingPpm.setStatus(existingPpm.getStatus()); // Preserve existing status
            }

            if (request.getAcId() != null) {
                Ac ac = acRepository.findById(request.getAcId())
                        .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
                existingPpm.setAc(ac);
            } else {
                existingPpm.setAc(existingPpm.getAc()); // Preserve existing Ac
            }

            return repository.save(existingPpm);
        }
    }

    @Override
    public void delete(Integer ppmId, Integer ppmVer) {
        repository.softDelete(ppmId, ppmVer);
    }


    @Override
    public Ppm updateStatus(Integer ppmId, Integer ppmVer, Integer statusId) {
        Optional<Ppm> ppmOpt = findById(ppmId, ppmVer);
        if (ppmOpt.isEmpty()) {
            return null;
        }

        Ppm existingPpm = ppmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            return null;
        }

        PpmRequest request = new PpmRequest();
        request.setRef(existingPpm.getRef());
        request.setNom(existingPpm.getNom());
        request.setAnnee(existingPpm.getAnnee());
        request.setBudgetEstime(existingPpm.getBudgetEstime());
        if (existingPpm.getAc() != null) {
            request.setAcId(existingPpm.getAc().getId());
        }

        // Fetch RefParam for the statusId
        RefParam status = refParamRepository.findById(statusId)
                .orElse(null);
        if (status == null) {
            return null;
        }
        request.setStatus(status);

        try {
            return update(ppmId, ppmVer, request);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Updates a PPM by creating a new version with the updated data.
     * The old version is preserved in the database.
     * <p>
     * This method handles hierarchical versioning:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes, both the Realisation and PPM versions are incremented
     * <p>
     * This method handles various Realisation scenarios:
     * - Existing Realisation with ID and version: uses that specific version
     * - Existing Realisation with only ID: finds and uses the latest version
     * - New Realisation without ID: creates a new Realisation
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the current version of the PPM
     * @param request the request containing the updated data
     * @return the newly created version of the PPM
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    @Transactional
    @Override
    public Ppm updateWithVersioning(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        logUpdateRequest(ppmId, ppmVer, request);
        if (isDuplicateRequest(ppmId, ppmVer, request)) {
            return getExistingPpm(ppmId, ppmVer);
        }
        storeRequestHash(ppmId, ppmVer, request);

        Ppm existingPpm = getExistingPpm(ppmId, ppmVer);
        Ppm tempPpm = buildTempPpm(existingPpm, request);
        boolean hasDirectChanges = !arePpmFieldsIdentical(existingPpm, tempPpm);

        List<Realisation> updatedRealisations = new ArrayList<>();
        boolean realisationsChanged = processRealisationsWithLotLogic(existingPpm, request, updatedRealisations);

        List<Docs> updatedDocs = new ArrayList<>();
        boolean documentsChanged = processDocuments(existingPpm, request, updatedDocs);

        boolean shouldCreateNewVersion = hasDirectChanges || realisationsChanged || documentsChanged;
        logVersionDecision(shouldCreateNewVersion, hasDirectChanges, realisationsChanged, documentsChanged);
        if (!shouldCreateNewVersion) {
            return existingPpm;
        }
        return createAndSaveNewPpmVersion(ppmId, ppmVer, tempPpm, updatedRealisations, updatedDocs, existingPpm);
    }

    /**
     * Updates a PPM with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the PPM with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing the updated data
     * @return the newly created version of the PPM
     * @throws RuntimeException if no versions of the PPM exist or if all versions are deleted
     */
    @Transactional
    @Override
    public Ppm updateWithVersioning(Integer ppmId, PpmBaseRequestDto request) {
        // Find the latest version of the PPM
        Integer latestVersion = repository.findMaxVersionById(ppmId);

        if (latestVersion == null) {
            throw new RuntimeException("No versions found for PPM with ID: " + ppmId);
        }

        System.out.println("Found latest version for PPM ID " + ppmId + ": " + latestVersion);

        // Call the version-specific method with the latest version
        return updateWithVersioning(ppmId, latestVersion, request);
    }

    /**
     * Updates a PPM by creating a new version with the updated data including lots.
     * The old version is preserved in the database.
     * <p>
     * This method handles hierarchical versioning with explicit support for Lot entities:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes or is added, both the Realisation and PPM versions are incremented
     * <p>
     * This method is specifically designed to handle requests that include Lot data within Realisations.
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the current version of the PPM
     * @param request the request containing the updated data with Lots
     * @return the newly created version of the PPM
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    @Transactional
    @Override
    public Ppm updateWithVersioningWithLots(Integer ppmId, Integer ppmVer, PpmWithLotsRequestDto request) {
        // Convert PpmWithLotsRequestDto to PpmBaseRequestDto for core functionality
        PpmBaseRequestDto baseRequest = new PpmBaseRequestDto();

        // Copy all basic fields
        baseRequest.setRef(request.getRef());
        baseRequest.setNom(request.getNom());
        baseRequest.setAnnee(request.getAnnee());
        baseRequest.setBudgetEstime(request.getBudgetEstime());
        baseRequest.setStatus(request.getStatus());
        baseRequest.setAcId(request.getAcId());
        baseRequest.setDocIds(request.getDocIds());

        // Get realisations with lots
        List<RealisationWithLotsRequestDto> realisationsWithLots = request.getRealisationsWithLots();

        // Convert RealisationWithLotsRequestDto to RealisationBaseRequestDto
        if (realisationsWithLots != null && !realisationsWithLots.isEmpty()) {
            List<RealisationBaseRequestDto> baseRealisations = new ArrayList<>();

            for (RealisationWithLotsRequestDto realisationWithLots : realisationsWithLots) {
                // Create a new BaseRequestDto and copy fields
                RealisationBaseRequestDto baseRealisation = new RealisationBaseRequestDto();

                // Copy all fields from the WithLots DTO to the Base DTO
                baseRealisation.setId(realisationWithLots.getId());
                baseRealisation.setVersion(realisationWithLots.getVersion());
                baseRealisation.setRealisationEnvisagees(realisationWithLots.getRealisationEnvisagees());
                baseRealisation.setService(realisationWithLots.getService());
                baseRealisation.setDirection(realisationWithLots.getDirection());
                baseRealisation.setSourceFinancement(realisationWithLots.getSourceFinancement());
                baseRealisation.setMonais(realisationWithLots.getMonais());
                baseRealisation.setTypeMarcher(realisationWithLots.getTypeMarcher());
                baseRealisation.setModeSelection(realisationWithLots.getModeSelection());
                baseRealisation.setDatePrevLps(realisationWithLots.getDatePrevLps());
                baseRealisation.setDatePrevAc(realisationWithLots.getDatePrevAc());
                baseRealisation.setDatePrevDp(realisationWithLots.getDatePrevDp());
                baseRealisation.setDatePrevAp(realisationWithLots.getDatePrevAp());
                baseRealisation.setBudgetEstime(realisationWithLots.getBudgetEstime());
                baseRealisation.setIsPublished(realisationWithLots.getIsPublished());
                baseRealisation.setPublishedAt(realisationWithLots.getPublishedAt());
                baseRealisation.setSort(realisationWithLots.getSort());
                baseRealisation.setDocIds(realisationWithLots.getDocIds());

                // Most importantly - set the lots to make sure they're included
                baseRealisation.setLots(realisationWithLots.getLots());

                baseRealisations.add(baseRealisation);
            }

            baseRequest.setRealisations(baseRealisations);
        } else {
            // If no realisationsWithLots provided, just use the regular realisations
            baseRequest.setRealisations(request.getRealisations());
        }

        // Use the existing updateWithVersioning method with the populated base request
        return updateWithVersioning(ppmId, ppmVer, baseRequest);
    }

    /**
     * Updates a PPM by creating a new version with the updated data including lots.
     * Automatically finds and uses the latest version of the PPM.
     * <p>
     * This method handles hierarchical versioning with explicit support for Lot entities:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes or is added, both the Realisation and PPM versions are incremented
     * <p>
     * This method is specifically designed to handle requests that include Lot data within Realisations.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing the updated data with Lots
     * @return the newly created version of the PPM
     * @throws RuntimeException if the PPM is not found or if all versions are deleted
     */
    @Transactional
    @Override
    public Ppm updateWithVersioningWithLots(Integer ppmId, PpmWithLotsRequestDto request) {
        // Find the latest version of the PPM
        Integer latestVersion = repository.findMaxVersionById(ppmId);

        if (latestVersion == null) {
            throw new RuntimeException("No versions found for PPM with ID: " + ppmId);
        }

        System.out.println("Found latest version for PPM ID " + ppmId + ": " + latestVersion);

        // Call the version-specific method with the latest version
        return updateWithVersioningWithLots(ppmId, latestVersion, request);
    }

    /**
     * Helper method to compare two sets of Lots to determine if they are equal.
     *
     * @param set1 First set of Lots
     * @param set2 Second set of Lots
     * @return true if the sets contain the same Lot IDs, false otherwise
     */
    private boolean areLotSetsEqual(Set<Lot> set1, Set<Lot> set2) {
        // Handle null cases
        if (set1 == null && set2 == null) return true;
        if (set1 == null || set2 == null) return false;
        if (set1.size() != set2.size()) return false;

        // Extract IDs for comparison
        Set<Integer> ids1 = set1.stream().map(Lot::getId).collect(Collectors.toSet());
        Set<Integer> ids2 = set2.stream().map(Lot::getId).collect(Collectors.toSet());

        return ids1.equals(ids2);
    }

    // --- Helper Methods for updateWithVersioning ---

    private void logUpdateRequest(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        System.out.println("Updating PPM ID: " + ppmId + ", Current Version: " + ppmVer);
        System.out.println("Request data: " + request);
    }

    private void logVersionDecision(boolean shouldCreate, boolean direct, boolean real, boolean docs) {
        System.out.println("Should create new PPM version: " + shouldCreate);
        System.out.println("  - Direct changes: " + direct);
        System.out.println("  - Realisations changed: " + real);
        System.out.println("  - Documents changed: " + docs);
    }

    private boolean isDuplicateRequest(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        String requestHash = generateRequestHash(request);
        String previousRequestHash = getPreviousRequestHash(ppmId, ppmVer);
        return requestHash != null && requestHash.equals(previousRequestHash);
    }

    private void storeRequestHash(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        String requestHash = generateRequestHash(request);
        storePreviousRequestHash(ppmId, ppmVer, requestHash);
    }

    private Ppm getExistingPpm(Integer ppmId, Integer ppmVer) {
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        return repository.findById(compositeId)
                .orElseThrow(() -> new RuntimeException("PPM with ID " + ppmId + " and version " + ppmVer + " not found"));
    }

    private Ppm buildTempPpm(Ppm existingPpm, PpmBaseRequestDto request) {
        Ppm tempPpm = new Ppm();
        tempPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
        tempPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
        tempPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
        tempPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
        RefParam requestStatus = null;
        if (request.getStatus() != null) {
            requestStatus = refParamRepository.findById(request.getStatus().getId()).orElse(null);
        }
        tempPpm.setStatus(requestStatus != null ? requestStatus : existingPpm.getStatus());
        Ac requestAc = null;
        if (request.getAcId() != null) {
            requestAc = acRepository.findById(request.getAcId()).orElse(null);
        }
        tempPpm.setAc(requestAc != null ? requestAc : existingPpm.getAc());
        return tempPpm;
    }

    /**
     * Processes realisations and their lots, preserving the original versioning logic:
     * - Only the PPM version is incremented when Lots are added/removed/changed.
     * - Lot version is incremented if its own fields change.
     * - Realisation version is incremented only if its own fields change.
     *
     * @param existingPpm The existing PPM
     * @param request The update request
     * @param updatedRealisations The list to populate with updated realisations
     * @return true if realisations changed (for PPM versioning), false otherwise
     */
    private boolean processRealisationsWithLotLogic(Ppm existingPpm, PpmBaseRequestDto request, List<Realisation> updatedRealisations) {
        boolean realisationsChanged = false;
        Map<String, Lot> newLotsMap = new HashMap<>();
        Set<Integer> processedRealisationIds = new HashSet<>();
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            for (RealisationBaseRequestDto realisationDto : request.getRealisations()) {
                try {
                    if (realisationDto.getId() != null) {
                        processedRealisationIds.add(realisationDto.getId());
                        Integer versionToUse = realisationDto.getVersion();
                        if (versionToUse == null) {
                            Integer maxVersion = realisationRepository.findMaxVersionById(realisationDto.getId());
                            if (maxVersion != null) {
                                versionToUse = maxVersion;
                            } else {
                                throw new RuntimeException("No versions found for Realisation with ID: " + realisationDto.getId());
                            }
                        }
                        CompositeId realisationId = new CompositeId(realisationDto.getId(), versionToUse);
                        Realisation existingRealisation = realisationRepository.findById(realisationId)
                                .orElseThrow(() -> new RuntimeException("Realisation not found with ID: " + realisationId.getId() + " and version: " + realisationId.getVersion()));
                        boolean updateNeeded = realisationNeedsUpdate(existingRealisation, realisationDto);
                        if (updateNeeded) {
                            Realisation updatedRealisation = realisationService.updateWithVersioning(
                                    realisationDto.getId(), versionToUse, realisationDto);
                            updatedRealisations.add(updatedRealisation);
                            realisationsChanged = true;
                        } else {
                            // Use the existing Realisation as is, but update its lots if needed
                            Set<Lot> updatedLots = processLotsForRealisation(existingRealisation, realisationDto, newLotsMap);
                            boolean lotChanges = lotsChanged(existingRealisation, realisationDto, updatedLots);
                            if (lotChanges) {
                                existingRealisation.setLots(updatedLots);
                                realisationsChanged = true;
                            }
                            updatedRealisations.add(existingRealisation);
                        }
                    } else {
                        RealisationRequest newRealisationRequest = mapToRealisationRequest(realisationDto);
                        Realisation newRealisation = realisationService.create(newRealisationRequest);
                        updatedRealisations.add(newRealisation);
                        realisationsChanged = true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process Realisation: " + e.getMessage(), e);
                }
            }
            List<Realisation> existingRealisations = existingPpm.getRealisations();
            if (existingRealisations != null) {
                for (Realisation existingRealisation : existingRealisations) {
                    if (!processedRealisationIds.contains(existingRealisation.getId())) {
                        existingRealisation.setIsDeleted(true);
                        realisationRepository.save(existingRealisation);
                        realisationsChanged = true;
                    }
                }
            }
            if (!realisationsChanged && existingPpm.getRealisations() != null) {
                if (existingPpm.getRealisations().size() != updatedRealisations.size()) {
                    realisationsChanged = true;
                } else {
                    Map<Integer, Integer> existingRealisationVersions = existingPpm.getRealisations().stream()
                            .collect(Collectors.toMap(Realisation::getId, Realisation::getVersion, (a, b) -> a));
                    for (Realisation updatedRealisation : updatedRealisations) {
                        Integer existingVersion = existingRealisationVersions.get(updatedRealisation.getId());
                        if (existingVersion == null || !existingVersion.equals(updatedRealisation.getVersion())) {
                            realisationsChanged = true;
                            break;
                        }
                    }
                }
            }
        } else if (existingPpm.getRealisations() != null && !existingPpm.getRealisations().isEmpty()) {
            realisationsChanged = true;
        }
        return realisationsChanged;
    }

    /**
     * Checks if a lot is being replaced by a new version in the request.
     *
     * @param requestLots List of lot DTOs from the request
     * @param existingLot The existing lot to check
     * @return true if the lot is being replaced, false otherwise
     */
    private boolean isLotBeingReplaced(Set<LotResponse> requestLots, Lot existingLot) {
        if (requestLots == null || existingLot == null) return false;

        for (LotResponse lotDto : requestLots) {
            // If the DTO has the same ID but a different description, it's replacing the existing lot
            if (lotDto.getId() != null && lotDto.getId().equals(existingLot.getId()) &&
                    lotDto.getDescription() != null && !lotDto.getDescription().equals(existingLot.getDescription())) {
                System.out.println("Lot " + existingLot.getId() + " description changed: "
                        + existingLot.getDescription() + " -> " + lotDto.getDescription());
                return true;
            }
        }
        return false;
    }

    /**
     * Processes lots for a realisation, preserving the original versioning logic.
     * - Only the PPM version is incremented when Lots are added/removed/changed.
     * - Lot version is incremented if its own fields change.
     * - Lots that are removed are soft deleted (isDeleted=true)
     *
     * @param existingRealisation The existing Realisation
     * @param realisationDto The DTO for the realisation
     * @param newLotsMap Map to track newly created lots
     * @return The updated set of lots
     */
    private Set<Lot> processLotsForRealisation(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, Map<String, Lot> newLotsMap) {
        Set<Lot> updatedLots = new HashSet<>();
        Set<Integer> processedLotIds = new HashSet<>();
        Set<Integer> lotsToSoftDelete = new HashSet<>();

        // If no lots in the request, soft delete all existing lots
        if (realisationDto.getLots() == null || realisationDto.getLots().isEmpty()) {
            if (existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
                for (Lot existingLot : existingRealisation.getLots()) {
                    existingLot.setIsDeleted(true);
                    lotRepository.save(existingLot);
                    System.out.println("Soft deleted lot (empty request): " + existingLot.getId());
                }
            }
            return new HashSet<>();
        }

        // Get existing lot IDs for comparison
        Set<Integer> existingLotIds = existingRealisation.getLots() != null ?
                existingRealisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet()) :
                new HashSet<>();

        // Identify lots that are not in the request (to be soft deleted)
        if (existingRealisation.getLots() != null) {
            for (Lot existingLot : existingRealisation.getLots()) {
                boolean lotInRequest = false;
                for (LotResponse lotDto : realisationDto.getLots()) {
                    if (lotDto.getId() != null && lotDto.getId().equals(existingLot.getId())) {
                        lotInRequest = true;
                        break;
                    }
                }

                if (!lotInRequest) {
                    lotsToSoftDelete.add(existingLot.getId());
                }
            }
        }

        // First, process and keep existing lots not being replaced
        if (existingRealisation.getLots() != null) {
            for (Lot existingLot : existingRealisation.getLots()) {
                // Skip lots that need to be replaced with new versions
                if (isLotBeingReplaced(realisationDto.getLots(), existingLot)) {
                    System.out.println("Skipping lot " + existingLot.getId() + " as it will be replaced");
                    continue;
                }

                // If the lot is not in the request at all, soft delete it
                boolean lotInRequest = false;
                for (LotResponse lotDto : realisationDto.getLots()) {
                    if (lotDto.getId() != null && lotDto.getId().equals(existingLot.getId())) {
                        lotInRequest = true;
                        break;
                    }
                }

                if (!lotInRequest) {
                    System.out.println("Lot " + existingLot.getId() + " not in request, will be soft deleted");
                    continue;
                }

                updatedLots.add(existingLot);
                processedLotIds.add(existingLot.getId());
                System.out.println("Keeping existing lot: " + existingLot.getId() + ", version: " + existingLot.getVersion());
            }
        }

        // Process all lots from the request
        for (LotResponse lotDto : realisationDto.getLots()) {
            // Process new lots (without IDs)
            if (lotDto.getId() == null) {
                String lotHash = lotDto.getDescription();

                // Check if we already created this lot in this transaction
                if (newLotsMap.containsKey(lotHash)) {
                    Lot existingLot = newLotsMap.get(lotHash);
                    updatedLots.add(existingLot);
                    processedLotIds.add(existingLot.getId());
                    System.out.println("Reusing newly created lot: " + existingLot.getId());
                    continue;
                }

                // Check if a lot with the same description already exists
                boolean lotExists = false;
                if (existingRealisation.getLots() != null) {
                    for (Lot existingLot : existingRealisation.getLots()) {
                        if (existingLot.getDescription() != null &&
                                existingLot.getDescription().equals(lotDto.getDescription())) {
                            updatedLots.add(existingLot);
                            processedLotIds.add(existingLot.getId());
                            lotExists = true;
                            System.out.println("Using existing lot with matching description: " + existingLot.getId());
                            break;
                        }
                    }
                }

                if (lotExists) {
                    continue;
                }

                // Create a new lot
                Lot newLot = new Lot();
                newLot.setDescription(lotDto.getDescription());
                newLot.setVersion(1);  // First version
                newLot.setIsDeleted(false);
                Integer maxLotId = lotRepository.findMaxId();
                Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                newLot.setId(lotId);

                Lot savedLot = lotRepository.save(newLot);
                updatedLots.add(savedLot);
                processedLotIds.add(savedLot.getId());
                newLotsMap.put(lotHash, savedLot);
                System.out.println("Created new lot: " + savedLot.getId() + " with description: " + savedLot.getDescription());
            }
            // Process existing lots (with IDs)
            else {
                // Skip if we've already processed this lot
                if (processedLotIds.contains(lotDto.getId())) {
                    System.out.println("Skipping duplicate lot ID: " + lotDto.getId());
                    continue;
                }

                Integer lotId = lotDto.getId();
                Integer lotVersion = lotDto.getVersion();

                // Find latest version if not specified
                if (lotVersion == null) {
                    lotVersion = lotRepository.findMaxVersionById(lotId);
                    if (lotVersion == null) {
                        System.out.println("Lot ID " + lotId + " not found, skipping");
                        continue;
                    }
                }

                // Get the existing lot
                Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                if (existingLotOpt.isPresent()) {
                    Lot existingLot = existingLotOpt.get();

                    // If description changed, create a new version
                    if (lotDto.getDescription() != null &&
                            !lotDto.getDescription().equals(existingLot.getDescription())) {
                        Lot newLot = new Lot();
                        newLot.setId(existingLot.getId());
                        newLot.setVersion(existingLot.getVersion() + 1);
                        newLot.setDescription(lotDto.getDescription());
                        newLot.setIsDeleted(false);

                        Lot savedLot = lotRepository.save(newLot);
                        updatedLots.add(savedLot);
                        processedLotIds.add(savedLot.getId());
                        System.out.println("Created new version of lot: " + savedLot.getId() +
                                ", version: " + savedLot.getVersion() +
                                ", description changed: " + existingLot.getDescription() +
                                " -> " + savedLot.getDescription());
                    } else {
                        // No changes, use existing lot
                        updatedLots.add(existingLot);
                        processedLotIds.add(existingLot.getId());
                        System.out.println("Using existing lot without changes: " + existingLot.getId() +
                                ", version: " + existingLot.getVersion());
                    }
                } else {
                    System.out.println("Lot not found: id=" + lotId + ", version=" + lotVersion);
                }
            }
        }

        // Soft delete lots that were removed
        for (Integer lotId : lotsToSoftDelete) {
            Integer lotVersion = lotRepository.findMaxVersionById(lotId);
            if (lotVersion != null) {
                Optional<Lot> lotToDelete = lotRepository.findById(new CompositeId(lotId, lotVersion));
                if (lotToDelete.isPresent()) {
                    Lot lot = lotToDelete.get();
                    lot.setIsDeleted(true);
                    lotRepository.save(lot);
                    System.out.println("Soft deleted lot: " + lotId + ", version: " + lotVersion);
                }
            }
        }

        return updatedLots;
    }

    /**
     * Checks if the lots have changed for a realisation (add/remove/change).
     * @return true if lots have changed, false otherwise
     */
    private boolean lotsChanged(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, Set<Lot> updatedLots) {
        // If lots collection is empty in both request and existing, no change
        if ((realisationDto.getLots() == null || realisationDto.getLots().isEmpty()) &&
                (existingRealisation.getLots() == null || existingRealisation.getLots().isEmpty())) {
            return false;
        }

        // If lots are in request but not in existing, or vice versa, change detected
        if ((realisationDto.getLots() == null || realisationDto.getLots().isEmpty()) &&
                (existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty())) {
            System.out.println("Lots removed from realisation");
            return true;
        }

        if ((realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) &&
                (existingRealisation.getLots() == null || existingRealisation.getLots().isEmpty())) {
            System.out.println("Lots added to realisation");
            return true;
        }

        // Compare lot IDs between request and existing
        Set<Integer> requestLotIds = realisationDto.getLots().stream()
                .filter(lotDto -> lotDto.getId() != null)
                .map(LotResponse::getId)
                .collect(Collectors.toSet());

        Set<Integer> existingLotIds = existingRealisation.getLots().stream()
                .map(Lot::getId)
                .collect(Collectors.toSet());

        // Add IDs of new lots (without ID in request)
        Set<Integer> newLotIds = updatedLots.stream()
                .map(Lot::getId)
                .filter(id -> !existingLotIds.contains(id))
                .collect(Collectors.toSet());

        requestLotIds.addAll(newLotIds);

        // Check for lot ID differences (additions/removals)
        if (!requestLotIds.equals(existingLotIds)) {
            System.out.println("Lot IDs changed: existing=" + existingLotIds + ", request=" + requestLotIds);
            return true;
        }

        // Check for lot version changes (description updates)
        Map<Integer, Integer> existingLotVersions = existingRealisation.getLots().stream()
                .collect(Collectors.toMap(Lot::getId, Lot::getVersion));

        Map<Integer, Integer> updatedLotVersions = updatedLots.stream()
                .collect(Collectors.toMap(Lot::getId, Lot::getVersion));

        for (Integer lotId : existingLotIds) {
            Integer existingVersion = existingLotVersions.get(lotId);
            Integer updatedVersion = updatedLotVersions.get(lotId);

            if (updatedVersion != null && !updatedVersion.equals(existingVersion)) {
                System.out.println("Lot " + lotId + " version changed: " + existingVersion + " -> " + updatedVersion);
                return true;
            }
        }

        return false;
    }

    /**
     * Processes documents and determines if they have changed.
     * @return true if documents have changed, false otherwise
     */
    private boolean processDocuments(Ppm existingPpm, PpmBaseRequestDto request, List<Docs> updatedDocs) {
        boolean documentsChanged = false;
        if (request.getDocIds() != null) {
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            Set<Integer> existingDocIds = existingPpm.getDocs() != null ?
                    existingPpm.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                    new HashSet<>();
            if (!requestDocIds.equals(existingDocIds)) {
                updatedDocs.clear();
                for (Integer docId : request.getDocIds()) {
                    Docs doc = docsRepository.findById(docId)
                            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                    updatedDocs.add(doc);
                }
                documentsChanged = true;
            } else {
                updatedDocs.clear();
                if (existingPpm.getDocs() != null) {
                    updatedDocs.addAll(existingPpm.getDocs());
                }
            }
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            documentsChanged = true;
            updatedDocs.clear();
        } else {
            updatedDocs.clear();
        }
        return documentsChanged;
    }

    /**
     * Creates and saves a new PPM version with all relationships established.
     *
     * @param ppmId The PPM ID
     * @param ppmVer The current PPM version (to be incremented)
     * @param tempPpm The temporary PPM with updated fields
     * @param updatedRealisations The list of updated/new realisations
     * @param updatedDocs The list of updated/new documents
     * @param existingPpm The existing PPM for reference
     * @return The newly created PPM version
     */
    private Ppm createAndSaveNewPpmVersion(Integer ppmId, Integer ppmVer, Ppm tempPpm,
                                           List<Realisation> updatedRealisations, List<Docs> updatedDocs, Ppm existingPpm) {
        // Create a new PPM version
        System.out.println("Creating new PPM version: " + (ppmVer + 1));
        Ppm newPpm = new Ppm();
        newPpm.setId(ppmId);
        newPpm.setVersion(ppmVer + 1);

        // Copy properties from temp PPM
        newPpm.setRef(tempPpm.getRef());
        newPpm.setNom(tempPpm.getNom());
        newPpm.setAnnee(tempPpm.getAnnee());
        newPpm.setBudgetEstime(tempPpm.getBudgetEstime());
        newPpm.setStatus(tempPpm.getStatus());
        newPpm.setAc(tempPpm.getAc());

        // Initialize empty collections to avoid NPEs
        newPpm.setRealisations(new ArrayList<>());
        newPpm.setDocs(new ArrayList<>());

        // Save the new PPM entity first before establishing relationships
        newPpm = repository.save(newPpm);
        System.out.println("Saved new PPM: " + newPpm.getId() + " version " + newPpm.getVersion());

        // Set up the relationships for Realisations
        if (!updatedRealisations.isEmpty()) {
            // Update the inverse side of the relationship first
            for (Realisation realisation : updatedRealisations) {
                List<Ppm> ppmList = realisation.getPpms();
                if (ppmList == null) {
                    ppmList = new ArrayList<>();
                    realisation.setPpms(ppmList);
                }
                if (!ppmList.contains(newPpm)) {
                    ppmList.add(newPpm);
                }
                // Save the realisation with updated relationship
                realisation = realisationRepository.save(realisation);
            }

            // Now set the realisations on the PPM
            newPpm.setRealisations(updatedRealisations);
        } else if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealization : existingPpm.getRealisations()) {
                List<Ppm> ppmList = existingRealization.getPpms();
                if (ppmList == null) {
                    ppmList = new ArrayList<>();
                    existingRealization.setPpms(ppmList);
                }
                if (!ppmList.contains(newPpm)) {
                    ppmList.add(newPpm);
                }
                // Save the realisation with updated relationship
                existingRealization = realisationRepository.save(existingRealization);
            }

            newPpm.setRealisations(new ArrayList<>(existingPpm.getRealisations()));
        }

        // Handle documents similarly
        if (!updatedDocs.isEmpty()) {
            // Update the inverse side of the relationship first
            for (Docs doc : updatedDocs) {
                List<Ppm> ppmList = doc.getPpms();
                if (ppmList == null) {
                    ppmList = new ArrayList<>();
                    doc.setPpms(ppmList);
                }
                if (!ppmList.contains(newPpm)) {
                    ppmList.add(newPpm);
                }
                // Save the doc with updated relationship
                docsRepository.save(doc);
            }

            // Now set the docs on the PPM
            newPpm.setDocs(updatedDocs);
        } else if (existingPpm.getDocs() != null) {
            for (Docs existingDoc : existingPpm.getDocs()) {
                List<Ppm> ppmList = existingDoc.getPpms();
                if (ppmList == null) {
                    ppmList = new ArrayList<>();
                    existingDoc.setPpms(ppmList);
                }
                if (!ppmList.contains(newPpm)) {
                    ppmList.add(newPpm);
                }
                // Save the doc with updated relationship
                docsRepository.save(existingDoc);
            }

            newPpm.setDocs(new ArrayList<>(existingPpm.getDocs()));
        }

        // Save and return the updated PPM with all relationships
        return repository.save(newPpm);
    }

    /**
     * Maps a RealisationBaseRequestDto to a RealisationRequest.
     *
     * @param dto The DTO to map
     * @return The mapped RealisationRequest
     */
    private RealisationRequest mapToRealisationRequest(RealisationBaseRequestDto dto) {
        RealisationRequest request = new RealisationRequest();
        request.setRealisationEnvisagees(dto.getRealisationEnvisagees());
        request.setService(dto.getService());
        request.setDirection(dto.getDirection());
        request.setSourceFinancement(dto.getSourceFinancement());
        request.setMonais(dto.getMonais());
        request.setTypeMarcher(dto.getTypeMarcher());
        request.setModeSelection(dto.getModeSelection());
        request.setDatePrevLps(dto.getDatePrevLps());
        request.setDatePrevAc(dto.getDatePrevAc());
        request.setDatePrevDp(dto.getDatePrevDp());
        request.setDatePrevAp(dto.getDatePrevAp());
        request.setBudgetEstime(dto.getBudgetEstime());
        request.setIsPublished(dto.getIsPublished());
        request.setPublishedAt(dto.getPublishedAt());
        request.setSort(dto.getSort());
        request.setDocIds(dto.getDocIds());

        // Set Lots from the DTO
        if (dto.getLots() != null && !dto.getLots().isEmpty()) {
            request.setLots(dto.getLots());
        }

        return request;
    }

    /**
     * Determines if a realisation needs to be updated based on changes in its fields.
     * This does not consider changes to lots, which are handled separately.
     *
     * @param existingRealisation The existing realisation
     * @param realisationDto The DTO with potential changes
     * @return true if fields have changed requiring a new version, false otherwise
     */
    private boolean realisationNeedsUpdate(Realisation existingRealisation, RealisationBaseRequestDto realisationDto) {
        // Check basic text field
        if (realisationDto.getRealisationEnvisagees() != null &&
                !realisationDto.getRealisationEnvisagees().equals(existingRealisation.getRealisationEnvisagees())) {
            return true;
        }

        // Check service
        if ((realisationDto.getService() != null && existingRealisation.getService() == null) ||
                (realisationDto.getService() == null && existingRealisation.getService() != null) ||
                (realisationDto.getService() != null && existingRealisation.getService() != null &&
                        !realisationDto.getService().equals(existingRealisation.getService()))) {
            return true;
        }

        // Check direction
        if ((realisationDto.getDirection() != null && existingRealisation.getDirection() == null) ||
                (realisationDto.getDirection() == null && existingRealisation.getDirection() != null) ||
                (realisationDto.getDirection() != null && existingRealisation.getDirection() != null &&
                        !realisationDto.getDirection().equals(existingRealisation.getDirection()))) {
            return true;
        }

        // Check budget
        if ((realisationDto.getBudgetEstime() != null && existingRealisation.getBudgetEstime() == null) ||
                (realisationDto.getBudgetEstime() == null && existingRealisation.getBudgetEstime() != null) ||
                (realisationDto.getBudgetEstime() != null && existingRealisation.getBudgetEstime() != null &&
                        !realisationDto.getBudgetEstime().equals(existingRealisation.getBudgetEstime()))) {
            return true;
        }

        // Check dates
        if (areDatesChanged(realisationDto.getDatePrevLps(), existingRealisation.getDatePrevLps()) ||
                areDatesChanged(realisationDto.getDatePrevAc(), existingRealisation.getDatePrevAc()) ||
                areDatesChanged(realisationDto.getDatePrevDp(), existingRealisation.getDatePrevDp()) ||
                areDatesChanged(realisationDto.getDatePrevAp(), existingRealisation.getDatePrevAp())) {
            return true;
        }

        // For other reference fields like sourceFinancement, monais, typeMarcher, modeSelection
        // We'd need to compare IDs if these are entities, or values if these are direct values

        return false;
    }

    /**
     * Helper to check if dates differ
     */
    private boolean areDatesChanged(LocalDate date1, LocalDate date2) {
        if (date1 == null && date2 == null) return false;
        if (date1 == null || date2 == null) return true;
        return !date1.equals(date2);
    }

    /**
     * Check if two PPMs have identical direct fields.
     *
     * @param ppm1 First PPM
     * @param ppm2 Second PPM
     * @return true if all direct fields are identical, false otherwise
     */
    private boolean arePpmFieldsIdentical(Ppm ppm1, Ppm ppm2) {
        if (ppm1 == null || ppm2 == null) return false;

        // Compare ref
        if ((ppm1.getRef() == null && ppm2.getRef() != null) ||
                (ppm1.getRef() != null && !ppm1.getRef().equals(ppm2.getRef()))) {
            System.out.println("Ref change: " + ppm1.getRef() + " -> " + ppm2.getRef());
            return false;
        }

        // Compare nom
        if ((ppm1.getNom() == null && ppm2.getNom() != null) ||
                (ppm1.getNom() != null && !ppm1.getNom().equals(ppm2.getNom()))) {
            System.out.println("Nom change: " + ppm1.getNom() + " -> " + ppm2.getNom());
            return false;
        }

        // Compare annee
        if ((ppm1.getAnnee() == null && ppm2.getAnnee() != null) ||
                (ppm1.getAnnee() != null && !ppm1.getAnnee().equals(ppm2.getAnnee()))) {
            System.out.println("Annee change: " + ppm1.getAnnee() + " -> " + ppm2.getAnnee());
            return false;
        }

        // Compare budgetEstime
        if ((ppm1.getBudgetEstime() == null && ppm2.getBudgetEstime() != null) ||
                (ppm1.getBudgetEstime() != null && !ppm1.getBudgetEstime().equals(ppm2.getBudgetEstime()))) {
            System.out.println("Budget change: " + ppm1.getBudgetEstime() + " -> " + ppm2.getBudgetEstime());
            return false;
        }

        // Compare status
        if ((ppm1.getStatus() == null && ppm2.getStatus() != null) ||
                (ppm1.getStatus() != null && ppm2.getStatus() != null && !ppm1.getStatus().getId().equals(ppm2.getStatus().getId()))) {
            System.out.println("Status change: " +
                    (ppm1.getStatus() != null ? ppm1.getStatus().getId() : "null") + " -> " +
                    (ppm2.getStatus() != null ? ppm2.getStatus().getId() : "null"));
            return false;
        }

        // Compare AC
        Integer ac1Id = getAcId(ppm1.getAc());
        Integer ac2Id = getAcId(ppm2.getAc());
        if ((ac1Id == null && ac2Id != null) ||
                (ac1Id != null && !ac1Id.equals(ac2Id))) {
            System.out.println("AC change: " + ac1Id + " -> " + ac2Id);
            return false;
        }

        return true;
    }

    /**
     * Safely extracts the ID from a RefParam object.
     *
     * @param refParam The RefParam object
     * @return The ID of the RefParam or null if the RefParam is null
     */
    private Integer getRefParamId(RefParam refParam) {
        return refParam != null ? refParam.getId() : null;
    }

    /**
     * Safely extracts the ID from an Ac object.
     *
     * @param ac The Ac object
     * @return The ID of the Ac or null if the Ac is null
     */
    private Integer getAcId(Ac ac) {
        return ac != null ? ac.getId() : null;
    }

    /**
     * Generates a hash string representing the request for duplicate detection.
     * This is a simple implementation that can be enhanced for better duplicate detection.
     *
     * @param request The request to hash
     * @return A string hash of the request content
     */
    private String generateRequestHash(PpmBaseRequestDto request) {
        if (request == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ref:").append(request.getRef()).append(";");
        sb.append("nom:").append(request.getNom()).append(";");
        sb.append("annee:").append(request.getAnnee()).append(";");
        sb.append("budgetEstime:").append(request.getBudgetEstime()).append(";");

        if (request.getStatus() != null) {
            sb.append("statusId:").append(request.getStatus().getId()).append(";");
        }

        if (request.getAcId() != null) {
            sb.append("acId:").append(request.getAcId()).append(";");
        }

        if (request.getDocIds() != null) {
            sb.append("docIds:");
            for (Integer docId : request.getDocIds()) {
                sb.append(docId).append(",");
            }
            sb.append(";");
        }

        if (request.getRealisations() != null) {
            for (RealisationBaseRequestDto realisation : request.getRealisations()) {
                sb.append("real[");
                if (realisation.getId() != null) {
                    sb.append("id:").append(realisation.getId()).append(",");
                }
                if (realisation.getVersion() != null) {
                    sb.append("ver:").append(realisation.getVersion()).append(",");
                }
                sb.append("text:").append(realisation.getRealisationEnvisagees()).append(",");

                if (realisation.getLots() != null) {
                    sb.append("lots:[");
                    for (LotResponse lot : realisation.getLots()) {
                        sb.append("{");
                        if (lot.getId() != null) {
                            sb.append("id:").append(lot.getId()).append(",");
                        }
                        if (lot.getVersion() != null) {
                            sb.append("ver:").append(lot.getVersion()).append(",");
                        }
                        sb.append("desc:").append(lot.getDescription());
                        sb.append("},");
                    }
                    sb.append("]");
                }

                sb.append("];");
            }
        }

        return sb.toString();
    }

    // Simple in-memory cache for request hashes
    private static final Map<String, String> requestHashCache = new HashMap<>();

    /**
     * Gets the hash of the previous request for a PPM ID and version.
     *
     * @param ppmId The PPM ID
     * @param ppmVer The PPM version
     * @return The hash string or null if no previous request
     */
    private String getPreviousRequestHash(Integer ppmId, Integer ppmVer) {
        String key = ppmId + "-" + ppmVer;
        return requestHashCache.get(key);
    }

    /**
     * Stores the hash of a request for a PPM ID and version.
     *
     * @param ppmId The PPM ID
     * @param ppmVer The PPM version
     * @param hash The hash string to store
     */
    private void storePreviousRequestHash(Integer ppmId, Integer ppmVer, String hash) {
        String key = ppmId + "-" + ppmVer;
        requestHashCache.put(key, hash);
    }

    /**
     * Creates a new PPM with full support for relationships including documents.
     * This method creates a PPM with its realisations, lots, and documents.
     *
     * @param request the request containing PPM data, realisations, lots, and document files
     * @return the created PPM with all relationships established
     */

    @Transactional
    @Override
    public Ppm createWithRelationships(PpmWithLotsRequestDto request) {
        // Debug output to see what's in the request
        System.out.println("Starting createWithRelationships with request: " + request);
        System.out.println("RealisationsWithLots from request: " + request.getRealisationsWithLots());

        // Generate PPM ID if not provided
        Integer ppmId = repository.findMaxId();
        ppmId = (ppmId != null) ? ppmId + 1 : 1;

        // Always use version 1 for new PPMs
        Integer ppmVersion = 1;

        // Check if PPM already exists
        Optional<Ppm> existingPpm = repository.findById(new CompositeId(ppmId, ppmVersion));
        if (existingPpm.isPresent()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVersion + " already exists.");
        }

        // Create new PPM
        Ppm ppm = new Ppm();
        ppm.setId(ppmId);
        ppm.setVersion(ppmVersion);
        ppm.setRef(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setAnnee(request.getAnnee());
        ppm.setBudgetEstime(request.getBudgetEstime());

        // Set status if provided
        if (request.getStatus() != null) {
            ppm.setStatus(refParamRepository.findById(request.getStatus().getId())
                    .orElseThrow(() -> new RuntimeException("Status with ID " + request.getStatus().getId() + " not found")));
        }

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            ppm.setAc(ac);
        }

        // Set empty collections to avoid NPEs
        ppm.setRealisations(new ArrayList<>());
        ppm.setDocs(new ArrayList<>());

        // Save PPM initially without associations
        Ppm savedPpm = repository.save(ppm);
        System.out.println("Initial save of PPM - ID: " + savedPpm.getId() + ", Version: " + savedPpm.getVersion());

        // Handle documents if provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                doc.getPpms().add(savedPpm);
                docs.add(doc);

                // Save the document with updated relationship
                docsRepository.save(doc);
            }

            // Update PPM with documents
            savedPpm.setDocs(docs);
            System.out.println("Added " + docs.size() + " documents to PPM");
        }

        // Process document files if provided
        if(request.getPpmFiles() != null) {
            try {
                System.out.println("Processing " + request.getPpmFiles().size() + " files for PPM");
                processAndSaveDocuments(request.getPpmFiles(), savedPpm);
                System.out.println("Processed PPM files successfully");
            } catch (IOException e) {
                System.err.println("Error processing PPM files: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        List<Realisation> realisations = new ArrayList<>();

        // Choose which collection of realisations to process, preferring realisationsWithLots
        List<? extends RealisationBaseRequestDto> realisationsToProcess = null;
        if (request.getRealisationsWithLots() != null && !request.getRealisationsWithLots().isEmpty()) {
            realisationsToProcess = request.getRealisationsWithLots();
            System.out.println("Processing realisationsWithLots: " + realisationsToProcess.size() + " items");

            // Log the lots in each realisation
            for (int i = 0; i < realisationsToProcess.size(); i++) {
                RealisationBaseRequestDto r = realisationsToProcess.get(i);
                System.out.println("Realisation " + i + ": " + r);
                if (r.getLots() != null) {
                    System.out.println("  - Has " + r.getLots().size() + " lots");
                    for (LotResponse lot : r.getLots()) {
                        System.out.println("    - Lot: id=" + lot.getId() + ", desc=" + lot.getDescription());
                    }
                } else {
                    System.out.println("  - No lots found");
                }

//                // AUTOMATIC TEST FIX - Add a test lot directly if there are no lots
//                if (r instanceof RealisationWithLotsRequestDto) {
//                    RealisationWithLotsRequestDto rwl = (RealisationWithLotsRequestDto) r;
//                    if (rwl.getLots() == null || rwl.getLots().isEmpty()) {
//                        System.out.println("  - Adding a test lot automatically");
//                        Set<LotResponse> testLots = new HashSet<>();
//                        LotResponse testLot = new LotResponse();
//                        testLot.setDescription("Auto-generated test lot");
//                        testLots.add(testLot);
//                        rwl.setLots(testLots);
//                        System.out.println("  - Test lot added, realisation now has " + rwl.getLots().size() + " lots");
//                    }
//                }
            }
        } else if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            realisationsToProcess = request.getRealisations();
            System.out.println("Processing standard realisations: " + realisationsToProcess.size() + " items");
        }

        // Keep track of newly created lots to detect duplicates
        Map<String, Lot> newLotsMap = new HashMap<>();

        if (realisationsToProcess != null && !realisationsToProcess.isEmpty()) {
            for (RealisationBaseRequestDto realisationRequest : realisationsToProcess) {
                // Create a new Realisation
                Realisation realisation = new Realisation();

                // Generate a new ID for the realisation
                Integer maxRealisationId = realisationRepository.findMaxId();
                Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;

                // Always use version 1 for newly created realisations
                realisation.setId(realisationId);
                realisation.setVersion(1);

                // Set all fields from the request
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setService(realisationRequest.getService());
                realisation.setDirection(realisationRequest.getDirection());
                realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                realisation.setMonais(realisationRequest.getMonais());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                realisation.setModeSelection(realisationRequest.getModeSelection());
                realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                // Set up the relationship with PPM (similar to how it's done in PAA)
                List<Ppm> ppmList = new ArrayList<>();
                ppmList.add(savedPpm);
                realisation.setPpms(ppmList);

                // CRITICAL: Save the realisation FIRST to ensure it has an ID
                realisation = realisationRepository.save(realisation);
                System.out.println("Initially saved realisation ID: " + realisation.getId() + " version: " + realisation.getVersion());

                // Process realisation files AFTER saving the realisation
                if(realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    try {
                        System.out.println("Processing " + realisationRequest.getRealisationFiles().size() +
                                          " files for realisation ID: " + realisation.getId());

                        // Create list to track docs to add to realisation
                        List<Docs> uploadedDocs = new ArrayList<>();

                        // Process each file one by one
                        for (MultipartFile file : realisationRequest.getRealisationFiles()) {
                            if (file == null || file.isEmpty()) {
                                System.out.println("Skipping empty file");
                                continue;
                            }

                            System.out.println("Processing file: " + file.getOriginalFilename() +
                                             ", size: " + file.getSize() +
                                             ", content type: " + file.getContentType());

                            // Try saving the single file
                            List<MultipartFile> singleFileList = new ArrayList<>();
                            singleFileList.add(file);
                            processAndSaveRelisationsDocuments(singleFileList, realisation);
                        }

                        // After processing files, fetch the updated realisation with its documents
                        realisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));

                        System.out.println("After file processing, realisation has " +
                            (realisation.getDocs() != null ? realisation.getDocs().size() : 0) + " documents");

                        // Save the realisation again
                        realisation = realisationRepository.save(realisation);
                    } catch (IOException e) {
                        System.err.println("Error processing realisation files: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                // Process realisation documents if provided
                if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                    List<Docs> docs = new ArrayList<>();
                    for (Integer docId : realisationRequest.getDocIds()) {
                        Docs doc = docsRepository.findById(docId)
                                .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                        // Set up bidirectional relationship
                        if (doc.getRealisations() == null) {
                            doc.setRealisations(new ArrayList<>());
                        }
                        if (!doc.getRealisations().contains(realisation)) {
                            doc.getRealisations().add(realisation);
                        }

                        // Ensure doc is active
                        doc.setIsDeleted(false);
                        docsRepository.save(doc);
                        docs.add(doc);
                    }

                    // Set docs list for new realisation and save
                    if (realisation.getDocs() == null) {
                        realisation.setDocs(docs);
                    } else {
                        realisation.getDocs().addAll(docs);
                    }
                    realisation = realisationRepository.save(realisation);
                    System.out.println("Added " + docs.size() + " documents to realisation");
                }

                // Handle Lots if provided - IMPROVED IMPLEMENTATION
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    System.out.println("Processing " + realisationRequest.getLots().size() + " lots for realisation ID: " + realisation.getId());
                    Set<Lot> lots = new HashSet<>();
                    Set<Integer> processedLotIds = new HashSet<>();

                    // Process each lot in the request
                    for (LotResponse lotDto : realisationRequest.getLots()) {
                        System.out.println("Processing lot: " + lotDto);

                        if (lotDto.getId() == null) {
                            // Handle new lot without ID
                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                            System.out.println("Processing new lot with description: " + lotHash);

                            // Check for duplicate lots in this transaction
                            if (newLotsMap.containsKey(lotHash)) {
                                System.out.println("Using previously created lot for description: " + lotHash);
                                lots.add(newLotsMap.get(lotHash));
                                processedLotIds.add(newLotsMap.get(lotHash).getId());
                                continue;
                            }

                            // Create a new lot
                            System.out.println("Creating new lot with description: " + lotHash);
                            Lot newLot = new Lot();
                            newLot.setDescription(lotDto.getDescription());
                            newLot.setVersion(1); // First version
                            newLot.setIsDeleted(false); // Ensure it's not deleted

                            // Generate new ID if not provided
                            Integer maxLotId = lotRepository.findMaxId();
                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                            newLot.setId(lotId);

                            Lot savedLot = lotRepository.save(newLot);
                            lots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            newLotsMap.put(lotHash, savedLot);
                            System.out.println("Created new Lot with ID: " + savedLot.getId());
                        } else {
                            // Handle existing lot
                            if (processedLotIds.contains(lotDto.getId())) {
                                System.out.println("Skipping duplicate lot ID: " + lotDto.getId());
                                continue;
                            }

                            Integer lotId = lotDto.getId();
                            Integer lotVersion = lotDto.getVersion();

                            // Try to find the latest version if not specified
                            if (lotVersion == null) {
                                lotVersion = lotRepository.findMaxVersionById(lotId);
                                if (lotVersion == null) {
                                    System.out.println("Lot ID " + lotId + " not found, creating new lot");
                                    // Create a new lot with the specified ID
                                    Lot newLot = new Lot();
                                    newLot.setId(lotId);
                                    newLot.setVersion(1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);
                                    Lot savedLot = lotRepository.save(newLot);
                                    lots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    continue;
                                }
                            }

                            // Get the existing lot
                            Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                            if (existingLotOpt.isPresent()) {
                                Lot existingLot = existingLotOpt.get();
                                String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";

                                // If descriptions are different, create a new version
                                if (!requestDesc.equals(existingDesc)) {
                                    Lot newLot = new Lot();
                                    newLot.setId(existingLot.getId());
                                    newLot.setVersion(existingLot.getVersion() + 1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);

                                    Lot savedLot = lotRepository.save(newLot);
                                    lots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
                                } else {
                                    // Use existing lot
                                    lots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                    System.out.println("Using existing Lot: " + existingLot.getId() + ", Version: " + existingLot.getVersion());
                                }
                            } else {
                                // Lot version not found, create new version 1
                                Lot newLot = new Lot();
                                newLot.setId(lotId);
                                newLot.setVersion(1);
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setIsDeleted(false);
                                Lot savedLot = lotRepository.save(newLot);
                                lots.add(savedLot);
                                processedLotIds.add(savedLot.getId());
                                System.out.println("Created new Lot with provided ID: " + savedLot.getId());
                            }
                        }
                    }

                    // Set the lots on the realisation
                    realisation.setLots(lots);
                    System.out.println("Set " + lots.size() + " lots on realisation ID: " + realisation.getId());

                    // Save the realisation with lots
                    realisation = realisationRepository.save(realisation);

                    // Verify the lots were properly saved
                    Realisation verifyRealisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                            .orElseThrow(() -> new RuntimeException("Could not find realisation after saving lots"));

                    System.out.println("After saving, realisation ID: " + verifyRealisation.getId() +
                            " has " + (verifyRealisation.getLots() != null ? verifyRealisation.getLots().size() : 0) +
                            " lots");
                }

                // Add the finished realisation to our list
                realisations.add(realisation);
            }

            // Update the PPM with the list of realisations
            savedPpm.setRealisations(realisations);
            System.out.println("Added " + realisations.size() + " realisations to PPM");
        }

        // Save PPM again to persist all associations
        savedPpm = repository.save(savedPpm);
        System.out.println("Final save of PPM - ID: " + savedPpm.getId() + ", Version: " + savedPpm.getVersion());

        // Verify the final PPM has all associated entities
        System.out.println("Final PPM has: " +
                (savedPpm.getDocs() != null ? savedPpm.getDocs().size() : 0) + " documents, " +
                (savedPpm.getRealisations() != null ? savedPpm.getRealisations().size() : 0) + " realisations");

        // Print realisation details to verify
        if (savedPpm.getRealisations() != null) {
            for (Realisation r : savedPpm.getRealisations()) {
                System.out.println("Realisation ID: " + r.getId() +
                        ", Docs: " + (r.getDocs() != null ? r.getDocs().size() : 0) +
                        ", Lots: " + (r.getLots() != null ? r.getLots().size() : 0));
            }
        }

        return savedPpm;
    }

    private void processAndSaveDocuments(List<MultipartFile> files, Ppm savedPpm) throws IOException {

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    UUID uuid = UUID.randomUUID();
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

                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_PPM + "/%d/%d",
                                savedPpm.getId(), savedPpm.getVersion());

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = "ppm_" + uuid + "_" + extension;
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);
                        if (savedPpm.getDocs() == null) {
                            savedPpm.setDocs(new ArrayList<>());
                        }
                        savedPpm.getDocs().add(document);

                        docsRepository.save(document);
                    } catch (Exception e) {
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private final Object folderCreationLock = new Object();

    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        List<Docs> docs = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        // Add a file counter to ensure unique filenames even with the same UUID
        AtomicInteger fileCounter = new AtomicInteger(0);

        if (files != null && !files.isEmpty()) {
            // Create a completely unique folder name with timestamp to avoid conflicts
            String timestamp = String.valueOf(System.currentTimeMillis());
            // Use parent folder path from Constants but avoid using "REALISATION" directly
             String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
//            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION;
            String folderPath = String.format("%s/REL_%d/%d",
                    baseFolder, savedRealisation.getId(), savedRealisation.getVersion());

            // Create the folder structure first, synchronizing to prevent conflicts
            String folderId;
            synchronized(folderCreationLock) {
                try {
                    // Get or create the folder first, outside the file processing loop
                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
//                    log.info("Created/Retrieved Realisation folder: Path={}, ID={}", folderPath, folderId);
                } catch (Exception e) {
//                    log.error("Failed to create folder: Path={}, Error={}", folderPath, e.getMessage());
                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
                }
            }

            // Now process each file using the already-created folder
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        // Include timestamp and counter to ensure uniqueness
                        String uniqueFileName = String.format("realisation_%s_%s_%d%s",
                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

//                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}",
//                                originalFilename, uniqueFileName, folderPath, folderId);

                        // Upload directly to the already-created folder
                        String alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
//                        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}", alfrescoId, uniqueFileName);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);

                        // Ensure collections are initialized
                        if (document.getRealisations() == null) {
                            document.setRealisations(new ArrayList<>());
                        }
                        if (savedRealisation.getDocs() == null) {
                            savedRealisation.setDocs(new ArrayList<>());
                        }

                        // Set up bidirectional relationship
                        document.getRealisations().add(savedRealisation);
                        savedRealisation.getDocs().add(document);

                        // Save the document
                        docsRepository.save(document);
                        docs.add(document);
                    } catch (Exception e) {
//                        log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }

        // Update the realisation's docs
        savedRealisation.setDocs(docs);
    }

    //TODO: the upload not working if i use this method

//    private void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    UUID uuid = UUID.randomUUID();
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename();
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_REALISATION + "/%d/%d",
//                                savedRealisation.getId(), savedRealisation.getVersion());
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        String uniqueFileName = "real_" + uuid + "_" + extension;
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//
//                        Docs document = new Docs();
//                        document.setName(baseName);
//                        document.setAlfresco_id(alfrescoId);
//                        document.setPathFolder(folderPath);
//                        document.setFileSize(formattedSize);
//                        document.setContentType(detectedMimeType);
//                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
//                        document.setAlfrescoName(uniqueFileName);
//                        if (savedRealisation.getDocs() == null) {
//                            savedRealisation.setDocs(new ArrayList<>());
//                        }
//                        savedRealisation.getDocs().add(document);
//
//                        docsRepository.save(document);
//                    } catch (Exception e) {
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//    }


//    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//        List<Docs> docs = new ArrayList<>();
//        UUID uuid = UUID.randomUUID();
//        // Add a file counter to ensure unique filenames even with the same UUID
//        AtomicInteger fileCounter = new AtomicInteger(0);
//
//        if (files != null && !files.isEmpty()) {
//            // Create a completely unique folder name with timestamp to avoid conflicts
//            String timestamp = String.valueOf(System.currentTimeMillis());
//            // Use parent folder path from Constants but avoid using "REALISATION" directly
//            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
////            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION;
//            String folderPath = String.format("%s/REL_%d/%d_%s",
//                    baseFolder, savedRealisation.getId(), savedRealisation.getVersion(), timestamp);
//
//            // Create the folder structure first, synchronizing to prevent conflicts
//            String folderId;
//            synchronized(folderCreationLock) {
//                try {
//                    // Get or create the folder first, outside the file processing loop
//                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
//                } catch (Exception e) {
//                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
//                }
//            }
//
//            // Now process each file using the already-created folder
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        // Include timestamp and counter to ensure uniqueness
//                        String uniqueFileName = String.format("realisation_%s_%s_%d%s",
//                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}",
//                                originalFilename, uniqueFileName, folderPath, folderId);
//
//                        // Upload directly to the already-created folder
//                        String alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
//                        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}", alfrescoId, uniqueFileName);
//
//                        Docs document = new Docs();
//                        document.setName(baseName);
//                        document.setAlfresco_id(alfrescoId);
//                        document.setPathFolder(folderPath);
//                        document.setFileSize(formattedSize);
//                        document.setContentType(detectedMimeType);
//                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
//                        document.setAlfrescoName(uniqueFileName);
//
//                        // Ensure collections are initialized
//                        if (document.getRealisations() == null) {
//                            document.setRealisations(new ArrayList<>());
//                        }
//                        if (savedRealisation.getDocs() == null) {
//                            savedRealisation.setDocs(new ArrayList<>());
//                        }
//
//                        // Set up bidirectional relationship
//                        document.getRealisations().add(savedRealisation);
//                        savedRealisation.getDocs().add(document);
//
//                        // Save the document
//                        docsRepository.save(document);
//                        docs.add(document);
//                    } catch (Exception e) {
//                        log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//
//        // Update the realisation's docs
//        savedRealisation.setDocs(docs);
//    }


    /**
     * Process and save document files for a PPM.
     *
     * @param files The document files to process
     * @param savedPpm The PPM to associate the documents with
     * @return List of created Doc entities
     * @throws IOException If file processing fails
     */
//    private List<Docs> processAndSaveDocuments(List<MultipartFile> files, Ppm savedPpm) throws IOException {
//        List<Docs> docs = new ArrayList<>();
//
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
//                        String contentType = file.getContentType();
//
//                        // Create a new document
//                        Docs document = new Docs();
//                        document.setName(originalFilename);
//                        document.setContentType(contentType);
//
//                        // You would add file upload logic here, similar to PaaServiceImpl
//                        // For example, upload to Alfresco or other document storage
//
//                        // Set up bidirectional relationship
//                        if (document.getPpms() == null) {
//                            document.setPpms(new ArrayList<>());
//                        }
//                        document.getPpms().add(savedPpm);
//
//                        // Save the document
//                        document = docsRepository.save(document);
//                        docs.add(document);
//                    } catch (Exception e) {
//                        throw new IOException("Failed to process document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//
//        return docs;
//    }

    /**
     * Process and save document files for a Realisation.
     *
     * @param files The document files to process
     * @param savedRealisation The Realisation to associate the documents with
     * @return List of created Doc entities
     * @throws IOException If file processing fails
     */
    private List<Docs> processAndSaveRealisationDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        List<Docs> docs = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
                        String contentType = file.getContentType();

                        // Create a new document
                        Docs document = new Docs();
                        document.setName(originalFilename);
                        document.setContentType(contentType);

                        // You would add file upload logic here, similar to PaaServiceImpl
                        // For example, upload to Alfresco or other document storage

                        // Set up bidirectional relationship
                        if (document.getRealisations() == null) {
                            document.setRealisations(new ArrayList<>());
                        }
                        document.getRealisations().add(savedRealisation);

                        // Save the document
                        document = docsRepository.save(document);
                        docs.add(document);
                    } catch (Exception e) {
                        throw new IOException("Failed to process document: " + e.getMessage(), e);
                    }
                }
            }
        }

        return docs;
    }

    /**
     * Update an existing PPM (always version 1) and all its relationships without versioning.
     * This method updates a PPM along with its realisations, lots, and documents.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing updated PPM data, realisations, lots, and document files
     * @return the updated PPM with all relationships established
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    @Transactional
    @Override
    public Ppm updateGlobalWithoutVersionning(Integer ppmId, PpmWithLotsRequestDto request) {
        // Always use version 1
        Integer ppmVer = 1;

        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);

        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException(
                    "Ppm with ID " + ppmId + " and version " + ppmVer + " was not found in the database.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException(
                    "Cannot update a deleted PPM with ID " + ppmId + " and version " + ppmVer);
        }

        // Update fields of the existing PPM
        existingPpm.setRef(request.getRef());
        existingPpm.setNom(request.getNom());
        existingPpm.setAnnee(request.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime());

        // Set status if provided
        if (request.getStatus() != null) {
            existingPpm.setStatus(refParamRepository.findById(request.getStatus().getId())
                    .orElseThrow(() -> new RuntimeException("Status with ID " + request.getStatus().getId() + " not found")));
        }

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            existingPpm.setAc(ac);
        }

        // Handle document management by comparing existing docs with requested docIds
        if (request.getDocIds() != null) {
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());

            // Get active document IDs
            Set<Integer> activeExistingDocIds = existingPpm.getDocs() != null ?
                    existingPpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet()) : new HashSet<>();

            // Log document changes
            if (!requestedDocIds.equals(activeExistingDocIds)) {
                System.out.println("Document changes detected for PPM update ID: " + ppmId);

                // Find new documents
                Set<Integer> newDocIds = new HashSet<>(requestedDocIds);
                newDocIds.removeAll(activeExistingDocIds);
                if (!newDocIds.isEmpty()) {
                    System.out.println("  - New documents added: " + newDocIds);
                }

                // Find removed documents
                Set<Integer> removedDocIds = new HashSet<>(activeExistingDocIds);
                removedDocIds.removeAll(requestedDocIds);
                if (!removedDocIds.isEmpty()) {
                    System.out.println("  - Documents marked as deleted: " + removedDocIds);
                }
            }

            // Create a list to hold updated documents
            List<Docs> updatedDocs = new ArrayList<>();

            if (existingPpm.getDocs() != null) {
                // For each existing document in the PPM
                for (Docs existingDoc : existingPpm.getDocs()) {
                    if (requestedDocIds.contains(existingDoc.getId())) {
                        // This doc is in the request, ensure it's not marked as deleted
                        existingDoc.setIsDeleted(false);
                        docsRepository.save(existingDoc);
                        updatedDocs.add(existingDoc);

                        // Remove from requested IDs since we've handled it
                        requestedDocIds.remove(existingDoc.getId());
                    } else {
                        // This doc is not in the request, mark it as deleted
                        existingDoc.setIsDeleted(true);
                        docsRepository.save(existingDoc);
                        updatedDocs.add(existingDoc); // Keep it in the collection but as deleted
                        System.out.println("Marked document with ID " + existingDoc.getId() + " as deleted (not in request)");
                    }
                }
            }

            // Add any new docs from the request
            for (Integer docId : requestedDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                if (!doc.getPpms().contains(existingPpm)) {
                    doc.getPpms().add(existingPpm);
                }

                // Ensure doc is active
                doc.setIsDeleted(false);
                docsRepository.save(doc);

                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " to PPM");
            }

            // Update the PPM with the final docs list
            existingPpm.setDocs(updatedDocs);
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            // No docIds provided - mark all existing documents as deleted
            List<Docs> existingDocs = existingPpm.getDocs();
            for (Docs doc : existingDocs) {
                if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                    doc.setIsDeleted(true);
                    docsRepository.save(doc);
                    System.out.println("Marked document with ID " + doc.getId() + " as deleted (no docIds provided)");
                }
            }
            // Keep the existing docs list to preserve relationships (just all marked as deleted)
        }

        // Handle PPM files if provided
//        if (request.getPpmFiles() != null && !request.getPpmFiles().isEmpty()) {
//            try {
//                List<Docs> uploadedDocs = processAndSaveDocuments(request.getPpmFiles(), existingPpm);
//                if (existingPpm.getDocs() == null) {
//                    existingPpm.setDocs(new ArrayList<>());
//                }
//                existingPpm.getDocs().addAll(uploadedDocs);
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to process PPM document files: " + e.getMessage(), e);
//            }
//        }

        // Process realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();

            for (RealisationBaseRequestDto realisationRequest : request.getRealisations()) {
                Realisation realisation;

                if (realisationRequest.getId() != null) {
                    // Update existing realisation - always use version 1
                    Integer versionToUse = 1;

                    CompositeId realisationId = new CompositeId(realisationRequest.getId(), versionToUse);
                    Optional<Realisation> existingRealisationOpt = realisationRepository.findById(realisationId);

                    if (existingRealisationOpt.isEmpty()) {
                        throw new RuntimeException("Realisation not found with ID: " + realisationId.getId());
                    }

                    realisation = existingRealisationOpt.get();

                    // Update realisation fields
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService());
                    realisation.setDirection(realisationRequest.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    // Handle realisation document management - same approach as for PPM docs
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            doc.getRealisations().add(realisation);

                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);

                            docs.add(doc);
                        }

                        // Set docs list for new realisation
                        realisation.setDocs(docs);
                    }

                    // Handle Lots if provided
                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        Set<Lot> lots = new HashSet<>();
                        for (LotResponse lotRequest : realisationRequest.getLots()) {
                            Lot lot = new Lot();
                            Integer lotId = lotRequest.getId();
                            if (lotId == null) {
                                Integer maxLotId = lotRepository.findMaxId();
                                lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                            }
                            lot.setId(lotId);
                            lot.setVersion(1);
                            lot.setDescription(lotRequest.getDescription());
                            lot = lotRepository.save(lot);
                            lots.add(lot);
                        }
                        realisation.setLots(lots);
                    }

                    // Save the realisation
                    realisation = realisationRepository.save(realisation);
                } else {
                    // Create new realisation
                    realisation = new Realisation();
                    Integer maxRealisationId = realisationRepository.findMaxId();
                    Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;
                    realisation.setId(realisationId);
                    realisation.setVersion(1); // Always version 1 for new realisations

                    // Set all fields from the request
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService());
                    realisation.setDirection(realisationRequest.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    // Handle realisation files if provided
                    if (realisationRequest instanceof RealisationWithLotsRequestDto) {
                        RealisationWithLotsRequestDto realisationWithLots = (RealisationWithLotsRequestDto) realisationRequest;
                        if (realisationWithLots.getRealisationFiles() != null && !realisationWithLots.getRealisationFiles().isEmpty()) {
                            try {
                                List<Docs> uploadedDocs = processAndSaveRealisationDocuments(realisationWithLots.getRealisationFiles(), realisation);
                                if (realisation.getDocs() == null) {
                                    realisation.setDocs(new ArrayList<>());
                                }
                                realisation.getDocs().addAll(uploadedDocs);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to process realisation document files: " + e.getMessage(), e);
                            }
                        }
                    }

                    // Set up the relationship with PPM
                    List<Ppm> ppmList = new ArrayList<>();
                    ppmList.add(existingPpm);
                    realisation.setPpms(ppmList);

                    // Handle Realisation documents if docIds are provided
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            if (!doc.getRealisations().contains(realisation)) {
                                doc.getRealisations().add(realisation);
                            }

                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);

                            docs.add(doc);
                        }

                        // Set docs list for new realisation
                        realisation.setDocs(docs);
                    }

                    // Handle Lots if provided
                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        Set<Lot> lots = new HashSet<>();
                        for (LotResponse lotRequest : realisationRequest.getLots()) {
                            Lot lot = new Lot();
                            Integer lotId = lotRequest.getId();
                            if (lotId == null) {
                                Integer maxLotId = lotRepository.findMaxId();
                                lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                            }
                            lot.setId(lotId);
                            lot.setVersion(1);
                            lot.setDescription(lotRequest.getDescription());
                            lot = lotRepository.save(lot);
                            lots.add(lot);
                        }
                        realisation.setLots(lots);
                    }

                    // Save the realisation
                    realisation = realisationRepository.save(realisation);
                }

                realisations.add(realisation);
            }

            // Update the PPM with the list of realisations
            existingPpm.setRealisations(realisations);
        } else if (existingPpm.getRealisations() != null) {
            existingPpm.getRealisations().clear(); // Clear if no new realisations provided
        }

        // Save the updated PPM
        Ppm savedPpm = repository.save(existingPpm);

        // Create a copy for the response with only non-deleted documents
        Ppm copyForResponse = new Ppm();
        BeanUtils.copyProperties(savedPpm, copyForResponse);

        // Filter out deleted documents for the response
        if (savedPpm.getDocs() != null) {
            copyForResponse.setDocs(
                    savedPpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList())
            );
        }

        // Also filter realisation documents
        if (copyForResponse.getRealisations() != null) {
            copyForResponse.getRealisations().forEach(realisation -> {
                if (realisation.getDocs() != null) {
                    realisation.setDocs(
                            realisation.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList())
                    );
                }
            });
        }

        return copyForResponse;
    }

    /**
     * Update an existing PPM with all its relationships.
     * This method updates a PPM along with its realisations, lots, and documents.
     *
     * @param ppmId   the ID of the PPM to update
     * @param ppmVer  the version of the PPM to update
     * @param request the request containing updated PPM data
     * @return the updated PPM with all relationships established
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    @Transactional
    @Override
    public Ppm updateWithRelationships(Integer ppmId, Integer ppmVer, PpmWithLotsRequestDto request) {
        // Find the existing PPM
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);
        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVer + " not found.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException("Cannot update a deleted Ppm with ID " + ppmId + " and version " + ppmVer);
        }

        // Debug output to see what's in the request
        System.out.println("Starting updateWithRelationships with request: " + request);
        System.out.println("RealisationsWithLots from request: " + request.getRealisationsWithLots());

        // Update PPM fields
        existingPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
        existingPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
        existingPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());

        // Set status if provided
        if (request.getStatus() != null) {
            existingPpm.setStatus(refParamRepository.findById(request.getStatus().getId())
                    .orElseThrow(() -> new RuntimeException("Status with ID " + request.getStatus().getId() + " not found")));
        }

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            existingPpm.setAc(ac);
        }

        // Handle documents if provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            // Get set of document IDs to add
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());

            // Get existing document IDs
            Set<Integer> existingDocIds = existingPpm.getDocs() != null ?
                    existingPpm.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                    new HashSet<>();

            // Documents to add (those in request but not in existing)
            Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
            docsToAdd.removeAll(existingDocIds);

            // Documents to remove (those in existing but not in request)
            Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
            docsToRemove.removeAll(requestDocIds);

            // Get current docs list or initialize
            List<Docs> docs = existingPpm.getDocs() != null ?
                    new ArrayList<>(existingPpm.getDocs()) :
                    new ArrayList<>();

            // Remove documents that are no longer in the request
            if (!docsToRemove.isEmpty()) {
                docs.removeIf(doc -> docsToRemove.contains(doc.getId()));

                // Declare final variable outside the lambda
                final Ppm ppmRef = existingPpm;

                for (Integer docId : docsToRemove) {
                    docsRepository.findById(docId).ifPresent(doc -> {
                        if (doc.getPpms() != null) {
                            doc.getPpms().remove(ppmRef);
                            docsRepository.save(doc);
                        }
                    });
                }
            }

            // Add new documents
            for (Integer docId : docsToAdd) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                doc.getPpms().add(existingPpm);
                docs.add(doc);

                // Save the document with updated relationship
                docsRepository.save(doc);
            }

            // Update PPM with documents
            existingPpm.setDocs(docs);
            System.out.println("Updated documents: " + docs.size() + " total documents");
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            // Clear document associations if request has empty docIds
            for (Docs doc : existingPpm.getDocs()) {
                if (doc.getPpms() != null) {
                    doc.getPpms().remove(existingPpm);
                    docsRepository.save(doc);
                }
            }
            existingPpm.setDocs(new ArrayList<>());
        }

        // Process document files if provided
        if(request.getPpmFiles() != null && !request.getPpmFiles().isEmpty()) {
            try {
                System.out.println("Processing " + request.getPpmFiles().size() + " files for PPM");
                processAndSaveDocuments(request.getPpmFiles(), existingPpm);
                System.out.println("Processed PPM files successfully");
            } catch (IOException e) {
                System.err.println("Error processing PPM files: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        List<Realisation> updatedRealisations = new ArrayList<>();
        Set<Integer> processedRealisationIds = new HashSet<>();

        // Keep track of newly created lots to detect duplicates
        Map<String, Lot> newLotsMap = new HashMap<>();

        // Choose which collection of realisations to process, preferring realisationsWithLots
        List<? extends RealisationBaseRequestDto> realisationsToProcess = null;
        if (request.getRealisationsWithLots() != null && !request.getRealisationsWithLots().isEmpty()) {
            realisationsToProcess = request.getRealisationsWithLots();
            System.out.println("Processing realisationsWithLots: " + realisationsToProcess.size() + " items");
        } else if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            realisationsToProcess = request.getRealisations();
            System.out.println("Processing standard realisations: " + realisationsToProcess.size() + " items");
        }

        if (realisationsToProcess != null && !realisationsToProcess.isEmpty()) {
            for (RealisationBaseRequestDto realisationRequest : realisationsToProcess) {
                Realisation realisation;

                // Check if updating existing realisation or creating new one
                if (realisationRequest.getId() != null) {
                    // Process existing realisation
                    processedRealisationIds.add(realisationRequest.getId());

                    // Find the existing realisation
                    Integer realisationVersion = realisationRequest.getVersion() != null ?
                            realisationRequest.getVersion() : 1; // Default to version 1

                    CompositeId realisationId = new CompositeId(realisationRequest.getId(), realisationVersion);
                    Optional<Realisation> existingRealisationOpt = realisationRepository.findById(realisationId);

                    if (existingRealisationOpt.isEmpty()) {
                        throw new RuntimeException("Realisation with ID " + realisationRequest.getId() +
                                " and version " + realisationVersion + " not found");
                    }

                    realisation = existingRealisationOpt.get();

                    // Update realisation fields
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees() != null ?
                            realisationRequest.getRealisationEnvisagees() : realisation.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService() != null ?
                            realisationRequest.getService() : realisation.getService());
                    realisation.setDirection(realisationRequest.getDirection() != null ?
                            realisationRequest.getDirection() : realisation.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement() != null ?
                            realisationRequest.getSourceFinancement() : realisation.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais() != null ?
                            realisationRequest.getMonais() : realisation.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher() != null ?
                            realisationRequest.getTypeMarcher() : realisation.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection() != null ?
                            realisationRequest.getModeSelection() : realisation.getModeSelection());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps() != null ?
                            realisationRequest.getDatePrevLps() : realisation.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc() != null ?
                            realisationRequest.getDatePrevAc() : realisation.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp() != null ?
                            realisationRequest.getDatePrevDp() : realisation.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp() != null ?
                            realisationRequest.getDatePrevAp() : realisation.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime() != null ?
                            realisationRequest.getBudgetEstime() : realisation.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished() != null ?
                            realisationRequest.getIsPublished() : realisation.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt() != null ?
                            realisationRequest.getPublishedAt() : realisation.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort() != null ?
                            realisationRequest.getSort() : realisation.getSort());

                    // Ensure the PPM relationship is maintained
                    if (realisation.getPpms() == null) {
                        realisation.setPpms(new ArrayList<>());
                    }
                    if (!realisation.getPpms().contains(existingPpm)) {
                        realisation.getPpms().add(existingPpm);
                    }

                    // Process realisation documents if provided
                    if (realisationRequest.getDocIds() != null) {
                        // Get set of document IDs to add
                        Set<Integer> requestDocIds = new HashSet<>(realisationRequest.getDocIds());

                        // Get existing document IDs
                        Set<Integer> existingDocIds = realisation.getDocs() != null ?
                                realisation.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                                new HashSet<>();

                        // Documents to add (those in request but not in existing)
                        Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
                        docsToAdd.removeAll(existingDocIds);

                        // Documents to remove (those in existing but not in request)
                        Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
                        docsToRemove.removeAll(requestDocIds);

                        // Get current docs list or initialize
                        List<Docs> docs = realisation.getDocs() != null ?
                                new ArrayList<>(realisation.getDocs()) :
                                new ArrayList<>();

                        // Remove documents that are no longer in the request
                        if (!docsToRemove.isEmpty()) {
                            docs.removeIf(doc -> docsToRemove.contains(doc.getId()));

                            // Declare final variable outside the lambda
                            final Realisation realisationRef = realisation;

                            // For each removed doc, update the relationship
                            for (Integer docId : docsToRemove) {
                                docsRepository.findById(docId).ifPresent(doc -> {
                                    if (doc.getRealisations() != null) {
                                        doc.getRealisations().remove(realisationRef);
                                        docsRepository.save(doc);
                                    }
                                });
                            }
                        }

                        // Add new documents
                        for (Integer docId : docsToAdd) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            if (!doc.getRealisations().contains(realisation)) {
                                doc.getRealisations().add(realisation);
                            }

                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);
                            docs.add(doc);
                        }

                        // Set docs list for updated realisation
                        realisation.setDocs(docs);
                        System.out.println("Updated docs for realisation ID " + realisation.getId() + ": " + docs.size() + " total docs");
                    }
                } else {
                    // Create a new Realisation
                    realisation = new Realisation();

                    // Generate a new ID for the realisation
                    Integer maxRealisationId = realisationRepository.findMaxId();
                    Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;

                    // Always use version 1 for newly created realisations
                    realisation.setId(realisationId);
                    realisation.setVersion(1);

                    // Set all fields from the request
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService());
                    realisation.setDirection(realisationRequest.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    // Set up the relationship with PPM
                    List<Ppm> ppmList = new ArrayList<>();
                    ppmList.add(existingPpm);
                    realisation.setPpms(ppmList);

                    // Process realisation documents if provided
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            if (!doc.getRealisations().contains(realisation)) {
                                doc.getRealisations().add(realisation);
                            }

                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);
                            docs.add(doc);
                        }

                        // Set docs list for new realisation
                        realisation.setDocs(docs);
                        System.out.println("Added " + docs.size() + " documents to new realisation");
                    }
                }

                // Save the realisation before processing files to ensure it has an ID
                realisation = realisationRepository.save(realisation);
                System.out.println("Saved realisation ID: " + realisation.getId() + " version: " + realisation.getVersion());

                // Process realisation files AFTER saving the realisation
                if (realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    try {
                        System.out.println("Processing " + realisationRequest.getRealisationFiles().size() +
                                " files for realisation ID: " + realisation.getId());

                        // Process each file one by one
                        for (MultipartFile file : realisationRequest.getRealisationFiles()) {
                            if (file == null || file.isEmpty()) {
                                System.out.println("Skipping empty file");
                                continue;
                            }

                            System.out.println("Processing file: " + file.getOriginalFilename() +
                                    ", size: " + file.getSize() +
                                    ", content type: " + file.getContentType());

                            // Try saving the single file
                            List<MultipartFile> singleFileList = new ArrayList<>();
                            singleFileList.add(file);
                            processAndSaveRelisationsDocuments(singleFileList, realisation);
                        }

                        // After processing files, fetch the updated realisation with its documents
                        realisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));

                        System.out.println("After file processing, realisation has " +
                                (realisation.getDocs() != null ? realisation.getDocs().size() : 0) + " documents");

                        // Save the realisation again
                        realisation = realisationRepository.save(realisation);
                    } catch (IOException e) {
                        System.err.println("Error processing realisation files: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    System.out.println("Processing " + realisationRequest.getLots().size() +
                            " lots for realisation ID: " + realisation.getId());

                    Set<Lot> updatedLots = new HashSet<>();
                    Set<Integer> processedLotIds = new HashSet<>();

                    // Get existing lot IDs
                    Set<Integer> existingLotIds = realisation.getLots() != null ?
                            realisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet()) :
                            new HashSet<>();

                    // Process each lot in the request
                    for (LotResponse lotDto : realisationRequest.getLots()) {
                        if (lotDto.getId() == null) {
                            // Handle new lot without ID
                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";

                            // Check for duplicate lots in this transaction
                            if (newLotsMap.containsKey(lotHash)) {
                                updatedLots.add(newLotsMap.get(lotHash));
                                processedLotIds.add(newLotsMap.get(lotHash).getId());
                                continue;
                            }

                            // Create a new lot
                            Lot newLot = new Lot();
                            newLot.setDescription(lotDto.getDescription());
                            newLot.setVersion(1); // First version
                            newLot.setIsDeleted(false); // Ensure it's not deleted

                            // Generate new ID
                            Integer maxLotId = lotRepository.findMaxId();
                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                            newLot.setId(lotId);

                            Lot savedLot = lotRepository.save(newLot);
                            updatedLots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            newLotsMap.put(lotHash, savedLot);
                            System.out.println("Created new Lot with ID: " + savedLot.getId());
                        } else {
                            // Handle existing lot
                            if (processedLotIds.contains(lotDto.getId())) {
                                continue; // Skip duplicate lot IDs
                            }

                            Integer lotId = lotDto.getId();
                            Integer lotVersion = lotDto.getVersion();

                            // Try to find the latest version if not specified
                            if (lotVersion == null) {
                                lotVersion = lotRepository.findMaxVersionById(lotId);
                                if (lotVersion == null) {
                                    System.out.println("Lot ID " + lotId + " not found, creating new lot");
                                    // Create a new lot with the specified ID
                                    Lot newLot = new Lot();
                                    newLot.setId(lotId);
                                    newLot.setVersion(1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);
                                    Lot savedLot = lotRepository.save(newLot);
                                    updatedLots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    continue;
                                }
                            }

                            // Get the existing lot
                            Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                            if (existingLotOpt.isPresent()) {
                                Lot existingLot = existingLotOpt.get();
                                String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";

                                // If descriptions are different, create a new version
                                if (!requestDesc.equals(existingDesc)) {
                                    Lot newLot = new Lot();
                                    newLot.setId(existingLot.getId());
                                    newLot.setVersion(existingLot.getVersion());
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);

                                    Lot savedLot = lotRepository.save(newLot);
                                    updatedLots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    System.out.println("Created new version of Lot: " + savedLot.getId() +
                                            ", Version: " + savedLot.getVersion());
                                } else {
                                    // Use existing lot
                                    updatedLots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                }
                            } else {
                                // Lot version not found, create new version 1
                                Lot newLot = new Lot();
                                newLot.setId(lotId);
                                newLot.setVersion(1);
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setIsDeleted(false);
                                Lot savedLot = lotRepository.save(newLot);
                                updatedLots.add(savedLot);
                                processedLotIds.add(savedLot.getId());
                                System.out.println("Created new Lot with provided ID: " + savedLot.getId());
                            }
                        }
                    }

                    // Set the lots on the realisation
                    realisation.setLots(updatedLots);
                    System.out.println("Set " + updatedLots.size() + " lots on realisation ID: " + realisation.getId());

                    // Save the realisation with lots
                    realisation = realisationRepository.save(realisation);

                    // Verify the lots were properly saved
                    Realisation verifyRealisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                            .orElseThrow(() -> new RuntimeException("Could not find realisation after saving lots"));

                    System.out.println("After saving, realisation ID: " + verifyRealisation.getId() +
                            " has " + (verifyRealisation.getLots() != null ? verifyRealisation.getLots().size() : 0) +
                            " lots");
                } else if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                    // Clear lots if none provided in request
                    realisation.setLots(new HashSet<>());
                    realisation = realisationRepository.save(realisation);
                    System.out.println("Cleared lots for realisation ID: " + realisation.getId());
                }

                // Add the realisation to our updated list
                updatedRealisations.add(realisation);
            }
        }

        // Handle removal of realisations not in the request
        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                if (!processedRealisationIds.contains(existingRealisation.getId())) {
                    // Remove PPM from realisation's PPMs list
                    if (existingRealisation.getPpms() != null) {
                        existingRealisation.getPpms().remove(existingPpm);
                        realisationRepository.save(existingRealisation);
                    }
                }
            }
        }

        // Update the PPM with the list of realisations
        existingPpm.setRealisations(updatedRealisations);
        System.out.println("Updated PPM with " + updatedRealisations.size() + " realisations");

        // Save PPM again to persist all associations
        existingPpm = repository.save(existingPpm);
        System.out.println("Final save of PPM - ID: " + existingPpm.getId() + ", Version: " + existingPpm.getVersion());

        // Verify the final PPM has all associated entities
        System.out.println("Final PPM has: " +
                (existingPpm.getDocs() != null ? existingPpm.getDocs().size() : 0) + " documents, " +
                (existingPpm.getRealisations() != null ? existingPpm.getRealisations().size() : 0) + " realisations");

        // Print realisation details to verify
        if (existingPpm.getRealisations() != null) {
            for (Realisation r : existingPpm.getRealisations()) {
                System.out.println("Realisation ID: " + r.getId() +
                        ", Docs: " + (r.getDocs() != null ? r.getDocs().size() : 0) +
                        ", Lots: " + (r.getLots() != null ? r.getLots().size() : 0));
            }
        }

        return existingPpm;
    }
}