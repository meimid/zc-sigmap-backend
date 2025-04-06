package com.sigmap.common.agpm.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for returning Agpm data.
 * Includes actId and docIds for use in the application.
 */
@Data
public class AgpmResponse {
    private Integer id; // Agpm ID
    private Integer version; // Agpm version
    private String ref; // Reference field
    private String nom; // Name field
    private String contenu; // Content field
    private Integer statusId; // Status ID
    private Integer actId; // ID of the associated Ac entity
    private List<Integer> docIds; // List of associated Doc IDs
    private BaseEntityResponse metadata; // Audit metadata (createdAt, updatedAt, etc.)
}