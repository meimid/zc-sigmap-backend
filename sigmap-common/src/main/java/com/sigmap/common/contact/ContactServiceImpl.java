package com.sigmap.common.contact;

import com.sigmap.common.contact.Mapper.ContactMapper;
import com.sigmap.common.contact.request.ContactRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {
    private final ContactRepository repository;

    public ContactServiceImpl(ContactRepository repository) {
        this.repository = repository;
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
    public Optional<Contact> findById(Integer id) {
        return repository.findById(id);
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
    public Contact update(ContactRequest request) {
        Contact contact=repository.findById(request.getId()).orElseThrow(() -> new RuntimeException("contact not found: " + request.getId()));
        ContactMapper.INSTANCE.updateFromRequest(request, contact);
        return repository.save(contact);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);

    }
}
