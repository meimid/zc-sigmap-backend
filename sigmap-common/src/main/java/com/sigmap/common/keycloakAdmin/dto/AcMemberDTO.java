package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;

import java.util.Calendar;

@Data
public class AcMemberDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean acPilote;
    private Calendar createdAt;  // de UserAc
    private String createdBy;    // de UserAc
}