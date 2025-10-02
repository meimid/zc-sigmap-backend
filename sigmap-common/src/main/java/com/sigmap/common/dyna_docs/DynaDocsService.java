package com.sigmap.common.dyna_docs;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.dyna_docs.exceptions.DynaDocNotFound;
import com.sigmap.common.dyna_docs.exception.ActiveAAOAlreadyExistsException;
import com.sigmap.common.dyna_docs.request.DynaDocsRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DynaDocsService {
  /**
   * Retrieves a paginated list of DynaDocs entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @param pageable The pagination and sorting information
   * @return A Page of DynaDocs entities matching the specification
   */
  Page<DynaDocs> findAll(Specification<DynaDocs> spec, Pageable pageable);

  /**
   * Retrieves a single DynaDocs entity based on the provided specification.
   *
   * @param specification The JPA specification
   * @return An Optional containing the Agpm entity if found, or empty if not found
   */
  Optional<DynaDocs> findOne(Specification<DynaDocs> specification);

  /**
   * Retrieves the number of DynaDocs entities matching the provided specification.
   *
   * @param specification The JPA specification
   * @return The number of DynaDocs entities matching the specification
   */
  long count(Specification<DynaDocs> specification);

  /**
   * Retrieves an DynaDocs entity by its ID and version.
   *
   * @param dynaDocId The ID
   * @param dynaDocVer The version
   * @return The DynaDoc entity if found
   * @throws DynaDocNotFound if the Agpm with the specified ID and version is not found
   */
  Optional<DynaDocs> findById(Integer dynaDocId, Integer dynaDocVer) throws DynaDocNotFound;

  /**
   * Retrieves the latest version of an DynaDocs entity by its ID.
   *
   * @param dynaDocId The ID
   * @return The DynaDoc entity if found
   * @throws DynaDocNotFound if the Agpm with the specified ID and version is not found
   */
  Optional<DynaDocs> findLatestById(Integer dynaDocId);

  /**
   * Saves a DynaDoc entity to the database.
   *
   * @param dynaDoc The dynaDoc entity to save
   * @return The saved DynaDoc entity
   */
  DynaDocs save(DynaDocs dynaDoc);

  /**
   * Creates a new DynaDoc entity based on the provided request data.
   *
   * @param request The Dyna doc DTO containing the data to create the DynaDoc entity
   * @return The created DynaDoc entity
   */
  DynaDocs create(DynaDocsRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound, ActiveAAOAlreadyExistsException;

  /**
   * Update dynadoc
   *
   * @param dynaDocId ID
   * @param request dynaDoc request
   */
  DynaDocs update(Integer dynaDocId, DynaDocsRequest request) throws DynaDocNotFound, ModeDePassationSelectionNotFound, ActiveAAOAlreadyExistsException;

  /**
   * Update dynadoc in place (without versioning)
   *
   * @param dynaDocId ID
   * @param request dynaDoc request
   */
  DynaDocs updateInPlace(Integer dynaDocId, DynaDocsRequest request) throws DynaDocNotFound, ModeDePassationSelectionNotFound, ActiveAAOAlreadyExistsException;

  // /**
  //  * Soft deletes an Agpm entity by its ID and version.
  //  *
  //  * @param agpmId The ID of the Agpm entity to delete
  //  * @param agpmVer The version of the Agpm entity to delete
  //  * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
  //  * @throws AgpmSoftDelete if soft deletion is not allowed
  //  * @throws AgpmInvalidStatusException if the Agpm entity has an invalid status for deletion
  //  */
  // void delete(Integer agpmId, Integer agpmVer)
  //     throws AgpmNotFound, AgpmSoftDelete, AgpmInvalidStatusException;
  //
  // /**
  //  * Updates an existing Agpm entity with versioning, using the provided request data.
  //  *
  //  * @param agpmId The ID of the Agpm entity to update
  //  * @param agpmVer The version of the Agpm entity to update
  //  * @param request The AgpmRequest DTO containing the updated data
  //  * @return The updated Agpm entity
  //  * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
  //  * @throws AcNotFound if the associated Ac entity is not found
  //  * @throws DocNotFound if a document referenced in the request is not found
  //  * @throws ReferenceNotFound if a referenced parameter is not found
  //  * @throws IOException if an error occurs during file processing
  //  */
  // Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request)
  //     throws AgpmNotFound, AcNotFound, DocNotFound, ReferenceNotFound, IOException;
  //
  // /**
  //  * Updates an existing Agpm entity without versioning, using the provided request data.
  //  *
  //  * @param agpmId The ID of the Agpm entity to update
  //  * @param agpmVer The version of the Agpm entity to update
  //  * @param request The AgpmRequest DTO containing the updated data
  //  * @return The updated Agpm entity
  //  * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
  //  * @throws DocNotFound if a document referenced in the request is not found
  //  * @throws IOException if an error occurs during file processing
  //  * @throws InvalidIdAndVersionAgpm if the ID and version combination is invalid
  //  * @throws AgpmInvalidStateException if the Agpm entity is in an invalid state for updating
  //  */
  // Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request)
  //     throws AgpmNotFound,
  //         DocNotFound,
  //         IOException,
  //         InvalidIdAndVersionAgpm,
  //         AgpmInvalidStateException;
  //
  // /**
  //  * Updates the status of an Agpm entity.
  //  *
  //  * @param agpmId The ID of the Agpm entity to update
  //  * @param agpmVer The version of the Agpm entity to update
  //  * @param statusId The new status ID to set
  //  * @return The updated Agpm entity
  //  * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
  //  */
  // Agpm updateStatus(Integer agpmId, Integer agpmVer, Integer statusId) throws AgpmNotFound;
}
