package com.sigmap.common.reunion;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.ExtraitResponse;
import com.sigmap.common.reunion.response.PvResponse;
import com.sigmap.common.reunion.response.ReunionWithParticipantsAndPvDto;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ReunionService {
    List<Reunion> findAll();
    List<Reunion> findAll(Specification<Reunion> spec);
    Page<Reunion> findAll(Specification<Reunion> spec, Pageable pageable);
    Page<Reunion> findAllDeleted(Specification<Reunion> spec, Pageable pageable);
    Page<Reunion> findAllDeleted(Pageable pageable);
    Optional<Reunion> findDeletedById(Integer id);
    Optional<Reunion> findOne(Specification<Reunion> spec);
    Reunion findById(Integer id) throws ReunionNotFound;
    Reunion save(Reunion reunion);
    Reunion create(ReunionRequest request) throws ComNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound;
    Reunion updateBrillon(Integer id, ReunionRequest request,ReunionStatus status) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound;
    Reunion updatePlanifierOrNot(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException, DocNotFound;
    Reunion updateAnnule(Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException;

    void delete(Integer id) throws ReunionNotFound;

    void sendReunionInvitations(Integer id) throws MessagingException, IOException;
    void updateReunionStatusToEnRetard();
    Reunion startReunion(Integer id) throws ReunionNotFound;
    Reunion updateReunionAfterStart(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound;
    Reunion validateReunion(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound;
    ProcessVerbalReunion injectPv(Integer reunionId, MultipartFile file) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound;
     boolean checkParticipantPresenceWithReunionId(Integer id) throws ReunionNotFound;
    List<Reunion> findAllByPpmIdListForAdminCentralAndSuperAdmin(Integer ppmId,Specification<Reunion> specifications) ;
    List<Reunion> findAllByPpmIdListForUserCom(Integer ppmId,String userId,Specification<Reunion> specifications) ;
    List<Reunion> findAllByPpmIdList(Integer ppmId,Specification<Reunion> specifications);
    ReunionWithParticipantsAndPvDto particpantHistoryByReunionIdAndPPmIdForAdminCentralAndSuperAdmin(Integer reunionId, Integer ppmId) ;
    ReunionWithParticipantsAndPvDto particpantHistoryByReunionIdAndPPmIdForUserCom(Integer reunionId, Integer ppmId,String userId) ;
    ReunionWithParticipantsAndPvDto particpantHistoryByReunionIdAndPPmId(Integer reunionId, Integer ppmId) ;
    List<Reunion> findAllByPpmIdAndOptionalComId(Integer ppmId, Integer comId, Specification<Reunion> specifications);
    ProcessVerbalReunionExtrait injectExtraitForAc(Integer reunionId, Integer ppmId, MultipartFile file) throws IOException;
    Page<ProcessVerbalReunionExtrait> findAllProcessVerbalReunionExtraitByReunionId(Integer reunionId,Pageable pageable);




}
