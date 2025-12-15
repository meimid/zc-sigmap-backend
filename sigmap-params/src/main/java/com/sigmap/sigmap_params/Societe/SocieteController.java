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
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.keycloak_website_admin.service.InscriptionOrchestrationService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;

import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.*;
import com.sigmap.common.societe.response.SocieteDropListDto;
import com.sigmap.common.societe.response.SocieteEnCourResponseDto;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.societe.response.SocieteResponseDtoSoft;

import com.sigmap.common.societe_blak_list.SocieteBlackList;
import com.sigmap.common.societe_blak_list.SocieteBlackListService;
import com.sigmap.common.societe_blak_list.exception.BlackListNotFound;
import com.sigmap.common.societe_blak_list.exception.InvalidBlackListDecision;
import com.sigmap.common.societe_blak_list.mappers.SocieteBlackListMapper;
import com.sigmap.common.societe_blak_list.request.SocieteBlackListQuery;
import com.sigmap.common.societe_blak_list.request.SocieteBlackListRequest;
import com.sigmap.common.societe_blak_list.response.SocieteBlackListResponse;
import com.sigmap.common.societe_blak_list.response.SocieteBlackListResponseDto;
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
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.exception.UserSocieteNotFound;
import com.sigmap.common.user_societe.mapper.UserSocieteMapper;
import com.sigmap.common.user_societe.response.UserSocieteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/societe")

public class SocieteController {
    private final Logger logger = LoggerFactory.getLogger(SocieteController.class);
    private final SocieteService societeService;
    private final  SocieteContactService societeContactService;
    private final CompteBancaireService  compteBancaireService;
    private final ContactService contactService;
    private final UserSocieteService userSocieteService;
    private final InscriptionOrchestrationService inscriptionOrchestrationService;
    private final SocieteBlackListService blackListService;

    public SocieteController(SocieteService societeService, SocieteContactService societeContactService, CompteBancaireService compteBancaireService, ContactService contactService, UserSocieteService userSocieteService, InscriptionOrchestrationService inscriptionOrchestrationService, SocieteBlackListService blackListService) {
        this.societeService = societeService;
        this.societeContactService = societeContactService;
        this.compteBancaireService = compteBancaireService;
        this.contactService = contactService;
        this.userSocieteService = userSocieteService;
        this.inscriptionOrchestrationService = inscriptionOrchestrationService;
        this.blackListService = blackListService;
    }

    /**
     * Return a page of Societe based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Societe
     */
    @GetMapping
    public ResponseEntity<Page<SocieteResponseDtoSoft>> findAll(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAll(query.toSpecification(), pageable);
        Page<SocieteResponseDtoSoft> societeResponses = mapToSocieteResponseDtoSoftPage(params, pageable);
        return ResponseEntity.ok(societeResponses);
    }

    @GetMapping("/not-en-cours")
    public ResponseEntity<Page<SocieteResponseDtoSoft>> findAllNotEncours(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAllNotEncours(query.toSpecification(), pageable);
        Page<SocieteResponseDtoSoft> societeResponses = mapToSocieteResponseDtoSoftPage(params, pageable);
        return ResponseEntity.ok(societeResponses);
    }

