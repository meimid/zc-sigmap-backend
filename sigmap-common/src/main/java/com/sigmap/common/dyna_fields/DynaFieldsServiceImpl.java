package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.DynaDocsSpecifications;
import com.sigmap.common.dyna_docs.exceptions.DynaDocNotFound;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsService;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DynaFieldsServiceImpl implements DynaFieldsService {
  private final DynaDocsService dynaDocsService;
  private final DynaFieldsRepository dynaFieldsRepository;
  private final DynaFieldsDetailsService dynaFieldsDetailsService;
  private final RefParamService refParamService;

  /**
   * Constructor for DynaFieldsServiceImpl with dependency injection.
   *
   * @param dynaDocsService Service for DynaDocs entities
   * @param dynaFieldsRepository Repository for DynaFields entities
   * @param refParamService Service for RefParam entities
   */
  public DynaFieldsServiceImpl(
      DynaDocsService dynaDocsService,
      DynaFieldsRepository dynaFieldsRepository,
      DynaFieldsDetailsService dynaFieldsDetailsService,
      RefParamService refParamService) {
    this.dynaDocsService = dynaDocsService;
    this.dynaFieldsRepository = dynaFieldsRepository;
    this.dynaFieldsDetailsService = dynaFieldsDetailsService;
    this.refParamService = refParamService;
  }

  @Override
  public Page<DynaFields> findAll(Specification<DynaFields> spec, Pageable pageable) {
    return dynaFieldsRepository.findAll(spec, pageable);
  }

  @Override
  public List<DynaFields> findAll(Specification<DynaFields> spec) {
    return dynaFieldsRepository.findAll(spec);
  }

  @Override
  public Optional<DynaFields> findOne(Specification<DynaFields> specification) {
    return dynaFieldsRepository.findOne(specification);
  }

  @Override
  public long count(Specification<DynaFields> specification) {
    return dynaFieldsRepository.count(specification);
  }

  @Override
  public Optional<DynaFields> findById(Integer dynaFieldId) {
    Specification<DynaFields> spec = DynaFieldsSpecifications.dynaFieldId(dynaFieldId);
    return dynaFieldsRepository.findOne(spec);
  }

  @Override
  public DynaFields save(DynaFields dynaField) {
    return dynaFieldsRepository.save(dynaField);
  }

  @Override
  public DynaFields create(DynaFieldsRequest request) throws RefParamNotFound {
    DynaFields dynaFields = DynaFieldsMapper.INSTANCE.map(request);

    Specification<DynaDocs> spec = DynaDocsSpecifications.latestById(request.getDynaDocsId());
    DynaDocs dynaDocs =
        dynaDocsService
            .findOne(spec)
            .orElseThrow(() -> new DynaDocNotFound(request.getDynaDocsId()));

    dynaFields.setDynaDocsId(dynaDocs.getDynaDocsId());
    dynaFields.setDynaDocsVer(dynaDocs.getDynaDocsVer());
    dynaFields.setDynaDocs(dynaDocs);

    RefParam typeId = refParamService.findById(request.getTypeId());
    dynaFields.setType_id(typeId);

    dynaFields = dynaFieldsRepository.save(dynaFields);

    log.info("DynaFields created with ID: {}", dynaFields.getDynaFieldsId());

    if (request.getDetails() != null && !request.getDetails().isEmpty()) {
      List<DynaFieldsDetailsRequest> detailsReq =
          DynaFieldsMapper.INSTANCE.mapDetails(request.getDetails());
      List<DynaFieldsDetails> detailsList =
          dynaFieldsDetailsService.createAll(dynaFields, detailsReq);
      Set<DynaFieldsDetails> details = DynaFieldsMapper.INSTANCE.detailsToSet(detailsList);
      dynaFields.setDynaFieldsDetails(details);
    }

    return dynaFields;
  }
}
