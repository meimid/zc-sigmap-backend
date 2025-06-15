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
    @Transactional(rollbackOn = Exception.class)
    public Direction create(DirectionRequest request) throws AcNotFound {
        if(request.getLibelle() == null) {
            throw new LibelleRequired("");
        }
        Specification<Direction> specification = DirectionSpecifications.deleted(false).and(DirectionSpecifications.libelle(request.getLibelle()));
        if(findOne(specification).isPresent()) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
        Direction direction = DirectionMapper.INSTANCE.mapFromRequest(request);
        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            List<Integer> acIds = request.getAcIds();


            int size = getSizeList(acIds);

            // Check if all Ac IDs exist
            var acs = acRepository.findAllById(acIds);
            if (acs.size() != size) {
                List<Integer> existingAcIds = acs.stream()
                        .map(Ac::getId)
                        .toList();
                List<Integer> nonExistentAcIds = acIds.stream()
                        .filter(acId -> !existingAcIds.contains(acId))
                        .toList();
                throw new AcNotFound("", nonExistentAcIds);
            }
             List<Integer> deletedAcs = acs.stream().filter(BaseEntity::getIsDeleted).map(Ac::getId).toList();
            if(!deletedAcs.isEmpty()) {
                throw new AcNotFound("", deletedAcs);
            }
            for (Integer acId : acIds) {
                    updateDirectionAssociation(acId, direction, true);
            }
            direction.setAcs(acs);
        }
        else {
            direction.setAcs(new ArrayList<>());
        }
            return save(direction);
    }

    @Override
    public Direction update(Integer id, DirectionRequest request) throws DirectionNotFound, AcNotFound {
        if(request.getLibelle() == null) {
            throw new LibelleRequired("");
        }
        Direction direction = findById(id);
        Specification<Direction> specification = DirectionSpecifications.deleted(false).and(DirectionSpecifications.libelle(request.getLibelle()));
        if(findOne(specification).isPresent()&&!findOne(specification).get().getId().equals(id)) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
        DirectionMapper.INSTANCE.updateFromRequest(request,direction);
        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            List<Integer> acIds = request.getAcIds();

            int size = getSizeList(acIds);

            // Check if all Ac IDs exist
            var acs = acRepository.findAllById(acIds);
            if (acs.size() != size) {
                List<Integer> existingAcIds = acs.stream()
                        .map(Ac::getId)
                        .toList();
                List<Integer> nonExistentAcIds = acIds.stream()
                        .filter(acId -> !existingAcIds.contains(acId))
                        .toList();
                throw new AcNotFound("", nonExistentAcIds);
            }
            List<Integer> deletedAcs = acs.stream().filter(BaseEntity::getIsDeleted).map(Ac::getId).toList();
            if(!deletedAcs.isEmpty()) {
                throw new AcNotFound("", deletedAcs);
            }
            List<Integer> oldAcIds = direction.getAcs().stream().filter(ac->!ac.getIsDeleted()).map(Ac::getId).toList();
            direction.setAcs(acs);
            for (Integer acId : acIds) {
                if (!oldAcIds.contains(acId)) {
                    updateDirectionAssociation(acId, direction, true);
                }
            }

            for (Integer acId : oldAcIds) {
                if (!acIds.contains(acId)) {
                    updateDirectionAssociation(acId, direction, false);
                }
            }
        }

        return save(direction);
    }

    @Override
    public void delete(Integer id) {
        findById(id);
        repository.softDelete(id);
    }

    @Override
    public Direction createUnderAc(Integer acId, DirectionunderAcRequest request) throws AcNotFound {
        if(request.getLibelle() == null | request.getLibelle().isEmpty()) {
            throw new LibelleRequired("");
        }
        Specification<Direction> specification = DirectionSpecifications.deleted(false).and(DirectionSpecifications.libelle(request.getLibelle()));
        if(findOne(specification).isPresent()) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
        Ac ac=acService.findById(acId);
        Direction direction= DirectionMapper.INSTANCE.mapFromDirectionUnderAcRequest(request);
        direction.setAcs(new ArrayList<>());
        direction.getAcs().add(ac);
        updateDirectionAssociation(acId, direction, true);

        return save(direction);
    }

    @Override
    public Direction updateUnderAc(Integer acId, Integer id, DirectionunderAcRequest request) throws DirectionNotFound, AcNotFound {
        if(request.getLibelle() == null | request.getLibelle().isEmpty()) {
            throw new LibelleRequired("");
        }
       Ac ac= acService.findById(acId);
        Direction direction = findById(id);

        if(!direction.getAcs().contains(ac)) {
            throw new DirectionNotBelongToAc("",id,acId);
        }
        Specification<Direction> specification = DirectionSpecifications.deleted(false).and(DirectionSpecifications.libelle(request.getLibelle()));
        if(findOne(specification).isPresent()&&!findOne(specification).get().getId().equals(id)) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
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

        if(ac.getDirections()==null || ac.getDirections().isEmpty()) {
            ac.setDirections(new ArrayList<>());
        }
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
