package com.sigmap.common.direction;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.direction.exception.*;
import com.sigmap.common.direction.mappers.DirectionMapper;
import com.sigmap.common.direction.request.DirectionRequest;

import com.sigmap.common.utils.SequenceGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DirectionServiceImpl implements DirectionService {
    private final DirectionRepository repository;
    private final AcService acService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public DirectionServiceImpl(DirectionRepository repository, AcService acService, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.acService = acService;
        this.sequenceGeneratorService = sequenceGeneratorService;
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
    public Optional<Direction> findDeletedByIds(Integer directionid,Integer acId) {
        Specification<Direction> specification = DirectionSpecifications.deleted(true)
                .and( DirectionSpecifications.isMemberOfAc(acId)).and(DirectionSpecifications.id(directionid));
        return findOne(specification);
    }

    @Override
    public Optional<Direction> findOne(Specification<Direction> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Direction findByIds(Integer directionId,Integer acId) throws DirectionNotFound {
        Specification<Direction> specification = DirectionSpecifications.deleted(false).
                and(DirectionSpecifications.id(directionId)).and( DirectionSpecifications.isMemberOfAc(acId));
        return findOne(specification).orElseThrow(()->new DirectionNotFound("",directionId,acId));
    }

    @Override
    public Direction save(Direction direction) {
        return repository.save(direction);
    }


    @Override
    public void delete(Integer directionId, Integer acId) {
        findByIds(directionId,acId);
        repository.softDelete(directionId,acId);
    }

    @Override
    public Direction createUnderAc(Integer acId, DirectionRequest request) throws AcNotFound {
        if(request.getLibelle() == null || request.getLibelle().isEmpty()) {
            throw new LibelleRequired("");
        }
        Specification<Direction> specification = DirectionSpecifications.deleted(false).
                and(DirectionSpecifications.libelle(request.getLibelle())).and( DirectionSpecifications.isMemberOfAc(acId));
        if(findOne(specification).isPresent()) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }
        Ac ac=acService.findById(acId);
        Direction direction= DirectionMapper.INSTANCE.mapFromRequest(request);
        direction.setAcId(ac.getId());
        direction.setId( sequenceGeneratorService.getNextId(Constants.DIRECTION_SEQ));
        direction = save(direction);

         return direction;
    }

    @Override
    public Direction updateUnderAc(Integer acId, Integer id, DirectionRequest request) throws DirectionNotFound, AcNotFound {
        if(request.getLibelle() == null | request.getLibelle().isEmpty()) {
            throw new LibelleRequired("");
        }
        acService.findById(acId);
        Specification<Direction> specification = DirectionSpecifications.deleted(false).
                and(DirectionSpecifications.libelle(request.getLibelle())).and( DirectionSpecifications.isMemberOfAc(acId));
        if(findOne(specification).isPresent()&&!findOne(specification).get().getId().equals(id)) {
            throw new DirectionAlreadyExist("", request.getLibelle());
        }

        Direction direction = findByIds(id,acId);
        direction.setLibelle(request.getLibelle());
        return save(direction);
    }

    @Override
    public Page<Direction> findAllPageableUnderAc(Integer acId, Pageable pageable) throws AcNotFound {
        acService.findById(acId);
        Specification<Direction> spec = DirectionSpecifications.isMemberOfAc(acId).and(DirectionSpecifications.deleted(false));
        return repository.findAll(spec, pageable);
    }

    @Override
    public List<Direction> findAllListUnderAc(Integer acId) throws AcNotFound {
        acService.findById(acId);
        Specification<Direction> spec = DirectionSpecifications.isMemberOfAc(acId).and(DirectionSpecifications.deleted(false));
        return repository.findAll(spec);

    }


}
