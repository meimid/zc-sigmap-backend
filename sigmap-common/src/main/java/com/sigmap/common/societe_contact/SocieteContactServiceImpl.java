package com.sigmap.common.societe_contact;

import com.sigmap.common.contact.ContactRepository;
import com.sigmap.common.contact.ContactService;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import com.sigmap.common.societe_contact.mapper.SociteContactMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class SocieteContactServiceImpl implements SocieteContactService {

    RefParamService refParamService;
    ContactService contactService;
    SocieteRepository societeRepository;
    SocieteService societeService;
    SocieteContactRepository repository;
    ContactRepository contactRepository;


    public SocieteContactServiceImpl(ContactRepository contactRepository,SocieteContactRepository repository,RefParamService refParamService, ContactService contactService, SocieteRepository societeRepository, SocieteService societeService) {
        this.refParamService = refParamService;
        this.contactService = contactService;
        this.societeRepository = societeRepository;
        this.societeService = societeService;
        this.repository = repository;
        this.contactRepository = contactRepository;
    }

    @Override
    public List<SocieteContact> findAll() {
        Specification<SocieteContact> spec = SocieteContactSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<SocieteContact> findAll(Specification<SocieteContact> societeContact, Pageable pageable) {
        //Specification<SocieteContact> spec = SocieteContactSpecifications.deleted(false);
        return repository.findAll(societeContact, pageable);
    }

    @Override
    public Page<SocieteContact> findAllDeleted(Specification<SocieteContact> societeContact, Pageable pageable) {
        societeContact = SocieteContactSpecifications.deleted(true);
        return repository.findAll(societeContact, pageable);
    }

    @Override
    public Page<SocieteContact> findAllDeleted(Pageable pageable) {
        Specification<SocieteContact> com = SocieteContactSpecifications.deleted(true);
        return repository.findAll(com, pageable);
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
    public SocieteContact create(SocieteContactRequest request) {
        SocieteContact societeContact = new SocieteContact();
        SocieteContactId societeContactId = new SocieteContactId();
        societeContact= SociteContactMapper.INSTANCE.map(request);
        societeContact.setSocieteContactId(societeContactId);
        societeContactId.setSociete(societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new RuntimeException("Societe not found: " + request.getSocieteId())));
        societeContactId.setContact(contactRepository.findById(request.getContactId())
                .orElseThrow(() -> new RuntimeException("Contact not found: " + request.getContactId())));

        return repository.save(societeContact);
    }

    @Override
    public SocieteContact update(SocieteContactRequest request) {
        SocieteContact societeContact = repository.findByIds(request.getSocieteId(), request.getContactId()).orElseThrow(() -> new RuntimeException("SocieteContact not found"));
      //  societeContact= SociteContactMapper.INSTANCE.map(request);
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
