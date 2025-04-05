package com.sigmap.common.ref_params;

import com.sigmap.common.ref_params.request.RefParamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link RefParam}.
 * The service provides methods to retrieve and manage RefParams.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link RefParamSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see RefParamSpecifications
 * @see Specification
 */
public interface RefParamService {
  List<RefParam> findAll();

  /**
   * Return a page of RefParams based on the given specification and pageable.
   *
   * @param spec the specification to use
   * @param pageable the pageable to use
   * @return a page of RefParams
   */
  Page<RefParam> findAll(Specification<RefParam> spec, Pageable pageable);

  /**
   * Return a page of deleted RefParams based on the given specification and pageable.
   *
   * @param spec the specification to use
   * @param pageable the pageable to use
   * @return a page of RefParams
   */
  Page<RefParam> findAllDeleted(Specification<RefParam> spec, Pageable pageable);

  /**
   * Return a page of deleted RefParams based on the given pageable.
   *
   * @param pageable the pageable to use
   * @return a page of RefParams
   */
  Page<RefParam> findAllDeleted(Pageable pageable);

  /**
   * Return the deleted RefParam with the given id.
   *
   * @param id the id of the RefParam to return
   * @return the RefParam with the given id
   */
  Optional<RefParam> findDeletedById(Integer id);

  /**
   * Return the RefParam with the given specification.
   *
   * @param specification the specification to use
   * @return the RefParam with the given specification
   */
  Optional<RefParam> findOne(Specification<RefParam> specification);

  /** 
   * Return the RefParam with the given id.
   * 
   * @param id the id of the RefParam to return
   * @return the RefParam with the given id
   */
  Optional<RefParam> findById(Integer id);

  /** 
   * Save a RefParam.
   * 
   * @param refParam the RefParam to save
   * @return the saved RefParam
   */
  RefParam save(RefParam refParam);

  /** 
   * Create a RefParam.
   * 
   * @param request the request to create the RefParam from
   * @return the created RefParam
   */
  RefParam create(RefParamRequest request);
  /**
   * update RefParam.
   *
   * @param request the request to update the RefParam from
   * @return the updated RefParam
   */
  RefParam update(RefParamRequest request);

  /**
   * Delete a RefParam.
   *
   * @param id the id of the RefParam to delete
   */
  void delete(Integer id);
}
