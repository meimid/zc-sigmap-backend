package com.sigmap.sigmap_params.ac;

import com.sigmap.common.ac.exception.AcHasAssociatedCom;
import com.sigmap.common.com.exception.ComDeleted;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcQuery;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

public class AcController {
    private final Logger logger = LoggerFactory.getLogger(AcController.class);
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    private final AcService acService;

    public AcController(AcService acService) {
        this.acService = acService;
    }

    /**
     * Return a page of ac based on the given specifica tion and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of ac
     */
    @GetMapping
    public ResponseEntity<Page<AcResponse>> findAll(
            AcQuery query, Pageable pageable) {
        Page<Ac> params = acService.findAll(query.toSpecification(), pageable);
        Page<AcResponse> acResponses = params.map(AcMapper.INSTANCE::map);
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
    public ResponseEntity<AcResponse> findById(@PathVariable Integer id) throws AcNotFound {
        Ac param = acService.findById(id);
        AcResponse response = AcMapper.INSTANCE.map(param);
        List<UserDTO> users = keycloakAdminService.getUsersByAc(id);
        response.setUsers(users);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new ac.
     *
     * @param request the request to create the ac from
     * @return the created ac
     */
    @PostMapping
    public ResponseEntity<AcResponse> create(@RequestBody AcRequest request) throws ComNotFound, ComDeleted {
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
    @PutMapping("/{id}")
    public ResponseEntity<AcResponse> update(@PathVariable Integer id, @RequestBody AcRequest request) throws AcNotFound, ComNotFound, DuplicateComIdsException,ComDeleted{
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
    public ResponseEntity<AcResponse> deleteById(@PathVariable Integer id)  throws AcHasAssociatedCom {
        acService.delete(id);

        Ac deleted = acService.findDeletedById(id)
                .orElseThrow();
        AcResponse response = AcMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