    /**
     * Return a page of deleted Societe based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted Societe
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<SocieteResponseDtoSoft>> findAllDeleted(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAllDeleted(query.toSpecification(), pageable);
        Page<SocieteResponseDtoSoft> societeResponses = mapToSocieteResponseDtoSoftPage(params, pageable);
        return ResponseEntity.ok(societeResponses);
    }

    /**
     * Return a Societe based on the given id.
     *
     * @param id the id of the Societe to return
     * @return the Societe
     */
    @GetMapping("/{id}")
    public ResponseEntity<SocieteEnCourResponseDto> findById(@PathVariable Integer id) throws SocieteNotFound {
        Societe param = societeService.findById(id);
        SocieteEnCourResponseDto response = SocieteMapper.INSTANCE.toEnCourResponseDto(param);
        boolean blacklisted = societeService.isCurrentlyBlackListed(param.getId());
        response.setBlackListed(blacklisted);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Societe.
     *
     * @param request the request to create the Societe from
     * @return the created Societe
     */
/*    @PostMapping
    public ResponseEntity<SocieteResponse> create(@RequestBody SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        Societe societe = societeService.create(request);
        SocieteResponse response = SocieteMapper.INSTANCE.map(societe);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }*/
    /**
     * Update  Societe.
     *
     * @param request the request to update the Societe from
     * @return the updated Societe
     */
    @PutMapping("/{id}")
    public ResponseEntity<SocieteResponse> update(@PathVariable Integer id,@RequestBody SocieteRequest request) throws RefParamNotFound,SocieteNotFound {
        Societe Societe = societeService.update(id,request);
        SocieteResponse response = SocieteMapper.INSTANCE.map(Societe);
        boolean blacklisted = societeService.isCurrentlyBlackListed(id);
        response.setBlackListed(blacklisted);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Delete a Societe based on the given id.
     *
     * @param id the id of the Societe to delete
     * @return the deleted Societe
     */
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL')")

    @DeleteMapping("/{id}")
    public ResponseEntity<SocieteResponse> deleteById(@PathVariable Integer id) throws SocieteNotFound {
        societeService.delete(id);

        Societe deleted = societeService.findDeletedById(id)
                .orElseThrow();
        SocieteResponse response = SocieteMapper.INSTANCE.map(deleted);
        boolean blacklisted = societeService.isCurrentlyBlackListed(id);
        response.setBlackListed(blacklisted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/validee")
    public ResponseEntity<Page<SocieteResponseDtoSoft>> findAllWithStatusValidee(
    SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAllWithStatus(query.toSpecification(),SocieteStatus.VALIDEE, pageable);
        Page<SocieteResponseDtoSoft> societeResponses = mapToSocieteResponseDtoSoftPage(params, pageable);
        return ResponseEntity.ok(societeResponses);
    }
    @GetMapping("/status/rejetee")
    public ResponseEntity<Page<SocieteResponseDtoSoft>> findAllWithStatusRejetee(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAllWithStatus(query.toSpecification(),SocieteStatus.REJETEE, pageable);
        Page<SocieteResponseDtoSoft> societeResponses = mapToSocieteResponseDtoSoftPage(params, pageable);
        return ResponseEntity.ok(societeResponses);
    }
    @GetMapping("/status/en-cours")
    public ResponseEntity<Page<SocieteResponseDtoSoft>> findAllWithStatusEnCours(
            SocieteQuery query, Pageable pageable) {
        Page<Societe> params = societeService.findAllWithStatus(query.toSpecification(),SocieteStatus.EN_COURS, pageable);
        Page<SocieteResponseDtoSoft> societeResponses = mapToSocieteResponseDtoSoftPage(params, pageable);
        return ResponseEntity.ok(societeResponses);
    }

/*    @PutMapping("/rejetee/{id}")
    public ResponseEntity<SocieteResponse> updateStatusRejetee(@PathVariable Integer id) throws RefParamNotFound,SocieteNotFound {

        Societe societe = societeService.updateStatus(id,SocieteStatus.REJETEE);
        inscriptionOrchestrationService.validateUser(societe.getUsersSociete().get(0).getUserId());
        SocieteResponse response = SocieteMapper.INSTANCE.map(societe);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/validee/{id}")
    public ResponseEntity<SocieteResponse> updateStatusValidee(@PathVariable Integer id) throws RefParamNotFound,SocieteNotFound {

        Societe societe = societeService.updateStatus(id,SocieteStatus.VALIDEE);
        inscriptionOrchestrationService.validateUser(societe.getUsersSociete().get(0).getUserId());
        SocieteResponse response = SocieteMapper.INSTANCE.map(societe);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }*/

    @PutMapping("/update-user-societe")
    public ResponseEntity<UserSocieteResponse> updateWebsiteUserWithSociete(@RequestBody SocieteRequestInscription request,@AuthenticationPrincipal Jwt jwt) throws RefParamNotFound, SocieteNotFound {
        String responsableId = jwt.getSubject();
        UserSociete param =inscriptionOrchestrationService.updateUserWithSociete(request,responsableId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }


    /**
     * Find a Societe by userId only.
     *
     * @param userId the user ID
     * @return the UserSocieteResponse
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<SocieteResponse> findByUserId(@PathVariable String userId)
            throws UserSocieteNotFound {
        UserSociete param = userSocieteService.findByUserId(userId);
        SocieteResponse response=SocieteMapper.INSTANCE.map(param.getSociete());
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
    public ResponseEntity<Page<SocieteContactResponseDto>> findAll(@PathVariable Integer societeId,
                                                         SocieteContactQuery query, Pageable pageable) {
        Page<SocieteContact> params = societeContactService.findAll(societeId,query.toSpecification(), pageable);
        Page<SocieteContactResponseDto> contactResponses = params.map(SociteContactMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(contactResponses);
    }


    @GetMapping("/{societeId}/contact/deleted")
    public ResponseEntity<Page<ContactResponse>> findAllDeleted(@PathVariable Integer societeId, SocieteContactQuery query, Pageable pageable) throws SocieteNotFound {

        Page<Contact> params = societeContactService.findAllDeleted(societeId,query.toSpecification(), pageable);
        Page<ContactResponse> ContactResponse = params.map(ContactMapper.INSTANCE::map);
        return ResponseEntity.ok(ContactResponse);
    }

    // a verifier fi url wella kifech taccedi li contact avec id x
    @GetMapping("/{societeId}/contact/{contactId}")
    public ResponseEntity<SocieteContactResponse> findById(@PathVariable Integer societeId, @PathVariable Integer contactId) throws SocieteContactNotFound {
        SocieteContact param = societeContactService.findByIds(societeId,contactId);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{societeId}/contact/{contactId}")
    public ResponseEntity<SocieteContactResponse> create(@PathVariable Integer societeId,@PathVariable Integer contactId,@RequestBody SocieteContactRequest request) throws SocieteNotFound, SocieteContactAlreadyExist {
        SocieteContact societeContact = societeContactService.create(societeId,contactId,request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{societeId}/contact/{contactId}")
    public ResponseEntity<SocieteContactResponse> update(@PathVariable Integer societeId,@PathVariable Integer contactId,@RequestBody SocieteContactRequest request) throws SocieteContactNotFound, SocieteNotFound {
        SocieteContact societeContact = societeContactService.update(societeId,contactId,request);
        SocieteContactResponse response = SociteContactMapper.INSTANCE.map(societeContact);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @DeleteMapping("/{idSociete}/contact/{idContact}")
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


    // create CompteBancaire with Socicete Id
    @PostMapping("/{societeId}/compte_bancaire")
    public ResponseEntity<CompteBancaireResponse> createCompteBancaire(@PathVariable Integer societeId, @RequestBody CompteBancaireUnderSocieteRequest request) throws RuntimeException, SocieteNotFound,RefParamNotFound {
        CompteBancaire compteBancaire = compteBancaireService.createUnderSociete(societeId,request);
        CompteBancaireResponse response = CompteBancaireMapper.INSTANCE.map(compteBancaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // get list of compte bancaire under societe
    @GetMapping("/{societeId}/compte_bancaire")
    public ResponseEntity<Page<CompteBancaireResponseDto>> findCompteBancaireListBySocieteId(@PathVariable Integer societeId,Pageable pageable) throws SocieteNotFound {
        Page<CompteBancaire> params=compteBancaireService.findCompteBancaireListBySocieteId(societeId,pageable);
        Page<CompteBancaireResponseDto> compteBancaireResponses = params.map(CompteBancaireMapper.INSTANCE::mapToDtoResponse);
        return ResponseEntity.ok(compteBancaireResponses);


    }

    // get list of contact under societe
    @GetMapping("/{societeId}/contact_list")
    public ResponseEntity<Page<ContactResponseDto>> findContactsBySocieteId(@PathVariable Integer societeId, Pageable pageable) throws SocieteNotFound {
        Page<Contact> params = contactService.findContactsBySocieteId(societeId, pageable);
        Page<ContactResponseDto> contactResponses = params.map(ContactMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(contactResponses);
    }


    @PutMapping("/{societeId}/contact/{contactId}/societe-contact")
    public ResponseEntity<SocieteContactResponseDto> updateContactAndSocieteContact(@PathVariable Integer societeId,@PathVariable Integer contactId,@RequestBody ContactAndSocieteContactRequest request) throws SocieteContactNotFound, SocieteNotFound {
        SocieteContact param = societeContactService.updateContactAndSocieteContact(societeId,contactId,request);
        SocieteContactResponseDto response = SociteContactMapper.INSTANCE.mapToDto(param);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }


    private Page<SocieteResponseDtoSoft> mapToSocieteResponseDtoSoftPage(Page<Societe> societePage, Pageable pageable) {
        List<SocieteResponseDtoSoft> dtoList = societePage.getContent().stream()
                .map(this::mapToSocieteResponseDtoSoft)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, societePage.getTotalElements());
    }
    private SocieteResponseDtoSoft mapToSocieteResponseDtoSoft(Societe s) {
        SocieteResponseDtoSoft dto = new SocieteResponseDtoSoft();
        dto.setId(s.getId());
        dto.setNom(s.getNom());
        dto.setNif(s.getNif());
        dto.setPays(s.getAddress().getPays().getDescription());

        if (s.getAddress() != null ) {
            dto.setEmail(s.getAddress().getEmail());
        }

        s.getUsersSociete().stream()
                .filter(UserSociete::getResponsable)
                .findFirst()
                .ifPresent(responsable -> {
                    dto.setResponsableNom(responsable.getNom());
                    dto.setResponsablePrenom(responsable.getPrenom());
                    dto.setResponsableEmail(responsable.getEmail());
                });
        dto.setBlackListed(societeService.isCurrentlyBlackListed(s.getId()));


        return dto;
    }
/**
 *
 *
 *
 ******************************* this part for Societe black Listed  ***********************************
 *
 *
 *
 *
 */

@GetMapping("/blacklist")
public ResponseEntity<Page<SocieteBlackListResponseDto>> findAllBlackListed(
        SocieteBlackListQuery query,
        Pageable pageable) {
    Page<SocieteBlackList> page =
            blackListService.findAll(query.toSpecification(), pageable);

    Page<SocieteBlackListResponseDto> response =
            page.map(SocieteBlackListMapper.INSTANCE::toResponseDto);

    return ResponseEntity.ok(response);
}

    @GetMapping("/{societeId}/blacklist")
    public ResponseEntity<Page<SocieteBlackListResponseDto>> findAllBySocieteId(
            @PathVariable Integer societeId,
            SocieteBlackListQuery query,
            Pageable pageable) {
        query.setSocieteId(societeId);

        Page<SocieteBlackList> page =
                blackListService.findAll(query.toSpecification(), pageable);

        Page<SocieteBlackListResponseDto> response =
                page.map(SocieteBlackListMapper.INSTANCE::toResponseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{societeId}/blacklist/{id}")
    public ResponseEntity<SocieteBlackListResponse> findByBlacklistById(
            @PathVariable Integer societeId,
            @PathVariable Integer id) throws BlackListNotFound {
        SocieteBlackList entry = blackListService.findById(id, societeId);
        return ResponseEntity.ok(SocieteBlackListMapper.INSTANCE.toResponse(entry));
    }
    @PostMapping("/blacklist")
    public ResponseEntity<SocieteBlackListResponse> create(
            @RequestBody SocieteBlackListRequest request
    ) throws SocieteNotFound, InvalidBlackListDecision {

        SocieteBlackList entry = blackListService.create(request);
        return ResponseEntity.ok(SocieteBlackListMapper.INSTANCE.toResponse(entry));
    }
    @PutMapping("/blacklist/{id}")
    public ResponseEntity<SocieteBlackListResponse> update(
            @PathVariable Integer id,
            @RequestBody SocieteBlackListRequest request
    ) throws BlackListNotFound, InvalidBlackListDecision, SocieteNotFound {

        SocieteBlackList updated = blackListService.update(id,  request);

        return ResponseEntity.ok(
                SocieteBlackListMapper.INSTANCE.toResponse(updated)
        );
    }
    @DeleteMapping("/{societeId}/blacklist/{id}")
    public ResponseEntity<SocieteBlackListResponse> delete(
            @PathVariable Integer societeId,
            @PathVariable Integer id
    ) throws BlackListNotFound {

        blackListService.delete(id, societeId);
        SocieteBlackList entry = blackListService.findDeletedById(id, societeId);
        SocieteBlackListResponse response=SocieteBlackListMapper.INSTANCE.toResponse(entry);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all-validee-list")
    public ResponseEntity<List<SocieteDropListDto>> findAllValidee(
            SocieteQuery query) {
        List<Societe> list =
                societeService.findAllValidee(query.toSpecification());

        return ResponseEntity.ok(list.stream().map(SocieteMapper.INSTANCE::mapToDropListDto).collect(Collectors.toList()));
    }

}
