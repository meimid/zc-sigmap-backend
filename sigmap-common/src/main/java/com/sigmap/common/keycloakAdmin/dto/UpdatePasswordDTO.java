package com.sigmap.common.keycloakAdmin.dto;


import lombok.Data;

@Data
public class UpdatePasswordDTO {
   // private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
