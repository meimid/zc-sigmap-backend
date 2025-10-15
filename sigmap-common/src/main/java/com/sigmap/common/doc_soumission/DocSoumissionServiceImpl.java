package com.sigmap.common.doc_soumission;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import com.sigmap.common.dac_doc_soumission.DacDocSoumissionRepository;
import com.sigmap.common.dac_doc_soumission.DacDocSoumissionSpecifications;
import com.sigmap.common.doc_soumission.exception.DocSoumissionNotFound;
import com.sigmap.common.doc_soumission.exception.HasDacDocSoumission;
import com.sigmap.common.doc_soumission.exception.NomRequired;
import com.sigmap.common.doc_soumission.exception.TypeDossierRequired;
import com.sigmap.common.doc_soumission.mapper.DocSoumissionMapper;
import com.sigmap.common.doc_soumission.request.DocSoumissionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocSoumissionServiceImpl implements DocSoumissionService {
    private final DocSoumissionRepository repository;
    private final DacDocSoumissionRepository dacDocSoumissionRepository;
    private final DacService dacService;
    public DocSoumissionServiceImpl(DocSoumissionRepository repository, DacDocSoumissionRepository dacDocSoumissionRepository, DacService dacService) {
        this.repository = repository;
        this.dacDocSoumissionRepository = dacDocSoumissionRepository;
        this.dacService = dacService;
    }
    @Override
    public List<DocSoumission> findAllList(Specification<DocSoumission> docSoumission) {
        docSoumission = docSoumission != null ? docSoumission : Specification.where(null);

        return repository.findAll(docSoumission);
    }
    @Override
    public List<DocSoumission> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<DocSoumission> findAll(Specification<DocSoumission> docSoumission, Pageable pageable) {
        docSoumission = docSoumission != null ? docSoumission : Specification.where(null);
        return repository.findAll(docSoumission, pageable);

    }
    

    @Override
    public Optional<DocSoumission> findOne(Specification<DocSoumission> specification) {
        return repository.findOne(specification);
    }

    @Override
    public DocSoumission findById(Integer id) throws DocSoumissionNotFound {
        Specification<DocSoumission> specification = DocSoumissionSpecifications.id(id);
        return findOne(specification).orElseThrow(() -> new DocSoumissionNotFound("",id));

    }

    @Override
    public DocSoumission save(DocSoumission docSoumission) {
        return repository.save(docSoumission);
    }

    @Override
    public DocSoumission create(DocSoumissionRequest request) {
        if(request.getNom() == null ||  request.getNom().isBlank()){
            throw new NomRequired("");
        }
        if(request.getTypeDossier()==null){
            throw new TypeDossierRequired("");
        }

        DocSoumission docSoumission = DocSoumissionMapper.INSTANCE.mapFromRequest(request);
        return save(docSoumission);

    }

    @Override
    public DocSoumission update(Integer id, DocSoumissionRequest request) throws DocSoumissionNotFound {
        DocSoumission docSoumission = findById(id);
        if(request.getNom() == null ||  request.getNom().isBlank()){
            throw new NomRequired("");
        }
        if(request.getTypeDossier()==null){
            throw new TypeDossierRequired("");
        }
        docSoumission.setNom(request.getNom());
        docSoumission.setTypeDossier(request.getTypeDossier());
        return save(docSoumission);
    }

    @Override
    public void delete(Integer id) throws DocSoumissionNotFound {
        Specification<DacDocSoumission> spec = Specification.where(DacDocSoumissionSpecifications.hasDacDocSoumission(id));

        boolean hasDacDocSoumission = dacDocSoumissionRepository.count(spec) > 0;
        if (hasDacDocSoumission) {
            throw new HasDacDocSoumission("");
        }
       DocSoumission docSoumission= findById(id);
       repository.delete(docSoumission);

    }

    @Override
    public List<DocSoumission> findNotExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId) {
  
        Integer dacLatestVersion;
        try {
            dacLatestVersion = dacService.findLatestVersionById(dacId);
        } catch (Exception e) {
            throw new DacNotFound("DAC not found with ID " + dacId, dacId, null);
        }

        dacService.findById(dacId,dacLatestVersion).orElseThrow(()->new DacNotFound("DAC not found with ID " + dacId, dacId, null));

        return repository.findNotExistingDocSoumissionOfDacDocSoumissionByDacId(dacId);
    }
}
