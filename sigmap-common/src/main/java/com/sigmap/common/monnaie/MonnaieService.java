package com.sigmap.common.monnaie;

import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.monnaie.request.MonnaieRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Monnaie}.
 * The service provides methods to retrieve and manage Monnaies.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link MonnaieSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see MonnaieSpecifications
 * @see Specification
 */
public interface MonnaieService {
  List<Monnaie> findAll();

  /**
   * Return a page of Monnaies based on the given specification and pageable.
   *
   * @param spec the specification to use
   * @param pageable the pageable to use
   * @return a page of Monnaies
   */
  Page<Monnaie> findAll(Specification<Monnaie> spec, Pageable pageable);

  /**
   * Return a page of deleted Monnaies based on the given specification and pageable.
   *
   * @param spec the specification to use
   * @param pageable the pageable to use
   * @return a page of Monnaies
   */
  Page<Monnaie> findAllDeleted(Specification<Monnaie> spec, Pageable pageable);

  /**
   * Return a page of deleted Monnaies based on the given pageable.
   *
   * @param pageable the pageable to use
   * @return a page of Monnaies
   */
  Page<Monnaie> findAllDeleted(Pageable pageable);

  /**
   * Return the deleted Monnaie with the given id.
   *
   * @param id the id of the Monnaie to return
   * @return the Monnaie with the given id
   */
  Optional<Monnaie> findDeletedById(Integer id);

  /**
   * Return the Monnaie with the given specification.
   *
   * @param specification the specification to use
   * @return the Monnaie with the given specification
   */
  Optional<Monnaie> findOne(Specification<Monnaie> specification);

  /** 
   * Return the Monnaie with the given id.
   * 
   * @param id the id of the Monnaie to return
   * @return the Monnaie with the given id
   */
  Monnaie findById(Integer id)throws MonnaieNotFound;

  /** 
   * Save a Monnaie.
   * 
   * @param Monnaie the Monnaie to save
   * @return the saved Monnaie
   */
  Monnaie save(Monnaie Monnaie);

  /** 
   * Create a Monnaie.
   * 
   * @param request the request to create the Monnaie from
   * @return the created Monnaie
   */
  Monnaie create(MonnaieRequest request) ;
  /**
   * update Monnaie.
   *
   * @param request the request to update the Monnaie from
   * @return the updated Monnaie
   */
  Monnaie update(Integer id,MonnaieRequest request)throws  MonnaieNotFound ;

  /**
   * Delete a Monnaie.
   *
   * @param id the id of the Monnaie to delete
   */
  void delete(Integer id) throws MonnaieNotFound;


}
