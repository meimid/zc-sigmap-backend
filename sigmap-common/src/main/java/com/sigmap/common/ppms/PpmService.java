package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.response.AcAvailablePpmResponse;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.exceptions.PaaIdNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.*;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.realisations.Realisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Ppm}.
 * The service provides methods to retrieve and manage Ppms.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link PpmSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see PpmSpecifications
 * @see Specification
 */
public interface PpmService {
    List<Ppm> findAll();

    Integer findLatestVersionById(Integer ppmId);
    /**
     * Return a page of Ppms based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) throws AcNotFound, PpmNotFound;
    Page<Ppm> findAllForDac(Specification<Ppm> spec, Pageable pageable) throws AcNotFound, PpmNotFound;

    /**
     * Return a page of deleted Ppms based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAllDeleted(Specification<Ppm> spec, Pageable pageable);

    /**
     * Return a page of deleted Ppms based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Ppm with the given composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     * @return the Ppm with the given ID components
     */
    Optional<Ppm> findDeletedById(Integer ppmId, Integer ppmVer);

    /**
     * Return the Ppm with the given specification.
     *
     * @param specification the specification to use
     * @return the Ppm with the given specification
     */
    Optional<Ppm> findOne(Specification<Ppm> specification);

    /**
     * Return the Ppm entity with the given composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     * @return the Ppm entity with the given ID components
     */
    Optional<Ppm> findById(Integer ppmId, Integer ppmVer) throws PpmNotFound, AcNotFound;

    /**
     * Find the latest version of a Ppm entity by its ID.
     *
     * @param ppmId The ID of the Ppm to find.
     * @return An Optional containing the latest version of the Ppm, or empty if not found.
     */
    Optional<Ppm> findLatestById(Integer ppmId) throws PpmNotFound, AcNotFound;

    /**
     * Save a Ppm entity.
     *
     * @param ppm the Ppm entity to save
     * @return the saved Ppm entity
     */
    Ppm save(Ppm ppm);

    /**
     * Create a Ppm.
     *
     * @param request the request to create the Ppm from
     * @return the created Ppm
     */
    Ppm create(PpmRequest request) throws AcNotFound;

    /**
     * Create a new PPM with full support for relationships including documents.
     * This method creates a PPM with its realisations, lots, and documents.
     * <p>
     * The method handles:
     * - Creating a new PPM with the provided fields
     * - Creating and associating document files for the PPM
     * - Creating Realisations with their fields
     * - Creating and associating document files for each Realisation
     * - Creating Lots for each Realisation
     * - Establishing all bidirectional relationships
     *
     * @param request the request containing PPM data, realisations, lots, and document files
     * @return the created PPM with all relationships established
     */
    Ppm createWithRelationships(PpmWithLotsRequestDto request) throws AcNotFound;

