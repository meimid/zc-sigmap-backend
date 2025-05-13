package com.sigmap.common.lots;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.lots.exception.UpdateDeletedLot;
import com.sigmap.common.lots.request.LotBaseRequestDto;
import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LotServiceImpl implements LotService {
    private final LotRepository repository;
    private final RealisationRepository realisationRepository;

    public LotServiceImpl(LotRepository lotRepository, RealisationRepository realisationRepository) {
        this.repository = lotRepository;
        this.realisationRepository = realisationRepository;
    }

    @Override
    public List<Lot> findAll() {
        Specification<Lot> spec = LotSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Lot> findAll(Specification<Lot> spec, Pageable pageable) {

        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Lot> findAllDeleted(Specification<Lot> spec, Pageable pageable) {
        Specification<Lot> deletedSpec = LotSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Lot> findAllDeleted(Pageable pageable) {
        Specification<Lot> spec = LotSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Lot> findDeletedById(Integer lotsId, Integer lotsVer) {
        Specification<Lot> spec = LotSpecifications.id(new CompositeId(lotsId, lotsVer))
                .and(LotSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Lot> findOne(Specification<Lot> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Lot findById(Integer lotsId, Integer lotsVer) throws LotNotFound {
        return repository.findById(new CompositeId(lotsId, lotsVer)).orElseThrow(() -> new LotNotFound("", List.of(lotsId), List.of(lotsVer)));
    }

    @Override
    public Lot save(Lot lot) {
        return repository.save(lot);
    }


    @Override
    public Lot create(LotRequest request) throws RealisationNotFoundException {
        Lot lot = new Lot();

        Integer id = request.getId();
        if (id == null) {
            Integer maxId = repository.findMaxId();
            id = (maxId != null) ? maxId + 1 : 1;
            System.out.println("Generated Lot ID: " + id);
        }

        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        lot.setId(id);
        lot.setVersion(version);
        lot.setDescription(request.getDescription());

        Lot savedLot = repository.save(lot);

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            Set<Realisation> realisations = new HashSet<>();
            for (CompositeId realisationId : request.getRealisationIds()) {
                Realisation realisation = realisationRepository.findById(realisationId)
                        .orElseThrow(() -> new RealisationNotFoundException("",realisationId.getId(),realisationId.getVersion()));

                if (realisation.getLots() == null) {
                    realisation.setLots(new HashSet<>());
                }

                realisation.getLots().add(savedLot);
                realisations.add(realisation);
            }
            savedLot.setRealisations(realisations);

            savedLot = repository.save(savedLot);
        }

        return savedLot;
    }

    @Override
    public Lot update(Integer lotId, Integer lotVer, LotRequest request) throws LotNotFound, UpdateDeletedLot{
        CompositeId compositeId = new CompositeId(lotId, lotVer);
        Lot existingLot = repository.findById(compositeId)
                .orElseThrow(() -> new LotNotFound("" , List.of(lotId) , List.of(lotVer) ));
        if (Boolean.TRUE.equals(existingLot.getIsDeleted())) {
            throw new UpdateDeletedLot("" , List.of(lotId) , List.of(lotVer));
        }
        existingLot.setDescription(request.getDescription());

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            Set<Realisation> realisations = new HashSet<>();
            for (CompositeId realisationId : request.getRealisationIds()) {
                Realisation realisation = realisationRepository.findById(realisationId)
                        .orElseThrow(() -> new RuntimeException("Realisation not found: " + realisationId));
                realisations.add(realisation);
            }
            existingLot.setRealisations(realisations);
        } else {
            existingLot.setRealisations(new HashSet<>()); // Clear associations if none provided
        }

        return repository.save(existingLot);
    }

    @Override
    public void delete(Integer lotsId, Integer lotsVer) {
       repository.softDelete(lotsId, lotsVer);
    }


    @Transactional
    public Lot updateWithVersioning(Integer lotId, Integer lotVer, LotBaseRequestDto request) {
        // Find the existing lot
        CompositeId compositeId = new CompositeId(lotId, lotVer);
        Lot existingLot = repository.findById(compositeId)
                .orElseThrow(() -> new RuntimeException("Lot with ID " + lotId + " and version " + lotVer + " not found"));

        if (Boolean.TRUE.equals(existingLot.getIsDeleted())) {
            throw new RuntimeException("Cannot update a deleted Lot with ID " + lotId + " and version " + lotVer);
        }

        // Log the current state and request
        System.out.println("Updating Lot ID: " + lotId + ", Current Version: " + lotVer);
        System.out.println("Request data: " + request);
        System.out.println("Current description: " + existingLot.getDescription());
        System.out.println("New description: " + request.getDescription());

        // Create a temporary lot to check for changes
        Lot tempLot = new Lot();
        String requestDescription = request.getDescription() != null ? request.getDescription() : existingLot.getDescription();
        tempLot.setDescription(requestDescription);

        // Check if there are actual changes to the description using the extracted method
        boolean hasChanges = !areDescriptionsIdentical(existingLot.getDescription(), tempLot.getDescription());

        System.out.println("Existing description: '" + existingLot.getDescription() + "'");
        System.out.println("New description: '" + tempLot.getDescription() + "'");
        System.out.println("Has changes: " + hasChanges);

        // Handle realisations separately if provided
        // Update realisations without triggering version increment
        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            updateLotRealisations(existingLot, request.getRealisationIds());
            System.out.println("Updated realisations relationship without incrementing version");
        }

        // If no changes to description, return the existing lot with possibly updated realisations
        if (!hasChanges) {
            System.out.println("No changes detected in description, returning existing version: " + lotVer);
            return existingLot;
        }

        // If there are changes to description, create a new version
        System.out.println("Changes detected in description, creating new version: " + (lotVer + 1));

        Lot newLot = new Lot();
        newLot.setId(lotId);
        newLot.setVersion(lotVer + 1);
        newLot.setDescription(tempLot.getDescription());

        // Copy relationships
        if (existingLot.getRealisations() != null) {
            newLot.setRealisations(new HashSet<>(existingLot.getRealisations()));
        } else {
            newLot.setRealisations(new HashSet<>());
        }

        // Save and return the new version
        return repository.save(newLot);
    }

    /**
     * Updates the realisations relationship for a Lot without incrementing the version.
     *
     * @param lot The existing lot to update
     * @param realisationIds The realisation IDs to associate with the lot
     */
    private void updateLotRealisations(Lot lot, Set<CompositeId> realisationIds) {
        if (realisationIds == null || realisationIds.isEmpty()) {
            return;
        }

        Set<Realisation> realisations = new HashSet<>();
        for (CompositeId realisationId : realisationIds) {
            Realisation realisation = realisationRepository.findById(realisationId)
                    .orElseThrow(() -> new RuntimeException("Realisation not found: " + realisationId));
            realisations.add(realisation);
        }

        lot.setRealisations(realisations);
        repository.save(lot);
    }

    /**
     * Compares two description strings to check if they are exactly identical.
     * This method does NOT trim strings, so whitespace differences will be detected.
     *
     * @param description1 The first description string
     * @param description2 The second description string
     * @return true if the descriptions are exactly identical (character by character), false otherwise
     */
    private boolean areDescriptionsIdentical(String description1, String description2) {
        // If both are null, they're identical
        if (description1 == null && description2 == null) {
            return true;
        }

        // If only one is null, they're different
        if (description1 == null || description2 == null) {
            return false;
        }

        // Character by character comparison (this will detect any difference including whitespace)
        return description1.equals(description2);
    }

    /**
     * Updates a lot with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the Lot with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param lotId the ID of the Lot to update
     * @param request the request containing the updated data
     * @return the newly created version of the Lot if description changes, otherwise the existing lot
     * @throws RuntimeException if no versions of the Lot exist or if all versions are deleted
     */
    @Transactional
    public Lot updateWithVersioning(Integer lotId, LotBaseRequestDto request) {
        // Find the latest version of the Lot
        Integer latestVersion = repository.findMaxVersionById(lotId);

        if (latestVersion == null) {
            throw new RuntimeException("No versions found for Lot with ID: " + lotId);
        }

        System.out.println("Found latest version for Lot ID " + lotId + ": " + latestVersion);

        // Call the version-specific method with the latest version
        return updateWithVersioning(lotId, latestVersion, request);
    }

}