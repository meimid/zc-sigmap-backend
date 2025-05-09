package com.sigmap.common.contact;

import com.sigmap.common.compte_bancaire.exception.CompteBancaireNotFound;
import com.sigmap.common.contact.Mapper.ContactMapper;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.contact.request.ContactRequest;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_contact.SocieteContact;
import com.sigmap.common.societe_contact.SocieteContactService;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {
    private final ContactRepository repository;
    private final SocieteService societeService;

    public ContactServiceImpl(ContactRepository repository, SocieteService societeService) {
        this.repository = repository;
        this.societeService = societeService;
    }

    @Override
    public List<Contact> findAll() {
        Specification<Contact> spec = ContactSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Contact> findAll(Specification<Contact> Contact, Pageable pageable) {
        return repository.findAll(Contact, pageable);
    }

    @Override
    public Page<Contact> findAllDeleted(Specification<Contact> contact, Pageable pageable) {
        contact = ContactSpecifications.deleted(true);
        return repository.findAll(contact, pageable);
    }

    @Override
    public Page<Contact> findAllDeleted(Pageable pageable) {
        Specification<Contact> contact = ContactSpecifications.deleted(true);
        return repository.findAll(contact, pageable);
    }

    @Override
    public Optional<Contact> findDeletedById(Integer id) {
        Specification<Contact> specification = ContactSpecifications.id(id).and(ContactSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Contact> findOne(Specification<Contact> specification) {
        return repository.findOne(specification);

    }

    @Override
    public Contact findById(Integer id) throws ContactNotFound {
        return repository.findById(id).orElseThrow(() -> new ContactNotFound("",List.of(id)));
    }

    @Override
    public Contact save(Contact contact) {
        return repository.save(contact);
    }

    @Override
    public Contact create(ContactRequest request) {
        Contact contact =ContactMapper.INSTANCE.map(request);
        return repository.save(contact);
    }

    @Override
    public Contact update(Integer id,ContactRequest request) throws ContactNotFound {
        Contact contact=repository.findById(id).orElseThrow(() -> new ContactNotFound("",List.of(id)));
        ContactMapper.INSTANCE.updateFromRequest(request, contact);
        return repository.save(contact);
    }

    @Override
    public void delete(Integer id) throws ContactNotFound , SocieteContactNotFound {
        repository.softDelete(id);


    }

    @Override
    public Page<Contact> findContactsBySocieteId(Integer societeId, Pageable pageable) throws SocieteNotFound {
        societeService.findById(societeId);
        return repository.findContactsBySocieteId(societeId, pageable);
    }
}
