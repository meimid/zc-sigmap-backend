package com.sigmap.common.com;

import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class ComServiceImpl implements ComService{
    private final ComRepository repository;
    private final RefParamService refParamService;
    private final AcRepository acRepository;

    public ComServiceImpl(ComRepository repository, RefParamService refParamService, AcRepository acRepository) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.acRepository = acRepository;
    }

    private Locale getLocal (){
        Locale locale = LocaleContextHolder.getLocale();
        return locale;
    }
    @Override
    public List<Com> findAll() {
        Specification<Com> spec = ComSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Com> findAll(Specification<Com> com, Pageable pageable) {
        return repository.findAll(com, pageable);
    }

    @Override
    public Page<Com> findAllDeleted(Specification<Com> com, Pageable pageable) {
        com = ComSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Page<Com> findAllDeleted(Pageable pageable) {
        Specification<Com> com = ComSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Optional<Com> findDeletedById(Integer id) {
        Specification<Com> specification = ComSpecifications.id(id).and(ComSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Com> findOne(Specification<Com> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Com findById(Integer id) throws ComNotFound {
        return repository.findById(id)
                .orElseThrow(() -> new ComNotFound("", List.of(id)));
    }

    @Override
    public Com save(Com com) {
        return repository.save(com);
    }

    @Override
    public Com create(ComRequest request) throws RefParamNotFound , AcNotFound{
        RefParam comType=refParamService.findById(request.getComTypeId())
                .orElseThrow(() -> new RefParamNotFound("",List.of(request.getComTypeId())));
        Com com = ComMapper.INSTANCE.map(request);
        if (comType.getId() != null) {
            com.setComType(comType);
        } else {
            com.setComType(null);
        }
        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            var acs = acRepository.findAllById(request.getAcIds());
            // Validate that we found all requested Com entities
            if (acs.size() != request.getAcIds().size()) {
                throw new AcNotFound("", request.getAcIds());
            }
            com.setAcs(acs);
        }
        return repository.save(com);
    }

    @Override
    public Com update(Integer id,ComRequest request) throws ComNotFound, RefParamNotFound, AcNotFound {
        Com com = repository.findById(id)
                .orElseThrow(() -> new ComNotFound("" , List.of(request.getComTypeId())));
        RefParam comType=refParamService.findById(request.getComTypeId())
                .orElseThrow(() -> new RefParamNotFound("" , List.of(request.getComTypeId())));
      ComMapper.INSTANCE.updateFromRequest(request, com);
        if (comType.getId() != null) {
            com.setComType(comType);
        } else {
            com.setComType(null);
        }
        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            var acs = acRepository.findAllById(request.getAcIds());
            if (acs.size() != request.getAcIds().size()) {
                throw new AcNotFound("",request.getAcIds());
            }
            com.setAcs(acs);
        }
        return repository.save(com);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
