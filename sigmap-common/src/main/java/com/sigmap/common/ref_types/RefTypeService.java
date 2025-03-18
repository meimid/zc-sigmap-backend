package com.sigmap.common.ref_types;

import com.sigmap.common.ref_types.request.RefTypeRequest;
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
   * Return a RefType by id.
   *
   * @param id the id of the RefType to return
   * @return the RefType with the given id
   */
  Optional<RefType> findById(Integer id);

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
  RefType create(RefTypeRequest request);
}
