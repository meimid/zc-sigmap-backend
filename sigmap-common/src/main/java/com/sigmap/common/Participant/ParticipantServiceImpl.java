package com.sigmap.common.Participant;

import com.sigmap.common.Participant.exception.ParticipantNotFound;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.utils.SequenceGeneratorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public ParticipantServiceImpl(ParticipantRepository repository,  SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public List<Participant> findAll() {
        return repository.findAll(ParticipantSpecifications.deleted(false));
    }

    @Override
    public Page<Participant> findAll(Specification<Participant> spec, Pageable pageable) {
        Specification<Participant> finalSpec = Specification.where(spec).and(ParticipantSpecifications.deleted(false));
        return repository.findAll(finalSpec, pageable);
    }

    @Override
    public List<Participant> findAllList(Specification<Participant> spec) {
        Specification<Participant> finalSpec = Specification.where(spec).and(ParticipantSpecifications.deleted(false));
        return repository.findAll(finalSpec);    }

    @Override
    public Optional<Participant> findOne(Specification<Participant> spec) {
        return repository.findOne(spec);
    }

    @Override
    public Optional<Participant> findOneByUserIdAndReunionId(String userId, Integer reunionId) {
        return repository.findParticipantByUserIdAndReunionId(userId,reunionId);
    }

    @Override
    public void softDeleteAll(List<Participant> members) {
        repository.softDeleteAll(members);
    }

    @Override
    public List<Participant> saveAll(List<Participant> members) {
        return repository.saveAll(members);
    }

    @Override
    public List<Participant> findByReunionId(Integer reunionId) {
       return repository.findByReunionIdAndIsDeletedFalse(reunionId);
    }

    @Override
    public Participant findById(Integer id, Integer reunionId) throws ParticipantNotFound {
        ParticipantId compositeId = new ParticipantId();
        compositeId.setId(id);
        compositeId.setReunionId(reunionId);
        return repository.findById(compositeId)
                .orElseThrow(() -> new ParticipantNotFound("Participant not found with id: " , id));
    }

    @Override
    public Participant save(Participant participant) {
        return repository.save(participant);
    }

    @Override
    @Transactional
    public Participant create(ParticipantRequest request) {
        Participant participant = ParticipantMapper.INSTANCE.map(request);
        participant.setId(sequenceGeneratorService.getNextId("participant_id_seq"));
        return repository.save(participant);
    }

    @Override
    @Transactional
    public Participant update(Integer id, Integer reunionId, ParticipantRequest request) throws ParticipantNotFound {
        Participant participantToUpdate = findById(id, reunionId);
        ParticipantMapper.INSTANCE.updateFromRequest(request, participantToUpdate);
        return repository.save(participantToUpdate);
    }

    @Override
    @Transactional
    public void delete(Integer id, Integer reunionId) throws ParticipantNotFound {
       findById(id, reunionId);
        repository.softDelete(id, reunionId); 
    }


}
