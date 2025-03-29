package com.sigmap.sigmap_params.contact;


import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.ContactService;
import com.sigmap.common.contact.Mapper.ContactMapper;
import com.sigmap.common.contact.request.ContactQuery;
import com.sigmap.common.contact.request.ContactRequest;
import com.sigmap.common.contact.response.ContactResponse;
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
@RequestMapping("/api/contact")

public class ContactController {
    private final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Return a page of Contact based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Contact
     */
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> findAll(
            ContactQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        logger.info("User {}", user.getSubject());

        Page<Contact> params = contactService.findAll(query.toSpecification(), pageable);
        Page<ContactResponse> ContactResponses = params.map(ContactMapper.INSTANCE::map);
        return ResponseEntity.ok(ContactResponses);
    }

    /**
     * Return a page of deleted Contact based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted Contact
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<ContactResponse>> findAllDeleted(
            ContactQuery query, Pageable pageable) {
        Page<Contact> params = contactService.findAllDeleted(query.toSpecification(), pageable);
        Page<ContactResponse> ContactResponses = params.map(ContactMapper.INSTANCE::map);
        return ResponseEntity.ok(ContactResponses);
    }

    /**
     * Return a Contact based on the given id.
     *
     * @param id the id of the Contact to return
     * @return the Contact
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> findById(@PathVariable Integer id) {
        Contact param = contactService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ContactResponse response = ContactMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Contact.
     *
     * @param request the request to create the Contact from
     * @return the created Contact
     */
    @PostMapping
    public ResponseEntity<ContactResponse> create(@RequestBody ContactRequest request) {
        Contact Contact = contactService.create(request);
        ContactResponse response = ContactMapper.INSTANCE.map(Contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Update  Contact.
     *
     * @param request the request to update the Contact from
     * @return the updated Contact
     */
    @PutMapping
    public ResponseEntity<ContactResponse> update(@RequestBody ContactRequest request) {
        Contact Contact = contactService.update(request);
        ContactResponse response = ContactMapper.INSTANCE.map(Contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Delete a Contact based on the given id.
     *
     * @param id the id of the Contact to delete
     * @return the deleted Contact
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ContactResponse> deleteById(@PathVariable Integer id) {
        contactService.delete(id);

        Contact deleted = contactService.findDeletedById(id)
                .orElseThrow();
        ContactResponse response = ContactMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
