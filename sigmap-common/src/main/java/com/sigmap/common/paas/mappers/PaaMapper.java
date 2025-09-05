package com.sigmap.common.paas.mappers;


import com.sigmap.common.docs.Docs;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface PaaMapper {
    PaaMapper INSTANCE = Mappers.getMapper(PaaMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", source = "realisations") // Maps the full list
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PaaResponse map(Paa source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", ignore = true)
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", ignore = true) // Ignored as it's typically set separately
    Paa toEntity(PaaRequest paaRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", ignore = true) // Ignored as it's typically managed separately
    Paa updatePaaFromRequest(PaaRequest request, @MappingTarget Paa paa);

    PaaResponseDto toDto(Paa paa);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "realisations", source = "realisations") // Maps the full list
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PaaResponseWithRealisations toDtoWithRelations(Paa paa);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PaaResponseDtoWithMetdata toDtoWithMetadata(Paa paa);


   /* @Mapping(source = "id", target = "id")
    @Mapping(source = "version", target = "version")
    @Mapping(source = "paa", target = "code", qualifiedByName = "generateCode")
    @Mapping(source = "ac.nom", target = "title")
    @Mapping(source = "updatedAt", target = "publicationDate", qualifiedByName = "formatDateToString")
    @Mapping(source = "updatedAt", target = "year", qualifiedByName = "getYearFromCalendar")
    @Mapping(source = "docs", target = "docs", qualifiedByName = "mapDocsToAlfrescoIds")
    PaaWebsiteResponse toWebsiteResponse(Paa paa);

    *//**
     * Custom mapping function to generate the 'code' field.
     * It combines the Paa name, the Ac name, and the year of the update.
     * @param paa The source paa entity.
     * @return The generated code string.
     *//*
    @Named("generateCode")
    default String generateCode(Paa paa) {
        if (paa == null) {
            return null;
        }
        // Use a StringBuilder for efficient string concatenation.
        StringBuilder codeBuilder = new StringBuilder();

        if (paa.getNom() != null) {
            codeBuilder.append(paa.getNom());
        }

        if (paa.getAc() != null && paa.getAc().getNom() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(paa.getAc().getNom());
        }

        if (paa.getUpdatedAt() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(paa.getUpdatedAt().get(Calendar.YEAR));
        }

        return codeBuilder.toString();
    }

    *//**
     * Custom mapping function to extract a list of alfresco_id strings from a list of Docs entities.
     * @param docs The source list of Docs entities.
     * @return A list of alfresco_id strings.
     *//*
    @Named("mapDocsToAlfrescoIds")
    default List<String> mapDocsToAlfrescoIds(List<Docs> docs) {
        if (docs == null || docs.isEmpty()) {
            return Collections.emptyList();
        }
        return docs.stream()
                .map(Docs::getAlfresco_id)
                .collect(Collectors.toList());
    }

    *//**
     * Custom mapping function to format a Calendar object into a "dd/MM/yyyy" string.
     * @param updatedAt The source Calendar object.
     * @return The formatted date string.
     *//*
    @Named("formatDateToString")
    default String formatDateToString(Calendar updatedAt) {
        if (updatedAt == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(updatedAt.getTime());
    }

    *//**
     * Custom mapping function to extract the year from a Calendar object.
     * @param updatedAt The source Calendar object.
     * @return The year as an Integer.
     *//*
    @Named("getYearFromCalendar")
    default Integer getYearFromCalendar(Calendar updatedAt) {
        if (updatedAt == null) {
            return null;
        }
        return updatedAt.get(Calendar.YEAR);
    }*/

}
