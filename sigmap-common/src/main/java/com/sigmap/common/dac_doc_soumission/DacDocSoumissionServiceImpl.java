package com.sigmap.common.dac_doc_soumission;

import com.sigmap.common.config.TypeDossier;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacIdRequired;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionAlreadyExist;
import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionNotFound;
import com.sigmap.common.dac_doc_soumission.exception.DocSoumissionIdRequired;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionCreateRequest;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionUpdateRequest;
import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.doc_soumission.DocSoumissionService;
import com.sigmap.common.reunion.exception.DateBefore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DacDocSoumissionServiceImpl implements DacDocSoumissionService {
    private final DacDocSoumissionRepository repository;
    private final DacService dacService;
    private final DocSoumissionService docSoumissionService;

    public DacDocSoumissionServiceImpl(DacDocSoumissionRepository repository, DacService dacService, DocSoumissionService docSoumissionService) {
        this.repository = repository;
        this.dacService = dacService;
        this.docSoumissionService = docSoumissionService;
    }


    @Override
    public List<DacDocSoumission> findAllList(Specification<DacDocSoumission> dacDocSoumission) {
        dacDocSoumission = dacDocSoumission != null ? dacDocSoumission : Specification.where(null);

        return repository.findAll(dacDocSoumission);
    }

    @Override
    public List<DacDocSoumission> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<DacDocSoumission> findAll(Specification<DacDocSoumission> dacDocSoumission, Pageable pageable) {
        dacDocSoumission = dacDocSoumission != null ? dacDocSoumission : Specification.where(null);
        return repository.findAll(dacDocSoumission, pageable);
    }

    @Override
    public Optional<DacDocSoumission> findOne(Specification<DacDocSoumission> specification) {
        return repository.findOne(specification);
    }

    @Override
    public DacDocSoumission findByDacIdAndDocSoumissionId(Integer dacId, Integer docSoumissionId) throws DacDocSoumissionNotFound {
        Integer dacLatestVersion;
        try {
            dacLatestVersion = dacService.findLatestVersionById(dacId);
        } catch (Exception e) {
            throw new DacNotFound("DAC not found with ID " + dacId, dacId, null);
        }
        Specification<DacDocSoumission> specification = DacDocSoumissionSpecifications.byDacId(dacId).and(DacDocSoumissionSpecifications.byDacVer(dacLatestVersion)).and(DacDocSoumissionSpecifications.byDocSoumissionId(docSoumissionId));
        return findOne(specification).orElseThrow(() -> new DacDocSoumissionNotFound(""));

    }

    @Override
    public List<DacDocSoumission> findByDacId(Integer dacId) throws DacDocSoumissionNotFound {
        Integer dacLatestVersion;
        try {
            dacLatestVersion = dacService.findLatestVersionById(dacId);
        } catch (Exception e) {
            throw new DacNotFound("DAC not found with ID " + dacId, dacId, null);
        }
          Specification <DacDocSoumission>specification = DacDocSoumissionSpecifications.byDacId(dacId);
        return repository.findAll(specification);
    }


    @Override
    public DacDocSoumission save(DacDocSoumission dacDocSoumission) {
        return repository.save(dacDocSoumission);
    }

    @Override
    public DacDocSoumission create(DacDocSoumissionCreateRequest request) {
        if(request.getDacId()==null) {
            throw new DacIdRequired("DAC ID is required");
        }
        if(request.getDocSoumissionId()==null) {
            throw new DocSoumissionIdRequired("Doc Soumission ID is required");
        }
        Integer dacLatestVersion;
        try {
            dacLatestVersion = dacService.findLatestVersionById(request.getDacId());
        } catch (Exception e) {
            throw new DacNotFound("DAC not found with ID " + request.getDacId(), request.getDacId(), null);
        }

        Dac dac = dacService.findById(request.getDacId(),dacLatestVersion).orElseThrow(()->new DacNotFound("DAC not found with ID " + request.getDacId(), request.getDacId(), null));
        DocSoumission docSoumission = docSoumissionService.findById(request.getDocSoumissionId());
        Specification<DacDocSoumission> specification = DacDocSoumissionSpecifications.byDacId(request.getDacId()).and(DacDocSoumissionSpecifications.byDacVer(dacLatestVersion)).and(DacDocSoumissionSpecifications.byDocSoumissionId(request.getDocSoumissionId()));
        DacDocSoumission checkExistingDacDocSoumission=  findOne(specification).orElse(null);
        if(checkExistingDacDocSoumission!=null) {
            throw new DacDocSoumissionAlreadyExist("", dac.getDacId(), request.getDacId(), request.getDocSoumissionId());
        }

        DacDocSoumission dacDocSoumission = new DacDocSoumission();
        dacDocSoumission.setDacId(dac.getDacId());
        dacDocSoumission.setDacVer(dac.getDacVer());
        dacDocSoumission.setDocSoumissionId(docSoumission.getId());
        dacDocSoumission.setDac(dac);
        dacDocSoumission.setDocSoumission(docSoumission);
        if(request.getDateValidate()!=null) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            log.info("today {}", today);

            if (request.getDateValidate().before(today.getTime())) {
                throw new DateBefore("La date de validation ne peut pas être antérieure à aujourd’hui.");
            }
            else {
                dacDocSoumission.setDateValidate(request.getDateValidate());
            }
        }

        return save(dacDocSoumission);
    }

    @Override
    public DacDocSoumission update(Integer dacId, Integer docSoumissionId, DacDocSoumissionUpdateRequest request) throws DacDocSoumissionNotFound {
        if(dacId==null) {
            throw new DacIdRequired("DAC ID is required");
        }
        if(docSoumissionId==null) {
            throw new DocSoumissionIdRequired("Doc Soumission ID is required");
        }
        DacDocSoumission dacDocSoumission = findByDacIdAndDocSoumissionId(dacId,docSoumissionId);

        if(request.getDateValidate()!=null) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            log.info("today {}", today);

            if (request.getDateValidate().before(today.getTime())) {
                throw new DateBefore("La date de validation ne peut pas être antérieure à aujourd’hui.");
            }
            else {
                dacDocSoumission.setDateValidate(request.getDateValidate());
            }
        }
        return save(dacDocSoumission);
    }

    @Override
    public void delete(Integer dacId, Integer docSoumissionId) throws DacDocSoumissionNotFound {

        DacDocSoumission dacDocSoumission = findByDacIdAndDocSoumissionId(dacId,docSoumissionId);
        repository.delete(dacDocSoumission);


    }

    @Override
    public List<DocSoumission> findNotExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId,Specification<DocSoumission> docSoumissionSpec) {
        return docSoumissionService.findNotExistingDocSoumissionOfDacDocSoumissionByDacId(dacId,docSoumissionSpec);
    }

    @Override
    public List<DocSoumission> findExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId,Specification<DocSoumission> docSoumissionSpec) {
        return docSoumissionService.findExistingDocSoumissionOfDacDocSoumissionByDacId(dacId,docSoumissionSpec);
    }




}
