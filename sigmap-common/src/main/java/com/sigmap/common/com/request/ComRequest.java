package com.sigmap.common.com.request;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComRequest {
    private Integer id;
    private String nom;
    private String decretCreation;
    private String responsable;
    private Boolean special;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    private Integer comTypeId;
    private List<Integer> acIds;
}
