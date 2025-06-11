package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;

import java.util.Calendar;

@Data
public class UserAcDTO {
    private Integer id;
    private String nom;
    private String ancrage;
    private String contact;
    private Boolean acPilote;
    private String createdBy;
    private Calendar createdAt;
}