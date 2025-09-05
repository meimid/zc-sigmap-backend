package com.sigmap.sigmap_params.Societe;

import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.CompteBancaireService;
import com.sigmap.common.compte_bancaire.mapper.CompteBancaireMapper;
import com.sigmap.common.compte_bancaire.request.CompteBancaireUnderSocieteRequest;
import com.sigmap.common.compte_bancaire.response.CompteBancaireResponse;
import com.sigmap.common.compte_bancaire.response.CompteBancaireResponseDto;
import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.ContactService;
import com.sigmap.common.contact.Mapper.ContactMapper;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.contact.response.ContactResponse;
import com.sigmap.common.contact.response.ContactResponseDto;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteHasAssociatedCompteBancaire;
import com.sigmap.common.societe.exception.SocieteHasAssociatedSocieteAddress;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteQuery;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressService;
import com.sigmap.common.societe_address.mappers.SocieteAddressMapper;
import com.sigmap.common.societe_address.request.SocieteAdressUnderSocieteRequest;
import com.sigmap.common.societe_address.response.SocieteAddressResponse;
import com.sigmap.common.societe_address.response.SocieteAdressResponseDto;
import com.sigmap.common.societe_contact.Request.ContactAndSocieteContactRequest;
import com.sigmap.common.societe_contact.Request.SocieteContactQuery;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.Response.SocieteContactResponse;
import com.sigmap.common.societe_contact.Response.SocieteContactResponseDto;
import com.sigmap.common.societe_contact.SocieteContact;
import com.sigmap.common.societe_contact.SocieteContactService;
import com.sigmap.common.societe_contact.exception.SocieteContactAlreadyExist;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import com.sigmap.common.societe_contact.mapper.SociteContactMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/societe")

public class SocieteController {
    private final Logger logger = LoggerFactory.getLogger(SocieteController.class);
    private final SocieteService societeService;
    private final  SocieteContactService societeContactService;
    private final SocieteAddressService societeAddressService;
    private final CompteBancaireService  compteBancaireService;
    private final ContactService contactService;

    public SocieteController(SocieteService societeService, SocieteContactService societeContactService, SocieteAddressService societeAddressService, CompteBancaireService compteBancaireService, ContactService contactService) {
        this.societeService = societeService;
        this.societeContactService = societeContactService;
        this.societeAddressService = societeAddressService;
        this.compteBancaireService = compteBancaireService;
        this.contactService = contactService;
    }

