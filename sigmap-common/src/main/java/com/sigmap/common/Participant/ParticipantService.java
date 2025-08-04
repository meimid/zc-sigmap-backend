package com.sigmap.common.Participant;

import com.sigmap.common.Participant.exception.ParticipantNotFound;
import com.sigmap.common.Participant.request.ParticipantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantService {
    List<Participant> findAll();
    Page<Participant> findAll(Specification<Participant> spec, Pageable pageable);
    List<Participant> findAllList(Specification<Participant> spec);
    Participant findById(Integer id,Integer reunionId) throws ParticipantNotFound;
    Participant save(Participant participant);
    Participant create(ParticipantRequest request);
    Participant update(Integer id,Integer reunionId, ParticipantRequest request) throws ParticipantNotFound;
    void delete(Integer id,Integer reunionId) throws ParticipantNotFound;
    Optional<Participant> findOne(Specification<Participant> spec);
    Optional<Participant> findOneByUserIdAndReunionId(String userId, Integer reunionId);
    void softDeleteAll( List<Participant> members);
    List<Participant> saveAll( List<Participant> members);
}
