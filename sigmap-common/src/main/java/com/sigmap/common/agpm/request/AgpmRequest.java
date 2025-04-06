package com.sigmap.common.agpm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating or updating an Agpm entity.
 */
@Data
public class AgpmRequest {
    private Integer id; // Agpm ID
    private Integer version; // Agpm version
    private String ref; // Reference field
    private String nom; // Name field
    private String contenu; // Content field
    @NotNull(message = "acId must not be null")
    private Integer acId; // ID of the associated Ac entity
    private Integer statusId; // Status ID
    private List<Integer> docIds; // List of Doc IDs for ManyToMany association
}