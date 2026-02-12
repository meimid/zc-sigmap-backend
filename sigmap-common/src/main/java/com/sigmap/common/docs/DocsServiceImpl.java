package com.sigmap.common.docs;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.paas.PaaService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DocsServiceImpl implements DocsService {
    private final DocsRepository repository;
    private final AlfrescoService alfrescoService;

    public DocsServiceImpl(DocsRepository repository, AlfrescoService alfrescoService) {
        this.repository = repository;
        this.alfrescoService = alfrescoService;

    }

    @Override
    public Docs findById(Integer id) throws DocsNotFoundException {
        Specification<Docs> specification = DocsSpecifications.id(id)
                .and(DocsSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new DocsNotFoundException(""));
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

    @Override
    public Optional <DocumentResponseDTO> getDocumentInformation(String alfrescoId) {
        return Optional.ofNullable(alfrescoService.getDocumentById(alfrescoId));
    }
    @Override
    public byte[] downloadDocument(String alfrescoId) throws IOException {

        if (alfrescoId == null || alfrescoId.isBlank()) {
            throw new IOException("Alfresco ID cannot be null or empty.");
        }
        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(alfrescoId));
        if (dummyContent.getFile()!=null) {
            return dummyContent.getFile();
        }
        else {
            throw new IOException("Error while downloading document.");
        }

    }




}
