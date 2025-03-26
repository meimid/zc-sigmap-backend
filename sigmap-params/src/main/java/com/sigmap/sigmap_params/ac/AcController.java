package com.sigmap.sigmap_params.ac;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcQuery;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.sigmap_params.ref_params.RefParamController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
@RequiredArgsConstructor
public class AcController {
    private final Logger logger = LoggerFactory.getLogger(AcController.class);

    private final AcService acService;



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
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        logger.info("User {}", user.getSubject());

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
    public ResponseEntity<AcResponse> findById(@PathVariable Integer id) {
        Ac param = acService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        AcResponse response = AcMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new ac.
     *
     * @param request the request to create the ac from
     * @return the created ac
     */
    @PostMapping
    public ResponseEntity<AcResponse> create(@RequestBody AcRequest request) {
        Ac ac = acService.create(request);
        AcResponse response = AcMapper.INSTANCE.map(ac);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete a ac based on the given id.
     *
     * @param id the id of the ac to delete
     * @return the deleted ac
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AcResponse> deleteById(@PathVariable Integer id) {
        acService.delete(id);

        Ac deleted = acService.findDeletedById(id)
                .orElseThrow();
        AcResponse response = AcMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
