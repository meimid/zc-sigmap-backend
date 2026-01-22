package com.sigmap.sigmap_params.reunion;


import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.response.ComSelectResponseDto;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.response.DacResponseReunion;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.email_trace.exeptions.EmailTraceNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.process_verbal_reunion_doc.response.ProcessVerbalReunionDocResponse;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitService;
import com.sigmap.common.process_verbal_reunion_extrait.mapper.ProcessVerbalReunionExtraitMapper;
import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;
import com.sigmap.common.process_verbal_reunion_extrait.response.ProcessVerbalReunionExtraitRespons;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.mapper.ReunionMapper;
import com.sigmap.common.reunion.request.ReunionQuery;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.*;
import com.sigmap.common.reunionDac.response.ReunionDacResponse;
import com.sigmap.common.reunionPpm.response.ReunionPpmResponse;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import com.sigmap.common.security.Authentication;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.sigmap.common.reunion.ReunionServiceImpl.mapReunionDacs;
import static com.sigmap.common.reunion.ReunionServiceImpl.mapReunionPpms;

@RestController
@RequestMapping("/api/reunions")
@Slf4j
public class ReunionController {

    private final ReunionService reunionService;
    private final ProcessVerbalReunionExtraitService processVerbalReunionExtraitService;
    public ReunionController(ReunionService reunionService, ProcessVerbalReunionExtraitService processVerbalReunionExtraitService) {
        this.reunionService = reunionService;
        this.processVerbalReunionExtraitService = processVerbalReunionExtraitService;
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

       response.setExtraits(mapExtraits(reunion));
        response.setHaveExtraits(haveExtraitCheck(reunion));

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
        ReunionResponse response = reunionService.updatePlanifierOrNotMapped(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<ReunionResponse> updateAnnuler(@PathVariable Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException {
        ReunionResponse response = reunionService.updateAnnulerMapped(id);
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CPMP-PER-RAPPORTEUR','CPMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")
    @PutMapping("/{reunionId}/extrait")
    public ResponseEntity<ProcessVerbalReunionExtraitRespons> injectExtrait(
            @PathVariable Integer reunionId,
            @ModelAttribute ProcessVerbalReunionExtraitRequest request
    ) throws IOException{

    ProcessVerbalReunionExtrait extrait =
                reunionService.injectExtraitForDossier(reunionId, request);

        ProcessVerbalReunionExtraitRespons response =
                ProcessVerbalReunionExtraitMapper.INSTANCE.toDto(extrait);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CPMP-PER-RAPPORTEUR','CPMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")
    @GetMapping("/{reunionId}/extraits")
    public ResponseEntity<Page<ProcessVerbalReunionExtraitRespons>> getExtraitsByReunion(
            @PathVariable Integer reunionId,
             Pageable pageable) {

        Page<ProcessVerbalReunionExtrait> page =
                reunionService.findAllProcessVerbalReunionExtraitByReunionId(reunionId, pageable);
        return ResponseEntity.ok(page.map(ProcessVerbalReunionExtraitMapper.INSTANCE::toDto));
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
    private List<ProcessVerbalReunionExtraitRespons> mapExtraits(Reunion reunion) {
        if (reunion == null || reunion.getProcessVerbal() == null) return List.of();

        Integer pvId = reunion.getProcessVerbal().getId();

        // Fetch all extraits for this PV
        List<ProcessVerbalReunionExtrait> extraits =
                processVerbalReunionExtraitService.findAllByPvId(pvId);

        return extraits.stream()
                .map(extrait -> {
                    ProcessVerbalReunionExtraitRespons dto = new ProcessVerbalReunionExtraitRespons();
                    dto.setExtraitId(extrait.getExtraitId());
                    dto.setPvId(extrait.getPvId());

                    if (extrait.getAc() != null) {
                        dto.setAc(AcMapper.INSTANCE.mapToWebsiteResponse(extrait.getAc()));
                    }
                    if(extrait.getDoc() != null) {
                        dto.setDoc(DocMapper.INSTANCE.mapToDto(extrait.getDoc()));

                    }
                    if(extrait.getPpm() != null) {
                     dto.setPpm(PpmMapper.INSTANCE.mapToPpmIdVerResponseDto(extrait.getPpm()));
                    }

                    return dto;
                })
                .toList();
    }
    private boolean haveExtraitCheck(Reunion reunion) {
        return reunion.getProcessVerbal()!=null?!processVerbalReunionExtraitService.findAllByPvId(reunion.getProcessVerbal().getId()).isEmpty():false;

    }


    @GetMapping("/history/{reunionId}/ppm/{ppmId}")
    public ResponseEntity<ReunionWithParticipantsAndPvDto> particpantHistoryByReunionIdAndPPmId(@PathVariable Integer reunionId, @PathVariable Integer ppmId) {
        ReunionWithParticipantsAndPvDto reunionWithParticipantsAndPvDto=reunionService.particpantHistoryByReunionIdAndPPmId(reunionId, ppmId);
        reunionWithParticipantsAndPvDto.setExtraits(mapExtraits(reunionService.findById(reunionId)));
        return ResponseEntity.ok(reunionWithParticipantsAndPvDto);
    }
   /* @GetMapping("/pv-list-by-com/ppm/{ppmId}/{ppmVer}")
    public ResponseEntity<PvResponse> findAllDropListByPpmId(@RequestParam List<Integer> comIds, @PathVariable Integer ppmId, @PathVariable Integer ppmVer){
        return ResponseEntity.ok(reunionService.listPvComByPpm(comIds,ppmId,ppmVer));
    }

    @GetMapping("/{acId}/{ppmId}/{ppmVer}")
    public ResponseEntity<ExtraitResponse> findAllExtraitsByAcIdAndPpmIdAndPpmVersion(@PathVariable Integer acId, @PathVariable Integer ppmId, @PathVariable Integer ppmVer){
        return ResponseEntity.ok(reunionService.findAllExtraitsByAcIdAndPpmIdAndPpmVersion(acId,ppmId,ppmVer));
    }*/

    @GetMapping("/history/drop-list-com/ppm/{ppmId}")
    public ResponseEntity<List<ComSelectResponseDto>> findAllComDropListByPpmId(@PathVariable Integer ppmId,ReunionQuery query) {

        List<Com> reunionList =
                reunionService.findAllByPpmIdList(ppmId, query.toSpecification())
                        .stream()
                        .map(r -> r.getCom())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                Com::getId,
                                com -> com,
                                (existing, replacement) -> existing
                        ))
                        .values()
                        .stream()
                        .collect(Collectors.toList());
        return ResponseEntity.ok(reunionList.stream().map(ComMapper.INSTANCE::mapToSelectDto).collect(Collectors.toList()));
    }

    @GetMapping("/history/pageable/ppm/{ppmId}")
    public ResponseEntity<Page<ReunionSearchResponseDto>> findAllComByPpmIdAndComId(Pageable pageable,ReunionQuery query, @PathVariable Integer ppmId, @RequestParam(required = false) Integer comId) {
        // List<String> roles = keycloakJwtConverter.extractRealmRoles(jwt);
        List<Reunion> reunionList = reunionService.findAllByPpmIdAndOptionalComId(ppmId,comId,query.toSpecification());

        List<ReunionSearchResponseDto> dtoList = reunionList.stream()
                .map(ReunionMapper.INSTANCE::mapToReunionSearchResponseDto)
                .peek(dto -> {
                    reunionList.stream()
                            .filter(re -> re.getId().equals(dto.getId()))
                            .findFirst()
                            .flatMap(re -> re.getReunionPpms().stream()
                                    .filter(ppm -> ppm.getPpmId().equals(ppmId))
                                    .findFirst()
                            )
                            .ifPresent(ppm -> dto.setDecision(ppm.getDecision()));
                })
                .collect(Collectors.toList());


        dtoList = applySorting(dtoList, pageable, ReunionSearchResponseDto.class);

        Page<ReunionSearchResponseDto> page = paginate(dtoList, pageable);

        return ResponseEntity.ok(page);
    }


// global function for paginated a list
    public static <T> Page<T> paginate(List<T> list, Pageable pageable) {

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());

        List<T> pageContent;

        if (start >= list.size()) {
            pageContent = Collections.emptyList();
        } else {
            pageContent = list.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, list.size());
    }

// global function for build field for sorting methode
    private static <T> Comparator<T> buildFieldComparator(Class<T> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);

            return (o1, o2) -> {
                try {
                    Object v1 = field.get(o1);
                    Object v2 = field.get(o2);

                    if (v1 == null && v2 == null) return 0;
                    if (v1 == null) return 1;
                    if (v2 == null) return -1;

                    if (v1 instanceof Comparable<?> && v2 instanceof Comparable<?>) {
                        Comparable<Object> c1 = (Comparable<Object>) v1;
                        return c1.compareTo(v2);
                    }


                    return 0;
                } catch (IllegalAccessException e) {
                    return 0;
                }
            };

        } catch (NoSuchFieldException e) {
            return null;
        }
    }
//global  function for sorting
    public static <T> List<T> applySorting(List<T> list, Pageable pageable, Class<T> type) {
        if (pageable.getSort().isUnsorted() || list.isEmpty()) {
            return list;
        }

        Comparator<T> globalComparator = null;

        for (Sort.Order order : pageable.getSort()) {
            Comparator<T> fieldComparator = buildFieldComparator(type, order.getProperty());

            if (fieldComparator != null) {
                if (order.isDescending()) {
                    fieldComparator = fieldComparator.reversed();
                }

                globalComparator = (globalComparator == null)
                        ? fieldComparator
                        : globalComparator.thenComparing(fieldComparator);
            }
        }

        if (globalComparator != null) {
            return list.stream().sorted(globalComparator).toList();
        }

        return list;
    }



}
