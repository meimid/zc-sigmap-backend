package com.sigmap.common.keycloakAdmin.dto;


import lombok.Data;

import java.util.Calendar;

@Data
public class UserComDTO {
    private Integer id;
    private String nom;
    private String createdBy;
    private Calendar createdAt;
}
