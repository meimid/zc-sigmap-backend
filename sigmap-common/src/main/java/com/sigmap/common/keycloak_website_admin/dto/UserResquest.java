package com.sigmap.common.keycloak_website_admin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResquest {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String mobile;
    private String fixe;
    private String cin;
    private LocalDate dateCIN;
}
