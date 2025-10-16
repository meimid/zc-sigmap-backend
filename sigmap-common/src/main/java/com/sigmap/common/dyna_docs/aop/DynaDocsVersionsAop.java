package com.sigmap.common.dyna_docs.aop;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.DynaDocsSpecifications;
import com.sigmap.common.dyna_docs.exceptions.DynaDocNotFound;
import com.sigmap.common.dyna_docs.mappers.DynaDocsMapper;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsService;
import com.sigmap.common.dyna_fields.DynaFieldsSpecifications;
import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.request.DynaFieldsQuery;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsService;
import com.sigmap.common.dyna_fields_details.mappers.DynaFieldsDetailsMapper;
import java.util.List;

import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsQuery;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Slf4j
public class DynaDocsVersionsAop {
  private final DynaDocsService dynaDocsService;
  private final DynaFieldsService dynaFieldsService;
  private final DynaFieldsDetailsService dynaFieldsDetailsService;

  /**
   * Constructor for DynaDocsVersionsAop
   *
   * @param dynaDocsService Service for DynaDocs entities
   * @param dynaFieldsService Service for DynaFields entities
   * @param dynaFieldsDetailsService Service for DynaFieldsDetails entities
   */
  public DynaDocsVersionsAop(
      DynaDocsService dynaDocsService,
      DynaFieldsService dynaFieldsService,
      DynaFieldsDetailsService dynaFieldsDetailsService) {
    this.dynaDocsService = dynaDocsService;
    this.dynaFieldsService = dynaFieldsService;
    this.dynaFieldsDetailsService = dynaFieldsDetailsService;
  }

  /**
   * Pointcut definition for DynaDoc update operations. Triggers when the DynaDocsService.update
   * method is called.
   */
  @Pointcut("execution(* com.sigmap.common.dyna_docs.DynaDocsService.update(..))")
  public void onDynaDocUpdatePointcut() {}

  @Pointcut("execution(* com.sigmap.common.dyna_fields.DynaFieldsService.create(..))")
  public void onDynaFieldCreatePointcut() {}

  /** Around advice for DynaDoc update operations. Logs before and after the update operation. */
  @Pointcut("execution(* com.sigmap.common.dyna_fields.DynaFieldsService.update(..))")
  public void onDynaFieldUpdatePointcut() {}

  @Pointcut("execution(* com.sigmap.common.dyna_fields.DynaFieldsService.delete(..))")
  public void onDynaFieldDeletePointcut() {}

  @Around("onDynaDocUpdatePointcut()")
  @Transactional(rollbackFor = Throwable.class)
  public Object onDynaDocUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("before dynaDoc update ...");

    Object[] args = joinPoint.getArgs();

    Integer dynaDocId = (Integer) args[0];

    DynaDocs dynaDoc =
        dynaDocsService.findLatestById(dynaDocId).orElseThrow(() -> new DynaDocNotFound(dynaDocId));

    log.info("Found dynadoc with id {} and version {}", dynaDocId, dynaDoc.getVersion());

    Integer oldVersion = dynaDoc.getVersion();

    Integer newDynaDocsVer = dynaDoc.getVersion() + 1;

    log.info("New version is {}", newDynaDocsVer);

    DynaDocs newDynaDoc = DynaDocsMapper.INSTANCE.mapWithoutId(dynaDoc);
    newDynaDoc.setId(Integer.valueOf(dynaDocId));
    newDynaDoc.setVersion(Integer.valueOf(newDynaDocsVer));

    log.info("Saving new dynadoc  {}  {}", newDynaDoc.getId(), newDynaDoc.getVersion());

    dynaDocsService.save(newDynaDoc);

    DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
    dynaFieldsQuery.setDynaDocId(dynaDocId);
    dynaFieldsQuery.setDynaDocVer(oldVersion);

    List<DynaFields> dynaFields = dynaFieldsService.findAll(dynaFieldsQuery.toSpecification());

    log.info("Found {} dynafields", dynaFields.size());

