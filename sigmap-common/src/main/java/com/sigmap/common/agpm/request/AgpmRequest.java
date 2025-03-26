package com.sigmap.common.agpm.request;

import com.sun.istack.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Data
public class AgpmRequest {
    @NotNull
    @Size(min = 1, max = 100)
    private String ref;

    @NotNull
    @Size(min = 1, max = 255)
    private String nom;

    private String contenu;

    private Integer status_id;

    @NotNull
    private Integer actId;

    private Set<Integer> docIds;
}