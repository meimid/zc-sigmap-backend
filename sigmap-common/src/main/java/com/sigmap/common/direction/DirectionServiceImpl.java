package com.sigmap.common.direction;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.exception.DuplicateAcIdsException;
import com.sigmap.common.direction.exception.*;
import com.sigmap.common.direction.mappers.DirectionMapper;
import com.sigmap.common.direction.request.DirectionRequest;
import com.sigmap.common.direction.request.DirectionunderAcRequest;
import com.sigmap.common.entity.BaseEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DirectionServiceImpl implements DirectionService {
    private final DirectionRepository repository;
    private final AcService acService;
    private final AcRepository acRepository;
    public DirectionServiceImpl(DirectionRepository repository, AcService acService, AcRepository acRepository) {
        this.repository = repository;
        this.acService = acService;
        this.acRepository = acRepository;
    }

    @Override
    public List<Direction> findAll() {
        Specification<Direction> spec = DirectionSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Direction> findAll(Specification<Direction> direction, Pageable pageable) {
        return repository.findAll(direction, pageable);
    }

    @Override
    public Page<Direction> findAllDeleted(Specification<Direction> direction, Pageable pageable) {
        direction = DirectionSpecifications.deleted(true);
        return repository.findAll(direction, pageable);
    }

    @Override
    public Page<Direction> findAllDeleted(Pageable pageable) {
        Specification<Direction> direction = DirectionSpecifications.deleted(true);
        return repository.findAll(direction, pageable);
    }

    @Override
    public Optional<Direction> findDeletedById(Integer id) {
        Specification<Direction> specification =
                DirectionSpecifications.id(id).and(DirectionSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Direction> findOne(Specification<Direction> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Direction findById(Integer id) throws DirectionNotFound {
        Specification<Direction> specification =
                DirectionSpecifications.id(id).and(DirectionSpecifications.deleted(false));
        return findOne(specification).orElseThrow(() -> new DirectionNotFound("", id));
    }

    @Override
    public Direction save(Direction direction) {
        return repository.save(direction);
    }


    @Override
    public void delete(Integer id) {
        findById(id);
        repository.softDelete(id);
    }

    @Override
    public Direction createUnderAc(Integer acId, DirectionRequest request) throws AcNotFound {
        if(request.getLibelle() == null | request.getLibelle().isEmpty()) {
            throw new LibelleRequired("");
        }
        Specification<Direction> specification = DirectionSpecifications.deleted(false).and(DirectionSpecifications.libelle(request.getLibelle())).and(DirectionSpecifications.hasAcWithId(acId));
        if(findOne(specification).isPresent()) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
        Ac ac=acService.findById(acId);
        Direction direction= DirectionMapper.INSTANCE.mapFromRequest(request);
        direction.setAc(ac);
        //ac.getDirections().add(direction);
      //  updateDirectionAssociation(acId, direction, true);

        return save(direction);
    }

    @Override
    public Direction updateUnderAc(Integer acId, Integer id, DirectionRequest request) throws DirectionNotFound, AcNotFound {
        if(request.getLibelle() == null | request.getLibelle().isEmpty()) {
            throw new LibelleRequired("");
        }
        Specification<Direction> specification = DirectionSpecifications.deleted(false).and(DirectionSpecifications.libelle(request.getLibelle())).and(DirectionSpecifications.hasAcWithId(acId));
        if(findOne(specification).isPresent()) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
       Ac ac= acService.findById(acId);
        Direction direction = findById(id);
        direction.setAc(ac);
        direction.setLibelle(request.getLibelle());
        return save(direction);
    }

    @Override
    public Page<Direction> findAllunderAc(Integer acId, Pageable pageable) throws AcNotFound {
        acService.findById(acId);
        Specification<Direction> spec = DirectionSpecifications.hasAcWithId(acId).and(DirectionSpecifications.deleted(false));
        return repository.findAll(spec, pageable);
    }


    private static int getSizeList(List<Integer> listIds) throws AcNotFound, DuplicateAcIdsException {
        int size = listIds.size();
        List<Integer> duplicateIds = new ArrayList<>();

        // null check for AcIds elements
        for (int i = 0; i < size; i++) {
            if (listIds.get(i) == null ) {
                throw new AcNotFound("" , List.of(listIds.get(i)));
            }
            for (int j = i + 1; j < size; j++) {
                if (listIds.get(i).equals(listIds.get(j))) {
                    duplicateIds.add(listIds.get(j));
                }
            }
            if (!duplicateIds.isEmpty()) {
                throw new DuplicateAcIdsException("", duplicateIds);
            }
        }
        return size;
    }
    @Transactional
    public void updateDirectionAssociation(Integer acId, Direction direction, boolean shouldBeAssociated) throws AcNotFound {
        Ac ac = acService.findById(acId);

       /* if(ac.getDirections()==null || ac.getDirections().isEmpty()) {
            ac.setDirections(new ArrayList<>());
        }*/
        if (shouldBeAssociated) {
            // Logic to add the direction to the Ac's list
            if (!ac.getDirections().contains(direction)) {
                ac.getDirections().add(direction);
            }
        } else {
            // Logic to remove the direction from the Ac's list
            ac.getDirections().remove(direction);
        }

    }

}
