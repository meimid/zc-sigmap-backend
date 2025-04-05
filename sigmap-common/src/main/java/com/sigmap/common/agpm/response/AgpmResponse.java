package com.sigmap.common.agpm.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.List;

@Data
public class AgpmResponse {
    private Integer id;
    private Integer version;
    private String ref;
    private String nom;
    private String contenu;
    private Integer statusId;
    private Integer actId;
    private List<Integer> docIds;
    private BaseEntityResponse metadata;
}