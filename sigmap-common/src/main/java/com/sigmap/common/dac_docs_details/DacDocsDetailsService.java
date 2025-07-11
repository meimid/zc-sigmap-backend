package com.sigmap.common.dac_docs_details;

import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import java.util.List;
import java.util.Optional;

import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DacDocsDetailsService {
  /**
   * Retrieves a paginated list of DacDocsDetails entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @param pageable The pagination and sorting information
   * @return A Page of DacDocsDetails entities matching the specification
   */
  Page<DacDocsDetails> findAll(Specification<DacDocsDetails> spec, Pageable pageable);

  /**
   * Retrieves a list of DacDocsDetails entities based on the provided specification.
   *
   * @param spec The JPA specification
   * @return A list of DacDocsDetails entities matching the specification
   */
  List<DacDocsDetails> findAll(Specification<DacDocsDetails> spec);

  /**
   * Retrieves a single DacDocsDetails entity based on the provided specification.
   *
   * @param specification The JPA specification
   * @return An Optional containing the DacDocsDetails entity if found, or empty if not found
   */
  Optional<DacDocsDetails> findOne(Specification<DacDocsDetails> specification);

  /**
   * Retrieves the number of DacDocsDetails entities matching the provided specification.
   *
   * @param specification The JPA specification
   * @return The number of DacDocsDetails entities matching the specification
   */
  long count(Specification<DacDocsDetails> specification);

  /**
   * Retrieves a DacDocsDetails entity by its composite ID.
   *
   * @param dynaFieldsDetailsId The dynamic fields details ID
   * @param rowId The row ID
   * @param dacId The DAC ID
   * @param dacVer The DAC version
   * @param dynaDocsId The dynamic docs ID
   * @param dynaDocsVer The dynamic docs version
   * @param dynaFieldsId The dynamic fields ID
   * @return The DacDocsDetails entity if found
   */
  Optional<DacDocsDetails> findById(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);

  /**
   * Saves a DacDocsDetails entity to the database.
   *
   * @param dacDocsDetails The DacDocsDetails entity to save
   * @return The saved DacDocsDetails entity
   */
  DacDocsDetails save(DacDocsDetails dacDocsDetails);

  /**
   * Creates a new DacDocsDetails entity based on the provided request data.
   *
   * @param dacDocs The DacDocs entity to create the details for
   * @param request The DacDocsDetails request containing the data to create the DacDocsDetails entity
   * @return The created DacDocsDetails entity
   */
  DacDocsDetails create(DacDocs dacDocs, DacDocsDetailsRequest request);

  /**
   * Creates a list of DacDocsDetails entities based on the provided request data.
   *
   * @param dacDocs The DacDocs entity to create the details for
   * @param requests The list of DacDocsDetails requests containing the data to create the DacDocsDetails entities
   * @return The created DacDocsDetails entities
   */
  public List<DacDocsDetails> createAll(DacDocs dynaField, List<DacDocsDetailsRequest> request) throws DynaFieldNotFound;

  /**
   * Updates an existing DacDocsDetails entity based on the provided request data.
   *
   * @param dynaFieldsDetailsId The dynamic fields details ID
   * @param rowId The row ID
   * @param dacId The DAC ID
   * @param dacVer The DAC version
   * @param dynaDocsId The dynamic docs ID
   * @param dynaDocsVer The dynamic docs version
   * @param dynaFieldsId The dynamic fields ID
   * @param request The DacDocsDetails request containing the data to update
   * @return The updated DacDocsDetails entity
   */
  DacDocsDetails update(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId, DacDocsDetailsRequest request);

  /**
   * Deletes a DacDocsDetails entity by its composite ID.
   *
   * @param dynaFieldsDetailsId The dynamic fields details ID
   * @param rowId The row ID
   * @param dacId The DAC ID
   * @param dacVer The DAC version
   * @param dynaDocsId The dynamic docs ID
   * @param dynaDocsVer The dynamic docs version
   * @param dynaFieldsId The dynamic fields ID
   */
  void delete(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);

  /**
   * Deletes all DacDocsDetails entities associated with a specific DacDocs.
   *
   * @param dacDocs The DacDocs entity to delete details for
   */
  void deleteByDacDocs(DacDocs dacDocs);
} 