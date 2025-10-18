package com.sigmap.sigmap_params.user_societe;

import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.response.SocieteResponseDto;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.UserSocieteSpecifications;
import com.sigmap.common.user_societe.exception.UserSocieteNotFound;
import com.sigmap.common.user_societe.mapper.UserSocieteMapper;
import com.sigmap.common.user_societe.request.UserSocieteQuery;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import com.sigmap.common.user_societe.response.SocieteWithUsersResponse;
import com.sigmap.common.user_societe.response.UserSocieteResponse;
import com.sigmap.common.user_societe.response.UserSocieteResponseDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-societe")
public class UserSocieteController {

    private final UserSocieteService userSocieteService;
    private final SocieteService societeService;
    public UserSocieteController(UserSocieteService userSocieteService, SocieteService societeService) {
        this.userSocieteService = userSocieteService;
        this.societeService = societeService;
    }

    /**
     * Return a page of UserSociete based on the given specification and pageable.
     *
     * @param query    the query to use
     * @param pageable the pageable to use
     * @return a page of UserSocieteResponseDto
     */
    @GetMapping
    public ResponseEntity<Page<UserSocieteResponseDto>> findAll(UserSocieteQuery query, Pageable pageable) {
        Page<UserSociete> params = userSocieteService.findAll(query.toSpecification(), pageable);
        Page<UserSocieteResponseDto> responses = params.map(UserSocieteMapper.INSTANCE::toDto);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return all UserSociete as a list (non-paginated).
     *
     * @param query the query specification (optional filters)
     * @return list of UserSocieteResponseDto
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserSocieteResponseDto>> findAllList(UserSocieteQuery query) {
        List<UserSociete> params = userSocieteService.findAllList(query.toSpecification());
        List<UserSocieteResponseDto> responses = params.stream()
                .map(UserSocieteMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }



    /**
     * Return a single UserSociete by userId and societeId.
     *
     * @param userId    the user ID
     * @param societeId the societe ID
     * @return the UserSocieteResponse
     */
    @GetMapping("/user/{userId}/societe/{societeId}")
    public ResponseEntity<UserSocieteResponse> findById(@PathVariable String userId,
                                                        @PathVariable Integer societeId) throws UserSocieteNotFound {
        UserSociete param = userSocieteService.findById(userId, societeId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new UserSociete.
     *
     * @param request the UserSocieteRequest to create
     * @return the created UserSocieteResponse
     */
    @PostMapping
    public ResponseEntity<UserSocieteResponse> create(@RequestBody UserSocieteRequest request) throws SocieteNotFound {
        UserSociete userSociete = userSocieteService.create(request);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(userSociete);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing UserSociete.
     *
     * @param userId    the user ID
     * @param societeId the societe ID
     * @param request   the UserSocieteRequest to update
     * @return the updated UserSocieteResponse
     */
    @PutMapping("/user/{userId}/societe/{societeId}")
    public ResponseEntity<UserSocieteResponse> update(@PathVariable String userId,
                                                      @PathVariable Integer societeId,
                                                      @RequestBody UserSocieteRequest request)
            throws SocieteNotFound, UserSocieteNotFound {
        UserSociete userSociete = userSocieteService.update(userId, societeId, request);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(userSociete);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete a UserSociete by userId and societeId.
     *
     * @param userId    the user ID
     * @param societeId the societe ID
     * @return the deleted UserSocieteResponse
     */
    @DeleteMapping("/user/{userId}/societe/{societeId}")
    public ResponseEntity<UserSocieteResponse> deleteById(@PathVariable String userId,
                                                          @PathVariable Integer societeId) throws UserSocieteNotFound {
        userSocieteService.delete(userId, societeId);
        UserSociete deleted = userSocieteService.findDeletedById(userId, societeId)
                .orElseThrow(() -> new UserSocieteNotFound("UserSociete not found", userId, societeId));
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Find all UserSociete by Societe ID.
     *
     * @param societeId the societe ID
     * @return a list of UserSocieteResponseDto
     */
    @GetMapping("/societe/{societeId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserSocieteResponseDto>> findBySocieteId(@PathVariable Integer societeId)
            throws SocieteNotFound {
        List<UserSociete> list = userSocieteService.findBySocieteId(societeId);
        List<UserSocieteResponseDto> responses = list.stream()
                .map(UserSocieteMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Find a UserSociete by userId only.
     *
     * @param userId the user ID
     * @return the UserSocieteResponse
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserSocieteResponse> findByUserId(@PathVariable String userId)
            throws UserSocieteNotFound {
        UserSociete param = userSocieteService.findByUserId(userId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Make a user responsable of a given société.
     *
     * @param userId the user ID
     * @param societeId the société ID
     * @return the updated UserSocieteResponse
     * @throws SocieteNotFound if the société does not exist
     */
    @PutMapping("/make-responsable/user/{userId}/societe/{societeId}")
    public ResponseEntity<UserSocieteResponse> makeUserSocieteResponsable(
            @PathVariable String userId,
            @PathVariable Integer societeId
    ) throws SocieteNotFound {
        UserSociete updated = userSocieteService.makeUserSocieteResponsable(userId, societeId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(updated);
        return ResponseEntity.ok(response);
    }

    /**
     * Toggle the activation status (ACTIF <-> NON_ACTIF) of a UserSociete.
     *
     * @param userId    the user ID
     * @param societeId the societe ID
     * @return the updated UserSocieteResponse
     */
    @PutMapping("/status/user/{userId}/societe/{societeId}")
    public ResponseEntity<UserSocieteResponse> toggleUserSocieteStatus(
            @PathVariable String userId,
            @PathVariable Integer societeId) throws UserSocieteNotFound {

        UserSociete updated = userSocieteService.toggleUserSocieteStatus(userId, societeId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(updated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-pageable/societe/{societeId}")
    public ResponseEntity<SocieteWithUsersResponse> getSocieteWithUsers(
                    @PathVariable Integer societeId,
            Pageable pageable) throws SocieteNotFound {

        Societe societe = societeService.findById(societeId);
        SocieteResponseDto societeDto = SocieteMapper.INSTANCE.toResponseDto(societe);

        Specification<UserSociete> spec = Specification
                .where(UserSocieteSpecifications.deleted(false))
                .and(UserSocieteSpecifications.societeId(societeId));

        Page<UserSociete> userPage = userSocieteService.findAll(spec, pageable);

        List<UserSocieteResponseDto> userDtos = userPage
                .map(UserSocieteMapper.INSTANCE::toDto)
                .getContent();

        SocieteWithUsersResponse response = SocieteWithUsersResponse.builder()
                .societe(societeDto)
                .content(userDtos)
                .pageable(userPage.getPageable())
                .last(userPage.isLast())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .size(userPage.getSize())
                .number(userPage.getNumber())
                .sort(userPage.getSort())
                .first(userPage.isFirst())
                .numberOfElements(userPage.getNumberOfElements())
                .empty(userPage.isEmpty())
                .build();

        return ResponseEntity.ok(response);
    }


}
