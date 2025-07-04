package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocDetailsDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.DacDocsDetailsRepository;
import com.sigmap.common.dac_docs_details.DacDocsDetailsService;
import com.sigmap.common.dac_docs_details.DacDocsDetailsSpecification;
import com.sigmap.common.dac_docs_details.request.DacDocsDetailsRequest;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsRepository;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DacDocsServiceImpl implements DacDocsService{

    @Autowired
    private DacDocsRepository dacDocsRepository;
    @Autowired
    private DynaFieldsRepository dynaFieldsRepository;
    @Autowired
    private DacDocsMapper dacDocsMapper;
    @Autowired
    DacRepository dacRepository;
    @Autowired
    DynaDocsRepository dynaDocsRepository;
    @Autowired
    DacDocsDetailsService dacDocsDetailsService;
    @Autowired
    DacDocsDetailsRepository dacDocsDetailsRepository;

    @Override
    @Transactional
    public DacDocs createDacDocs (DacDocsRequest request) {

        // 1- get the DynaDocs with last version
        Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaDocsId());

        // 2- test if the existingDynaDocs is empty
        if (existingDynaDocs.isEmpty()) {
            throw new RuntimeException("DynaDocs not found with id: " + request.getDynaDocsId());
        }

        // 3- get the DynaFields with last dynaDocs version for the given id
        List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
        if (existingDynaFields.isEmpty()) {
            throw new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId());
        }
        log.debug("existingDynaFields............. : {}", existingDynaFields);

        // 4-get the last dac (id and version) with the given id
        Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
        log.debug("existingDac............. : {}", existingDac);

        // 5- verifier l'existence du dac
        if (existingDac == null) {
            throw new RuntimeException("Dac not found with id: " + request.getDacId());
        }
        //6- create DacDocs and save
        DacDocs entity = dacDocsMapper.toEntity(request);
        log.debug("entity............. : {}", entity);

        // Set scalar fields
        entity.setSourceId(request.getSourceId());
        entity.setFieldValue(request.getFieldValue());

        // Set composite key
        DacDocsId id = new DacDocsId();
        id.setDacId(existingDac.getDacId());
        id.setDacVer(existingDac.getDacVer());
        id.setDynaDocsId(existingDynaDocs.get().getId());
        id.setDynaDocsVer(existingDynaDocs.get().getVersion());

        // Find the matching DynaFields by dynaFieldsId
        DynaFields matchedDynaField = existingDynaFields.stream()
                .filter(field -> field.getDynaFieldsId().equals(request.getDynaFieldsId()))
                .findFirst()
                .map(field -> {
                    id.setDynaFieldsId(field.getDynaFieldsId());
                    return field;
                })
                .orElse(null);
        if (matchedDynaField == null) {
            throw new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId());
        }
        entity.setId(id);

        // Set relationships
        entity.setDac(existingDac);
        entity.setDynaField(matchedDynaField);

        entity = dacDocsRepository.save(entity);

        // test if there are any details and if the field type is list or table
        log.debug("Dac Docs created with ID: {}", entity.getId());
        if (matchedDynaField.getType() == DynaFieldType.LIST || matchedDynaField.getType() == DynaFieldType.TABLE)
        {

            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                List<DacDocsDetailsRequest> detailsReq =
                        DacDocsMapper.INSTANCE.mapDetails(request.getDetails());

                List<DacDocsDetails> detailsList =
                        dacDocsDetailsService.createAll(entity, detailsReq);

                Set<DacDocsDetails> details = DacDocsMapper.INSTANCE.detailsToSet(detailsList);

                entity.setDacDocsDetails(details);
            }

        }

            return entity;
    }

    @Override
    @Transactional
    public DacDocs updateDacDocs( DacDocsRequest request) {

        // 1- get the DynaDocs with last version
        Optional<DynaDocs> existingDynaDocs = dynaDocsRepository.findLastDynaDocsVersionById(request.getDynaFieldsId());

        // 2- test if the existingDynaDocs is empty
        if (existingDynaDocs.isEmpty()) {
            throw new RuntimeException("DynaDocs not found with id: " + request.getDynaFieldsId());
        }

        // 3- Verify the existence of the dyna_field
        List<DynaFields> existingDynaFields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(existingDynaDocs.get().getId(), existingDynaDocs.get().getVersion());
        if (existingDynaFields.isEmpty()) {
            throw new RuntimeException("DynaField not found for id: " + request.getDynaFieldsId());
        }

        // 4- Verify the existence of the dac by id ==> get the last version
        Dac existingDac = dacRepository.findDacWithLatestVersionByDacId(request.getDacId());
        if (existingDac == null) {
            throw new RuntimeException("Dac not found with id: " + request.getDacId() );
        }

        // 5- Get the existing DacDocs
        Optional<DacDocs> existingDacDocs = dacDocsRepository.findDacDocsBYPKs(existingDac.getDacId(), existingDac.getDacVer(),existingDynaDocs.get().getId(),existingDynaDocs.get().getVersion(), existingDynaFields.get(0).getDynaFieldsId());

        if (existingDacDocs.isEmpty()) {
            throw new RuntimeException("DacDocs not found with dacId: " + existingDac.getDacId() + " and dacVer: " + existingDac.getDacVer() +
                    " and dynaDocsId: " + existingDynaDocs.get().getId() + " and dynaDocsVer: " + existingDynaDocs.get().getVersion() +
                    " and dynaFieldsId: " + existingDynaFields.get(0).getDynaFieldsId());
        }

        // 6- Update the main fields
        existingDacDocs.get().setFieldValue(request.getFieldValue());
        existingDacDocs.get().setSourceId(request.getSourceId());

        DacDocs updatedDacDocs = dacDocsRepository.save(existingDacDocs.get());
        log.info("Dac Docs updated with ID: {}", updatedDacDocs.getId());

        // 5- Handle details for LIST and TABLE types
        if (existingDynaFields.get(0).getType() == DynaFieldType.LIST || existingDynaFields.get(0).getType() == DynaFieldType.TABLE) {
            
            // Create new details if provided
            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                List<DacDocsDetailsRequest> detailsReq = DacDocsMapper.INSTANCE.mapDetails(request.getDetails());
                List<DacDocsDetails> detailsList = dacDocsDetailsService.createAll(existingDacDocs.get(), detailsReq);
                Set<DacDocsDetails> details = DacDocsMapper.INSTANCE.detailsToSet(detailsList);
                existingDacDocs.get().setDacDocsDetails(details);
            }
        }

        // 6- Save and return the updated entity

        return updatedDacDocs;
    }

    @Override
    public List<DacDocs> findOneById(Integer dacId, Integer dacVer) {
        return dacDocsRepository.findDacByID(dacId, dacVer);
    }
    @Override
    public Optional<DacDocs> findById(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
        return dacDocsRepository.findDacDocsBYPKs(dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
    }

    @Override
    public DacDocResponseDTO findByDacIdAndVer(Integer dacId, Integer dacVer) {
        List<DacDocsDetails> details = dacDocsDetailsRepository.findAll(
                Specification.where(DacDocsDetailsSpecification.byDacId(dacId))
                        .and(DacDocsDetailsSpecification.byDacVer(dacVer))
        );
        if (details.isEmpty()) {
            return null;
        }

        List<DacDocDetailsDTO> detailsDTOs = details.stream()
                .map(detail -> {
                    DacDocDetailsDTO dto = new DacDocDetailsDTO();
                    dto.setDynaFieldsDetailsId(detail.getDynaFieldsDetailsId());
                    dto.setRowId(detail.getRowId());
                    dto.setSourceId(detail.getSourceId());
                    dto.setFieldValue(detail.getFieldValue());

                    // Build DynaDocsResponseDTO list
                    List<DynaDocsResponseDTO> dynaDocsDTOs = new ArrayList<>();
                    DacDocs dacDocs = detail.getDac();
                    if (dacDocs != null && dacDocs.getDynaField() != null) {
                        DynaDocs dynaDocs = dacDocs.getDynaField().getDynaDocs();
                        if (dynaDocs != null) {
                            DynaDocsResponseDTO dynaDocsResponseDTO = dacDocsMapper.toDynaDocsResponse(dacDocs);

                            // Fetch all DynaFields for this DynaDocs
                            List<DynaFields> dynaFieldsList = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(
                                dynaDocs.getId(), dynaDocs.getVersion()
                            );
                            List<DynaFieldsDTO> dynaFieldsDTOs = dynaFieldsList.stream()
                                    .map(field -> {
                                        DynaFieldsDTO dynaFieldsDTO = dacDocsMapper.toDynaFieldsDTO(field);
                                        Set<DynaFieldsDetails> dynaFieldsDetailsSet = field.getDynaFieldsDetails();
                                        if (dynaFieldsDetailsSet != null) {
                                            Set<DynaFieldsDetailsDTO> dynaFieldsDetailsDTOs = dacDocsMapper.dynaFieldsDetailsToDTO(dynaFieldsDetailsSet);
                                            dynaFieldsDTO.setDynaFieldsDetails(dynaFieldsDetailsDTOs);
                                        } else {
                                            dynaFieldsDTO.setDynaFieldsDetails(Collections.emptySet());
                                        }
                                        return dynaFieldsDTO;
                                    }
                            ).collect(Collectors.toList());
                            dynaDocsResponseDTO.setDynaFields(dynaFieldsDTOs);
                            dynaDocsDTOs.add(dynaDocsResponseDTO);
                        }
                    }
                    dto.setDynaDocs(dynaDocsDTOs);
                    return dto;
        }).collect(Collectors.toList());

        DacDocResponseDTO response = new DacDocResponseDTO();
        response.setDacId(dacId);
        response.setDacVer(dacVer);
        response.setDacDocDetails(detailsDTOs);

        return response;
    }

    @Override
    public List<DacDocs> findByDacIdAndDacVer(Integer dacId, Integer dacVer) {
        return dacDocsRepository.findByDacIdAndDacVer(dacId, dacVer);
    }

    @Override
    @Transactional
    public List<DacDocs> createDacDocsBatch(List<DacDocsRequest> requests) {
        List<DacDocs> result = new ArrayList<>();
        for (DacDocsRequest req : requests) {
            result.add(createDacDocs(req));
        }
        return result;
    }
}
