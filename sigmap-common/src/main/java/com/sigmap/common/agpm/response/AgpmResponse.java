package com.sigmap.common.agpm.response;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;

import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for returning Agpm data.
 * Includes full Ac and Docs objects for use in the application.
 */
@Data
public class AgpmResponse {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String contenu;
    private Integer statusId;
    private Ac ac;
    private List<Docs> docs;
    private BaseEntityResponse metadata;
}