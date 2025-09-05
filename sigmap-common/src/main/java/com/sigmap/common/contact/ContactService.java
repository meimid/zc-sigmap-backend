package com.sigmap.common.contact;

import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.contact.request.ContactRequest;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ContactService {
    List<Contact> findAll();

    /**
     * Return a page of Contact based on the given specification and pageable.
     *
     * @param Contact the specification to use
     * @param pageable the pageable to use
     * @return a page of Contact
     */
    Page<Contact> findAll(Specification<Contact> Contact, Pageable pageable);

    /**
     * Return a page of deleted Contact based on the given specification and pageable.
     *
     * @param contact the specification to use
     * @param pageable the pageable to use
     * @return a page of Contact
     */
    Page<Contact> findAllDeleted(Specification<Contact> contact, Pageable pageable);

    /**
     * Return a page of deleted Contact based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Contact
     */
    Page<Contact> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Contact with the given id.
     *
     * @param id the id of the Contact to return
     * @return the Contact with the given id
     */
    Optional<Contact> findDeletedById(Integer id);

    /**
     * Return the Contact with the given specification.
     *
     * @param specification the specification to use
     * @return the Contact with the given specification
     */
    Optional<Contact> findOne(Specification<Contact> specification);

    /**
     * Return the Contact with the given id.
     *
     * @param id the id of the Contact to return
     * @return the Contact with the given id
     */
    Contact findById(Integer id)throws ContactNotFound;

    /**
     * Save a Contact.
     *
     * @param contact the Contact to save
     * @return the saved Contact
     */
    Contact save(Contact contact);

    /**
     * Create a Contact.
     *
     * @param request the request to create the Contact from
     * @return the created Contact
     */
    Contact create(ContactRequest request);
    /**
     * update Contact.
     *
     * @param request the request to update the Contact from
     * @return the updated Contact
     */
    Contact update(Integer id,ContactRequest request)throws ContactNotFound;
    /**
     * Delete a Contact.
     *
     * @param id the id of the Contact to delete
     */
    void delete(Integer id) throws ContactNotFound, SocieteContactNotFound;

    Page<Contact> findContactsBySocieteId(Integer societeId, Pageable pageable) throws SocieteNotFound;



}
