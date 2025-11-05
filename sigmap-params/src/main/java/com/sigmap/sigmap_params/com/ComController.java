package com.sigmap.sigmap_params.com;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.exception.CNCMPPNotFound;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.request.ComQuery;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.com.response.ComResponse;
import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.com.response.ComSelectResponseDto;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.keycloakAdmin.dto.ComMemberDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import com.sigmap.common.user_com.mappers.UserComMpapper;
import com.sigmap.common.user_com.response.UserComResponse;
import com.sigmap.common.user_commission_historique.UserCommissionHistorique;
import com.sigmap.common.user_commission_historique.UserCommissionHistoriqueSpecifications;
import com.sigmap.common.user_commission_historique.request.UserCommissionHistoriqueQuery;
import com.sigmap.common.user_commission_historique.response.UserCommissionHistoriqueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.cache.spi.support.SimpleTimestamper.next;

@RestController
@RequestMapping("/api/com")
public class ComController {
    private final Logger logger = LoggerFactory.getLogger(ComController.class);

    private final ComService comService;
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    @Autowired
    private UserComRepository userComRepository;
    @Autowired
    private ComRepository comRepository;

    public ComController(ComService comService) {
        this.comService = comService;
    }

    /**
     * Return a page of Com based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of com
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN', 'VALIDATEUR_AC', 'CPMP', 'ADMIN_AC')")
    public ResponseEntity<Page<ComResponse>> findAll(
            ComQuery query, Pageable pageable) {
        Page<Com> params = comService.findAll(query.toSpecification(), pageable);
        Page<ComResponse> acResponses = params.map(ComMapper.INSTANCE::map);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a page of deleted Com based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted Com
     */
    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ComResponse>> findAllDeleted(
            ComQuery query, Pageable pageable) {
        Page<Com> params = comService.findAllDeleted(query.toSpecification(), pageable);
        Page<ComResponse> acResponses = params.map(ComMapper.INSTANCE::map);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a Com based on the given id.
     *
     * @param id the id of the Com to return
     * @return the Com
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ComResponse> findById(@PathVariable Integer id) throws ComNotFound {
        Com param = comService.findById(id);
        ComResponse response = ComMapper.INSTANCE.map(param);

        List<UserCom> userComs = userComRepository.findAllByComIdAndIsDeletedFalse(id);

        List<ComMemberDTO> users = userComs.stream().map(userCom -> {
            try {
                UserDTO keycloakUser = keycloakAdminService.getUserById(userCom.getUserId());

                ComMemberDTO dto = new ComMemberDTO();
                dto.setId(keycloakUser.getId());
                dto.setFirstName(keycloakUser.getFirstName());
                dto.setLastName(keycloakUser.getLastName());
                dto.setEmail(keycloakUser.getEmail());
                dto.setMobile(keycloakUser.getMobile());
                dto.setCreatedAt(userCom.getCreatedAt());
                String userNameAffectedBy = keycloakAdminService.getUserById(userCom.getCreatedBy()).getUsername();
                dto.setCreatedBy(userNameAffectedBy);
                return dto;
            } catch (Exception e) {
                logger.warn("Impossible de récupérer l'utilisateur {} depuis Keycloak : {}", userCom.getUserId(), e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).toList();

        response.setUsers(users);
        comResponsable(response);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/batch/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ComResponse> findByIdBatch(@PathVariable Integer id) throws ComNotFound {
        Com param = comService.findById(id);
        ComResponse response = ComMapper.INSTANCE.map(param);

        // Fetch all UserAc non-deleted entries
        List<UserCom> userComs = userComRepository.findAllByComIdAndIsDeletedFalse(id);

        // Collect unique user IDs (userId and createdBy)
        Set<String> userIds = userComs.stream()
                .flatMap(userAc -> Stream.of(userAc.getUserId(), userAc.getCreatedBy()))
                .collect(Collectors.toSet());

        // Batch fetch users from Keycloak
        Map<String, UserDTO> keycloakUsers = keycloakAdminService.fetchUsersInBatch(userIds);

        List<ComMemberDTO> users = userComs.stream().map(userCom -> {
            UserDTO keycloakUser = keycloakUsers.get(userCom.getUserId());
            if (keycloakUser == null) {
                logger.warn("Utilisateur {} introuvable dans Keycloak", userCom.getUserId());
                return null;
            }

            ComMemberDTO dto = new ComMemberDTO();
            dto.setId(keycloakUser.getId());
            dto.setFirstName(keycloakUser.getFirstName());
            dto.setLastName(keycloakUser.getLastName());
            dto.setEmail(keycloakUser.getEmail());
            dto.setMobile(keycloakUser.getMobile());
            dto.setCreatedAt(userCom.getCreatedAt());
            UserDTO createdByUser = keycloakUsers.get(userCom.getCreatedBy());
            dto.setCreatedBy(createdByUser != null ? createdByUser.getEmail() : "system");
            return dto;

        }).filter(Objects::nonNull).toList();

        response.setUsers(users);
        String responsableId = comRepository.findIdResponsableByComId(response.getId());
        assert responsableId != null;
        if (responsableId != null) {
            response.setResponsableId(responsableId);
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/user/{userId}/coms")
    public ResponseEntity<List<ComResponse>> getComsByUserId(@PathVariable String userId) {
        List<UserCom> userComs = userComRepository.findAllByUserIdAndIsDeletedFalse(userId);

        List<ComResponse> coms = userComs.stream()
                .map(userCom -> {
                    Com com = userCom.getCom();
                    ComResponse response = ComMapper.INSTANCE.map(com);

                    // Exclure les membres et les métadonnées
                    response.setUsers(null);
                    response.setMetadata(null);

                    return response;
                })
                .toList();

        return ResponseEntity.ok(coms);
    }


    /**
     * Create a new Com.
     *
     * @param request the request to create the Com from
     * @return the created Com
     */
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ComResponse> create(@ModelAttribute("request") ComRequest request) throws AcNotFound, RefParamNotFound, ComNotFound, IOException {
        Com com = comService.create(request);
        ComResponse response = ComMapper.INSTANCE.map(com);
        comResponsable(response);
        return ResponseEntity.ok(response);
    }
    /**
     * Update  com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    @PutMapping(consumes = {"multipart/form-data"},value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ComResponse> update(@PathVariable Integer id,@ModelAttribute("request") ComRequest request) throws ComNotFound, RefParamNotFound, AcNotFound, AgpmDefaultStatus, DocNotFound, IOException {
        Com com = comService.update(id,request);
        ComResponse response = ComMapper.INSTANCE.map(com);
        comResponsable(response);
        return ResponseEntity.ok(response);
    }
    /**
     * Delete a Com based on the given id.
     *
     * @param id the id of the Com to delete
     * @return the deleted Com
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ComResponse> deleteById(@PathVariable Integer id) throws ComHasAssociatedAc, ComNotFound {
        comService.delete(id);

        Com deleted = comService.findDeletedById(id)
                .orElseThrow();
        ComResponse response = ComMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * make a user responsible in user com table
     *
     * @param userId the id of the User
     * @param comId the id of the Com
     *
     * @return the responsible User come
     */
    @PutMapping("/{userId}/{comId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<UserComResponse> makeUserResponsable(@PathVariable String userId,@PathVariable Integer comId) throws ComHasAssociatedAc {
       UserCom userCom= comService.makeUserResponsable(userId,comId);
        UserComResponse response = UserComMpapper.INSTANCE.map(userCom);
        response.getCom().setResponsableId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/cpmp")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN', 'VALIDATEUR_AC')")
    public ResponseEntity<List<ComResponse>> getComsWithTypeCPMP() throws RefParamNotFound {
        List<Com> coms = comService.getComsWithTypeCPMP();
        List<ComResponse> responses = coms.stream().map(ComMapper.INSTANCE::map).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type/cncmp")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN','CPMP')")
    public ResponseEntity<List<ComResponse>> getComsWithTypeCNCMP() throws RefParamNotFound {
        List<Com> coms = comService.getComsWithTypeCNCMP();
        List<ComResponse> responses = coms.stream().map(ComMapper.INSTANCE::map).toList();
        return ResponseEntity.ok(responses);
    }


    public void comResponsable(ComResponse response) {
        // 1. Get the list of links from the join table
        List<UserCom> membersLinks = userComRepository.findAllByComIdAndIsDeletedFalse(response.getId());

        if (membersLinks.isEmpty()) {
            response.setUsers(new ArrayList<>());
            next();
        }

        List<ComMemberDTO> members = UserComMpapper.INSTANCE.mapUsersToDto(membersLinks);
        for (ComMemberDTO member : members) {
            UserDTO keycloakUser = keycloakAdminService.getUserById(member.getId());

            if (keycloakUser != null) {
                member.setFirstName(keycloakUser.getFirstName());
                member.setLastName(keycloakUser.getLastName());
                member.setEmail(keycloakUser.getEmail());
                member.setMobile(keycloakUser.getMobile());
            }
        }
        UserCom responsable=userComRepository.findResponsableByComId(response.getId());
        if(responsable!=null){
        String responsibleUserId = userComRepository.findResponsableByComId(response.getId()).getUserId();
        response.setResponsableId(responsibleUserId);
        for (ComMemberDTO member : members) {
            if (member.getId().equals(responsibleUserId)) {
                member.setResponsable(true);
            } else {
                member.setResponsable(false);
            }
        }
        response.setUsers(members);
        }

    }
    @GetMapping("/by_own_tutel/{acId}")
    public ResponseEntity<ComSelectResponseDto> getCommissionDeTutelle(@PathVariable Integer acId) throws ComNotFound, AcNotFound {

            Com com = comService.getCommissionDeTutelle(acId);
        ComSelectResponseDto response = ComMapper.INSTANCE.mapToSelectDto(com);
            return ResponseEntity.ok(response);

    }

    @GetMapping("/private_com_of_ac/{acId}")
    public ResponseEntity<ComSelectResponseDto> getCommissionPrivee(@PathVariable Integer acId) throws ComNotFound, AcNotFound {
            Com com = comService.getCommissionPrivee(acId);
        ComSelectResponseDto response = ComMapper.INSTANCE.mapToSelectDto(com);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/all_coms_of_tutels/{acId}")
    public ResponseEntity<List<ComSelectResponseDto>> getAllCommissionsDeTutelles(@PathVariable Integer acId) {
        List<Com> coms = comService.getAllCommissionsDeTutelles(acId);
        List  <ComSelectResponseDto> response = coms.stream().map(ComMapper.INSTANCE::mapToSelectDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all_special")
    public ResponseEntity<List<ComSelectResponseDto>> getAllSpecialCommissions() {
        List<Com> coms = comService.getAllSpecialCommissions();
        List  <ComSelectResponseDto> response = coms.stream().map(ComMapper.INSTANCE::mapToSelectDto).toList();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/find_cncpp")
    public ResponseEntity<ComResponse> findCNCMPP() throws CNCMPPNotFound {
        Com com = comService.findCNCMPP();
        ComResponse response = ComMapper.INSTANCE.map(com);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN', 'VALIDATEUR_AC', 'CPMP', 'ADMIN_AC')")
    public ResponseEntity<List<ComSelectResponseDto>> findAllList() {
        List<Com> coms = comService.findAll();
        List<ComSelectResponseDto> response = coms.stream().map(ComMapper.INSTANCE::mapToSelectDto).toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/users/historique-by-com/{comId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserCommissionHistoriqueResponse>> getAllByComId(
            @RequestParam(name = "date", required = false) String exactDate,
            @PathVariable Integer comId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, UserCommissionHistoriqueQuery query) {
        Specification<UserCommissionHistorique> specification = query.toSpecification();

        Page<UserCommissionHistoriqueResponse> responses =
                keycloakAdminService.hitoriqueData(comId, exactDate, pageable, specification);

        return ResponseEntity.ok(responses);
    }

}
