package com.sigmap.common.ref_types;


import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypePositiveIdAndNotEmpty;
import com.sigmap.common.ref_types.request.RefTypeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link RefType}.
 * The service provides methods to retrieve and manage RefTypes.
 *
 * @see RefType
 */
public interface RefTypeService {
  /**
   * Return a list of all RefTypes.
   *
   * @return a list of all RefTypes
   */
  List<RefType> findAll();
  /**
   * Return a page of RefParams based on the given specification and pageable.
   *
   * @param spec the specification to use
   * @param pageable the pageable to use
   * @return a page of RefType
   */
  Page<RefType> findAll(Specification<RefType> spec, Pageable pageable);
  /**
   * Return the RefParam with the given specification.
   *
   * @param specification the specification to use
   * @return the RefParam with the given specification
   */
  Optional<RefType> findOne(Specification<RefType> specification);

  /**
   * Return a page of deleted RefParams based on the given specification and pageable.
   *
   * @param spec the specification to use
   * @param pageable the pageable to use
   * @return a page of RefParams
   */
  Page<RefType> findAllDeleted(Specification<RefType> spec, Pageable pageable);


  /**
   * Return a page of deleted RefType based on the given pageable.
   *
   * @param pageable the pageable to use
   * @return a page of RefType
   */

  Page<RefType> findAllDeleted(Pageable pageable);

  /**
   * Return the deleted RefType with the given id.
   *
   * @param id the id of the RefType to return
   * @return the RefParam with the given id
   */
  Optional<RefType> findDeletedById(Integer id);
  /**
   * Return a RefType by id.
   *
   * @param id the id of the RefType to return
   * @return the RefType with the given id
   */


  RefType findById(Integer id) throws RefTypeNotFound;

  /**
   * Save a RefType.
   *
   * @param refType the RefType to save
   * @return the saved RefType
   */
  RefType save(RefType refType);

  /**
   * Create a RefType.
   *
   * @param request the request to create the RefType from
   * @return the created RefType
   */
  RefType create(RefTypeRequest request) throws RefTypePositiveIdAndNotEmpty;
  /**
   * update RefType.
   *
   * @param request the request to update the RefType from
   * @return the updated RefType
   */
  RefType update(RefTypeRequest request)throws RefTypeNotFound;
  /**
   * Delete a RefType.
   *
   * @param id the id of the RefType to delete
   */
  void delete(Integer id);

}
