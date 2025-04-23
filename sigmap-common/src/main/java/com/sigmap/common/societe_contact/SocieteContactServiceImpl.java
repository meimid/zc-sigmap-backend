package com.sigmap.common.societe_contact;

import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.ContactRepository;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.mapper.SociteContactMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocieteContactServiceImpl implements SocieteContactService {

    private final SocieteRepository societeRepository;
    private final SocieteContactRepository repository;
    private final ContactRepository contactRepository;

    public SocieteContactServiceImpl(ContactRepository contactRepository, SocieteContactRepository repository, SocieteRepository societeRepository) {
        this.societeRepository = societeRepository;
        this.repository = repository;
        this.contactRepository = contactRepository;
    }

    @Override
    public List<Contact> findAll(Integer societeId) {
        List <SocieteContact> societeContacts=repository.findBySocieteId(societeId);
        List<Contact> contacts=societeContacts.stream().map(societeContact->societeContact.getSocieteContactId().getContact()).toList();

        return contacts;
    }

    @Override
    public Page<Contact> findAll(Integer societeId,Specification<SocieteContact> societeContact, Pageable pageable) {
        Page <SocieteContact> societeContacts= repository.findBySocieteId(societeId,societeContact, pageable);
        List<Contact> contactList = societeContacts
                .stream()
                .map(sc -> sc.getSocieteContactId().getContact())
                .toList();

        return new PageImpl<>(contactList, pageable, societeContacts.getTotalElements());


    }

    @Override
    public Page<Contact> findAllDeleted(Integer societeId ,Specification<SocieteContact> societeContact, Pageable pageable) {
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
    public SocieteContact create(SocieteContactRequest request) throws RuntimeException {

       Optional <SocieteContact> result = repository.findByIds(request.getSocieteId(),request.getContactId());

        if(result.isPresent()) {
           throw  new RuntimeException("There is one SocieteContact already exist with Societe ID :  "+request.getSocieteId()+" and Contact ID : " + request.getContactId());
        }
        else {
            SocieteContactId societeContactId = new SocieteContactId();
            SocieteContact societeContact =  SociteContactMapper.INSTANCE.map(request);
            societeContact.setSocieteContactId(societeContactId);
            societeContactId.setSociete(societeRepository.findById(request.getSocieteId())
                    .orElseThrow(() -> new RuntimeException("Societe not found: " + request.getSocieteId())));
            societeContactId.setContact(contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new RuntimeException("Contact not found: " + request.getContactId())));
            return repository.save(societeContact);
        }
    }

    @Override
    public SocieteContact update(SocieteContactRequest request) {
        SocieteContact societeContact = repository.findByIds(request.getSocieteId(), request.getContactId()).orElseThrow(() -> new RuntimeException("SocieteContact not found"));
        societeContact.getSocieteContactId().setSociete(societeRepository.findById(request.getSocieteId()).orElseThrow(() -> new RuntimeException("SocieteAddress not found: " +request.getSocieteId())));
        societeContact.getSocieteContactId().setContact(contactRepository.findById(request.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found: " +request.getContactId())));
        societeContact.setRepresentantLegal(request.isRepresentantLegal());
        societeContact.setPrincipal(request.isPrincipal());
        return repository.save(societeContact);
    }

    @Override
    public void delete(Integer societeId, Integer contactId) {
        repository.softDelete(societeId, contactId);
    }


}
