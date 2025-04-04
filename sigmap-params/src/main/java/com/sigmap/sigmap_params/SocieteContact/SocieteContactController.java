package com.sigmap.sigmap_params.SocieteContact;
import com.sigmap.common.societe_address.SocieteAddress;

import com.sigmap.common.societe_contact.Request.SocieteContactQuery;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.Response.SocieteContactResponse;
import com.sigmap.common.societe_contact.SocieteContact;
import com.sigmap.common.societe_contact.SocieteContactService;
import com.sigmap.common.societe_contact.mapper.SociteContactMapper;
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

@RestController
@RequestMapping("/api/societe_contact")
public class SocieteContactController {
    private final Logger logger = LoggerFactory.getLogger(SocieteContactController.class);

    SocieteContactService societeContactService;

    public SocieteContactController(SocieteContactService societeContactService) {
        this.societeContactService = societeContactService;
    }
    /**
     * Return a page of SocieteContact based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of SocieteContact
     */
    @GetMapping
    public ResponseEntity<Page<SocieteContactResponse>> findAll(
            SocieteContactQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        logger.info("User {}", user.getSubject());

        Page<SocieteContact> params = societeContactService.findAll(query.toSpecification(), pageable);
        Page<SocieteContactResponse> SocieteContactResponses = params.map(SociteContactMapper.INSTANCE::map);
        return ResponseEntity.ok(SocieteContactResponses);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<SocieteContactResponse>> findAllDeleted(SocieteContactQuery query, Pageable pageable) {
        Page<SocieteContact> params = societeContactService.findAllDeleted(query.toSpecification(), pageable);
        Page<SocieteContactResponse> societeContactResponse = params.map(SociteContactMapper.INSTANCE::map);
        return ResponseEntity.ok(societeContactResponse);
    }

    @GetMapping("/{id1}{id2}")
    public ResponseEntity<SocieteContactResponse> findById(@PathVariable Integer id1, @PathVariable Integer id2) {
        SocieteContact param = societeContactService.findByIds(id1,id2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<SocieteContactResponse> create(@RequestBody SocieteContactRequest request) {
        SocieteContact societeContact = societeContactService.create(request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<SocieteContactResponse> update(@RequestBody SocieteContactRequest request) {
        SocieteContact societeContact = societeContactService.update(request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @DeleteMapping("/{idSociete}/{idContact}")
    public ResponseEntity<SocieteContactResponse> deleteById(@PathVariable Integer idSociete,@PathVariable Integer idContact) {
        logger.info("this is before delete-------------------");
        societeContactService.delete(idSociete,idContact);
        logger.info("this is after delete--------------------");
        SocieteContact deleted = societeContactService.findDeletedByIds(idSociete,idContact)
                .orElseThrow();
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }


}
