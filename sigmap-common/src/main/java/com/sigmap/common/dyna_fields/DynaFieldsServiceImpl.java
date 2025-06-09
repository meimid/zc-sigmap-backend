package com.sigmap.common.dyna_fields;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.DynaDocsSpecifications;
import com.sigmap.common.dyna_docs.exceptions.DynaDocNotFound;
import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsService;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import java.util.HashSet;
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
  private final DynaFieldsSequenceGenerator dynaFieldsSequenceGenerator;
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
      DynaFieldsSequenceGenerator dynaFieldsSequenceGenerator,
      DynaFieldsRepository dynaFieldsRepository,
      DynaFieldsDetailsService dynaFieldsDetailsService,
      RefParamService refParamService) {
    this.dynaDocsService = dynaDocsService;
    this.dynaFieldsRepository = dynaFieldsRepository;
    this.dynaFieldsSequenceGenerator = dynaFieldsSequenceGenerator;
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

    List<DynaFields> dynaFields = dynaFieldsRepository.findAll(spec);

    if (dynaFields.isEmpty()) {
      return Optional.empty();
    }

    Integer dynaDocsId = dynaFields.get(0).getDynaDocsId();

    Specification<DynaDocs> dynaDocSpec = DynaDocsSpecifications.latestById(dynaDocsId);
    DynaDocs dynaDocs =
        dynaDocsService.findOne(dynaDocSpec).orElseThrow(() -> new DynaDocNotFound(dynaDocsId));

    Optional<DynaFields> dynaField =
        dynaFields.stream()
            .filter(item -> item.getDynaDocsId().equals(dynaDocsId))
            .filter(item -> item.getDynaDocsVer().equals(dynaDocs.getDynaDocsVer()))
            .findFirst();

    return dynaField;
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
    dynaFields.setDynaFieldsId(dynaFieldsSequenceGenerator.getNextId());
    dynaFields.setDynaDocs(dynaDocs);

    RefParam typeId = refParamService.findById(request.getTypeId());
    dynaFields.setType_id(typeId);

    dynaFields = dynaFieldsRepository.save(dynaFields);

    log.info("DynaField created with ID: {}", dynaFields.getDynaFieldsId());

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

  @Override
  public DynaFields update(Integer dynaFieldId, DynaFieldsRequest request) throws RefParamNotFound {
    Specification<DynaDocs> spec = DynaDocsSpecifications.latestById(request.getDynaDocsId());
    DynaDocs dynaDocs =
        dynaDocsService
            .findOne(spec)
            .orElseThrow(() -> new DynaDocNotFound(request.getDynaDocsId()));

    Specification<DynaFields> dynaFieldSpec =
        Specification.allOf(
            DynaFieldsSpecifications.dynaFieldId(dynaFieldId),
            DynaFieldsSpecifications.dynaDocId(dynaDocs.getDynaDocsId()),
            DynaFieldsSpecifications.dynaDocVer(dynaDocs.getDynaDocsVer()));

    DynaFields dynaFields =
        dynaFieldsRepository
            .findOne(dynaFieldSpec)
            .orElseThrow(() -> new DynaFieldNotFound(dynaFieldId));

    DynaFieldsMapper.INSTANCE.map(request, dynaFields);

    save(dynaFields);

    if (request.getDetails() != null && !request.getDetails().isEmpty()) {
      List<DynaFieldsDetailsRequest> detailsReq =
          DynaFieldsMapper.INSTANCE.mapDetails(request.getDetails());

      List<DynaFieldsDetails> detailsList =
          dynaFieldsDetailsService.createAll(dynaFields, detailsReq);

      Set<DynaFieldsDetails> details = DynaFieldsMapper.INSTANCE.detailsToSet(detailsList);

      dynaFields.setDynaFieldsDetails(details);
    } else {
      dynaFields.setDynaFieldsDetails(new HashSet<>());
    }

    return dynaFields;
  }

  /**
   * Delete DynaField.
   *
   * <p>WARNING : ....
   *
   * @param dynaFieldId ID
   */
  @Override
  public void delete(Integer dynaFieldId) {
    throw new UnsupportedOperationException();
  }
}
