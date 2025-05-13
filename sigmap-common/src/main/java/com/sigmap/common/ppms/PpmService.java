package com.sigmap.common.ppms;

import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

    /**
     * Return a page of Ppms based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable);

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
    Optional<Ppm> findById(Integer ppmId, Integer ppmVer);

    /**
     * Find the latest version of a Ppm entity by its ID.
     *
     * @param ppmId The ID of the Ppm to find.
     * @return An Optional containing the latest version of the Ppm, or empty if not found.
     */
    Optional<Ppm> findLatestById(Integer ppmId);

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
    Ppm create(PpmRequest request);

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
     * Delete a Ppm by its composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     */
    void delete(Integer ppmId, Integer ppmVer);


    Ppm updateStatus(Integer ppmId, Integer ppmVer, Integer statusId);

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
}