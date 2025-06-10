package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DynaFieldsService {
  /**
   * Retrieves a paginated list of DynaFields entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @param pageable The pagination and sorting information
   * @return A Page of DynaFields entities matching the specification
   */
  Page<DynaFields> findAll(Specification<DynaFields> spec, Pageable pageable);

  /**
   * Retrieves a list of DynaFields entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @return A list of DynaFields entities matching the specification
   */
  List<DynaFields> findAll(Specification<DynaFields> spec);

  /**
   * Retrieves a single DynaField entity based on the provided specification.
   *
   * @param specification The JPA specification
   * @return An Optional containing the Agpm entity if found, or empty if not found
   */
  Optional<DynaFields> findOne(Specification<DynaFields> specification);

  /**
   * Retrieves the number of DynaField entities matching the provided specification.
   *
   * @param specification The JPA specification
   * @return The number of DynaFields entities matching the specification
   */
  long count(Specification<DynaFields> specification);

  /**
   * Retrieves an DynaField entity by its ID and version.
   *
   * @param dynaFieldId The ID
   * @return The DynaFields entity if found
   */
  Optional<DynaFields> findById(Integer dynaFieldId);

  /**
   * Saves a DynaField entity to the database.
   *
   * @param dynaField The dynaDoc entity to save
   * @return The saved DynaDoc entity
   */
  DynaFields save(DynaFields dynaField);

  /**
   * Creates a new DynaField entity based on the provided request data.
   *
   * @param request The Dyna doc DTO containing the data to create the DynaDoc entity
   * @return The created DynaDoc entity
   */
  DynaFields create(DynaFieldsRequest request) throws RefParamNotFound;

  /**
   * Update DynaFields.
   *
   * @param dynaFieldId ID
   * @param request dynaDoc request
   */
  DynaFields update(Integer dynaFieldId, DynaFieldsRequest request) throws RefParamNotFound;

  /**
   * Delete DynaField.
   *
   * @param dynaFieldId ID
   */
  void delete(Integer dynaFieldId);
}
