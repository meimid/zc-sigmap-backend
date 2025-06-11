package com.sigmap.common.dyna_fields_details;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DynaFieldsDetailsService {
  /**
   * Retrieves a paginated list of DynaFields entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @param pageable The pagination and sorting information
   * @return A Page of DynaFields entities matching the specification
   */
  Page<DynaFieldsDetails> findAll(Specification<DynaFieldsDetails> spec, Pageable pageable);

  /**
   * Retrieves a list of DynaFields entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @return A list of DynaFields entities matching the specification
   */
  List<DynaFieldsDetails> findAll(Specification<DynaFieldsDetails> spec);

  /**
   * Retrieves a single DynaFields entity based on the provided specification.
   *
   * @param specification The JPA specification
   * @return An Optional containing the Agpm entity if found, or empty if not found
   */
  Optional<DynaFieldsDetails> findOne(Specification<DynaFieldsDetails> specification);

  /**
   * Retrieves the number of DynaFields entities matching the provided specification.
   *
   * @param specification The JPA specification
   * @return The number of DynaFields entities matching the specification
   */
  long count(Specification<DynaFieldsDetails> specification);

  /**
   * Retrieves an DynaFieldsDetails entity by its ID and version.
   *
   * @param detailId The ID
   * @return The DynaFields entity if found
   */
  Optional<DynaFieldsDetails> findById(Integer detailId);

  /**
   * Saves a DynaFieldsDetails entity to the database.
   *
   * @param dynaField The dynaDoc entity to save
   * @return The saved DynaFieldsDetails entity
   */
  DynaFieldsDetails save(DynaFieldsDetails dynaField);

  /**
   * Creates a new DynaFieldsDetails entity based on the provided request data.
   *
   * @param dynaField The ID of the DynaFields entity to create the details for
   * @param request The Dyna doc DTO containing the data to create the DynaFieldsDetails entity
   * @return The created DynaFieldsDetails entity
   */
  DynaFieldsDetails create(DynaFields dynaField, DynaFieldsDetailsRequest request)
      throws DynaFieldNotFound;

  /**
   * Creates a list of DynaFieldsDetails entity based on the provided request data.
   *
   * @param dynaFieldId The ID of the DynaFields entity to create the details for
   * @param request The Dyna doc DTO containing the data to create the DynaFieldsDetails entity
   * @return The created DynaFieldsDetails entity
   */
  List<DynaFieldsDetails> createAll(DynaFields dynaField, List<DynaFieldsDetailsRequest> request)
      throws DynaFieldNotFound;
}
