package com.sigmap.common.ppms.mappers;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.ppms.response.PpmWebsiteResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.response.PpmResponse;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.ppms.response.PpmResponseSoftDto;
import com.sigmap.common.ppms.response.PpmResponseSoftDtoWithMetaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PpmMapper {
    PpmMapper INSTANCE = Mappers.getMapper(PpmMapper.class);

    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "status", source = "status")
    PpmResponse map(Ppm source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version", defaultValue = "1")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    Ppm toEntity(PpmRequest request);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "ref", source = "ref")
    @Mapping(target = "nom", source = "nom")
    @Mapping(target = "annee", source = "annee")
    @Mapping(target = "ac", source = "ac")
    @Mapping(target = "budgetEstime", source = "budgetEstime")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "docs", source = "docs")
    @Mapping(target = "remarque", source = "remarque")
    @Mapping(target = "realisations", source = "realisations")
    @Mapping(target = "processInstanceId", source = "processInstanceId")
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PpmResponseDto mapToDto(Ppm source);
    PpmResponseSoftDto toDto(Ppm ppm);
    @Mapping(target = "metadata.createdAt", source = "createdAt")
    @Mapping(target = "metadata.createdBy", source = "createdBy")
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
    @Mapping(target = "metadata.updatedBy", source = "updatedBy")
    @Mapping(target = "metadata.isDeleted", source = "isDeleted")
    @Mapping(target = "metadata.deletedBy", source = "deletedBy")
    @Mapping(target = "metadata.deletedAt", source = "deletedAt")
    PpmResponseSoftDtoWithMetaData mapToDtoWithMetaData(Ppm ppm);


   /* @Mapping(source = "id", target = "id")
    @Mapping(source = "version", target = "version")
    @Mapping(source = "ppm", target = "code", qualifiedByName = "generateCode")
    @Mapping(source = "ac.nom", target = "title")
    @Mapping(source = "updatedAt", target = "publicationDate", qualifiedByName = "formatDateToString")
    @Mapping(source = "updatedAt", target = "year", qualifiedByName = "getYearFromCalendar")
    @Mapping(source = "docs", target = "docs", qualifiedByName = "mapDocsToAlfrescoIds")
    PpmWebsiteResponse toWebsiteResponse(Ppm ppm);

    *//**
            * Custom mapping function to generate the 'code' field.
     * It combines the Ppm name, the Ac name, and the year of the update.
            * @param ppm The source ppm entity.
     * @return The generated code string.
            *//*
    @Named("generateCode")
    default String generateCode(Ppm ppm) {
        if (ppm == null) {
            return null;
        }
        // Use a StringBuilder for efficient string concatenation.
        StringBuilder codeBuilder = new StringBuilder();

        if (ppm.getNom() != null) {
            codeBuilder.append(ppm.getNom());
        }

        if (ppm.getAc() != null && ppm.getAc().getNom() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(ppm.getAc().getNom());
        }

        if (ppm.getUpdatedAt() != null) {
            if (codeBuilder.length() > 0) codeBuilder.append(" ");
            codeBuilder.append(ppm.getUpdatedAt().get(Calendar.YEAR));
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
