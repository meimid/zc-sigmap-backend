package com.sigmap.common.dyna_docs;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.dyna_docs.exceptions.DynaDocNotFound;
import com.sigmap.common.dyna_docs.mappers.DynaDocsMapper;
import com.sigmap.common.dyna_docs.request.DynaDocsRequest;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.config.Constants;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionService;

import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.utils.SequenceGeneratorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DynaDocsServiceImpl implements DynaDocsService {

  private final DynaDocsRepository dynaDocsRepository;
  private final RefParamService refParamService;
  private final SequenceGeneratorService sequenceGeneratorService;
  private final ModeDePassationSelectionService modeDePassationSelectionService;


  public DynaDocsServiceImpl(

          DynaDocsRepository dynaDocsRepository,
          RefParamService refParamService, SequenceGeneratorService sequenceGeneratorService,
          ModeDePassationSelectionService modeDePassationSelectionService) {
      this.sequenceGeneratorService = sequenceGeneratorService;

    this.dynaDocsRepository = dynaDocsRepository;
    this.refParamService = refParamService;
    this.modeDePassationSelectionService = modeDePassationSelectionService;
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
    CompositeId dynaDocID = new CompositeId(dynaDocId, dynaDocVer);
    return dynaDocsRepository.findById(dynaDocID);
  }

  @Override
  public DynaDocs save(DynaDocs dynaDoc) {
    return dynaDocsRepository.save(dynaDoc);
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public DynaDocs create(DynaDocsRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound {
    DynaDocs dynaDocs = DynaDocsMapper.INSTANCE.map(request);

    dynaDocs.setId(sequenceGeneratorService.getNextId(Constants.DYNA_DOCS_SEQ));
    dynaDocs.setVersion(1);

    RefParam refParam = refParamService.findById(request.getTypeMarcheId());
    dynaDocs.setTypeMarche(refParam);

    if (request.getModePassationId() != null) {
      ModeDePassationSelection mp = modeDePassationSelectionService.findById(request.getModePassationId());
      dynaDocs.setModePassation(mp);
    } else {
      dynaDocs.setModePassation(null);
    }

    return save(dynaDocs);
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public DynaDocs update(Integer dynaDocId, DynaDocsRequest request) throws DynaDocNotFound, ModeDePassationSelectionNotFound {
    DynaDocs dynaDoc = findLatestById(dynaDocId).orElseThrow(() -> new DynaDocNotFound(dynaDocId));

    DynaDocsMapper.INSTANCE.map(request, dynaDoc);

    Specification<RefParam> refParamSpec = RefParamSpecifications.id(request.getTypeMarcheId());
    RefParam refParam = refParamService.findOne(refParamSpec).orElseThrow();
    dynaDoc.setTypeMarche(refParam);

    if (request.getModePassationId() != null) {
      ModeDePassationSelection mp = modeDePassationSelectionService.findById(request.getModePassationId());
      dynaDoc.setModePassation(mp);
    } else {
      dynaDoc.setModePassation(null);
    }

    return save(dynaDoc);
  }
}
