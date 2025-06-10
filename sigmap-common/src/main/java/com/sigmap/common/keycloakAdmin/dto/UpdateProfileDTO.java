package com.sigmap.common.keycloakAdmin.dto;


import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String mobile;
    private String fixe;
}