    /**
     * Update an existing Ppm.
     *
     * @param ppmId   the ID of the Ppm to update
     * @param ppmVer  the version of the Ppm to update
     * @param request the request containing updated Ppm data
     * @param isUpdatable whether to create a new version (default: false)
     * @return the updated Ppm
     */
    Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request, boolean isUpdatable);
    Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request);

    /**
     * Update an existing PPM (always version 1) and all its relationships without versioning.
     * This method updates a PPM along with its realisations, lots, and documents.
     * <p>
     * The method handles:
     * - Updating the PPM with the provided fields
     * - Processing document files for the PPM (adding new ones)
     * - Managing document references (adding/removing existing docs)
     * - Updating realisations with their fields
     * - Processing document files for each realisation
     * - Creating/updating lots for each realisation
     * - Establishing all bidirectional relationships
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing updated PPM data, realisations, lots, and document files
     * @return the updated PPM with all relationships established
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    Ppm updateGlobalWithoutVersionning(Integer ppmId, PpmWithLotsRequestDto request);

    /**
     * Delete a Ppm by its composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     */
    void delete(Integer ppmId, Integer ppmVer);


    Ppm updateStatus(Integer ppmId, Integer ppmVer, ProcessStatus status) throws PpmNotFound, AcNotFound;

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
    Ppm updateWithVersioning(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request);

    /**
     * Updates a PPM by creating a new version with the updated data.
     * Automatically finds and uses the latest version of the PPM.
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
     * @param request the request containing the updated data
     * @return the newly created version of the PPM
     * @throws RuntimeException if the PPM is not found or if all versions are deleted
     */
    Ppm updateWithVersioning(Integer ppmId, PpmBaseRequestDto request);

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
    Ppm updateWithVersioningWithLots(Integer ppmId, Integer ppmVer, PpmWithLotsRequestDto request);

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
    Ppm updateWithVersioningWithLots(Integer ppmId, PpmWithLotsRequestDto request);

    /**
     * Update an existing PPM with all its relationships.
     * This method updates a PPM along with its realisations, lots, and documents.
     * <p>
     * The method handles:
     * - Updating the PPM with the provided fields
     * - Processing document files for the PPM
     * - Updating/adding/removing realisations with their fields
     * - Processing document files for each realisation
     * - Updating/adding/removing lots for each realisation
     * - Establishing all bidirectional relationships
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the version of the PPM to update
     * @param request the request containing updated PPM data, realisations, lots, and document files
     * @return the updated PPM with all relationships established
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    Ppm updateWithRelationships(Integer ppmId, Integer ppmVer, PpmWithLotsRequestDto request);

    /**
     * Find a PPM by its ID and optionally its version.
     * If version is null, returns the latest version.
     *
     * @param ppmId the ID of the PPM
     * @param ppmVer the version of the PPM (can be null to get latest version)
     * @return Optional containing the PPM if found
     */
    Optional<Ppm> findByIdPpm(Integer ppmId, Integer ppmVer) throws PpmNotFound, AcNotFound;

    /**
     * find the list of ppms with Ac id
     * */
    Page<Ppm> findPpmsByAcId( Integer acId, Pageable pageable) throws AcNotFound;

    /**
     * Retrieves all versions of an Ppm entity by its ID, including only non-deleted documents,
     * in PpmResponseWithRealisations format with pagination.
     *
     * @param ppmId   ID of the Ppm entity
     * @param pageable Pagination and sorting configuration
     * @return Page of Ppm representing all versions for the given ID
     * @throws PpmIdNotFound if no Ppm entities are found for the ID
     */
    Page<Ppm> findAllVersionsById(Integer ppmId, Pageable pageable) throws PpmIdNotFound;
    /**
     * Récupère les réalisations qui ne sont pas liées à des DAC by ppmId et ppmVer
     *
     * @param ppmId    id du ppm
     * @param ppmVer   version du ppm
     * @param pageable paramètres de pagination
     * @return page de réalisations non liées à des DAC
     */
    Page<Realisation> getRealisationsWithoutDac(Integer ppmId, Integer ppmVer, Pageable pageable);

     Ppm createPpmPerAc(PpmWithLotsRequestDto request) throws PpmCreationLimitExceededException, AcNotFound, IOException;
     Page<Ppm> findCurrentYearPpmsByUser(Pageable pageable);
    List<AcAvailablePpmResponse> getAcsWithoutPpmForCurrentYear() throws AcNotFound;
    Page<Ppm> findAllForWebsite(Specification<Ppm> spec, Pageable pageable) ;

    /**
     * Creates an actualised version of the latest PPM for the given ID.
     * Copies core fields, resets workflow state (processInstanceId to null, status to BROUILLON),
     * sets actualise to true, and increments nbrActualisation by 1. Only non-deleted Docs are retained.
     * Active Realisations are re-linked to the new version and the PPM budget is recalculated.
     *
     * @param ppmId ID of the PPM to actualise (latest version is used as the source)
     * @return The newly created PPM version
     * @throws PpmNotFound if no PPM is found for the given ID
     * @throws PpmInvalidStatusException if the latest PPM version is not in PUBLIE status
     * @throws PpmNotAssociatedWithUserAcException if the PPM is not associated with one of the current user's ACs
     */
    Ppm actualise(Integer ppmId)
            throws PpmNotFound, PpmInvalidStatusException, PpmNotAssociatedWithUserAcException;
    List<Ppm> findAllForSuperAdminAndAdminCentral(Specification<Ppm> spec);
    List<Ppm> findAllForComUser(Specification<Ppm> spec, String userId);
     public List<Ppm> findAllPpms(Specification<Ppm> spec);


}

