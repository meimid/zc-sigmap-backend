package com.sigmap.common.process_verbal.mappers;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.process_verbal.response.ProcessVerbalResponse;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_docs.response.PvDocsResponseDto;
import com.sigmap.common.pv_membre_present.response.PvMembrePresentResponseDto;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")

public interface ProcessVerbalMapper {
    ProcessVerbalMapper INSTANCE = Mappers.getMapper(ProcessVerbalMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "membres", ignore = true) // Ignore if not needed
    //@Mapping(target = "documents", source = "documents")
    @Mapping(target = "documents", ignore = true) // Ignore if not needed
    ProcessVerbalResponse map(ProcessVerbal source);

    // Custom mapping for PvDocs collection
    Set<PvDocsResponseDto> mapDocuments(Set<PvDocs> source);

    // Mapping for individual PvDocs
    PvDocsResponseDto map(PvDocs source);

    // Default method to ensure safe iteration
   /* default Set<PvDocsResponseDto> mapDocuments(Set<PvDocs> source) {
        if (source == null) {
            return null;
        }
        Set<PvDocsResponseDto> target = new HashSet<>();
        for (PvDocs doc : new HashSet<>(source)) { // Defensive copy
            target.add(map(doc));
        }
        return target;
    }*/
    public static ProcessVerbalResponse toDto(ProcessVerbal entity) {
        if (entity == null) {
            return null;
        }

        ProcessVerbalResponse dto = new ProcessVerbalResponse();

        // 1. Map direct fields
        dto.setProcessVerbalId(entity.getProcessVerbalId());
        dto.setDateDebutReunion(entity.getDateDebutReunion());
        dto.setObservation(entity.getObservation());
        dto.setDecision(entity.isDecision());
        dto.setStatus(entity.getStatus());

        // 2. Delegate mapping of nested objects to their respective static mappers
        dto.setPpm(PpmMapper.INSTANCE.mapToDto(entity.getPpm()));
        dto.setTypeId(RefParamMapper.INSTANCE.mapToDto(entity.getTypeId()));
      //  dto.setMetadata(BaseEntityMapper.toDto(entity));

        // 3. Delegate mapping of collections by calling other static mappers
        BaseEntityResponse metaDto = new BaseEntityResponse();
        metaDto.setCreatedAt(entity.getCreatedAt());
        metaDto.setCreatedBy(entity.getCreatedBy());
        metaDto.setUpdatedAt(entity.getUpdatedAt());
        metaDto.setUpdatedBy(entity.getUpdatedBy());
        dto.setMetadata(metaDto);

        // 5. Manually map the 'membres' collection
        // 5. Manually map the 'membres' collection (FIXED)
        if (entity.getMembres() != null) {
            dto.setMembres(entity.getMembres().stream()
                    .filter(Objects::nonNull)
                    .map(membreEntity -> {
                        PvMembrePresentResponseDto membreDto = new PvMembrePresentResponseDto();
                        membreDto.setPvMembrePresentId(membreEntity.getPvMembrePresentId());
                        membreDto.setNom(membreEntity.getNom());
                        membreDto.setPrenom(membreEntity.getPrenom());
                        membreDto.setPresent(membreEntity.isPresent());
                        membreDto.setUserId(membreEntity.getUserId());
                        membreDto.setFoncation(membreEntity.getFoncation());

                        // Manually map the nested fonctionId (RefParam -> RefParamResponseDto)
                     /*   if (membreEntity.getFonctionId() != null) {
                            RefParamResponseDto fonctionDto = new RefParamResponseDto();
                            fonctionDto.setId(membreEntity.getFonctionId().getId());
                            membreDto.setFonctionId(fonctionDto);
                        }*/
                        return membreDto;
                    })
                    .collect(Collectors.toSet()));
        }

        // 6. Manually map the 'documents' collection (FIXED)
        if (entity.getDocuments() != null) {
            dto.setDocuments(entity.getDocuments().stream()
                    .filter(Objects::nonNull)
                    .map(docEntity -> {
                        PvDocsResponseDto docDto = new PvDocsResponseDto();
                        docDto.setPv(docEntity.isPv());

                        // Manually map the nested doc (Docs -> DocsResponseDTO)
                        if (docEntity.getDoc() != null) {
                            DocsResponseDTO nestedDocDto = new DocsResponseDTO();
                            nestedDocDto.setId(docEntity.getDoc().getId());
                            nestedDocDto.setName(docEntity.getDoc().getName());
                            // ... map other fields from docEntity.getDoc() to nestedDocDto ...
                            docDto.setDoc(nestedDocDto);
                        }
                        return docDto;
                    })
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

}
