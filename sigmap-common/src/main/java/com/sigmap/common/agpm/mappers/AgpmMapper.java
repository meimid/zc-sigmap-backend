package com.sigmap.common.agpm.mappers;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.ref_params.RefParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper interface for converting between Agpm entities, requests, and responses.
 */
@Mapper(componentModel = "spring")
public interface AgpmMapper {
    AgpmMapper INSTANCE = Mappers.getMapper(AgpmMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "annee", source = "annee")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmResponse map(Agpm source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    Agpm toEntity(AgpmRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "processInstanceId",source = "processInstanceId")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    AgpmResponseDto mapToDto(Agpm source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "contenu", source = "contenu")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "ac", ignore = true)
    @Mapping(target = "docs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateAgpmFromRequest(AgpmRequest request, @MappingTarget Agpm agpm);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "modelAlfrescoId", source = "modelAlfrescoId")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    AgpmMinimalResponseDto toMinimalResponseDto(Agpm agpm);
    AgpmResponseDtoSoft toDto(Agpm agpm);



    /*@Mapping(source = "id", target = "id")
    @Mapping(source = "version", target = "version")
    @Mapping(source = "agpm", target = "code", qualifiedByName = "generateCode")
    @Mapping(source = "ac.nom", target = "title")
    @Mapping(source = "updatedAt", target = "publicationDate", qualifiedByName = "formatDateToString")
    @Mapping(source = "updatedAt", target = "year", qualifiedByName = "getYearFromCalendar")
    @Mapping(source = "docs", target = "docs", qualifiedByName = "mapDocsToAlfrescoIds")
    AgpmWebsiteResponse toWebsiteResponse(Agpm agpm);

    *//**
     * Custom mapping function to generate the 'code' field.
     * It combines the Agpm name, the Ac name, and the year of the update.
     * @param agpm The source Agpm entity.
     * @return The generated code string.
     *//*
    @Named("generateCode")
    default String generateCode(Agpm agpm) {
        if (agpm == null) {
            return null;
        }
        // Use a StringBuilder for efficient string concatenation.
        StringBuilder codeBuilder = new StringBuilder();

        if (agpm.getNom() != null) {
            codeBuilder.append(agpm.getNom());
        }

        if (agpm.getAc() != null && agpm.getAc().getNom() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(agpm.getAc().getNom());
        }

        if (agpm.getUpdatedAt() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(agpm.getUpdatedAt().get(Calendar.YEAR));
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
