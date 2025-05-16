package com.sigmap.common.societe_contact;

import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.ContactRepository;
import com.sigmap.common.contact.ContactService;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_contact.Request.ContactAndSocieteContactRequest;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import com.sigmap.common.societe_contact.mapper.SociteContactMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class SocieteContactServiceImpl implements SocieteContactService {

    private final SocieteRepository societeRepository;
    private final SocieteContactRepository repository;
    private final ContactRepository contactRepository;
    private final ContactService contactService;
    private final SocieteService societeService;

    public SocieteContactServiceImpl(SocieteRepository societeRepository, SocieteContactRepository repository, ContactRepository contactRepository, ContactService contactService, SocieteService societeService) {
        this.societeRepository = societeRepository;
        this.repository = repository;
        this.contactRepository = contactRepository;
        this.contactService = contactService;
        this.societeService = societeService;
    }

    @Override
    public List<Contact> findAll(Integer societeId) {
        List <SocieteContact> societeContacts=repository.findBySocieteId(societeId);
        List<Contact> contacts=societeContacts.stream().map(societeContact->societeContact.getSocieteContactId().getContact()).toList();

        return contacts;
    }

    @Override
    public Page<SocieteContact> findAll(Integer societeId, Specification<SocieteContact> societeContact, Pageable pageable) {
        return repository.findBySocieteId(societeId,societeContact, pageable);




    }

    @Override
    public Page<Contact> findAllDeleted(Integer societeId ,Specification<SocieteContact> societeContact, Pageable pageable) throws SocieteNotFound {
        societeService.findById(societeId);
        Page <SocieteContact> societeContacts= repository.findSocieteContactsDeletedBySocieteId(societeId,societeContact, pageable);
        List<Contact> contactList = societeContacts
                .stream()
                .map(sc -> sc.getSocieteContactId().getContact())
                .toList();

        return new PageImpl<>(contactList, pageable, societeContacts.getTotalElements());
    }

    @Override
    public Page<Contact> findAllDeleted(Integer societeId,Pageable pageable) {
        Page <SocieteContact> societeContacts= repository.findSocieteContactsDeletedBySocieteId(societeId, pageable);
        List<Contact> contactList = societeContacts
                .stream()
                .map(sc -> sc.getSocieteContactId().getContact())
                .toList();

        return new PageImpl<>(contactList, pageable, societeContacts.getTotalElements());
    }

    @Override
    public Optional<SocieteContact> findDeletedByIds(Integer societeId, Integer contactId) {
        Specification<SocieteContact> specification = SocieteContactSpecifications.Ids(societeId,contactId).and(SocieteContactSpecifications.deleted(true));
        return findOne(specification);
    }

    // a verifier
    @Override
    public Optional<SocieteContact> findOne(Specification<SocieteContact> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<SocieteContact> findByIds(Integer societeId, Integer contactId) {
        return repository.findByIds(societeId,contactId);
    }


    @Override
    public SocieteContact save(SocieteContact societeContact) {
        return repository.save(societeContact);
    }

    @Override
    public SocieteContact create(Integer societeId, Integer contactId,SocieteContactRequest request) throws RuntimeException {

       Optional <SocieteContact> result = repository.findByIds(societeId,contactId);

        if(result.isPresent()) {
           throw  new ResponseStatusException(HttpStatus.BAD_REQUEST,"There is one SocieteContact already exist with Societe ID :  "+societeId+" and Contact ID : " + contactId);
        }
        else {
            SocieteContactId societeContactId = new SocieteContactId();
            SocieteContact societeContact =  SociteContactMapper.INSTANCE.map(request);
            societeContact.setSocieteContactId(societeContactId);
            societeContactId.setSociete(societeRepository.findById(societeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Societe not found: " + societeId)));
            societeContactId.setContact(contactRepository.findById(contactId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Contact not found: " + contactId)));
            return repository.save(societeContact);
        }
    }

    @Override
    public SocieteContact update(Integer societeId, Integer contactId,SocieteContactRequest request) throws SocieteContactNotFound{
        SocieteContact societeContact = repository.findByIds(societeId, contactId) .orElseThrow(() -> new SocieteContactNotFound("",societeId,contactId));
        societeContact.getSocieteContactId().setSociete(societeRepository.findById(societeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"SocieteAddress not found: " +societeId)));
        societeContact.getSocieteContactId().setContact(contactRepository.findById(contactId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Contact not found: " +contactId)));
        societeContact.setRepresentantLegal(request.isRepresentantLegal());
        societeContact.setPrincipal(request.isPrincipal());
        return repository.save(societeContact);
    }

    @Override
    public void delete(Integer societeId, Integer contactId) {
        repository.softDelete(societeId, contactId);
    }

    @Override
    public SocieteContact createContactAndSocieteContact(Integer societeId, ContactAndSocieteContactRequest request) throws RuntimeException, SocieteNotFound {
         Societe societe=societeService.findById(societeId);
        Contact contact=contactService.create(request.getContact());
            SocieteContactId societeContactId = new SocieteContactId();
            SocieteContact societeContact=new SocieteContact();
             societeContact =  SociteContactMapper.INSTANCE.map(request.getSocieteContactRequest());
            societeContactId.setSociete(societe);
            societeContactId.setContact(contact);
            societeContact.setSocieteContactId(societeContactId);

            return repository.save(societeContact);


    }

    @Override
    public SocieteContact findSocieteContactByContactId(Integer contactId) throws ContactNotFound, SocieteContactNotFound {
        contactService.findById(contactId);
        return repository.findSocieteContactByContactId(contactId);
    }


}
