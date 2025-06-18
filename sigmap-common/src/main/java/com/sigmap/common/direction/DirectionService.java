package com.sigmap.common.direction;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.direction.exception.DirectionNotFound;
import com.sigmap.common.direction.request.DirectionRequest;
import com.sigmap.common.direction.request.DirectionunderAcRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DirectionService {
    List<Direction> findAll();

    /**
     * Return a page of Direction based on the given specification and pageable.
     *
     * @param Direction the specification to use
     * @param pageable the pageable to use
     * @return a page of Direction
     */
    Page<Direction> findAll(Specification<Direction> Direction, Pageable pageable);

    /**
     * Return a page of deleted Direction based on the given specification and pageable.
     *
     * @param Direction the specification to use
     * @param pageable the pageable to use
     * @return a page of Direction
     */
    Page<Direction> findAllDeleted(Specification<Direction> Direction, Pageable pageable);

    /**
     * Return a page of deleted Direction based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Direction
     */
    Page<Direction> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Direction with the given id.
     *
     * @param id the id of the Direction to return
     * @return the Direction with the given id
     */
    Optional<Direction> findDeletedById(Integer id);

    /**
     * Return the Direction with the given specification.
     *
     * @param specification the specification to use
     * @return the Direction with the given specification
     */
    Optional<Direction> findOne(Specification<Direction> specification);

    /**
     * Return the Direction with the given id.
     *
     * @param id the id of the Direction to return
     * @return the Direction with the given id
     */
    Direction findById(Integer id) throws DirectionNotFound;

    /**
     * Save a Direction.
     *
     * @param Direction the Direction to save
     * @return the saved Direction
     */
    Direction save(Direction Direction);

    /**
     * Delete a Direction.
     *
     * @param id the id of the Direction to delete
     */
    void delete(Integer id) ;

    public Direction createUnderAc(Integer acId, DirectionRequest request) throws AcNotFound;

    public Direction updateUnderAc(Integer acId,Integer id, DirectionRequest request) throws DirectionNotFound, AcNotFound;

    Page<Direction> findAllunderAc(Integer acId, Pageable pageable) throws AcNotFound;
}