    for (DynaFields dynaField : dynaFields) {
      DynaFields newDynaField = DynaFieldsMapper.INSTANCE.mapWithoutId(dynaField);
      newDynaField.setDynaFieldsId(dynaField.getDynaFieldsId());
      newDynaField.setDynaDocsId(dynaDocId);
      newDynaField.setDynaDocsVer(newDynaDocsVer);
      dynaFieldsService.save(newDynaField);
    }

    //Object result = joinPoint.proceed();

    log.info("after dynaDoc update ...");

    // dynaFieldsDetails update
    DynaFieldsDetailsQuery dynaFieldsDetailsQuery = new DynaFieldsDetailsQuery();
    dynaFieldsDetailsQuery.setDynaDocsId(dynaDocId);
    dynaFieldsDetailsQuery.setDynaDocsVer(oldVersion);
    //dynaFieldsDetailsQuery.setDynaFieldsId(dynaFieldsDetailsQuery.getDynaFieldsId());

    List<DynaFieldsDetails> dynaFieldsDetails = dynaFieldsDetailsService.findAll(dynaFieldsDetailsQuery.toSpecification());

    log.info("Found {} dynaFieldsDetails", dynaFieldsDetails.size());

    for (DynaFieldsDetails dynaFieldsDetail : dynaFieldsDetails) {
      DynaFieldsDetails newDynaFieldDetails = DynaFieldsDetailsMapper.INSTANCE.mapWithoutId(dynaFieldsDetail);
      newDynaFieldDetails.setDynaFieldsDetailsId(dynaFieldsDetail.getDynaFieldsId());
      newDynaFieldDetails.setDynaDocsId(dynaDocId);
      newDynaFieldDetails.setDynaDocsVer(newDynaDocsVer);
      newDynaFieldDetails.setDynaFieldsId(dynaFieldsDetail.getDynaFieldsId());
      dynaFieldsDetailsService.save(newDynaFieldDetails);
    }

    Object result = joinPoint.proceed();

