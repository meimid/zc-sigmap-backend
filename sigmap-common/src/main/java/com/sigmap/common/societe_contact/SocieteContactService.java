package com.sigmap.common.societe_contact;

import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_contact.Request.ContactAndSocieteContactRequest;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.exception.SocieteContactAlreadyExist;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface SocieteContactService {

    List<Contact> findAll(Integer societeId);

    /**
     * Return a page of SocieteContact based on the given specification and pageable.
     *
     * @param SocieteContact the specification to use
     * @param pageable       the pageable to use
     * @return a page of SocieteContact
     */
    Page<SocieteContact> findAll(Integer societeId, Specification<SocieteContact> SocieteContact, Pageable pageable);

    /**
     * Return a page of deleted SocieteContact based on the given specification and pageable.
     *
     * @param SocieteContact the specification to use
     * @param pageable the pageable to use
     * @return a page of SocieteContact
     */
    Page<Contact> findAllDeleted(Integer societeId,Specification<SocieteContact> SocieteContact, Pageable pageable) throws SocieteNotFound;

    /**
     * Return a page of deleted SocieteContact based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of SocieteContact
     */
    Page<Contact> findAllDeleted(Integer societeId,Pageable pageable);

    /**
     * Return the deleted SocieteContact with the given id.
     *
     * @param sociteId
     * @param  ContactId
     * @return the SocieteContact with the given id
     */
    Optional<SocieteContact> findDeletedByIds(Integer sociteId, Integer ContactId);

    /**
     * Return the SocieteContact with the given specification.
     *
     * @param specification the specification to use
     * @return the SocieteContact with the given specification
     */
    Optional<SocieteContact> findOne(Specification<SocieteContact> specification);

    /**
     * Return the SocieteContact with the given id.
     *
     * @param societeId
     * @param contactId
     * @return the SocieteContact with the given id
     */
    SocieteContact findByIds(Integer societeId, Integer contactId) throws SocieteContactNotFound;

    /**
     * Save a SocieteContact.
     *
     * @param SocieteContact the SocieteContact to save
     * @return the saved SocieteContact
     */
    SocieteContact save(SocieteContact SocieteContact);

    /**
     * Create a SocieteContact.
     *
     * @param request the request to create the SocieteContact from
     * @return the created SocieteContact
     */
    SocieteContact create(Integer societeId, Integer contactId,SocieteContactRequest request) throws RuntimeException, SocieteContactAlreadyExist, SocieteNotFound;
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated SocieteContact
     */
    SocieteContact update(Integer societeId, Integer contactId,SocieteContactRequest request) throws SocieteContactNotFound, SocieteNotFound;
    /**
     * Delete a SocieteAddress.
     *
     * @param societeId
      * @param contactId the id of the SocieteContact to delete
     */
    void delete(Integer societeId,Integer contactId);

    /**
     * Create a SocieteContact.
     *
     * @param request the request to create the SocieteContact from
     * @return the created SocieteContact
     */
    SocieteContact createContactAndSocieteContact(Integer societeId, ContactAndSocieteContactRequest request) throws RuntimeException, SocieteNotFound;
    SocieteContact findSocieteContactByContactId(Integer contactId) throws ContactNotFound, SocieteContactNotFound;
    SocieteContact updateContactAndSocieteContact (Integer societeId,Integer ContactId,ContactAndSocieteContactRequest request) throws SocieteNotFound, SocieteContactNotFound,ContactNotFound;

}
