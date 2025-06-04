package com.sigmap.common.dyna_fields_details;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import com.sigmap.common.dyna_fields_details.mappers.DynaFieldsDetailsMapper;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DynaFieldsDetailsServiceImpl implements DynaFieldsDetailsService {
  private final DynaFieldsDetailsRepository dynaFieldsDetailsRepository;
  private final RefParamService refParamService;

  /**
   * Constructor for DynaFieldsDetailsServiceImpl with dependency injection.
   *
   * @param refParamService Service for RefParam entities
   * @param dynaFieldsDetailsRepository Repository for DynaFieldsDetails entities
   */
  public DynaFieldsDetailsServiceImpl(
      RefParamService refParamService, DynaFieldsDetailsRepository dynaFieldsDetailsRepository) {
    this.refParamService = refParamService;
    this.dynaFieldsDetailsRepository = dynaFieldsDetailsRepository;
  }

  @Override
  public Page<DynaFieldsDetails> findAll(Specification<DynaFieldsDetails> spec, Pageable pageable) {
    return dynaFieldsDetailsRepository.findAll(spec, pageable);
  }

  @Override
  public List<DynaFieldsDetails> findAll(Specification<DynaFieldsDetails> spec) {
    return dynaFieldsDetailsRepository.findAll(spec);
  }

  @Override
  public Optional<DynaFieldsDetails> findOne(Specification<DynaFieldsDetails> specification) {
    return dynaFieldsDetailsRepository.findOne(specification);
  }

  @Override
  public long count(Specification<DynaFieldsDetails> specification) {
    return dynaFieldsDetailsRepository.count(specification);
  }

  @Override
  public Optional<DynaFieldsDetails> findById(Integer detaildId) {
    Specification<DynaFieldsDetails> spec = DynaFieldsDetailsSpecification.detailId(detaildId);
    return dynaFieldsDetailsRepository.findOne(spec);
  }

  @Override
  public DynaFieldsDetails save(DynaFieldsDetails dynaField) {
    return dynaFieldsDetailsRepository.save(dynaField);
  }

  @Override
  public DynaFieldsDetails create(DynaFields dynaField, DynaFieldsDetailsRequest request) {

    DynaFieldsDetails dynaFieldsDetails = DynaFieldsDetailsMapper.INSTANCE.map(request);

    dynaFieldsDetails.setDynaFieldsId(dynaField.getDynaFieldsId());
    dynaFieldsDetails.setDynaDocsId(dynaField.getDynaDocsId());
    dynaFieldsDetails.setDynaDocsVer(dynaField.getDynaDocsVer());

    dynaFieldsDetails.setDynaFields(dynaField);

    Specification<RefParam> refSpec = RefParamSpecifications.id(request.getTypeId());

    RefParam typeId = refParamService.findOne(refSpec).orElseThrow();

    dynaFieldsDetails.setTypeId(typeId);

    return save(dynaFieldsDetails);
  }

  @Override
  public List<DynaFieldsDetails> createAll(
      DynaFields dynaField, List<DynaFieldsDetailsRequest> request) throws DynaFieldNotFound {
    List<DynaFieldsDetails> dynaFieldsDetails = new ArrayList<>();

    for (DynaFieldsDetailsRequest req : request) {
      dynaFieldsDetails.add(create(dynaField, req));
    }

    return dynaFieldsDetails;
  }
}