    /**
     * Return a page of Societe based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Societe
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteResponse> findById(@PathVariable Integer id) throws SocieteNotFound {
        Societe param = societeService.findById(id);
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
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteResponse> create(@RequestBody SocieteRequest request) throws RefParamNotFound {
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
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteResponse> update(@PathVariable Integer id,@RequestBody SocieteRequest request) throws RefParamNotFound,SocieteNotFound {
        Societe Societe = societeService.update(id,request);
        SocieteResponse response = SocieteMapper.INSTANCE.map(Societe);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Delete a Societe based on the given id.
     *
     * @param id the id of the Societe to delete
     * @return the deleted Societe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteResponse> deleteById(@PathVariable Integer id)  {
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
    @GetMapping("/{societeId}/contact")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<SocieteContactResponseDto>> findAll(@PathVariable Integer societeId,
                                                         SocieteContactQuery query, Pageable pageable) {
        Page<SocieteContact> params = societeContactService.findAll(societeId,query.toSpecification(), pageable);
        Page<SocieteContactResponseDto> contactResponses = params.map(SociteContactMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(contactResponses);
    }


    @GetMapping("/{societeId}/contact/deleted")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ContactResponse>> findAllDeleted(@PathVariable Integer societeId, SocieteContactQuery query, Pageable pageable) throws SocieteNotFound {

        Page<Contact> params = societeContactService.findAllDeleted(societeId,query.toSpecification(), pageable);
        Page<ContactResponse> ContactResponse = params.map(ContactMapper.INSTANCE::map);
        return ResponseEntity.ok(ContactResponse);
    }

    // a verifier fi url wella kifech taccedi li contact avec id x
    @GetMapping("/{societeId}/contact/{contactId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteContactResponse> findById(@PathVariable Integer societeId, @PathVariable Integer contactId) throws SocieteContactNotFound {
        SocieteContact param = societeContactService.findByIds(societeId,contactId);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{societeId}/contact/{contactId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteContactResponse> create(@PathVariable Integer societeId,@PathVariable Integer contactId,@RequestBody SocieteContactRequest request) throws SocieteNotFound, SocieteContactAlreadyExist {
        SocieteContact societeContact = societeContactService.create(societeId,contactId,request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{societeId}/contact/{contactId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteContactResponse> update(@PathVariable Integer societeId,@PathVariable Integer contactId,@RequestBody SocieteContactRequest request) throws SocieteContactNotFound, SocieteNotFound {
        SocieteContact societeContact = societeContactService.update(societeId,contactId,request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @DeleteMapping("/{idSociete}/contact/{idContact}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteContactResponse> deleteById(@PathVariable Integer idSociete,@PathVariable Integer idContact)throws ContactNotFound, SocieteContactNotFound {
        societeContactService.delete(idSociete,idContact);
        SocieteContact deleted = societeContactService.findDeletedByIds(idSociete,idContact)
                .orElseThrow();
        contactService.delete(idContact);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
// create Contact with Socicete Id
@PostMapping("/{societeId}/contact")
@PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
public ResponseEntity<SocieteContactResponse> createContact(@PathVariable Integer societeId,@RequestBody ContactAndSocieteContactRequest request) throws RuntimeException, SocieteNotFound {
    SocieteContact societeContact = societeContactService.createContactAndSocieteContact(societeId,request);
    SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

    // create Adress with Socicete Id
    @PostMapping("/{societeId}/societe_address")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteAddressResponse> createAdress(@PathVariable Integer societeId, @RequestBody SocieteAdressUnderSocieteRequest request) throws RuntimeException, SocieteNotFound,RefParamNotFound {
        SocieteAddress SocieteAddress = societeAddressService.createUnderSociete(societeId,request);
        SocieteAddressResponse response = SocieteAddressMapper.INSTANCE.map(SocieteAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // create CompteBancaire with Socicete Id
    @PostMapping("/{societeId}/compte_bancaire")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<CompteBancaireResponse> createCompteBancaire(@PathVariable Integer societeId, @RequestBody CompteBancaireUnderSocieteRequest request) throws RuntimeException, SocieteNotFound,RefParamNotFound {
        CompteBancaire compteBancaire = compteBancaireService.createUnderSociete(societeId,request);
        CompteBancaireResponse response = CompteBancaireMapper.INSTANCE.map(compteBancaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // get list of compte bancaire under societe
    @GetMapping("/{societeId}/compte_bancaire")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<CompteBancaireResponseDto>> findCompteBancaireListBySocieteId(@PathVariable Integer societeId,Pageable pageable) throws SocieteNotFound {
        Page<CompteBancaire> params=compteBancaireService.findCompteBancaireListBySocieteId(societeId,pageable);
        Page<CompteBancaireResponseDto> compteBancaireResponses = params.map(CompteBancaireMapper.INSTANCE::mapToDtoResponse);
        return ResponseEntity.ok(compteBancaireResponses);


    }

    // get list of contact under societe
    @GetMapping("/{societeId}/contact_list")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ContactResponseDto>> findContactsBySocieteId(@PathVariable Integer societeId, Pageable pageable) throws SocieteNotFound {
        Page<Contact> params = contactService.findContactsBySocieteId(societeId, pageable);
        Page<ContactResponseDto> contactResponses = params.map(ContactMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(contactResponses);
    }

    //get list of societe adress by societe by societe id
    @GetMapping("/{societeId}/societe_address")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<SocieteAdressResponseDto>> findSocieteAdressySocieteId(@PathVariable Integer societeId, Pageable pageable) throws SocieteNotFound {
        Page<SocieteAddress> params = societeAddressService.findSocieteAddressBySocieteId(societeId, pageable);
        Page<SocieteAdressResponseDto> SocieteAdressResponset = params.map(SocieteAddressMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(SocieteAdressResponset);
    }

    @PutMapping("/{societeId}/contact/{contactId}/societe-contact")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteContactResponseDto> updateContactAndSocieteContact(@PathVariable Integer societeId,@PathVariable Integer contactId,@RequestBody ContactAndSocieteContactRequest request) throws SocieteContactNotFound, SocieteNotFound {
        SocieteContact param = societeContactService.updateContactAndSocieteContact(societeId,contactId,request);
        SocieteContactResponseDto response = SociteContactMapper.INSTANCE.mapToDto(param);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

}