    log.info("after dynaFieldsDetails update ...");
    return result;
  }

  @Around("onDynaFieldCreatePointcut()")
  @Transactional(rollbackFor = Throwable.class)
  public Object onDynaFieldCreate(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("before dynaField update ...");

    Object[] args = joinPoint.getArgs();

    // second argument is dynaFieldsRequest
    DynaFieldsRequest dynaFieldsRequest = (DynaFieldsRequest) args[0];

    Specification<DynaDocs> spec =
        DynaDocsSpecifications.latestById(dynaFieldsRequest.getDynaDocsId());

    DynaDocs dynaDoc =
        dynaDocsService
            .findOne(spec)
            .orElseThrow(() -> new DynaDocNotFound(dynaFieldsRequest.getDynaDocsId()));

    Integer dynaDocId = dynaDoc.getId();

    log.info("Found dynadoc with id {} and version {}", dynaDocId, dynaDoc.getVersion());

    Integer oldVersion = dynaDoc.getVersion();

    Integer newDynaDocsVer = dynaDoc.getVersion() + 1;

    log.info("New version is {}", newDynaDocsVer);

    DynaDocs newDynaDoc = DynaDocsMapper.INSTANCE.mapWithoutId(dynaDoc);
    newDynaDoc.setId(Integer.valueOf(dynaDocId));
    newDynaDoc.setVersion(Integer.valueOf(newDynaDocsVer));

    log.info("Saving new dynadoc  {}  {}", newDynaDoc.getId(), newDynaDoc.getVersion());

    dynaDocsService.save(newDynaDoc);

    DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
    dynaFieldsQuery.setDynaDocId(dynaDocId);
    dynaFieldsQuery.setDynaDocVer(oldVersion);

    List<DynaFields> dynaFields = dynaFieldsService.findAll(dynaFieldsQuery.toSpecification());

    log.info("Found {} dynafields", dynaFields.size());

    for (DynaFields dynaField : dynaFields) {
      DynaFields newDynaField = DynaFieldsMapper.INSTANCE.mapWithoutId(dynaField);
      newDynaField.setDynaFieldsId(dynaField.getDynaFieldsId());
      newDynaField.setDynaDocsId(dynaDocId);
      newDynaField.setDynaDocsVer(newDynaDocsVer);
      dynaFieldsService.save(newDynaField);

      if (dynaField.getDynaFieldsDetails() != null && !dynaField.getDynaFieldsDetails().isEmpty()) {
        for (DynaFieldsDetails dynaFieldsDetails : dynaField.getDynaFieldsDetails()) {
          DynaFieldsDetails newDynaFieldsDetails =
              DynaFieldsDetailsMapper.INSTANCE.mapWithoutId(dynaFieldsDetails);

          newDynaFieldsDetails.setDynaFieldsDetailsId(dynaFieldsDetails.getDynaFieldsDetailsId());
          newDynaFieldsDetails.setDynaFieldsId(dynaFieldsDetails.getDynaFieldsId());
          newDynaFieldsDetails.setDynaDocsId(dynaDocId);
          newDynaFieldsDetails.setDynaDocsVer(newDynaDocsVer);
          dynaFieldsDetailsService.save(newDynaFieldsDetails);
        }
      }
    }

    Object result = joinPoint.proceed();

    log.info("after dynaField update ...");

    return result;
  }

  @Around("onDynaFieldUpdatePointcut()")
  @Transactional(rollbackFor = Throwable.class)
  public Object onDynaFieldUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("before dynaField update ...");

    Object[] args = joinPoint.getArgs();

    Integer dynaFieldId = (Integer) args[0];
    // second argument is dynaFieldsRequest
    DynaFieldsRequest dynaFieldsRequest = (DynaFieldsRequest) args[1];

    Specification<DynaDocs> spec =
        DynaDocsSpecifications.latestById(dynaFieldsRequest.getDynaDocsId());

    DynaDocs dynaDoc =
        dynaDocsService
            .findOne(spec)
            .orElseThrow(() -> new DynaDocNotFound(dynaFieldsRequest.getDynaDocsId()));

    Integer dynaDocId = dynaDoc.getId();

    log.info("Found dynadoc with id {} and version {}", dynaDocId, dynaDoc.getVersion());

    Integer oldVersion = dynaDoc.getVersion();

    Integer newDynaDocsVer = dynaDoc.getVersion() + 1;

    log.info("New version is {}", newDynaDocsVer);

    DynaDocs newDynaDoc = DynaDocsMapper.INSTANCE.mapWithoutId(dynaDoc);
    newDynaDoc.setId(Integer.valueOf(dynaDocId));
    newDynaDoc.setVersion(Integer.valueOf(newDynaDocsVer));

    log.info("Saving new dynadoc  {}  {}", newDynaDoc.getId(), newDynaDoc.getVersion());

    dynaDocsService.save(newDynaDoc);

    DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
    dynaFieldsQuery.setDynaDocId(dynaDocId);
    dynaFieldsQuery.setDynaDocVer(oldVersion);

    List<DynaFields> dynaFields = dynaFieldsService.findAll(dynaFieldsQuery.toSpecification());

    log.info("Found {} dynafields", dynaFields.size());

    for (DynaFields dynaField : dynaFields) {
      DynaFields newDynaField = DynaFieldsMapper.INSTANCE.mapWithoutId(dynaField);
      newDynaField.setDynaFieldsId(dynaField.getDynaFieldsId());
      newDynaField.setDynaDocsId(dynaDocId);
      newDynaField.setDynaDocsVer(newDynaDocsVer);
      dynaFieldsService.save(newDynaField);

      if (dynaField.getDynaFieldsDetails() != null
          && !dynaField.getDynaFieldsDetails().isEmpty()
          && !dynaField.getDynaFieldsId().equals(dynaFieldId)) {

        for (DynaFieldsDetails dynaFieldsDetails : dynaField.getDynaFieldsDetails()) {
          DynaFieldsDetails newDynaFieldsDetails =
              DynaFieldsDetailsMapper.INSTANCE.mapWithoutId(dynaFieldsDetails);

          newDynaFieldsDetails.setDynaFieldsDetailsId(dynaFieldsDetails.getDynaFieldsDetailsId());
          newDynaFieldsDetails.setDynaFieldsId(dynaFieldsDetails.getDynaFieldsId());
          newDynaFieldsDetails.setDynaDocsId(dynaDocId);
          newDynaFieldsDetails.setDynaDocsVer(newDynaDocsVer);
          dynaFieldsDetailsService.save(newDynaFieldsDetails);
        }
      }
    }

    Object result = joinPoint.proceed();

    log.info("after dynaField update ...");

    return result;
  }

  @Around("onDynaFieldDeletePointcut()")
  @Transactional(rollbackFor = Throwable.class)
  public void onDynaFieldDelete(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("before dynaField delete ...");

    Object[] args = joinPoint.getArgs();

    Integer dynaFieldId = (Integer) args[0];

    Specification<DynaFields> dynaFieldSpec = DynaFieldsSpecifications.dynaFieldId(dynaFieldId);
    DynaFields dynaField =
        dynaFieldsService.findAll(dynaFieldSpec).stream()
            .findFirst()
            .orElseThrow(() -> new DynaFieldNotFound(dynaFieldId));

    Specification<DynaDocs> spec = DynaDocsSpecifications.latestById(dynaField.getDynaDocsId());

    DynaDocs dynaDoc =
        dynaDocsService
            .findOne(spec)
            .orElseThrow(() -> new DynaDocNotFound(dynaField.getDynaDocsId()));

    Integer dynaDocId = dynaDoc.getId();

    log.info("Found dynadoc with id {} and version {}", dynaDocId, dynaDoc.getVersion());

    Integer oldVersion = dynaDoc.getVersion();

    Integer newDynaDocsVer = dynaDoc.getVersion() + 1;

    log.info("New version is {}", newDynaDocsVer);

    DynaDocs newDynaDoc = DynaDocsMapper.INSTANCE.mapWithoutId(dynaDoc);
    newDynaDoc.setId(Integer.valueOf(dynaDocId));
    newDynaDoc.setVersion(Integer.valueOf(newDynaDocsVer));

    log.info("Saving new dynadoc  {}  {}", newDynaDoc.getId(), newDynaDoc.getVersion());

    dynaDocsService.save(newDynaDoc);

    DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
    dynaFieldsQuery.setDynaDocId(dynaDocId);
    dynaFieldsQuery.setDynaDocVer(oldVersion);

    List<DynaFields> dynaFields =
        dynaFieldsService.findAll(dynaFieldsQuery.toSpecification()).stream()
            .filter(item -> !item.getDynaFieldsId().equals(dynaFieldId))
            .toList();

    log.info("Found {} dynafields", dynaFields.size());

    for (DynaFields item : dynaFields) {
      // Fix: Use 'item' (the current field being copied) instead of 'dynaField' (the deleted field)
      DynaFields newDynaField = DynaFieldsMapper.INSTANCE.mapWithoutId(item);
      newDynaField.setDynaFieldsId(item.getDynaFieldsId());
      newDynaField.setDynaDocsId(dynaDocId);
      newDynaField.setDynaDocsVer(newDynaDocsVer);
      dynaFieldsService.save(newDynaField);

      if (item.getDynaFieldsDetails() != null && !item.getDynaFieldsDetails().isEmpty()) {
        for (DynaFieldsDetails dynaFieldsDetails : item.getDynaFieldsDetails()) {
          DynaFieldsDetails newDynaFieldsDetails =
              DynaFieldsDetailsMapper.INSTANCE.mapWithoutId(dynaFieldsDetails);

          newDynaFieldsDetails.setDynaFieldsDetailsId(dynaFieldsDetails.getDynaFieldsDetailsId());
          newDynaFieldsDetails.setDynaFieldsId(dynaFieldsDetails.getDynaFieldsId());
          newDynaFieldsDetails.setDynaDocsId(dynaDocId);
          newDynaFieldsDetails.setDynaDocsVer(newDynaDocsVer);
          dynaFieldsDetailsService.save(newDynaFieldsDetails);
        }
      }
    }

    return;
    // Object result = joinPoint.proceed();
    //
    // log.info("after dynaField delete ...");
  }
}
