package com.sigmap.common.keycloakAdmin.dto;

import lombok.Data;

@Data
public class PasswordRequestDTO {
    private String newPassword;
    private String repeatNewPassword;
}
