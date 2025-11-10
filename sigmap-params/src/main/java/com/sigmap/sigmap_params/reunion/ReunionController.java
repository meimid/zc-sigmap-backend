package com.sigmap.sigmap_params.reunion;


import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.response.DacResponseReunion;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Response.DocsResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.email_trace.exeptions.EmailTraceNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.process_verbal.response.ProcessVerbalResponse;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion.mapper.ProcessVerbalReunionMapper;
import com.sigmap.common.process_verbal_reunion.response.ProcessVerbalReunionResponse;
import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.process_verbal_reunion_doc.response.ProcessVerbalReunionDocResponse;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.mapper.ReunionMapper;
import com.sigmap.common.reunion.request.ReunionQuery;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.ReunionDropListDto;
import com.sigmap.common.reunion.response.ReunionResponse;
import com.sigmap.common.reunion.response.ReunionResponseDto;
import com.sigmap.common.reunion.response.ReunionWithParticipantsAndPvDto;
import com.sigmap.common.reunionDac.response.ReunionDacResponse;
import com.sigmap.common.reunionPpm.response.ReunionPpmResponse;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import com.sigmap.common.security.Authentication;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reunions")
@Slf4j
public class ReunionController {

    private final ReunionService reunionService;

    public ReunionController(ReunionService reunionService) {
        this.reunionService = reunionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<Page<ReunionResponseDto>> findAll(ReunionQuery query, Pageable pageable) {
        Page<Reunion> page = reunionService.findAll(query.toSpecification(), pageable);
        return ResponseEntity.ok(page.map(r ->
                ReunionMapper.INSTANCE.mapToDto(r)

        ));
    }

   /* @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ReunionResponse>> findAllDeleted(ReunionQuery query, Pageable pageable) {
        Page<Reunion> page = reunionService.findAllDeleted(query.toSpecification(), pageable);
        return ResponseEntity.ok(page.map(ReunionMapper.INSTANCE::map));
    }*/

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> findById(@PathVariable Integer id) throws ReunionNotFound {
        Reunion reunion = reunionService.findById(id);
        ReunionResponse response=ReunionMapper.INSTANCE.map(reunion);
        response.setReunionDacs(mapReunionDacs(reunion.getReunionDacs()));
        response.setReunionPpms(mapReunionPpms(reunion.getReunionPpms()));
      if(response.getProcessVerbal()!=null){
          DocsResponseDTO pvDocDto = mapPvmDocs(reunion.getProcessVerbal().getDocuments());

          response.getProcessVerbal().setDocument(
                  pvDocDto != null
                          ? new ProcessVerbalReunionDocResponse(
                          reunion.getProcessVerbal().getId(),
                          pvDocDto.getId(),
                          pvDocDto,
                          true
                  )
                          : null
          );
      }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> create(@RequestBody ReunionRequest request) throws PpmNotFound, ComNotFound, AcNotFound, IOException, DocNotFound {
        Reunion createdReunion = reunionService.create(request);
        ReunionResponse response=ReunionMapper.INSTANCE.map(createdReunion);

        response.setReunionDacs(mapReunionDacs(createdReunion.getReunionDacs()).stream().filter(el->el.getDac()!=null).toList());
        response.setParticipants(response.getParticipants().stream().filter(el->el.getId()!=null).toList());
        response.setReunionPpms(mapReunionPpms(createdReunion.getReunionPpms().stream().filter(el->el.getPpm()!=null).toList()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/brouillon")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> updateBrouillon(@PathVariable Integer id, @RequestBody ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {
        Reunion updatedReunion = reunionService.updateBrillon(id, request,ReunionStatus.BROUILLON);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setParticipants(responseDtos);
        response.setReunionDacs(mapReunionDacs(updatedReunion.getReunionDacs()));
        response.setReunionPpms(mapReunionPpms(updatedReunion.getReunionPpms()));

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}/planifier_or_not")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> updatePlanifierOrNot(@PathVariable Integer id, @RequestBody ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException, DocNotFound {
        Reunion updatedReunion = reunionService.updatePlanifierOrNot(id, request);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setReunionDacs(mapReunionDacs(updatedReunion.getReunionDacs()));
        response.setReunionPpms(mapReunionPpms(updatedReunion.getReunionPpms()));
        response.setParticipants(responseDtos);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> updateAnnuler(@PathVariable Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException {
        Reunion updatedReunion = reunionService.updateAnnule(id);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setParticipants(responseDtos);
        response.setReunionDacs(mapReunionDacs(updatedReunion.getReunionDacs()));
        response.setReunionPpms(mapReunionPpms(updatedReunion.getReunionPpms()));
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> deleteById(@PathVariable Integer id) throws ReunionNotFound {
        reunionService.delete(id);
        Reunion deletedReunion = reunionService.findDeletedById(id)
                .orElseThrow(() -> new ReunionNotFound("Could not find reunion after deletion: " , id));
        ReunionResponse response=ReunionMapper.INSTANCE.map(deletedReunion);
        response.setReunionDacs(mapReunionDacs(deletedReunion.getReunionDacs()));
        response.setReunionPpms(mapReunionPpms(deletedReunion.getReunionPpms()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> startReunion(@PathVariable Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion updatedReunion = reunionService.startReunion(id);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setParticipants(responseDtos);
        response.setReunionDacs(mapReunionDacs(updatedReunion.getReunionDacs()));
        response.setReunionPpms(mapReunionPpms(updatedReunion.getReunionPpms()));
        return ResponseEntity.ok(response);
    }
    @PutMapping(
            value = "/{id}/update-after-start",
            consumes = {"multipart/form-data"}
    )
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")
    public ResponseEntity<ReunionResponse> updateReunionAfterStart(
            @PathVariable Integer id,
            @ModelAttribute ReunionRequest request
    ) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {

        Reunion updatedReunion = reunionService.updateReunionAfterStart(id, request);

        // filter participants
        List<Participant> participantList = updatedReunion.getParticipants()
                .stream()
                .filter(el -> !el.getIsDeleted())
                .toList();

        List<ParticipantResponseDto> responseDtos = ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response = ReunionMapper.INSTANCE.map(updatedReunion);

        response.setReunionDacs(mapReunionDacs(updatedReunion.getReunionDacs()));
        response.setParticipants(responseDtos);
        response.setReunionPpms(mapReunionPpms(updatedReunion.getReunionPpms()));


        DocsResponseDTO pvDocDto = mapPvmDocs(updatedReunion.getProcessVerbal().getDocuments());

        response.getProcessVerbal().setDocument(
                pvDocDto != null
                        ? new ProcessVerbalReunionDocResponse(
                        updatedReunion.getProcessVerbal().getId(),
                        pvDocDto.getId(),
                        pvDocDto,
                        true
                )
                        : null
        );
                return ResponseEntity.ok(response);
    }

    @PutMapping(
            value = "/{id}/validate",
            consumes = {"multipart/form-data"}
    )
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")
    public ResponseEntity<ReunionResponse> validateReunion(
            @PathVariable Integer id,
            @ModelAttribute ReunionRequest request
    ) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {

        Reunion updatedReunion = reunionService.validateReunion(id, request);

        // filter participants
        List<Participant> participantList = updatedReunion.getParticipants()
                .stream()
                .filter(el -> !el.getIsDeleted())
                .toList();

        List<ParticipantResponseDto> responseDtos = ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response = ReunionMapper.INSTANCE.map(updatedReunion);

        response.setReunionDacs(mapReunionDacs(updatedReunion.getReunionDacs()));
        response.setParticipants(responseDtos);
        response.setReunionPpms(mapReunionPpms(updatedReunion.getReunionPpms()));


        DocsResponseDTO pvDocDto = mapPvmDocs(updatedReunion.getProcessVerbal().getDocuments());

        response.getProcessVerbal().setDocument(
                pvDocDto != null
                        ? new ProcessVerbalReunionDocResponse(
                        updatedReunion.getProcessVerbal().getId(),
                        pvDocDto.getId(),
                        pvDocDto,
                        true
                )
                        : null
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(
            value = "/{reunionId}/upload-pv",
            consumes = {"multipart/form-data"}
    )
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")
    public ResponseEntity<DocsResponseDTO> injectPv(
            @PathVariable Integer reunionId,
            @RequestParam("file") MultipartFile file
    ) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {

        ProcessVerbalReunion pv = reunionService.injectPv(reunionId, file);

        DocsResponseDTO pvDocDto = mapPvmDocs(pv.getDocuments());


        return ResponseEntity.ok(pvDocDto);
    }


    @PostMapping("/{id}/send-email-reuinion")
    public ResponseEntity<Void> sendEmail (@PathVariable Integer id ) throws MessagingException, EmailTraceNotFound {

        // Get the current Admin ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String AdminId = currentUserId.orElse("System");

        try {
            reunionService.sendReunionInvitations(id);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().build();
    }
    private List<ReunionDacResponse> mapReunionDacs(List<com.sigmap.common.reunionDac.ReunionDac> reunionDacs) {
        if (reunionDacs == null) return List.of();

        return reunionDacs.stream()
                .map(rd -> {
                    var dac = rd.getDac();
                    var dacResponse = new DacResponseReunion();
                    if (dac != null) {
                        dacResponse.setDacId(dac.getDacId());
                        dacResponse.setDacVer(dac.getDacVer());
                        dacResponse.setStatus(dac.getStatus());
                        dacResponse.setTypeMarche(RefParamMapper.INSTANCE.mapToDto(dac.getTypeMarche()));
                        if (dac.getPpm() != null) {
                            dacResponse.setAc(AcMapper.INSTANCE.mapToWebsiteResponse(dac.getPpm().getAc()));
                        }
                    }

                    var rdResponse = new ReunionDacResponse();
                    rdResponse.setDac(dacResponse);
                    rdResponse.setProcessInstanceId(rd.getProcessInstanceId());
                    rdResponse.setTaskId(rd.getTaskId());
                    rdResponse.setDecision(rd.getDecision());
                    rdResponse.setObservation(rd.getObservation());

                    if (rd.getReunionDacDocs() != null) {
                        rdResponse.setDocs(mapDacDocs(rd.getReunionDacDocs()));
                    }

                    return rdResponse;
                })
                .toList();
    }
    private List<ReunionPpmResponse> mapReunionPpms(List<com.sigmap.common.reunionPpm.ReunionPpm> reunionPpms) {
        if (reunionPpms == null) return List.of();

        return reunionPpms.stream()
                .map(rp -> {
                    var ppm = rp.getPpm();
                    var ppmResponse = new com.sigmap.common.ppms.response.PpmIdVerNomResponseSoftDto();
                    if (ppm != null) {
                        ppmResponse.setId(ppm.getId());
                        ppmResponse.setVersion(ppm.getVersion());
                        ppmResponse.setRef(ppm.getRef());
                        ppmResponse.setNom(ppm.getNom());
                        if (ppm.getAc() != null) {
                            ppmResponse.setAc(AcMapper.INSTANCE.mapToWebsiteResponse(ppm.getAc()));
                        }
                    }

                    var rpResponse = new com.sigmap.common.reunionPpm.response.ReunionPpmResponse();
                    rpResponse.setPpm(ppmResponse);
                    rpResponse.setProcessInstanceId(rp.getProcessInstanceId());
                    rpResponse.setTaskId(rp.getTaskId());
                    rpResponse.setDecision(rp.getDecision());
                    rpResponse.setObservation(rp.getObservation());

                    if (rp.getReunionPpmDocs() != null) {
                        rpResponse.setDocs(mapPpmDocs(rp.getReunionPpmDocs()));
                    }

                    return rpResponse;
                })
                .toList();
    }
    private List<DocsResponseDTO> mapDacDocs(List<ReunionDacDoc> reunionDacDocs) {
        if (reunionDacDocs == null) return null;
        return reunionDacDocs.stream()
                .map(ReunionDacDoc::getDocs).filter(doc->!doc.getIsDeleted()) // get the Docs entity
                .map(DocMapper.INSTANCE::mapToDto) // map to DTO using DocsMapper
                .collect(Collectors.toList());
    }
    private List<DocsResponseDTO> mapPpmDocs(List<ReunionPpmDoc> reunionPpmDocs) {
        if (reunionPpmDocs == null) return null;
        return reunionPpmDocs.stream()
                .map(ReunionPpmDoc::getDocs).filter(doc->!doc.getIsDeleted()) // get the Docs entity
                .map(DocMapper.INSTANCE::mapToDto) // map to DTO using DocsMapper
                .collect(Collectors.toList());
    }

    private DocsResponseDTO mapPvmDocs(List<ProcessVerbalReunionDoc> pvDocs) {
        if (pvDocs == null) return null;

        return pvDocs.stream()
                .filter(pvDoc -> pvDoc.isPv() && pvDoc.getDoc() != null && !pvDoc.getDoc().getIsDeleted())
                .map(ProcessVerbalReunionDoc::getDoc)
                .map(DocMapper.INSTANCE::mapToDto)
                .findFirst()
                .orElse(null);
    }


    @GetMapping("/list/ppm/{ppmId}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN')")
    public ResponseEntity<List<ReunionResponseDto>> findAllByPpmIdList(@PathVariable Integer ppmId) throws PpmNotFound, AcNotFound {
        List<Reunion> reunionList = reunionService.findByPpmIdList(ppmId);
        return ResponseEntity.ok(reunionList.stream().map(ReunionMapper.INSTANCE::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/history/{reunionId}/ppm/{ppmId}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN')")
    public ResponseEntity<List<ReunionWithParticipantsAndPvDto>> particpantHistoryByReunionIdAndPPmId(@PathVariable Integer reunionId, @PathVariable Integer ppmId) throws PpmNotFound, AcNotFound {
        List<Reunion> reunionList = reunionService.particpantHistoryByReunionIdAndPPmId(reunionId,ppmId);
        List<ReunionWithParticipantsAndPvDto> response=reunionList.stream()
                .map(reunion -> {
                    // map normally first
                    ReunionWithParticipantsAndPvDto dto =
                            ReunionMapper.INSTANCE.mapToReunionWithParticipantsAndPvDto(reunion);

                    // add PV document if available
                    if (reunion.getProcessVerbal() != null && dto.getProcessVerbal() != null) {
                        DocsResponseDTO pvDocDto = mapPvmDocs(reunion.getProcessVerbal().getDocuments());

                        dto.getProcessVerbal().setDocument(
                                pvDocDto != null
                                        ? new ProcessVerbalReunionDocResponse(
                                        dto.getProcessVerbal().getId(),
                                        pvDocDto.getId(),
                                        pvDocDto,
                                        true
                                )
                                        : null
                        );
                    }

                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/drop-list/ppm/{ppmId}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN')")
    public ResponseEntity<List<ReunionDropListDto>> findAllDropListByPpmId(@PathVariable Integer ppmId) throws PpmNotFound, AcNotFound {
        List<Reunion> reunionList = reunionService.findByPpmIdList(ppmId);
        return ResponseEntity.ok(reunionList.stream().map(ReunionMapper.INSTANCE::mapToDropListDto).collect(Collectors.toList()));
    }
}
