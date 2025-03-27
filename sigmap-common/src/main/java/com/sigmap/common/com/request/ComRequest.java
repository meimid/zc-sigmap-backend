package com.sigmap.common.com.request;
import lombok.Data;
@Data
public class ComRequest {
    private String nom;
    private String decretCreation;
    private String responsable;
    private Boolean special;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    private Integer refParamId;
}
