package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;

import java.util.Calendar;

@Data
public class ComMemberDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private boolean responsable;
    private Calendar createdAt;
    private String createdBy;
}

