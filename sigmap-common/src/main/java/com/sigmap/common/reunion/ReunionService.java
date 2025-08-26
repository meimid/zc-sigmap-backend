package com.sigmap.common.reunion;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.request.ReunionRequest;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ReunionService {
    List<Reunion> findAll();
    Page<Reunion> findAll(Specification<Reunion> spec, Pageable pageable);
    Page<Reunion> findAllDeleted(Specification<Reunion> spec, Pageable pageable);
    Page<Reunion> findAllDeleted(Pageable pageable);
    Optional<Reunion> findDeletedById(Integer id);
    Optional<Reunion> findOne(Specification<Reunion> spec);
    Reunion findById(Integer id) throws ReunionNotFound;
    Reunion save(Reunion reunion);
    Reunion create(ReunionRequest request) throws ComNotFound, PpmNotFound, AcNotFound;
    Reunion updateBrillon(Integer id, ReunionRequest request,ReunionStatus status) throws ReunionNotFound, PpmNotFound, AcNotFound;
    Reunion updatePlanifierOrNot(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException;
    Reunion updateAnnule(Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound;

    void delete(Integer id) throws ReunionNotFound;

    void sendReunionInvitations(Integer id) throws MessagingException, IOException;
    void updateReunionStatusToEnRetard();
    Reunion startReunion(Integer id) throws ReunionNotFound;



}
