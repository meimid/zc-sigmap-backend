package com.sigmap.common.agpm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating or updating an Agpm entity.
 */
@Data
public class AgpmRequest {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String contenu;
    @NotNull(message = "acId must not be null")
    private Integer acId;
    private Integer statusId;
    private List<Integer> docIds;

}