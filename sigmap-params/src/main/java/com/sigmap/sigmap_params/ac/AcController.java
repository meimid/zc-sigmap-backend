package com.sigmap.sigmap_params.ac;

import com.sigmap.common.ac.exception.*;
import com.sigmap.common.ac.response.*;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.exception.ComDeleted;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcQuery;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.keycloakAdmin.dto.AcMemberDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller to manage ac.
 *
 * @see Ac
 * @see AcRequest
 * @see AcResponse
 * @see AcQuery
 * @see AcService
 */
@RestController
@RequestMapping("/api/ac")
@Slf4j
public class AcController {
    private final Logger logger = LoggerFactory.getLogger(AcController.class);
    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserAcRepository userAcRepository;
    private final AcService acService;

    public AcController(AcService acService) {
        this.acService = acService;
    }
    @Value("${file.upload.ac-logo-path}")
    private String acLogoPath;
    /**
     * Return a page of ac based on the given specifica tion and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of ac
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AcResponse>> findAll(AcQuery query, Pageable pageable) {

        Specification<Ac> spec = query.toSpecification();

        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            List<Integer> allowedAcIds = currentUserService.getCurrentUserAcIds();
            spec = spec.and((root, query1, cb) -> root.get("id").in(allowedAcIds));
        }

        Page<Ac> page = acService.findAll(spec, pageable);
        Page<AcResponse> acResponses = page.map(AcMapper.INSTANCE::map);

        return ResponseEntity.ok(acResponses);
    }


    /**
     * Return a page of deleted ac based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted ac
     */
    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<Page<AcResponse>> findAllDeleted(
            AcQuery query, Pageable pageable) {
        Page<Ac> params = acService.findAllDeleted(query.toSpecification(), pageable);
        Page<AcResponse> acResponses = params.map(AcMapper.INSTANCE::map);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a ac based on the given id.
     *
     * @param id the id of the ac to return
     * @return the ac
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<AcResponse> findById(@PathVariable Integer id) throws AcNotFound {

        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            List<Integer> allowedAcIds = currentUserService.getCurrentUserAcIds();
            if (!allowedAcIds.contains(id)) {
                throw new AcAccessDeniedException("Accès refusé à cette autorité contractante.", id);
            }
        }

        Ac param = acService.findById(id);
        AcResponse response = AcMapper.INSTANCE.map(param);

        // Récupérer tous les UserAc non supprimés
        List<UserAc> userAcs = userAcRepository.findAllByAcIdAndIsDeletedFalse(id);

        // Mapper les UserAc avec infos Keycloak
        List<AcMemberDTO> users = userAcs.stream().map(userAc -> {
            try {
                UserDTO keycloakUser = keycloakAdminService.getUserById(userAc.getUserId());

                AcMemberDTO dto = new AcMemberDTO();
                dto.setId(keycloakUser.getId());
                dto.setFirstName(keycloakUser.getFirstName());
                dto.setLastName(keycloakUser.getLastName());
                dto.setEmail(keycloakUser.getEmail());
                dto.setMobile(keycloakUser.getMobile());
                dto.setAcPilote(userAc.getAcPilote());
                dto.setCreatedAt(userAc.getCreatedAt());
                String userNameAffectedBy = keycloakAdminService.getUserById(userAc.getCreatedBy()).getUsername();
                dto.setCreatedBy(userNameAffectedBy);

                return dto;
            } catch (Exception e) {
                logger.warn("Impossible de récupérer l'utilisateur {} depuis Keycloak : {}", userAc.getUserId(), e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).toList();

        response.setUsers(users);

        return ResponseEntity.ok(response);
    }



    @GetMapping("/batch/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<AcResponse> findByIdForBatch(@PathVariable Integer id) throws AcNotFound {
        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            List<Integer> allowedAcIds = currentUserService.getCurrentUserAcIds();
            if (!allowedAcIds.contains(id)) {
                throw new AcAccessDeniedException("Accès refusé à cette autorité contractante.", id);
            }
        }

        Ac param = acService.findById(id);
        AcResponse response = AcMapper.INSTANCE.map(param);

        // Fetch all UserAc non-deleted entries
        List<UserAc> userAcs = userAcRepository.findAllByAcIdAndIsDeletedFalse(id);

        // Collect unique user IDs (userId and createdBy)
        Set<String> userIds = userAcs.stream()
                .flatMap(userAc -> Stream.of(userAc.getUserId(), userAc.getCreatedBy()))
                .collect(Collectors.toSet());

        // Batch fetch users from Keycloak
        Map<String, UserDTO> keycloakUsers = keycloakAdminService.fetchUsersInBatch(userIds);

        // Map UserAc to AcMemberDTO
        List<AcMemberDTO> users = userAcs.stream().map(userAc -> {
            UserDTO keycloakUser = keycloakUsers.get(userAc.getUserId());
            if (keycloakUser == null) {
                logger.warn("Utilisateur {} introuvable dans Keycloak", userAc.getUserId());
                return null;
            }
            AcMemberDTO dto = new AcMemberDTO();
            dto.setId(keycloakUser.getId());
            dto.setFirstName(keycloakUser.getFirstName());
            dto.setLastName(keycloakUser.getLastName());
            dto.setEmail(keycloakUser.getEmail());
            dto.setMobile(keycloakUser.getMobile());
            dto.setAcPilote(userAc.getAcPilote());
            dto.setCreatedAt(userAc.getCreatedAt());
            UserDTO createdByUser = keycloakUsers.get(userAc.getCreatedBy());
            dto.setCreatedBy(createdByUser != null ? createdByUser.getUsername() : "system");
            return dto;
        }).filter(Objects::nonNull).toList();

        response.setUsers(users);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{userId}/acs")
    public ResponseEntity<List<AcResponse>> getAcsByUserId(@PathVariable String userId) {
        List<UserAc> userAcs = userAcRepository.findByUserIdAndIsDeletedFalse(userId);

        List<AcResponse> acs = userAcs.stream()
                .map(userAc -> {
                    Ac ac = userAc.getAc();
                    AcResponse response = AcMapper.INSTANCE.map(ac);
                    return response;
                })
                .toList();

        return ResponseEntity.ok(acs);
    }



    /**
     * Create a new ac.
     *
     * @param request the request to create the ac from
     * @return the created ac
     */
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<AcResponse> create(@ModelAttribute("request") AcRequest request) throws ComNotFound, ComDeleted, RefParamNotFound, AcNotFound, IOException {
        Ac ac = acService.create(request);
        AcResponse response = AcMapper.INSTANCE.map(ac);
        return ResponseEntity.ok(response);
    }
    /**
     * Update  ac.
     *
     * @param request the request to update the ac from
     * @return the updated ac
     */
    @PutMapping(consumes = {"multipart/form-data"},value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<AcResponse> update(@PathVariable Integer id, @ModelAttribute("request") AcRequest request) throws AcNotFound, ComNotFound, DuplicateComIdsException, ComDeleted, UserAcDefaultStatus, RefParamNotFound, IOException {
        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            List<Integer> allowedAcIds = currentUserService.getCurrentUserAcIds();
            if (!allowedAcIds.contains(id)) {
                throw new AcAccessDeniedException("Accès refusé à cette autorité contractante.", id);
            }
        }
        Ac ac = acService.update(id,request);
        AcResponse response = AcMapper.INSTANCE.map(ac);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a ac based on the given id.
     *
     * @param id the id of the ac to delete
     * @return the deleted ac
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<AcResponse> deleteById(@PathVariable Integer id)  throws AcHasAssociatedAgpm {
        acService.delete(id);

        Ac deleted = acService.findDeletedById(id)
                .orElseThrow();
        AcResponse response = AcMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Return a list of ac based on the given specifica tion and pageable.
     *
     * @param query the query to use
     * @return a list of ac
     */
    @GetMapping("/is_tutel")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<List<AcResponseDto>> findAll(AcQuery query) {

        Specification<Ac> spec = query.toSpecification();

        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            List<Integer> allowedAcIds = currentUserService.getCurrentUserAcIds();
            spec = spec.and((root, query1, cb) -> root.get("id").in(allowedAcIds));
        }

        List<Ac> acs = acService.findAllTutel(spec);
        List<AcResponseDto> acResponses = acs.stream().map(AcMapper.INSTANCE::mapToDto).collect(Collectors.toList());

        return ResponseEntity.ok(acResponses);
    }

    @GetMapping("/com/{comId}")
    public ResponseEntity<List<AcResponseDto>> getAcsByComId(@PathVariable Integer comId) throws ComNotFound {
        List<Ac> acs = acService.findAllByComId(comId);

        List<AcResponseDto> response = acs.stream()
                .map(AcMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/list")
    public ResponseEntity <List<AcSelectResponseDto>> findAllAsList()  {
        List<Ac> acs = acService.findAll();
        List<AcSelectResponseDto> responses= AcMapper.INSTANCE.mapToSelectDto(acs);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/list-um")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<List<AcResponse>> getAcsForCreateUser() {

        List<Ac> acs = null;
        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            acs = userAcRepository.findAcsByUserIdAndIsDeletedFalse(Authentication.getCurrentUserId().get());
        }
        else {
            acs = acService.findAll();
        }
        List<AcResponse> acsR = acs.stream()
                .map(ac -> {
                    AcResponse response = AcMapper.INSTANCE.map(ac);
                    return response;
                })
                .toList();

        return ResponseEntity.ok(acsR);
    }

    @GetMapping("/acs_list_with_no_com")
    public ResponseEntity<List<AcSelectResponseDto>> findAllAsListWithNoCom() {
        List<Ac> acs=acService.findAllAcsWithNoCom();
         List<AcSelectResponseDto> responses=AcMapper.INSTANCE.mapToSelectDto(acs);
         return ResponseEntity.ok(responses);
    }

    @GetMapping("/{acId}/photo/display")
    public ResponseEntity<LogoResponse> displayAcPhoto(@PathVariable Integer acId) throws AcNotFound {
        LogoResponse response = acService.getAcPhoto(acId, acLogoPath);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/acs_by_userId")

    public ResponseEntity<List<AcWebsiteResponse>> findAllAcsByUserId(@RequestParam String userId) {
        List<Ac> acList = acService.getAcsByUserId(userId);
        List<AcWebsiteResponse> acs = acList.stream()
                .map(ac -> {
                    AcWebsiteResponse response = AcMapper.INSTANCE.mapToWebsiteResponse(ac);
                    return response;
                })
                .toList();
        return ResponseEntity.ok(acs);
    }

    @GetMapping("/acs-tutel-list-with-no-com-cpmp")
    public ResponseEntity<List<AcSelectResponseDto>> findAllAsListWithNoComCPMP(
            @RequestParam(required = false) Integer comId,
            @RequestParam Integer typeComId) throws RefParamNotFound {

        List<Ac> acs = acService.findAllAcsWithNoComCPMP(comId, typeComId);

        List<AcSelectResponseDto> responses = AcMapper.INSTANCE.mapToSelectDto(acs);

        return ResponseEntity.ok(responses);
    }

}
