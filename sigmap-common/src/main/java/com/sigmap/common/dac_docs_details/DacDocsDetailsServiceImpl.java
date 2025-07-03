package com.sigmap.common.dac_docs_details;

import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs_details.exceptions.DacDocsDetailsNotFound;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsRepository;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j

public class DacDocsDetailsServiceImpl implements DacDocsDetailsService {

  @Autowired
  private DacDocsDetailsRepository dacDocsDetailsRepository;

  @Autowired
  private DacDocsDetailsSequenceGenerator sequenceGenerator;

  @Autowired
  private DynaFieldsDetailsRepository dynaFieldsDetailsRepository;

  @Override
  public Page<DacDocsDetails> findAll(Specification<DacDocsDetails> spec, Pageable pageable) {
    return dacDocsDetailsRepository.findAll(spec, pageable);
  }

  @Override
  public List<DacDocsDetails> findAll(Specification<DacDocsDetails> spec) {
    return dacDocsDetailsRepository.findAll(spec);
  }

  @Override
  public Optional<DacDocsDetails> findOne(Specification<DacDocsDetails> specification) {
    return dacDocsDetailsRepository.findOne(specification);
  }

  @Override
  public long count(Specification<DacDocsDetails> specification) {
    return dacDocsDetailsRepository.count(specification);
  }

  @Override
  public Optional<DacDocsDetails> findById(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
    DacDocsDetailsId id = new DacDocsDetailsId();
    id.setDynaFieldsDetailsId(dynaFieldsDetailsId);
    id.setRowId(rowId);
    id.setDacId(dacId);
    id.setDacVer(dacVer);
    id.setDynaDocsId(dynaDocsId);
    id.setDynaDocsVer(dynaDocsVer);
    id.setDynaFieldsId(dynaFieldsId);
    return dacDocsDetailsRepository.findById(id);
  }

  @Override
  public DacDocsDetails save(DacDocsDetails dacDocsDetails) {
    return dacDocsDetailsRepository.save(dacDocsDetails);
  }

  @Override
  public DacDocsDetails create(DacDocs dacDocs, DacDocsDetailsRequest request) {
    DacDocsDetails dacDocsDetails = new DacDocsDetails();

    // Set the composite ID fields from the parent DacDocs
    dacDocsDetails.setDacId(dacDocs.getId().getDacId());
    dacDocsDetails.setDacVer(dacDocs.getId().getDacVer());
    dacDocsDetails.setDynaDocsId(dacDocs.getId().getDynaDocsId());
    dacDocsDetails.setDynaDocsVer(dacDocs.getId().getDynaDocsVer());
    dacDocsDetails.setDynaFieldsId(dacDocs.getId().getDynaFieldsId());


    // Set the other request fields
    dacDocsDetails.setRowId(request.getRowId());
    dacDocsDetails.setSourceId(request.getSourceId());
    dacDocsDetails.setFieldValue(request.getFieldValue());
    dacDocsDetails.setDynaFieldsDetailsId(request.getDynaFieldsDetailsId());

    // Set the parent reference
    dacDocsDetails.setDac(dacDocs);
    DacDocsDetails savedDacDocsDetails = dacDocsDetailsRepository.save(dacDocsDetails);
    log.debug("DacDocsDetails created: ............. {}", savedDacDocsDetails);
    return savedDacDocsDetails ;
  }

  @Override
  @Transactional
  public List<DacDocsDetails> createAll(DacDocs dynaField, List<DacDocsDetailsRequest> request) throws DynaFieldNotFound {
    if (dynaField.getId() == null) {
      throw new IllegalArgumentException("Parent DacDocs must be persisted before adding details.");
    }
    List<DacDocsDetails> dacDocsDetails = new ArrayList<>();
    for (DacDocsDetailsRequest req : request) {
      DacDocsDetails detail = create(dynaField, req);
      log.debug("Saved DacDocsDetails: ............... {}", detail);
      dacDocsDetails.add(detail);
    }
    return dacDocsDetails;
  }

  @Override
  public DacDocsDetails update(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId, DacDocsDetailsRequest request) {
    // Check if the DacDocsDetails exists
    Optional<DacDocsDetails> existingDetails = findById(dynaFieldsDetailsId, rowId, dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);

    // check if dynaFieldsDetails exists
   // List<DynaFieldsDetails> findAllDynaFieldsDetails(Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);
    if (existingDetails.isPresent()) {

      // check if dynaFieldsDetails exists
      //List<DynaFieldsDetails> findAllDynaFieldsDetails = (Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);

      DacDocsDetails details = existingDetails.get();
      details.setSourceId(request.getSourceId());
      details.setFieldValue(request.getFieldValue());
      return dacDocsDetailsRepository.save(details);
    } else {
      throw new DacDocsDetailsNotFound(dynaFieldsDetailsId, rowId, dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
    }
  }

  @Override
  public void delete(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
    Optional<DacDocsDetails> existingDetails = findById(dynaFieldsDetailsId, rowId, dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
    
    if (existingDetails.isPresent()) {
      dacDocsDetailsRepository.delete(existingDetails.get());
    } else {
      throw new DacDocsDetailsNotFound(dynaFieldsDetailsId, rowId, dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
    }
  }

  @Override
  public void deleteByDacDocs(DacDocs dacDocs) {
    // Create a specification to find all details for the given DacDocs
    Specification<DacDocsDetails> spec = Specification.where(
        DacDocsDetailsSpecification.byDacId(dacDocs.getId().getDacId()))
        .and(DacDocsDetailsSpecification.byDacVer(dacDocs.getId().getDacVer()))
        .and(DacDocsDetailsSpecification.byDynaDocsId(dacDocs.getId().getDynaDocsId()))
        .and(DacDocsDetailsSpecification.byDynaDocsVer(dacDocs.getId().getDynaDocsVer()))
        .and(DacDocsDetailsSpecification.byDynaFieldsId(dacDocs.getId().getDynaFieldsId()));
    
    List<DacDocsDetails> detailsToDelete = findAll(spec);
    dacDocsDetailsRepository.deleteAll(detailsToDelete);
  }
} 