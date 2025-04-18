package com.sigmap.common.menu_item;


import com.sigmap.common.menu_item.exception.MenuItemAlreadyExistsExeption;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.request.MenuItemRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link MenuItem}.
 * The service provides methods to retrieve and manage Paa entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link MenuItemSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see MenuItemSpecifications
 * @see Specification
 */
public interface MenuItemService {

    List<MenuItem> findAll();

    /**
     * Return a page of MenuItem entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of MenuItem entities
     */
    Page<MenuItem> findAll(Specification<MenuItem> spec, Collection<String> userAuthorities, Pageable pageable);


    /**
     * Return a page of MenuItem entities for admin based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of MenuItem entities
     */
    Page<MenuItem> findAllForAdmin(Specification<MenuItem> spec, Pageable pageable);

    /**
     * Return a page of deleted MenuItem entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of MenuItem entities
     */
    Page<MenuItem> findAllDeleted(Specification<MenuItem> spec, Pageable pageable);

    /**
     * Return a page of deleted MenuItem entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of MenuItem entities
     */
    Page<MenuItem> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted MenuItem entity with the given ID components.
     *
     * @param id the Id of MenuItem entity
     * @return the MenuItem entity with the given ID
     */
    Optional<MenuItem> findDeletedById(Integer id);

    /**
     * Return the MenuItem entity with the given specification.
     *
     * @param specification the specification to use
     * @return the MenuItem entity with the given specification
     */
    Optional<MenuItem> findOne(Specification<MenuItem> specification);

    /**
     * Return the MenuItem entity with the given ID components.
     * @param id the ID of MenuItem entity
     * @return the MenuItem entity with the given ID
     */
    Optional<MenuItem> findById(Integer id);

    /**
     * Save a MenuItem entity.
     *
     * @param menuItem the MenuItem entity to save
     * @return the saved MenuItem entity
     */
    MenuItem save(MenuItem menuItem);

    /**
     * Create a MenuItem entity.
     *
     * @param request the request to create the MenuItem entity from
     * @return the created MenuItem entity
     */
    MenuItem create(MenuItemRequest request) throws MenuItemAlreadyExistsExeption;

    /**
     * Delete a MenuItem entity by its ID components.
     *
     * @param id the ID of the MenuItem entity
     */
    void delete(Integer id);

    /**
     * Update a MenuItem entity by its ID components, with a MenuItemRequest given.
     *
     * @param id the ID of the MenuItem entity
     * @param request the MenuItemRequest to update the MenuItem entity with
     */
    MenuItem update(Integer id,  MenuItemRequest request) throws MenuItemNotFound;

}
