package com.sigmap.sigmap_params.Societe;

import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteQuery;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.response.SocieteResponse;
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

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/societe")

public class SocieteController {
    private final Logger logger = LoggerFactory.getLogger(SocieteController.class);
    private final SocieteService societeService;
    private final  SocieteContactService societeContactService;

    public SocieteController(SocieteService societeService, SocieteContactService societeContactService) {
        this.societeService = societeService;
        this.societeContactService = societeContactService;
    }

    /**
     * Return a page of Societe based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Societe
     */
    @GetMapping
    public ResponseEntity<Page<SocieteResponse>> findAll(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAll(query.toSpecification(), pageable);
         Page<SocieteResponse> compteBancaireResponses = params.map(SocieteMapper.INSTANCE::map);
        return ResponseEntity.ok(compteBancaireResponses);
    }

    /**
     * Return a page of deleted Societe based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted Societe
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<SocieteResponse>> findAllDeleted(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAllDeleted(query.toSpecification(), pageable);
        Page<SocieteResponse> compteBancaireResponses = params.map(SocieteMapper.INSTANCE::map);
        return ResponseEntity.ok(compteBancaireResponses);
    }

    /**
     * Return a Societe based on the given id.
     *
     * @param id the id of the Societe to return
     * @return the Societe
     */
    @GetMapping("/{id}")
    public ResponseEntity<SocieteResponse> findById(@PathVariable Integer id) {
        Societe param = societeService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SocieteResponse response = SocieteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Societe.
     *
     * @param request the request to create the Societe from
     * @return the created Societe
     */
    @PostMapping
    public ResponseEntity<SocieteResponse> create(@RequestBody SocieteRequest request) {
        Societe societe = societeService.create(request);
        SocieteResponse response = SocieteMapper.INSTANCE.map(societe);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Update  Societe.
     *
     * @param request the request to update the Societe from
     * @return the updated Societe
     */
    @PutMapping
    public ResponseEntity<SocieteResponse> update(@RequestBody SocieteRequest request) {
        Societe Societe = societeService.update(request);
        SocieteResponse response = SocieteMapper.INSTANCE.map(Societe);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Delete a Societe based on the given id.
     *
     * @param id the id of the Societe to delete
     * @return the deleted Societe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SocieteResponse> deleteById(@PathVariable Integer id) {
        societeService.delete(id);

        Societe deleted = societeService.findDeletedById(id)
                .orElseThrow();
        SocieteResponse response = SocieteMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
/**
 *
 *
 *
              ******************************* this part for Societe Contact ***********************************
 *
 *
 *
 *
 */

    /**
     * Return a page of SocieteContact based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of SocieteContact
     */
    @GetMapping("/contact")
    public ResponseEntity<Page<SocieteContactResponse>> findAll(
            SocieteContactQuery query, Pageable pageable) {
        Page<SocieteContact> params = societeContactService.findAll(query.toSpecification(), pageable);
        Page<SocieteContactResponse> SocieteContactResponses = params.map(SociteContactMapper.INSTANCE::map);
        return ResponseEntity.ok(SocieteContactResponses);
    }

    @GetMapping("/contact/deleted")
    public ResponseEntity<Page<SocieteContactResponse>> findAllDeleted(SocieteContactQuery query, Pageable pageable) {
        Page<SocieteContact> params = societeContactService.findAllDeleted(query.toSpecification(), pageable);
        Page<SocieteContactResponse> societeContactResponse = params.map(SociteContactMapper.INSTANCE::map);
        return ResponseEntity.ok(societeContactResponse);
    }

    @GetMapping("/contact/{societeId}{contactId}")
    public ResponseEntity<SocieteContactResponse> findById(@PathVariable Integer societeId, @PathVariable Integer contactId) {
        SocieteContact param = societeContactService.findByIds(societeId,contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/contact")
    public ResponseEntity<SocieteContactResponse> create(@RequestBody SocieteContactRequest request) {
        SocieteContact societeContact = societeContactService.create(request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/contact")
    public ResponseEntity<SocieteContactResponse> update(@RequestBody SocieteContactRequest request) {
        SocieteContact societeContact = societeContactService.update(request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @DeleteMapping("/contact/{idSociete}/{idContact}")
    public ResponseEntity<SocieteContactResponse> deleteById(@PathVariable Integer idSociete,@PathVariable Integer idContact) {
        societeContactService.delete(idSociete,idContact);
        SocieteContact deleted = societeContactService.findDeletedByIds(idSociete,idContact)
                .orElseThrow();
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

}
