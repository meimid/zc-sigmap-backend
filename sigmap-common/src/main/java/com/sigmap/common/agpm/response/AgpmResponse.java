package com.sigmap.common.agpm.response;

import lombok.Data;



@Data
public class AgpmResponse {
    private Integer id;
    private Integer agpm_ver;
    private String ref;
    private String nom;
    private String contenu;
    private Integer status_id;
//    private Integer actId; // ID of the AC entity
//    private Set<Integer> docIds; // IDs of the Doc entities
}