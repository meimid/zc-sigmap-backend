package com.sigmap.common.dyna_docs;

import com.sigmap.common.dyna_docs.exceptions.DynaDocNotFound;
import com.sigmap.common.dyna_docs.mappers.DynaDocsMapper;
import com.sigmap.common.dyna_docs.request.DynaDocsRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.Optional;

import com.sigmap.common.utils.SequenceGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sigmap.common.config.Constants;

@Service
@Slf4j
public class DynaDocsServiceImpl implements DynaDocsService {

  private final DynaDocsRepository dynaDocsRepository;
  private final RefParamService refParamService;
  private final SequenceGeneratorService sequenceGeneratorService;


  public DynaDocsServiceImpl(

          DynaDocsRepository dynaDocsRepository,
          RefParamService refParamService, SequenceGeneratorService sequenceGeneratorService) {
      this.sequenceGeneratorService = sequenceGeneratorService;

    this.dynaDocsRepository = dynaDocsRepository;
    this.refParamService = refParamService;
  }

  @Override
  public Page<DynaDocs> findAll(Specification<DynaDocs> spec, Pageable pageable) {
    spec = spec.and(DynaDocsSpecifications.latestVersionPerId());
    return dynaDocsRepository.findAll(spec, pageable);
  }

  @Override
  public Optional<DynaDocs> findOne(Specification<DynaDocs> specification) {
    return dynaDocsRepository.findOne(specification);
  }

  @Override
  public Optional<DynaDocs> findLatestById(Integer dynaDocId) {
    Specification<DynaDocs> spec = DynaDocsSpecifications.latestById(dynaDocId);
    return dynaDocsRepository.findOne(spec);
  }

  @Override
  public long count(Specification<DynaDocs> specification) {
    return dynaDocsRepository.count(specification);
  }

  @Override
  public Optional<DynaDocs> findById(Integer dynaDocId, Integer dynaDocVer) throws DynaDocNotFound {
    DynaDocId dynaDocID = new DynaDocId(dynaDocId, dynaDocVer);
    return dynaDocsRepository.findById(dynaDocID);
  }

  @Override
  public DynaDocs save(DynaDocs dynaDoc) {
    return dynaDocsRepository.save(dynaDoc);
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public DynaDocs create(DynaDocsRequest request) throws RefParamNotFound {
    DynaDocs dynaDocs = DynaDocsMapper.INSTANCE.map(request);

    dynaDocs.setId(sequenceGeneratorService.getNextId(Constants.DYNA_DOCS_SEQ));
    dynaDocs.setVersion(1);

    RefParam refParam = refParamService.findById(request.getCategorieId());
    dynaDocs.setCategorieId(refParam);

    return save(dynaDocs);
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public DynaDocs update(Integer dynaDocId, DynaDocsRequest request) throws DynaDocNotFound {
    DynaDocs dynaDoc = findLatestById(dynaDocId).orElseThrow(() -> new DynaDocNotFound(dynaDocId));

    DynaDocsMapper.INSTANCE.map(request, dynaDoc);

    Specification<RefParam> refParamSpec = RefParamSpecifications.id(request.getCategorieId());
    RefParam refParam = refParamService.findOne(refParamSpec).orElseThrow();
    dynaDoc.setCategorieId(refParam);

    return save(dynaDoc);
  }
}
