package com.sigmap.common.docs;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocsServiceImpl implements DocsService {
    private final DocsRepository repository;

    public DocsServiceImpl(DocsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Docs> findById(Integer id) {
        return repository.findById(id);
    }



    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }

    @Override
    public Optional<Docs> findByAlfescoId(String alfrescoId) {
        return repository.findDocsByAlfrescoId(alfrescoId);
    }

    @Override
    public List<Docs> findDocsByPaasIdAndPaasVersion(Integer paaId, Integer paaVersion) {
        return repository.findDocsByPaasIdAndPaasVersion(paaId, paaVersion);
    }
    @Override
    public List<Docs> findDocsByRealisationIdAndRealisationVersion(Integer realisationId, Integer realisationVersion) {
        return repository.findDocsByRealisationIdAndRealisationVersion(realisationId, realisationVersion);
    }

    @Override
    public List<Docs> findDocsByPpmIdAndPpmVersion(Integer ppmId, Integer ppmVersion) {
        return repository.findDocsByPpmIdAndPpmVersion(ppmId, ppmVersion);
    }
    @Override
    public Optional<Docs> findDeletedById(Integer id) {
        Specification<Docs> specification = DocsSpecifications.id(id).and(DocsSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Docs> findOne(Specification<Docs> specification) {
        return repository.findOne(specification);
    }
}
