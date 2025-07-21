package com.sigmap.common.ModeDePassationSelection;


import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ModeDePassationSelection.request.ModeDePassationSelectionRequest;

import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import java.util.List;
import java.util.Optional;

public interface ModeDePassationSelectionService {
    List<ModeDePassationSelection> findBytypeMarcherId(Integer typeMarcherId) throws RefParamNotFound;
    List<ModeDePassationSelection> findAll();

    /**
     * Return a page of ModeDePassationSelection based on the given specification and pageable.
     *
     * @param ModeDePassationSelection the specification to use
     * @param pageable the pageable to use
     * @return a page of ModeDePassationSelection
     */
    Page<ModeDePassationSelection> findAll(Specification<ModeDePassationSelection> ModeDePassationSelection, Pageable pageable);

    /**
     * Return a page of deleted ModeDePassationSelection based on the given specification and pageable.
     *
     * @param ModeDePassationSelection the specification to use
     * @param pageable the pageable to use
     * @return a page of ModeDePassationSelection
     */
    Page<ModeDePassationSelection> findAllDeleted(Specification<ModeDePassationSelection> ModeDePassationSelection, Pageable pageable);

    /**
     * Return a page of deleted ModeDePassationSelection based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of ModeDePassationSelection
     */
    Page<ModeDePassationSelection> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted ModeDePassationSelection with the given id.
     *
     * @param id the id of the ModeDePassationSelection to return
     * @return the ModeDePassationSelection with the given id
     */
    Optional<ModeDePassationSelection> findDeletedById(Integer id);

    /**
     * Return the ModeDePassationSelection with the given specification.
     *
     * @param specification the specification to use
     * @return the ModeDePassationSelection with the given specification
     */
    Optional<ModeDePassationSelection> findOne(Specification<ModeDePassationSelection> specification);

    /**
     * Return the ModeDePassationSelection with the given id.
     *
     * @param id the id of the ModeDePassationSelection to return
     * @return the ModeDePassationSelection with the given id
     */
    ModeDePassationSelection findById(Integer id) throws ModeDePassationSelectionNotFound;

    /**
     * Save a ModeDePassationSelection.
     *
     * @param ModeDePassationSelection the ModeDePassationSelection to save
     * @return the saved ModeDePassationSelection
     */
    ModeDePassationSelection save(ModeDePassationSelection ModeDePassationSelection);

    /**
     * Create a ModeDePassationSelection.
     *
     * @param request the request to create the ModeDePassationSelection from
     * @return the created ModeDePassationSelection
     */
    ModeDePassationSelection create(ModeDePassationSelectionRequest request) throws  RefParamNotFound;

    /**
     * update ModeDePassationSelection.
     *
     * @param request the request to create the ModeDePassationSelection from
     * @return the updated ModeDePassationSelection
     */
    ModeDePassationSelection update(Integer id,ModeDePassationSelectionRequest request) throws ModeDePassationSelectionNotFound, RefParamNotFound;

    /**
     * Delete a ModeDePassationSelection.
     *
     * @param id the id of the ModeDePassationSelection to delete
     */
    void delete(Integer id) throws ModeDePassationSelectionNotFound;

}

